package com.github.kory33.s2mctest
package connection.protocol.codec

import connection.protocol.data.PacketDataCompoundTypes.*
import connection.protocol.data.PacketDataPrimitives.*
import connection.protocol.typeclass.IntLike
import connection.protocol.macros.GenByteDecode
import algebra.ReadBytes
import typenbtio.{ReadNBT, WriteNBT}

import cats.Monad
import fs2.Chunk
import net.katsstuff.typenbt.NBTCompound
import shapeless3.deriving.K0

import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.UUID
import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.reflect.ClassTag

object ByteCodecs {

  import ByteDecode.*
  import cats.implicits.given

  inline def autogenerateFor[T](using gen: K0.Generic[T]): ByteCodec[T] =
    ByteCodec(GenByteDecode.gen[T], ByteEncode.forADT[T])

  object Common {

    inline given codecToEncode[A: ByteCodec]: ByteEncode[A] = ByteCodec[A].encode
    inline given codecToDecode[A: ByteCodec]: ByteDecode[A] = ByteCodec[A].decode

    /** Codec of Unit (empty type). */
    given ByteCodec[Unit] = ByteCodec[Unit](
      Monad[ByteDecode].pure(()),
      (x: Unit) => Chunk.empty[Byte]
    )

    given ByteCodec[Boolean] = ByteCodec[Boolean](
      readByteBlock(1).map(_.head.get == (0x01: Byte)),
      (x: Boolean) => Chunk[Byte](if x then 0x01 else 0x00)
    )

    given ByteCodec[Byte] = ByteCodec[Byte](
      readByteBlock(1).map(_.head.get),
      (x: Byte) => Chunk(x)
    )

    given ByteCodec[Short] = ByteCodec[Short](
      readByteBlock(2).map(c => java.nio.ByteBuffer.wrap(c.toArray).getShort),
      (x: Short) => Chunk.array(java.nio.ByteBuffer.allocate(2).putShort(x).array())
    )

    given ByteCodec[Int] = ByteCodec[Int](
      readByteBlock(4).map(c => java.nio.ByteBuffer.wrap(c.toArray).getInt),
      (x: Int) => Chunk.array(java.nio.ByteBuffer.allocate(4).putInt(x).array())
    )

    given ByteCodec[Long] = ByteCodec[Long](
      readByteBlock(8).map(c => java.nio.ByteBuffer.wrap(c.toArray).getLong),
      (x: Long) => Chunk.array(java.nio.ByteBuffer.allocate(8).putLong(x).array())
    )

    given ByteCodec[Float] = ByteCodec[Float](
      readByteBlock(4).map(c => java.nio.ByteBuffer.wrap(c.toArray).getFloat),
      (x: Float) => Chunk.array(java.nio.ByteBuffer.allocate(4).putFloat(x).array())
    )

    given ByteCodec[Double] = ByteCodec[Double](
      readByteBlock(8).map(c => java.nio.ByteBuffer.wrap(c.toArray).getDouble),
      (x: Double) => Chunk.array(java.nio.ByteBuffer.allocate(8).putDouble(x).array())
    )

    given ByteCodec[UByte] = ByteCodec[Byte].imap(UByte.fromRawByte)(_.asRawByte)

    given ByteCodec[UShort] = ByteCodec[Short].imap(UShort.fromRawShort)(_.asRawShort)

    private object VarNumCodecs {
      /**
       * Decode variable-length integer which has maximum bits of [[maxBits]].
       *
       * Quoting from https://wiki.vg/Protocol,
       *
       * <pre>
       *  > These are very similar to Protocol Buffer Varints:
       *  > the 7 least significant bits are used to encode the value
       *  > and the most significant bit indicates whether there's another byte after it
       *  > for the next part of the number.
       *  >
       *  > The least significant group is written first, followed by each of the more significant groups;
       *  > thus, VarInts are effectively little endian (however, groups are 7 bits, not 8).
       * </pre>
       *
       * For example, consider the following byte stream obtained from network
       * whose head is expected to be a variable-integer:
       *
       * <pre>
       * 10001001 10010101 01111000 10100111 ...
       * </pre>
       *
       * Since third byte has the most significant bit set to zero, variable-integer data ends here.
       *
       * <pre>
       * X0001001 X0010101 X1111000 | 10100111 ...
       * </pre>
       *
       * Lower 7 bits from each byte is aggregated, and we obtain little-endian 7bit blocks:
       *
       * <pre>
       * 0001001 0010101 1111000 0000000 0000XXX
       * </pre>
       *
       * In big endian 8bit blocks, this data corresponds to
       *
       * <pre>
       * 00000000 00011110 00001010 10001001
       * </pre>
       */
      def decodeVarNum(maxBits: Int): ByteDecode[Chunk[Byte]] = {
        import scodec.bits.{ByteVector, BitVector}

        extension (bv: BitVector)
          def appendedAll(another: BitVector) = BitVector.concat(Seq(bv, another))

        case class State(remainingBits: Int, accum: BitVector)

        def concludeLoopWith(result: BitVector) =
          Monad[ByteDecode].pure(Right(Chunk.array(result.reverseBitOrder.toByteArray)))

        def nextIterationWith(state: State) =
          Monad[ByteDecode].pure(Left(state))

        def concludeLoopWithError(latestState: State, nextByte: Byte) = raisePacketError {
          s"encountered excess bytes while reading variable-length integer.\n" +
            s"maxBits was ${maxBits}, but the state reached is: st = $latestState, nextByte = $nextByte"
        }

        Monad[ByteDecode].tailRecM(State(maxBits, BitVector.empty)) { case st @ State(remainingBits, accum) =>
          if remainingBits > 0 then
            readByte.flatMap { nextByte =>
              val nextBits = BitVector.fromByte(nextByte, 7).reverseBitOrder
              val continuationFlag = (nextByte & 0x80) != 0
              val totalAccum = accum.appendedAll(nextBits)

              if remainingBits <= 7 && continuationFlag then concludeLoopWithError(st, nextByte)
              else if continuationFlag then nextIterationWith(State(remainingBits - 7, totalAccum))
              else concludeLoopWith(totalAccum)
            }
          else
            concludeLoopWith(accum)
        }
      }

      def encodeVarNum(fixedSizeBigEndianBytes: Chunk[Byte]): Chunk[Byte] = {
        extension [A] (list: List[A])
          def dropRightWhile(predicate: A => Boolean): List[A] = list.reverse.dropWhile(predicate).reverse
          def unconsLast: (List[A], A) = list.reverse match {
            case ::(last, restRev) => (restRev.reverse, last)
            case Nil => throw IllegalArgumentException("unconsLast on an empty list")
          }

        require(fixedSizeBigEndianBytes.nonEmpty, "expected nonempty Chunk[Byte] for encodeVarNum")

        // for example, let the parameter be 32-bit big endian integer Chunk(00000000, 00000001, 11101010, 10010100).

        // bits in fixedSizeBigEndianBytes, with LSB at the beginning and MSB at the tail
        // with the example, this would be BitVector(00101001 01010111 10000000 00000000)
        val reversedBits = scodec.bits.BitVector.view(fixedSizeBigEndianBytes.toArray).reverseBitOrder

        // bits split into 7bits group and then redundant most significant part dropped.
        // with the example, this would be List(BitVector(0010100), BitVector(1010101), BitVector(1110000))
        val splitInto7Bits = reversedBits.grouped(7).toList.dropRightWhile(_.populationCount == 0)

        // bits split into 7bits, with flag for data continuation appended to each bit group
        // with the example, this would be List(BitVector(00101001), BitVector(10101011), BitVector(11100000))
        val flagsAppended = splitInto7Bits.unconsLast match {
          case (rest, last) => rest.map(_ :+ true).appended(last :+ false)
        }

        // finally reverse each bit groups and concat them into a Chunk[Byte]
        // with the example, this would be Chunk(10010100 11010101 00000111)
        Chunk.array(scodec.bits.BitVector.concat(flagsAppended.map(_.reverseBitOrder)).toByteArray)
      }

      given ByteCodec[VarInt] = ByteCodec[VarInt](
        decodeVarNum(32).flatMap(readPrecise(_, ByteCodec[Int].decode)).map(VarInt.apply),
        (x: VarInt) => encodeVarNum(ByteCodec[Int].encode.write(x.raw))
      )

      given ByteCodec[VarLong] = ByteCodec[VarLong](
        decodeVarNum(64).flatMap(readPrecise(_, ByteCodec[Long].decode)).map(VarLong.apply),
        (x: VarLong) => encodeVarNum(ByteCodec[Long].encode.write(x.raw))
      )
    }

    export VarNumCodecs.given

    given ByteCodec[String] = {
      val utf8Charset = Charset.forName("UTF-8")

      ByteCodec[String](
        ByteCodec[VarInt].decode.flatMap(length => ReadBytes[ByteDecode].forUTF8String(length.raw)),
        (x: String) => ByteCodec[VarInt].encode.write(VarInt(x.length)) ++ Chunk.array(x.getBytes(utf8Charset))
      )
    }

    // TODO this is not a common codec
    given ByteCodec[Position] = ByteCodec[Position](???, ???)

    given lenPrefixed[L: IntLike: ByteCodec, A: ByteCodec]: ByteCodec[LenPrefixedSeq[L, A]] = {
      val decode: ByteDecode[LenPrefixedSeq[L, A]] = for {
        length <- ByteCodec[L].decode
        intLength = IntLike[L].toInt(length)
        aList <- ByteCodec[A].decode.replicateA(intLength)
      } yield LenPrefixedSeq(aList.toVector)

      val encode: ByteEncode[LenPrefixedSeq[L, A]] = { (lenSeq: LenPrefixedSeq[L, A]) =>
        ByteCodec[L].encode.write(lenSeq.lLength) ++ ByteCodec[A].encode.writeSeq(lenSeq.asVector)
      }

      ByteCodec[LenPrefixedSeq[L, A]](decode, encode)
    }

    given fixedPoint5ForIntegral[A: ByteCodec: Integral]: ByteCodec[FixedPoint5[A]] =
      ByteCodec[A].imap(FixedPoint5.fromRaw[A])(_.rawValueFP5)

    given fixedPoint12ForIntegral[A: ByteCodec: Integral]: ByteCodec[FixedPoint12[A]] =
      ByteCodec[A].imap(FixedPoint12.fromRaw[A])(_.rawValueFP12)

    given ByteCodec[UnspecifiedLengthByteArray] = ByteCodec[UnspecifiedLengthByteArray](
      ByteDecode.readUntilPacketEnd.map(c => UnspecifiedLengthByteArray(c.toArray)),
      ulArray => Chunk.array(ulArray.asArray)
    )

    given ByteCodec[ChatComponent] = ByteCodec[String].imap(ChatComponent.apply)(_.json)

    given ByteCodec[UUID] = {
      val longCodec = ByteCodec[Long]
      ByteCodec[UUID](
        (longCodec.decode, longCodec.decode).mapN(UUID(_, _)),
        (uuid) =>
          longCodec.encode.write(uuid.getMostSignificantBits) ++
          longCodec.encode.write(uuid.getLeastSignificantBits)
      )
    }

    given ByteCodec[Statistic] = autogenerateFor[Statistic]
    given ByteCodec[ChunkMeta] = autogenerateFor[ChunkMeta]
    given ByteCodec[MapIcon] = autogenerateFor[MapIcon]
    given ByteCodec[SpawnProperty] = autogenerateFor[SpawnProperty]
    given ByteCodec[ExplosionRecord] = autogenerateFor[ExplosionRecord]

    given ByteCodec[BlockChangeRecord] = autogenerateFor[BlockChangeRecord]

    given ByteCodec[Tag] = autogenerateFor[Tag]
    given ByteCodec[Slot] = autogenerateFor[Slot]

    given ByteCodec[Trade] = autogenerateFor[Trade]
    given ByteCodec[EntityPropertyModifier] = autogenerateFor[EntityPropertyModifier]
    given ByteCodec[EntityPropertyShort] = autogenerateFor[EntityPropertyShort]
    given ByteCodec[EntityProperty] = autogenerateFor[EntityProperty]

    given ByteCodec[EntityEquipment] = autogenerateFor[EntityEquipment]

    /** see https://wiki.vg/index.php?title=Protocol&oldid=16953#Entity_Equipment for details */
    given ByteCodec[EntityEquipments] = {
      import extensions.MonadValueExt.repeatWhileM

      ByteCodec[EntityEquipments](
        ByteCodec[EntityEquipment].decode.repeatWhileM { case EntityEquipment(slot, _) =>
          (slot & 0x80) != 0
        },
        ByteCodec[EntityEquipment].encode.writeSeq(_)
      )
    }

    given ByteCodec[Biomes3D] =
      ByteCodec[Biomes3D](
        ByteCodec[Int].decode.replicateA(1024).map { intList => Biomes3D(intList.toArray) },
        biome => ByteCodec[Int].encode.writeSeq(biome.arrayData.toSeq)
      )

    given ByteCodec[PlayerProperty] = autogenerateFor[PlayerProperty]
    given ByteCodec[PlayerInfoDataRecord.AddPlayer] = autogenerateFor[PlayerInfoDataRecord.AddPlayer]
    given ByteCodec[PlayerInfoDataRecord.UpdateGamemode] = autogenerateFor[PlayerInfoDataRecord.UpdateGamemode]
    given ByteCodec[PlayerInfoDataRecord.UpdateLatency] = autogenerateFor[PlayerInfoDataRecord.UpdateLatency]
    given ByteCodec[PlayerInfoDataRecord.UpdateDisplayName] = autogenerateFor[PlayerInfoDataRecord.UpdateDisplayName]
    given ByteCodec[PlayerInfoDataRecord.RemovePlayer] = autogenerateFor[PlayerInfoDataRecord.RemovePlayer]
    given ByteCodec[PlayerInfoData] = ByteCodec[PlayerInfoData](
      ByteCodec[VarInt].decode.flatMap { actionVarInt =>
        actionVarInt.raw match {
          case 0 => ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.AddPlayer]].decode
            .map(PlayerInfoData.AddPlayer.apply)
          case 1 => ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.UpdateGamemode]].decode
            .map(PlayerInfoData.UpdateGamemode.apply)
          case 2 => ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.UpdateLatency]].decode
            .map(PlayerInfoData.UpdateLatency.apply)
          case 3 => ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.UpdateDisplayName]].decode
            .map(PlayerInfoData.UpdateDisplayName.apply)
          case 4 => ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.RemovePlayer]].decode
            .map(PlayerInfoData.RemovePlayer.apply)
        }
      },
      (playerInfoData: PlayerInfoData) => playerInfoData match {
        case PlayerInfoData.AddPlayer(players) =>
          ByteCodec[VarInt].encode.write(VarInt(0)) ++
          ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.AddPlayer]].encode.write(players)
        case PlayerInfoData.UpdateGamemode(players) =>
          ByteCodec[VarInt].encode.write(VarInt(1)) ++
          ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.UpdateGamemode]].encode.write(players)
        case PlayerInfoData.UpdateLatency(players) =>
          ByteCodec[VarInt].encode.write(VarInt(2)) ++
          ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.UpdateLatency]].encode.write(players)
        case PlayerInfoData.UpdateDisplayName(players) =>
          ByteCodec[VarInt].encode.write(VarInt(3)) ++
          ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.UpdateDisplayName]].encode.write(players)
        case PlayerInfoData.RemovePlayer(players) =>
          ByteCodec[VarInt].encode.write(VarInt(4)) ++
          ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.RemovePlayer]].encode.write(players)
      }
    )

    def recipeDataTypeString(recipeData: RecipeData): String = recipeData match {
      case RecipeData.Shapeless(_, _, _) => "crafting_shapeless"
      case RecipeData.Shaped(_, _, _, _, _) => "crafting_shaped"
      case RecipeData.Cooking(tpe, _) => tpe match {
        case CookingDataType.Smelting => "smelting"
        case CookingDataType.Blasting => "blasting"
        case CookingDataType.Smoking => "smoking"
        case CookingDataType.CampfireCooking => "campfire_cooking"
      }
      case RecipeData.Stonecutting(_, _, _) => "stonecutting"
      case RecipeData.Smithing(_, _, _) => "smithing"
      case RecipeData.NoAdditionalData(recipeType) => recipeType
    }

    given ByteCodec[CookingRecipeData] = autogenerateFor[CookingRecipeData]
    given ByteCodec[RecipeData.Shapeless] = autogenerateFor[RecipeData.Shapeless]
    given ByteCodec[RecipeData.Shaped] = autogenerateFor[RecipeData.Shaped]
    given ByteCodec[RecipeData.Stonecutting] = autogenerateFor[RecipeData.Stonecutting]
    given ByteCodec[RecipeData.Smithing] = autogenerateFor[RecipeData.Smithing]

    def decodeRecipeData(recipeType: String): ByteDecode[RecipeData] = {
      def decodeCookingRecipeDataWith(recipeType: CookingDataType) =
        ByteCodec[CookingRecipeData].decode.map(RecipeData.Cooking(recipeType, _))

      recipeType match {
        case "crafting_shapeless" => ByteCodec[RecipeData.Shapeless].decode
        case "crafting_shaped" => ByteCodec[RecipeData.Shaped].decode
        case "stonecutting" => ByteCodec[RecipeData.Stonecutting].decode
        case "smithing" => ByteCodec[RecipeData.Smithing].decode
        case "smelting" => decodeCookingRecipeDataWith(CookingDataType.Smelting)
        case "blasting" => decodeCookingRecipeDataWith(CookingDataType.Blasting)
        case "smoking" => decodeCookingRecipeDataWith(CookingDataType.Smoking)
        case "campfire_cooking" => decodeCookingRecipeDataWith(CookingDataType.CampfireCooking)
        case "crafting_special_armordye" |
             "crafting_special_bookcloning" |
             "crafting_special_mapcloning" |
             "crafting_special_mapextending" |
             "crafting_special_firework_rocket" |
             "crafting_special_firework_star" |
             "crafting_special_firework_star_fade" |
             "crafting_special_repairitem" |
             "crafting_special_tippedarrow" |
             "crafting_special_bannerduplicate" |
             "crafting_special_banneraddpattern" |
             "crafting_special_shielddecoration" |
             "crafting_special_shulkerboxcoloring" |
             "crafting_special_suspiciousstew" =>
          Monad[ByteDecode].pure(RecipeData.NoAdditionalData(recipeType))
        case _ =>
          giveupParsingPacket(s"The recipe type ${recipeType} is unknown to the parser.")
      }
    }

    // encoder for RecipeData. This encoder does not write out the type of the recipe.
    // for details, see https://wiki.vg/index.php?title=Protocol&oldid=16953#Declare_Recipes
    lazy val encodeRecipeData: ByteEncode[RecipeData] = (recipeData: RecipeData) => recipeData match {
      case recipeData: RecipeData.Shapeless => ByteCodec[RecipeData.Shapeless].encode.write(recipeData)
      case recipeData: RecipeData.Shaped => ByteCodec[RecipeData.Shaped].encode.write(recipeData)
      case recipeData: RecipeData.Stonecutting => ByteCodec[RecipeData.Stonecutting].encode.write(recipeData)
      case recipeData: RecipeData.Smithing => ByteCodec[RecipeData.Smithing].encode.write(recipeData)
      case RecipeData.Cooking(_, data) => ByteCodec[CookingRecipeData].encode.write(data)
      case RecipeData.NoAdditionalData(_) => Chunk.empty[Byte]
    }

    given ByteCodec[Recipe] = {
      val decode: ByteDecode[Recipe] =
        for {
          recipeType <- ByteCodec[String].decode
          recipeId <- ByteCodec[String].decode
          recipeData <- decodeRecipeData(recipeType)
        } yield Recipe(recipeId, recipeData)

      val encode: ByteEncode[Recipe] = { case Recipe(identifier, data) =>
        ByteCodec[String].encode.write(recipeDataTypeString(data)) ++
        ByteCodec[String].encode.write(identifier) ++
        encodeRecipeData.write(data)
      }

      ByteCodec[Recipe](decode, encode)
    }

    given ByteCodec[CommandArgument.DoubleA] = autogenerateFor[CommandArgument.DoubleA]
    given ByteCodec[CommandArgument.FloatA] = autogenerateFor[CommandArgument.FloatA]
    given ByteCodec[CommandArgument.IntegerA] = autogenerateFor[CommandArgument.IntegerA]
    given ByteCodec[CommandArgument.LongA] = autogenerateFor[CommandArgument.LongA]
    given ByteCodec[CommandArgument.StringA] = autogenerateFor[CommandArgument.StringA]
    given ByteCodec[CommandArgument.EntityA] = autogenerateFor[CommandArgument.EntityA]
    given ByteCodec[CommandArgument.ScoreHolderA] = autogenerateFor[CommandArgument.ScoreHolderA]
    given ByteCodec[CommandArgument.RangeA] = autogenerateFor[CommandArgument.RangeA]

    def decodeCommandArgumentFor(typeIdentifier: String): ByteDecode[CommandArgument] = typeIdentifier match {
      case "brigadier:double" => ByteCodec[CommandArgument.DoubleA].decode
      case "brigadier:float" => ByteCodec[CommandArgument.FloatA].decode
      case "brigadier:integer" => ByteCodec[CommandArgument.IntegerA].decode
      case "brigadier:long" => ByteCodec[CommandArgument.LongA].decode
      case "brigadier:string" => ByteCodec[CommandArgument.StringA].decode
      case "brigadier:entity" => ByteCodec[CommandArgument.EntityA].decode
      case "brigadier:score_holder" => ByteCodec[CommandArgument.ScoreHolderA].decode
      case "brigadier:range" => ByteCodec[CommandArgument.RangeA].decode
      case "brigadier:bool" |
           "minecraft:game_profile" |
           "minecraft:block_pos" |
           "minecraft:column_pos" |
           "minecraft:vec3" |
           "minecraft:vec2" |
           "minecraft:block_state" |
           "minecraft:block_predicate" |
           "minecraft:item_stack" |
           "minecraft:item_predicate" |
           "minecraft:color" |
           "minecraft:component" |
           "minecraft:message" |
           "minecraft:nbt" |
           "minecraft:nbt_path" |
           "minecraft:objective" |
           "minecraft:objective_criteria" |
           "minecraft:operation" |
           "minecraft:particle" |
           "minecraft:rotation" |
           "minecraft:angle" |
           "minecraft:scoreboard_slot" |
           "minecraft:swizzle" |
           "minecraft:team" |
           "minecraft:item_slot" |
           "minecraft:resource_location" |
           "minecraft:mob_effect" |
           "minecraft:function" |
           "minecraft:entity_anchor" |
           "minecraft:int_range" |
           "minecraft:float_range" |
           "minecraft:item_enchantment" |
           "minecraft:entity_summon" |
           "minecraft:dimension" |
           "minecraft:uuid" |
           "minecraft:nbt_tag" |
           "minecraft:nbt_compound_tag" |
           "minecraft:time" |
           "forge:modid" |
           "forge:enum" => Monad[ByteDecode].pure(CommandArgument.ArgumentWithoutProperties(typeIdentifier))
      case _ => giveupParsingPacket(s"command argument type $typeIdentifier is unknown")
    }

    /**
     * a CommandArgument is encoded as a pair of parser specifier (String) and parser property (varying type).
     * see https://wiki.vg/Command_Data for details */
    given ByteCodec[CommandArgument] = ByteCodec[CommandArgument](
      ByteCodec[String].decode.flatMap(decodeCommandArgumentFor),
      (argInfo: CommandArgument) => argInfo match {
        case argInfo: CommandArgument.DoubleA =>
          ByteCodec[String].encode.write("brigadier:double") ++
          ByteCodec[CommandArgument.DoubleA].encode.write(argInfo)
        case argInfo: CommandArgument.FloatA =>
          ByteCodec[String].encode.write("brigadier:float") ++
          ByteCodec[CommandArgument.FloatA].encode.write(argInfo)
        case argInfo: CommandArgument.IntegerA =>
          ByteCodec[String].encode.write("brigadier:integer") ++
          ByteCodec[CommandArgument.IntegerA].encode.write(argInfo)
        case argInfo: CommandArgument.LongA =>
          ByteCodec[String].encode.write("brigadier:long") ++
          ByteCodec[CommandArgument.LongA].encode.write(argInfo)
        case argInfo: CommandArgument.StringA =>
          ByteCodec[String].encode.write("brigadier:string") ++
          ByteCodec[CommandArgument.StringA].encode.write(argInfo)
        case argInfo: CommandArgument.EntityA =>
          ByteCodec[String].encode.write("brigadier:entity") ++
          ByteCodec[CommandArgument.EntityA].encode.write(argInfo)
        case argInfo: CommandArgument.ScoreHolderA =>
          ByteCodec[String].encode.write("brigadier:score_holder") ++
          ByteCodec[CommandArgument.ScoreHolderA].encode.write(argInfo)
        case argInfo: CommandArgument.RangeA =>
          ByteCodec[String].encode.write("brigadier:range") ++
          ByteCodec[CommandArgument.RangeA].encode.write(argInfo)
        case argInfo: CommandArgument.ArgumentWithoutProperties =>
          ByteCodec[String].encode.write(argInfo.typeIdentifier)
      }
    )

    given ByteCodec[CommandNode] = {
      given ByteDecode[CommandArgument] = codecToDecode[CommandArgument]

      autogenerateFor[CommandNode]
    }

    given ByteCodec[NBTCompound] = ByteCodec[NBTCompound](
      ReadNBT.read[ByteDecode].map(_._2),
      (compound: NBTCompound) => WriteNBT.toChunk(compound, rootName = "", gzip = false)
    )

  }

}
