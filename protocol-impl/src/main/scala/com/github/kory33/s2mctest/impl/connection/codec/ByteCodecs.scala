package com.github.kory33.s2mctest.impl.connection.codec

import cats.Monad
import com.github.kory33.s2mctest.core.connection.codec.dsl.{DecodeBytes, DecodeFiniteBytes}
import com.github.kory33.s2mctest.core.connection.codec.{ByteCodec, ByteEncode}
import com.github.kory33.s2mctest.impl.connection.codec.decode.macros.GenByteDecode
import com.github.kory33.s2mctest.impl.connection.codec.decode.{PrimitiveDecodes, VarNumDecodes}
import com.github.kory33.s2mctest.impl.connection.codec.encode.{PrimitiveEncodes, VarNumEncodes}
import com.github.kory33.s2mctest.impl.connection.codec.typenbtio.{ReadNBT, WriteNBT}
import com.github.kory33.s2mctest.impl.connection.typeclass.IntLike
import fs2.Chunk
import net.katsstuff.typenbt.NBTCompound
import shapeless3.deriving.K0

import java.nio.charset.Charset
import java.util.UUID
import scala.util.NotGiven

object ByteCodecs {

  import com.github.kory33.s2mctest.impl.connection.codec.decode.PrimitiveDecodes.*
  import com.github.kory33.s2mctest.impl.connection.codec.encode.PrimitiveEncodes.*

  import com.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketDataCompoundTypes.*
  import cats.implicits.given

  // because DecodeFiniteBytes is invariant but we would like it to behave like a covariant ADT...
  import com.github.kory33.s2mctest.core.generic.conversions.AutoWidenFunctor.given

  import scala.language.implicitConversions

  inline def autogenerateFor[T](using gen: K0.Generic[T]): ByteCodec[T] =
    ByteCodec(GenByteDecode.gen[T], ByteEncode.forADT[T])

  object Common {

    inline given codecToEncode[A: ByteCodec]: ByteEncode[A] = ByteCodec[A].encode

    inline given codecToDecode[A: ByteCodec]: DecodeFiniteBytes[A] = ByteCodec[A].decode

    /**
     * Codec of Unit (empty type).
     */
    given ByteCodec[Unit] =
      ByteCodec[Unit](decodeUnit.inject, encodeUnit)

    given ByteCodec[Boolean] =
      ByteCodec[Boolean](decodeBoolean.inject, encodeBoolean)

    given ByteCodec[Byte] =
      ByteCodec[Byte](decodeByte.inject, encodeByte)

    given ByteCodec[Short] = ByteCodec[Short](decodeBigEndianShort.inject, encodeShortBigEndian)

    given ByteCodec[Int] = ByteCodec[Int](decodeBigEndianInt.inject, encodeIntBigEndian)

    given ByteCodec[Long] = ByteCodec[Long](decodeBigEndianLong.inject, encodeLongBigEndian)

    given ByteCodec[Float] = ByteCodec[Float](decodeFloat.inject, encodeFloat)

    given ByteCodec[Double] = ByteCodec[Double](decodeDouble.inject, encodeDouble)

    given ByteCodec[UByte] = ByteCodec[Byte].imap(UByte.fromRawByte)(_.asRawByte)

    given ByteCodec[UShort] = ByteCodec[Short].imap(UShort.fromRawShort)(_.asRawShort)

    given ByteCodec[VarInt] =
      ByteCodec[Int](VarNumDecodes.decodeVarIntAsInt.inject, VarNumEncodes.encodeIntAsVarInt)
        .imap(VarInt.apply)(_.raw)

    given ByteCodec[VarLong] =
      ByteCodec[Long](
        VarNumDecodes.decodeVarLongAsLong.inject,
        VarNumEncodes.encodeLongAsVarLong
      ).imap(VarLong.apply)(_.raw)

    given ByteCodec[String] =
      ByteCodec[String](
        ByteCodec[VarInt]
          .decode
          .flatMap(length => PrimitiveDecodes.decodeUTF8String(length.raw).inject),
        (x: String) =>
          ByteCodec[VarInt].encode.write(VarInt(x.length)) ++ PrimitiveEncodes
            .encodeUTF8String
            .write(x)
      )

    given lenPrefixed[L: IntLike: ByteCodec, A: ByteCodec](
      // we wish to specialise for A =:= Byte using lenPrefixedByteSeq
      using NotGiven[A =:= Byte]
    ): ByteCodec[LenPrefixedSeq[L, A]] = {
      val decode: DecodeFiniteBytes[LenPrefixedSeq[L, A]] = for {
        length <- ByteCodec[L].decode
        intLength = IntLike[L].toInt(length)
        aList <- ByteCodec[A].decode.replicateA(intLength)
      } yield LenPrefixedSeq(aList.toVector)

      val encode: ByteEncode[LenPrefixedSeq[L, A]] = { (lenSeq: LenPrefixedSeq[L, A]) =>
        ByteCodec[L].encode.write(lenSeq.lLength) ++ ByteEncode[Vector[A]]
          .write(lenSeq.asVector)
      }

      ByteCodec[LenPrefixedSeq[L, A]](decode, encode)
    }

    given lenPrefixedByteSeq[L: IntLike: ByteCodec]: ByteCodec[LenPrefixedByteSeq[L]] = {
      val decode: DecodeFiniteBytes[LenPrefixedByteSeq[L]] = for {
        length <- ByteCodec[L].decode
        intLength = IntLike[L].toInt(length)
        resultChunk <- DecodeFiniteBytes.read(intLength)
      } yield LenPrefixedSeq(resultChunk.toVector)

      val encode: ByteEncode[LenPrefixedByteSeq[L]] = { (lenSeq: LenPrefixedByteSeq[L]) =>
        ByteCodec[L].encode.write(lenSeq.lLength) ++ fs2.Chunk.vector(lenSeq.asVector)
      }

      ByteCodec[LenPrefixedByteSeq[L]](decode, encode)
    }

    given fixedPoint5ForIntegral[A: ByteCodec: Integral]: ByteCodec[FixedPoint5[A]] =
      ByteCodec[A].imap(FixedPoint5.fromRaw[A])(_.rawValueFP5)

    given fixedPoint12ForIntegral[A: ByteCodec: Integral]: ByteCodec[FixedPoint12[A]] =
      ByteCodec[A].imap(FixedPoint12.fromRaw[A])(_.rawValueFP12)

    given ByteCodec[UnspecifiedLengthByteArray] = ByteCodec[UnspecifiedLengthByteArray](
      DecodeFiniteBytes.readUntilTheEnd.map(c => UnspecifiedLengthByteArray(c.toArray)),
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

    /**
     * see https://wiki.vg/index.php?title=Protocol&oldid=16953#Entity_Equipment for details
     */
    given ByteCodec[EntityEquipments] = {
      import com.github.kory33.s2mctest.core.generic.extensions.MonadValueExt.repeatWhileM

      ByteCodec[EntityEquipments](
        ByteCodec[EntityEquipment].decode.repeatWhileM {
          case EntityEquipment(slot, _) =>
            (slot & 0x80) != 0
        },
        summon[ByteEncode[Vector[EntityEquipment]]]
      )
    }

    given ByteCodec[Biomes3D] =
      ByteCodec[Biomes3D](
        ByteCodec[Int].decode.replicateA(1024).map { intList => Biomes3D(intList.toArray) },
        biome => ByteEncode[Vector[Int]].write(biome.arrayData.toVector)
      )

    given ByteCodec[PlayerProperty] = autogenerateFor[PlayerProperty]

    given ByteCodec[PlayerInfoDataRecord.AddPlayer] =
      autogenerateFor[PlayerInfoDataRecord.AddPlayer]

    given ByteCodec[PlayerInfoDataRecord.UpdateGamemode] =
      autogenerateFor[PlayerInfoDataRecord.UpdateGamemode]

    given ByteCodec[PlayerInfoDataRecord.UpdateLatency] =
      autogenerateFor[PlayerInfoDataRecord.UpdateLatency]

    given ByteCodec[PlayerInfoDataRecord.UpdateDisplayName] =
      autogenerateFor[PlayerInfoDataRecord.UpdateDisplayName]

    given ByteCodec[PlayerInfoDataRecord.RemovePlayer] =
      autogenerateFor[PlayerInfoDataRecord.RemovePlayer]

    given ByteCodec[PlayerInfoData] = ByteCodec[PlayerInfoData](
      ByteCodec[VarInt].decode.flatMap { actionVarInt =>
        actionVarInt.raw match {
          case 0 =>
            ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.AddPlayer]]
              .decode
              .map(PlayerInfoData.AddPlayer.apply)
          case 1 =>
            ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.UpdateGamemode]]
              .decode
              .map(PlayerInfoData.UpdateGamemode.apply)
          case 2 =>
            ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.UpdateLatency]]
              .decode
              .map(PlayerInfoData.UpdateLatency.apply)
          case 3 =>
            ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.UpdateDisplayName]]
              .decode
              .map(PlayerInfoData.UpdateDisplayName.apply)
          case 4 =>
            ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.RemovePlayer]]
              .decode
              .map(PlayerInfoData.RemovePlayer.apply)
        }
      },
      {
        case PlayerInfoData.AddPlayer(players) =>
          ByteCodec[VarInt].encode.write(VarInt(0)) ++
            ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.AddPlayer]]
              .encode
              .write(players)
        case PlayerInfoData.UpdateGamemode(players) =>
          ByteCodec[VarInt].encode.write(VarInt(1)) ++
            ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.UpdateGamemode]]
              .encode
              .write(players)
        case PlayerInfoData.UpdateLatency(players) =>
          ByteCodec[VarInt].encode.write(VarInt(2)) ++
            ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.UpdateLatency]]
              .encode
              .write(players)
        case PlayerInfoData.UpdateDisplayName(players) =>
          ByteCodec[VarInt].encode.write(VarInt(3)) ++
            ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.UpdateDisplayName]]
              .encode
              .write(players)
        case PlayerInfoData.RemovePlayer(players) =>
          ByteCodec[VarInt].encode.write(VarInt(4)) ++
            ByteCodec[LenPrefixedSeq[VarInt, PlayerInfoDataRecord.RemovePlayer]]
              .encode
              .write(players)
      }
    )

    def recipeDataTypeString(recipeData: RecipeData): String = recipeData match {
      case RecipeData.Shapeless(_, _, _)    => "crafting_shapeless"
      case RecipeData.Shaped(_, _, _, _, _) => "crafting_shaped"
      case RecipeData.Cooking(tpe, _) =>
        tpe match {
          case CookingDataType.Smelting        => "smelting"
          case CookingDataType.Blasting        => "blasting"
          case CookingDataType.Smoking         => "smoking"
          case CookingDataType.CampfireCooking => "campfire_cooking"
        }
      case RecipeData.Stonecutting(_, _, _)        => "stonecutting"
      case RecipeData.Smithing(_, _, _)            => "smithing"
      case RecipeData.NoAdditionalData(recipeType) => recipeType
    }

    given ByteCodec[CookingRecipeData] = autogenerateFor[CookingRecipeData]

    given ByteCodec[RecipeData.Shapeless] = autogenerateFor[RecipeData.Shapeless]

    given ByteCodec[RecipeData.Shaped] = {
      val decode: DecodeFiniteBytes[RecipeData.Shaped] =
        for {
          width <- summon[DecodeFiniteBytes[VarInt]]
          height <- summon[DecodeFiniteBytes[VarInt]]
          group <- summon[DecodeFiniteBytes[String]]
          ingredients <- summon[DecodeFiniteBytes[RecipeIngredient]]
            .replicateA(width.raw * height.raw)
          result <- summon[DecodeFiniteBytes[Slot]]
        } yield RecipeData.Shaped(width, height, group, ingredients.toVector, result)

      ByteCodec(decode, ByteEncode.forADT[RecipeData.Shaped])
    }

    given ByteCodec[RecipeData.Stonecutting] = autogenerateFor[RecipeData.Stonecutting]

    given ByteCodec[RecipeData.Smithing] = autogenerateFor[RecipeData.Smithing]

    def decodeRecipeData(recipeType: String): DecodeFiniteBytes[RecipeData] = {
      def decodeCookingRecipeDataWith(recipeType: CookingDataType) =
        ByteCodec[CookingRecipeData].decode.map(RecipeData.Cooking(recipeType, _))

      recipeType.stripPrefix("minecraft:") match {
        case "crafting_shapeless" => ByteCodec[RecipeData.Shapeless].decode
        case "crafting_shaped"    => ByteCodec[RecipeData.Shaped].decode
        case "stonecutting"       => ByteCodec[RecipeData.Stonecutting].decode
        case "smithing"           => ByteCodec[RecipeData.Smithing].decode
        case "smelting"           => decodeCookingRecipeDataWith(CookingDataType.Smelting)
        case "blasting"           => decodeCookingRecipeDataWith(CookingDataType.Blasting)
        case "smoking"            => decodeCookingRecipeDataWith(CookingDataType.Smoking)
        case "campfire_cooking" => decodeCookingRecipeDataWith(CookingDataType.CampfireCooking)
        case "crafting_special_armordye" | "crafting_special_bookcloning" |
            "crafting_special_mapcloning" | "crafting_special_mapextending" |
            "crafting_special_firework_rocket" | "crafting_special_firework_star" |
            "crafting_special_firework_star_fade" | "crafting_special_repairitem" |
            "crafting_special_tippedarrow" | "crafting_special_bannerduplicate" |
            "crafting_special_banneraddpattern" | "crafting_special_shielddecoration" |
            "crafting_special_shulkerboxcoloring" | "crafting_special_suspiciousstew" =>
          DecodeFiniteBytes.pure(RecipeData.NoAdditionalData(recipeType))
        case _ =>
          DecodeFiniteBytes.giveUp(s"The recipe type ${recipeType} is unknown to the parser.")
      }
    }

    // encoder for RecipeData. This encoder does not write out the type of the recipe.
    // for details, see https://wiki.vg/index.php?title=Protocol&oldid=16953#Declare_Recipes
    lazy val encodeRecipeData: ByteEncode[RecipeData] = {
      case recipeData: RecipeData.Shapeless =>
        ByteCodec[RecipeData.Shapeless].encode.write(recipeData)
      case recipeData: RecipeData.Shaped =>
        ByteCodec[RecipeData.Shaped].encode.write(recipeData)
      case recipeData: RecipeData.Stonecutting =>
        ByteCodec[RecipeData.Stonecutting].encode.write(recipeData)
      case recipeData: RecipeData.Smithing =>
        ByteCodec[RecipeData.Smithing].encode.write(recipeData)
      case RecipeData.Cooking(_, data)    => ByteCodec[CookingRecipeData].encode.write(data)
      case RecipeData.NoAdditionalData(_) => Chunk.empty[Byte]
    }

    given ByteCodec[Recipe] = {
      val decode: DecodeFiniteBytes[Recipe] =
        for {
          recipeType <- ByteCodec[String].decode
          recipeId <- ByteCodec[String].decode
          recipeData <- decodeRecipeData(recipeType)
        } yield Recipe(recipeId, recipeData)

      val encode: ByteEncode[Recipe] = {
        case Recipe(identifier, data) =>
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

    given ByteCodec[CommandArgument.ScoreHolderA] =
      autogenerateFor[CommandArgument.ScoreHolderA]

    given ByteCodec[CommandArgument.RangeA] = autogenerateFor[CommandArgument.RangeA]

    def decodeCommandArgumentFor(typeIdentifier: String): DecodeFiniteBytes[CommandArgument] =
      typeIdentifier match {
        case "brigadier:double"       => ByteCodec[CommandArgument.DoubleA].decode
        case "brigadier:float"        => ByteCodec[CommandArgument.FloatA].decode
        case "brigadier:integer"      => ByteCodec[CommandArgument.IntegerA].decode
        case "brigadier:long"         => ByteCodec[CommandArgument.LongA].decode
        case "brigadier:string"       => ByteCodec[CommandArgument.StringA].decode
        case "minecraft:entity"       => ByteCodec[CommandArgument.EntityA].decode
        case "minecraft:score_holder" => ByteCodec[CommandArgument.ScoreHolderA].decode
        case "minecraft:range"        => ByteCodec[CommandArgument.RangeA].decode
        case "brigadier:bool" | "minecraft:game_profile" | "minecraft:block_pos" |
            "minecraft:column_pos" | "minecraft:vec3" | "minecraft:vec2" |
            "minecraft:block_state" | "minecraft:block_predicate" | "minecraft:item_stack" |
            "minecraft:item_predicate" | "minecraft:color" | "minecraft:component" |
            "minecraft:message" | "minecraft:nbt" | "minecraft:nbt_path" |
            "minecraft:objective" | "minecraft:objective_criteria" | "minecraft:operation" |
            "minecraft:particle" | "minecraft:rotation" | "minecraft:angle" |
            "minecraft:scoreboard_slot" | "minecraft:swizzle" | "minecraft:team" |
            "minecraft:item_slot" | "minecraft:resource_location" | "minecraft:mob_effect" |
            "minecraft:function" | "minecraft:entity_anchor" | "minecraft:int_range" |
            "minecraft:float_range" | "minecraft:item_enchantment" | "minecraft:entity_summon" |
            "minecraft:dimension" | "minecraft:uuid" | "minecraft:nbt_tag" |
            "minecraft:nbt_compound_tag" | "minecraft:time" | "forge:modid" | "forge:enum" =>
          DecodeFiniteBytes.pure(CommandArgument.ArgumentWithoutProperties(typeIdentifier))
        case _ => DecodeFiniteBytes.giveUp(s"command argument type $typeIdentifier is unknown")
      }

    /**
     * a CommandArgument is encoded as a pair of parser specifier (String) and parser property
     * (varying type). see https://wiki.vg/Command_Data for details
     */
    given ByteCodec[CommandArgument] = ByteCodec[CommandArgument](
      ByteCodec[String].decode.flatMap(decodeCommandArgumentFor),
      {
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
      given DecodeFiniteBytes[CommandArgument] = codecToDecode[CommandArgument]

      autogenerateFor[CommandNode]
    }

    given ByteCodec[NBTCompound] = ByteCodec[NBTCompound](
      ReadNBT.read.map(_._2).inject,
      (compound: NBTCompound) => WriteNBT.toChunk(compound, rootName = "", gzip = false)
    )

    given ByteCodec[NBTCompoundOrEnd] = ByteCodec[NBTCompoundOrEnd](
      ReadNBT
        .readCompoundOrEnd
        .map {
          case Some((_, compound)) => NBTCompoundOrEnd.Compound(compound)
          case None                => NBTCompoundOrEnd.End
        }
        .inject,
      {
        case NBTCompoundOrEnd.Compound(compound) =>
          WriteNBT.toChunk(compound, rootName = "", gzip = false)
        case NBTCompoundOrEnd.End => fs2.Chunk(0x00: Byte)
      }
    )
  }

  object PositionCodec {

    // current codec of Position type.
    // see https://wiki.vg/Protocol#Position for details.

    import Common.given

    given ByteCodec[Position] =
      ByteCodec[Long].imap { long =>
        Position((long >> 38).toInt, (long & 0xfff).toShort, ((long << 26) >> 38).toInt)
      } {
        case Position(x, y, z) =>
          ((x & 0x3ffffff).toLong << 38) | ((z & 0x3ffffff).toLong << 12) | (y & 0xfff).toLong
      }
  }

  object PositionCodecBefore1_14 {

    // codec of Position type before 1.14.
    // see https://wiki.vg/index.php?title=Data_types&oldid=14345#Position for details

    import Common.given

    given ByteCodec[Position] =
      ByteCodec[Long].imap { long =>
        Position((long >> 38).toInt, ((long >> 26) & 0xfff).toShort, ((long << 38) >> 38).toInt)
      } {
        case Position(x, y, z) =>
          ((x & 0x03ffffff).toLong << 38) | ((y & 0x00000fff).toLong << 26) | (z & 0x03ffffff).toLong
      }
  }

}
