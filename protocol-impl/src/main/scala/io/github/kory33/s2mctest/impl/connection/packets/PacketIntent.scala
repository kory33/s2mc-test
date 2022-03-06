package io.github.kory33.s2mctest.impl.connection.packets

import cats.Monad
import cats.instances.map
import fs2.Chunk
import net.katsstuff.typenbt.NBTCompound

import java.util.UUID

import PacketDataPrimitives.*
import PacketDataCompoundTypes.*

/**
 * -- from Stevenarella(https://github.com/iceiix/stevenarella),
 * -- which is based on Steven (https://github.com/thinkofname/steven)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/**
 * A collection of class, each of which represents a record of a particular action.
 *
 * Note that these definitions are pure data and are not aware of how they are being encoded
 * into (or decoded from) binary data. Codec is defined elsewhere and it is not a job of these
 * classes.
 */
object PacketIntent {
  object Handshaking {
    object ServerBound {

      /**
       * Handshake is the first packet sent in the protocol. Its used for deciding if the
       * request is a client is requesting status information about the server (MOTD, players
       * etc) or trying to login to the server.
       *
       * The host and port fields are not used by the vanilla server but are there for virtual
       * server hosting to be able to redirect a client to a target server with a single address
       * + port.
       *
       * Some modified servers/proxies use the handshake field differently, packing information
       * into the field other than the hostname due to the protocol not providing any system for
       * custom information to be transfered by the client to the server until after login.
       */
      case class Handshake(
        /**
         * The protocol version of the connecting client
         */
        protocolVersion: VarInt,
        /**
         * The hostname the client connected to
         */
        host: String,
        /**
         * The port the client connected to
         */
        port: UShort,
        /**
         * The next protocol state the client wants
         */
        next: VarInt
      )
    }
    object ClientBound {}
  }
  object Play {
    object ServerBound {

      /**
       * TeleportConfirm is sent by the client as a reply to a telport from the server.
       */
      case class TeleportConfirm(teleportId: VarInt)

      case class QueryBlockNBT(transactionId: VarInt, location: Position)

      case class SetDifficulty(newDifficulty: UByte)

      /**
       * TabComplete is sent by the client when the client presses tab in the chat box.
       */
      case class TabComplete(
        text: String,
        assumeCommand: Boolean,
        hasTarget: Boolean,
        target: Option[Position]
      ) {
        require(target.nonEmpty == (hasTarget))
      }

      case class TabComplete_NoAssume(
        text: String,
        hasTarget: Boolean,
        target: Option[Position]
      ) {
        require(target.nonEmpty == (hasTarget))
      }

      case class TabComplete_NoAssume_NoTarget(text: String)

      /**
       * ChatMessage is sent by the client when it sends a chat message or executes a command
       * (prefixed by '/').
       */
      case class ChatMessage(message: String)

      /**
       * ClientStatus is sent to update the client's status
       */
      case class ClientStatus(actionId: VarInt)

      case class ClientStatus_u8(actionId: UByte)

      /**
       * ClientSettings is sent by the client to update its current settings.
       */
      case class ClientSettings(
        locale: String,
        viewDistance: UByte,
        chatMode: VarInt,
        chatColors: Boolean,
        displayedSkinParts: UByte,
        mainHand: VarInt
      )

      case class ClientSettings_u8(
        locale: String,
        viewDistance: UByte,
        chatMode: UByte,
        chatColors: Boolean,
        displayedSkinParts: UByte,
        mainHand: VarInt
      )

      case class ClientSettings_u8_Handsfree(
        locale: String,
        viewDistance: UByte,
        chatMode: UByte,
        chatColors: Boolean,
        displayedSkinParts: UByte
      )

      case class ClientSettings_u8_Handsfree_Difficulty(
        locale: String,
        viewDistance: UByte,
        chatMode: UByte,
        chatColors: Boolean,
        difficulty: UByte,
        displayedSkinParts: UByte
      )

      /**
       * ConfirmTransactionServerbound is a reply to ConfirmTransaction.
       */
      case class ConfirmTransactionServerbound(
        id: UByte,
        actionNumber: Short,
        accepted: Boolean
      )

      /**
       * EnchantItem is sent when the client enchants an item.
       */
      case class EnchantItem(id: UByte, enchantment: UByte)

      /**
       * ClickWindowButton is used for clicking an enchantment, lectern, stonecutter, or loom.
       */
      case class ClickWindowButton(id: UByte, button: UByte)

      /**
       * ClickWindow is sent when the client clicks in a window.
       */
      case class ClickWindow_State[S <: Slot](
        id: UByte,
        state: VarInt,
        slot: Short,
        button: Byte,
        mode: VarInt,
        slots: LenPrefixedSeq[Short, S],
        clickedItem: S
      )

      case class ClickWindow[S <: Slot](
        id: UByte,
        slot: Short,
        button: UByte,
        actionNumber: UShort,
        mode: VarInt,
        clickedItem: S
      )

      case class ClickWindow_u8(
        id: UByte,
        slot: Short,
        button: UByte,
        actionNumber: UShort,
        mode: UByte,
        clickedItem: Slot.Upto_1_12_2
      )

      /**
       * CloseWindow is sent when the client closes a window.
       */
      case class CloseWindow(id: UByte)

      /**
       * PluginMessageServerbound is used for custom messages between the client and server.
       * This is mainly for plugins/mods but vanilla has a few channels registered too.
       */
      case class PluginMessageServerbound(channel: String, data: UnspecifiedLengthByteArray)

      case class PluginMessageServerbound_i16(channel: String, data: LenPrefixedByteSeq[Short])

      case class EditBook(newBook: Slot.Upto_1_17_1, isSigning: Boolean, hand: VarInt)

      case class QueryEntityNBT(transactionId: VarInt, entityId: VarInt)

      /**
       * UseEntity is sent when the user interacts (right clicks) or attacks (left clicks) an
       * entity.
       */
      case class UseEntity_Sneakflag(
        targetId: VarInt,
        ty: VarInt,
        targetX: Option[Float],
        targetY: Option[Float],
        targetZ: Option[Float],
        hand: Option[VarInt],
        sneaking: Boolean
      ) {
        require(targetX.nonEmpty == (ty == VarInt(2)))
        require(targetY.nonEmpty == (ty == VarInt(2)))
        require(targetZ.nonEmpty == (ty == VarInt(2)))
        require(hand.nonEmpty == (ty == VarInt(0) || ty == VarInt(2)))
      }

      case class UseEntity_Hand(
        targetId: VarInt,
        ty: VarInt,
        targetX: Option[Float],
        targetY: Option[Float],
        targetZ: Option[Float],
        hand: Option[VarInt]
      ) {
        require(targetX.nonEmpty == (ty == VarInt(2)))
        require(targetY.nonEmpty == (ty == VarInt(2)))
        require(targetZ.nonEmpty == (ty == VarInt(2)))
        require(hand.nonEmpty == (ty == VarInt(0) || ty == VarInt(2)))
      }

      case class UseEntity_Handsfree(
        targetId: VarInt,
        ty: VarInt,
        targetX: Option[Float],
        targetY: Option[Float],
        targetZ: Option[Float]
      ) {
        require(targetX.nonEmpty == (ty == VarInt(2)))
        require(targetY.nonEmpty == (ty == VarInt(2)))
        require(targetZ.nonEmpty == (ty == VarInt(2)))
      }

      case class UseEntity_Handsfree_i32(targetId: Int, ty: UByte)

      /**
       * Sent when Generate is pressed on the Jigsaw Block interface.
       */
      case class GenerateStructure(location: Position, levels: VarInt, keepJigsaws: Boolean)

      /**
       * KeepAliveServerbound is sent by a client as a response to a KeepAliveClientbound. If
       * the client doesn't reply the server may disconnect the client.
       */
      case class KeepAliveServerbound_i64(id: Long)

      case class KeepAliveServerbound_VarInt(id: VarInt)

      case class KeepAliveServerbound_i32(id: Int)

      case class LockDifficulty(locked: Boolean)

      /**
       * PlayerPosition is used to update the player's position.
       */
      case class PlayerPosition(x: Double, y: Double, z: Double, onGround: Boolean)

      case class PlayerPosition_HeadY(
        x: Double,
        feetY: Double,
        headY: Double,
        z: Double,
        onGround: Boolean
      )

      /**
       * PlayerPositionLook is a combination of PlayerPosition and PlayerLook.
       */
      case class PlayerPositionLook(
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
      )

      case class PlayerPositionLook_HeadY(
        x: Double,
        feetY: Double,
        headY: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
      )

      /**
       * PlayerLook is used to update the player's rotation.
       */
      case class PlayerLook(yaw: Float, pitch: Float, onGround: Boolean)

      /**
       * Player is used to update whether the player is on the ground or not.
       */
      case class Player(onGround: Boolean)

      /**
       * Sent by the client when in a vehicle instead of the normal move packet.
       */
      case class VehicleMove(x: Double, y: Double, z: Double, yaw: Float, pitch: Float)

      /**
       * SteerBoat is used to visually update the boat paddles.
       */
      case class SteerBoat(leftPaddleTurning: Boolean, rightPaddleTurning: Boolean)

      case class PickItem(slotToUse: VarInt)

      /**
       * CraftRecipeRequest is sent when player clicks a recipe in the crafting book.
       */
      case class CraftRecipeRequest(windowId: UByte, recipe: VarInt, makeAll: Boolean)

      /**
       * ClientAbilities is used to modify the players current abilities. Currently flying is
       * the only one
       */
      case class ClientAbilities_f32(flags: UByte, flyingSpeed: Float, walkingSpeed: Float)

      case class ClientAbilities_u8(flags: UByte)

      /**
       * PlayerDigging is sent when the client starts/stops digging a block. It also can be sent
       * for droppping items and eating/shooting.
       */
      case class PlayerDigging(status: VarInt, location: Position, face: UByte)

      case class PlayerDigging_u8(status: UByte, location: Position, face: UByte)

      case class PlayerDigging_u8_u8y(status: UByte, x: Int, y: UByte, z: Int, face: UByte)

      /**
       * PlayerAction is sent when a player preforms various actions.
       */
      case class PlayerAction(entityId: VarInt, actionId: VarInt, jumpBoost: VarInt)

      case class PlayerAction_i32(entityId: Int, actionId: Byte, jumpBoost: Int)

      /**
       * SteerVehicle is sent by the client when steers or preforms an action on a vehicle.
       */
      case class SteerVehicle(sideways: Float, forward: Float, flags: UByte)

      case class SteerVehicle_jump_unmount(
        sideways: Float,
        forward: Float,
        jump: Boolean,
        unmount: Boolean
      )

      /**
       * CraftingBookData is sent when the player interacts with the crafting book.
       */
      case class CraftingBookData(
        action: VarInt,
        recipeId: Option[Int],
        craftingBookOpen: Option[Boolean],
        craftingFilter: Option[Boolean]
      ) {
        require(recipeId.nonEmpty == (action == VarInt(0)))
        require(craftingBookOpen.nonEmpty == (action == VarInt(1)))
        require(craftingFilter.nonEmpty == (action == VarInt(1)))
      }

      /**
       * Pong is used to response to the Ping
       */
      case class Pong(id: Int)

      /**
       * SetDisplayedRecipe replaces CraftingBookData, type 0.
       */
      case class SetDisplayedRecipe(recipeId: String)

      /**
       * SetRecipeBookState replaces CraftingBookData, type 1.
       */
      case class SetRecipeBookState(
        /**
         * TODO: enum, 0: crafting, 1: furnace, 2: blast furnace, 3: smoker
         */
        bookId: VarInt,
        bookOpen: Boolean,
        filterActive: Boolean
      )

      case class NameItem(itemName: String)

      /**
       * ResourcePackStatus informs the server of the client's current progress in activating
       * the requested resource pack
       */
      case class ResourcePackStatus(result: VarInt)

      case class ResourcePackStatus_hash(hash: String, result: VarInt)

      /**
       * TODO: Document
       */
      case class AdvancementTab(action: VarInt, tabId: Option[String]) {
        require(tabId.nonEmpty == (action == VarInt(0)))
      }

      case class SelectTrade(selectedSlot: VarInt)

      case class SetBeaconEffect(primaryEffect: VarInt, secondaryEffect: VarInt)

      /**
       * HeldItemChange is sent when the player changes the currently active hotbar slot.
       */
      case class HeldItemChange(slot: Short)

      case class UpdateCommandBlock(
        location: Position,
        command: String,
        mode: VarInt,
        flags: UByte
      )

      case class UpdateCommandBlockMinecart(
        entityId: VarInt,
        command: String,
        trackOutput: Boolean
      )

      /**
       * CreativeInventoryAction is sent when the client clicks in the creative inventory. This
       * is used to spawn items in creative.
       */
      case class CreativeInventoryAction[S <: Slot](slot: Short, clickedItem: S)

      case class UpdateJigsawBlock_Joint(
        location: Position,
        name: String,
        target: String,
        pool: String,
        finalState: String,
        jointType: String
      )

      case class UpdateJigsawBlock_Type(
        location: Position,
        attachmentType: String,
        targetPool: String,
        finalState: String
      )

      case class UpdateStructureBlock(
        location: Position,
        action: VarInt,
        mode: VarInt,
        name: String,
        offsetX: Byte,
        offsetY: Byte,
        offsetZ: Byte,
        sizeX: Byte,
        sizeY: Byte,
        sizeZ: Byte,
        mirror: VarInt,
        rotation: VarInt,
        metadata: String,
        integrity: Float,
        seed: VarLong,
        flags: Byte
      )

      /**
       * SetSign sets the text on a sign after placing it.
       */
      case class SetSign(
        location: Position,
        line1: String,
        line2: String,
        line3: String,
        line4: String
      )

      case class SetSign_i16y(
        x: Int,
        y: Short,
        z: Int,
        line1: String,
        line2: String,
        line3: String,
        line4: String
      )

      /**
       * ArmSwing is sent by the client when the player left clicks (to swing their arm).
       */
      case class ArmSwing(hand: VarInt)

      case class ArmSwing_Handsfree(empty: Unit)

      case class ArmSwing_Handsfree_ID(entityId: Int, animation: UByte)

      /**
       * SpectateTeleport is sent by clients in spectator mode to teleport to a player.
       */
      case class SpectateTeleport(target: UUID)

      /**
       * PlayerBlockPlacement is sent when the client tries to place a block.
       */
      case class PlayerBlockPlacement_f32(
        location: Position,
        face: VarInt,
        hand: VarInt,
        cursorX: Float,
        cursorY: Float,
        cursorZ: Float
      )

      case class PlayerBlockPlacement_u8(
        location: Position,
        face: VarInt,
        hand: VarInt,
        cursorX: UByte,
        cursorY: UByte,
        cursorZ: UByte
      )

      case class PlayerBlockPlacement_u8_Item(
        location: Position,
        face: UByte,
        hand: Slot.Upto_1_12_2,
        cursorX: UByte,
        cursorY: UByte,
        cursorZ: UByte
      )

      case class PlayerBlockPlacement_u8_Item_u8y(
        x: Int,
        y: UByte,
        z: Int,
        face: UByte,
        hand: Slot.Upto_1_12_2,
        cursorX: UByte,
        cursorY: UByte,
        cursorZ: UByte
      )

      case class PlayerBlockPlacement_insideblock(
        hand: VarInt,
        location: Position,
        face: VarInt,
        cursorX: Float,
        cursorY: Float,
        cursorZ: Float,
        /**
         * 1.14 added insideblock
         */
        insideBlock: Boolean
      )

      /**
       * UseItem is sent when the client tries to use an item.
       */
      case class UseItem(hand: VarInt)
    }
    object ClientBound {

      /**
       * SpawnObject is used to spawn an object or vehicle into the world when it is in range of
       * the client.
       */
      case class SpawnObject(
        entityId: VarInt,
        uuid: UUID,
        ty: UByte,
        x: Double,
        y: Double,
        z: Double,
        pitch: Byte,
        yaw: Byte,
        data: Int,
        velocityX: Short,
        velocityY: Short,
        velocityZ: Short
      )

      case class SpawnObject_i32(
        entityId: VarInt,
        uuid: UUID,
        ty: UByte,
        x: FixedPoint5[Int],
        y: FixedPoint5[Int],
        z: FixedPoint5[Int],
        pitch: Byte,
        yaw: Byte,
        data: Int,
        velocityX: Short,
        velocityY: Short,
        velocityZ: Short
      )

      case class SpawnObject_i32_NoUUID(
        entityId: VarInt,
        ty: UByte,
        x: FixedPoint5[Int],
        y: FixedPoint5[Int],
        z: FixedPoint5[Int],
        pitch: Byte,
        yaw: Byte,
        data: Int,
        velocityX: Option[Short],
        velocityY: Option[Short],
        velocityZ: Option[Short]
      ) {
        require(velocityX.nonEmpty == (data != 0))
        require(velocityY.nonEmpty == (data != 0))
        require(velocityZ.nonEmpty == (data != 0))
      }

      case class SpawnObject_VarInt(
        entityId: VarInt,
        uuid: UUID,
        /**
         * 1.14 changed u8 to VarInt
         */
        ty: VarInt,
        x: Double,
        y: Double,
        z: Double,
        pitch: Byte,
        yaw: Byte,
        data: Int,
        velocityX: Short,
        velocityY: Short,
        velocityZ: Short
      )

      /**
       * SpawnExperienceOrb spawns a single experience orb into the world when it is in range of
       * the client. The count controls the amount of experience gained when collected.
       */
      case class SpawnExperienceOrb(
        entityId: VarInt,
        x: Double,
        y: Double,
        z: Double,
        count: Short
      )

      case class SpawnExperienceOrb_i32(
        entityId: VarInt,
        x: FixedPoint5[Int],
        y: FixedPoint5[Int],
        z: FixedPoint5[Int],
        count: Short
      )

      /**
       * SpawnGlobalEntity spawns an entity which is visible from anywhere in the world.
       * Currently only used for lightning.
       */
      case class SpawnGlobalEntity(entityId: VarInt, ty: UByte, x: Double, y: Double, z: Double)

      case class SpawnGlobalEntity_i32(
        entityId: VarInt,
        ty: UByte,
        x: FixedPoint5[Int],
        y: FixedPoint5[Int],
        z: FixedPoint5[Int]
      )

      /**
       * SpawnMob is used to spawn a living entity into the world when it is in range of the
       * client.
       */
      case class SpawnMob_NoMeta(
        entityId: VarInt,
        uuid: UUID,
        ty: VarInt,
        x: Double,
        y: Double,
        z: Double,
        yaw: Byte,
        pitch: Byte,
        headPitch: Byte,
        velocityX: Short,
        velocityY: Short,
        velocityZ: Short
      )

      case class SpawnMob_WithMeta(
        entityId: VarInt,
        uuid: UUID,
        ty: VarInt,
        x: Double,
        y: Double,
        z: Double,
        yaw: Byte,
        pitch: Byte,
        headPitch: Byte,
        velocityX: Short,
        velocityY: Short,
        velocityZ: Short,
        metadata: Metadata
      )

      case class SpawnMob_u8(
        entityId: VarInt,
        uuid: UUID,
        ty: UByte,
        x: Double,
        y: Double,
        z: Double,
        yaw: Byte,
        pitch: Byte,
        headPitch: Byte,
        velocityX: Short,
        velocityY: Short,
        velocityZ: Short,
        metadata: Metadata
      )

      case class SpawnMob_u8_i32(
        entityId: VarInt,
        uuid: UUID,
        ty: UByte,
        x: FixedPoint5[Int],
        y: FixedPoint5[Int],
        z: FixedPoint5[Int],
        yaw: Byte,
        pitch: Byte,
        headPitch: Byte,
        velocityX: Short,
        velocityY: Short,
        velocityZ: Short,
        metadata: Metadata
      )

      case class SpawnMob_u8_i32_NoUUID(
        entityId: VarInt,
        ty: UByte,
        x: FixedPoint5[Int],
        y: FixedPoint5[Int],
        z: FixedPoint5[Int],
        yaw: Byte,
        pitch: Byte,
        headPitch: Byte,
        velocityX: Short,
        velocityY: Short,
        velocityZ: Short,
        metadata: Metadata
      )

      /**
       * SpawnPainting spawns a painting into the world when it is in range of the client. The
       * title effects the size and the texture of the painting.
       */
      case class SpawnPainting_VarInt(
        entityId: VarInt,
        uuid: UUID,
        motive: VarInt,
        location: Position,
        direction: UByte
      )

      case class SpawnPainting_String(
        entityId: VarInt,
        uuid: UUID,
        title: String,
        location: Position,
        direction: UByte
      )

      case class SpawnPainting_NoUUID(
        entityId: VarInt,
        title: String,
        location: Position,
        direction: UByte
      )

      case class SpawnPainting_NoUUID_i32(
        entityId: VarInt,
        title: String,
        x: Int,
        y: Int,
        z: Int,
        direction: Int
      )

      /**
       * SpawnPlayer is used to spawn a player when they are in range of the client. This packet
       * alone isn't enough to display the player as the skin and username information is in the
       * player information packet.
       */
      case class SpawnPlayer_f64_NoMeta(
        entityId: VarInt,
        uuid: UUID,
        x: Double,
        y: Double,
        z: Double,
        yaw: Byte,
        pitch: Byte
      )

      case class SpawnPlayer_f64(
        entityId: VarInt,
        uuid: UUID,
        x: Double,
        y: Double,
        z: Double,
        yaw: Byte,
        pitch: Byte,
        metadata: Metadata
      )

      case class SpawnPlayer_i32(
        entityId: VarInt,
        uuid: UUID,
        x: FixedPoint5[Int],
        y: FixedPoint5[Int],
        z: FixedPoint5[Int],
        yaw: Byte,
        pitch: Byte,
        metadata: Metadata
      )

      case class SpawnPlayer_i32_HeldItem(
        entityId: VarInt,
        uuid: UUID,
        x: FixedPoint5[Int],
        y: FixedPoint5[Int],
        z: FixedPoint5[Int],
        yaw: Byte,
        pitch: Byte,
        currentItem: UShort,
        metadata: Metadata
      )

      case class SpawnPlayer_i32_HeldItem_String(
        entityId: VarInt,
        uuid: String,
        name: String,
        properties: LenPrefixedSeq[VarInt, SpawnProperty],
        x: FixedPoint5[Int],
        y: FixedPoint5[Int],
        z: FixedPoint5[Int],
        yaw: Byte,
        pitch: Byte,
        currentItem: UShort,
        metadata: Metadata
      )

      /**
       * SculkVibration is used for SculkSensor's animation.
       */
      case class SculkVibrationSignal(
        sourcePosition: Position,
        destination: SculkVibrationSignalDestination,
        arrivalTicks: VarInt
      )

      /**
       * Animation is sent by the server to play an animation on a specific entity.
       */
      case class Animation(entityId: VarInt, animationId: UByte)

      /**
       * Statistics is used to update the statistics screen for the client.
       */
      case class Statistics(statistices: LenPrefixedSeq[VarInt, Statistic])

      /**
       * BlockBreakAnimation is used to create and update the block breaking animation played
       * when a player starts digging a block.
       */
      case class BlockBreakAnimation(entityId: VarInt, location: Position, stage: Byte)

      case class BlockBreakAnimation_i32(entityId: VarInt, x: Int, y: Int, z: Int, stage: Byte)

      /**
       * UpdateBlockEntity updates the nbt tag of a block entity in the world.
       */
      case class UpdateBlockEntity(location: Position, action: UByte, nbt: NBTCompound)

      case class UpdateBlockEntity_Data(
        x: Int,
        y: Short,
        z: Int,
        action: UByte,
        gzippedNbt: LenPrefixedByteSeq[Short]
      )

      /**
       * BlockAction triggers different actions depending on the target block.
       */
      case class BlockAction(location: Position, byte1: UByte, byte2: UByte, blockType: VarInt)

      case class BlockAction_u16(
        x: Int,
        y: UShort,
        z: Int,
        byte1: UByte,
        byte2: UByte,
        blockType: VarInt
      )

      /**
       * BlockChange is used to update a single block on the client.
       */
      case class BlockChange_VarInt(location: Position, blockId: VarInt)

      case class BlockChange_u8(x: Int, y: UByte, z: Int, blockId: VarInt, blockMetadata: UByte)

      /**
       * BossBar displays and/or changes a boss bar that is displayed on the top of the client's
       * screen. This is normally used for bosses such as the ender dragon or the wither.
       */
      case class BossBar(
        uuid: UUID,
        action: VarInt,
        title: Option[ChatComponent],
        health: Option[Float],
        color: Option[VarInt],
        style: Option[VarInt],
        flags: Option[UByte]
      ) {
        require(title.nonEmpty == (action == VarInt(0) || action == VarInt(3)))
        require(health.nonEmpty == (action == VarInt(0) || action == VarInt(2)))
        require(color.nonEmpty == (action == VarInt(0) || action == VarInt(4)))
        require(style.nonEmpty == (action == VarInt(0) || action == VarInt(4)))
        require(flags.nonEmpty == (action == VarInt(0) || action == VarInt(5)))
      }

      /**
       * ServerDifficulty changes the displayed difficulty in the client's menu as well as some
       * ui changes for hardcore.
       */
      case class ServerDifficulty(difficulty: UByte)

      case class ServerDifficulty_Locked(difficulty: UByte, locked: Boolean)

      /**
       * TabCompleteReply is sent as a reply to a tab completion request. The matches should be
       * possible completions for the command/chat the player sent.
       */
      case class TabCompleteReply(matches: LenPrefixedSeq[VarInt, String])

      case class DeclareCommands(nodes: LenPrefixedSeq[VarInt, CommandNode], rootIndex: VarInt)

      /**
       * ServerMessage is a message sent by the server. It could be from a player or just a
       * system message. The Type field controls the location the message is displayed at and
       * when the message is displayed.
       */
      case class ServerMessage_Sender(
        message: ChatComponent,
        /**
         * 0 - Chat message, 1 - System message, 2 - Action bar message
         */
        position: UByte,
        sender: UUID
      )

      case class ServerMessage_Position(
        message: ChatComponent,
        /**
         * 0 - Chat message, 1 - System message, 2 - Action bar message
         */
        position: UByte
      )

      case class ServerMessage_NoPosition(message: ChatComponent)

      /**
       * MultiBlockChange is used to update a batch of blocks in a single packet.
       */
      case class MultiBlockChange_Packed(
        chunkSectionPos: Long,
        noTrustEdges: Boolean,
        records: LenPrefixedSeq[VarInt, VarLong]
      )

      case class MultiBlockChange_VarInt(
        chunkX: Int,
        chunkZ: Int,
        records: LenPrefixedSeq[VarInt, BlockChangeRecord]
      )

      case class MultiBlockChange_u16(
        chunkX: Int,
        chunkZ: Int,
        recordCount: UShort,
        dataSize: Int,
        data: UnspecifiedLengthByteArray
      )

      /**
       * ConfirmTransaction notifies the client whether a transaction was successful or failed
       * (e.g. due to lag).
       */
      case class ConfirmTransaction(id: UByte, actionNumber: Short, accepted: Boolean)

      /**
       * WindowClose forces the client to close the window with the given id, e.g. a chest
       * getting destroyed.
       */
      case class WindowClose(id: UByte)

      /**
       * WindowOpen tells the client to open the inventory window of the given type. The ID is
       * used to reference the instance of the window in other packets.
       */
      case class WindowOpen(
        id: UByte,
        ty: String,
        title: ChatComponent,
        slotCount: UByte,
        entityId: Option[Int]
      ) {
        require(entityId.nonEmpty == (ty == "EntityHorse"))
      }

      case class WindowOpenHorse(windowId: UByte, numberOfSlots: VarInt, entityId: Int)

      case class WindowOpen_u8(
        id: UByte,
        ty: UByte,
        title: ChatComponent,
        slotCount: UByte,
        useProvidedWindowTitle: Boolean,
        entityId: Option[Int]
      ) {
        require(entityId.nonEmpty == (ty == UByte(11)))
      }

      case class WindowOpen_VarInt(id: VarInt, ty: VarInt, title: ChatComponent)

      /**
       * WindowItems sets every item in a window.
       */
      case class WindowItems[S <: Slot](id: UByte, items: LenPrefixedSeq[Short, S])

      case class WindowItems_withState[S <: Slot](
        windowId: UByte,
        stateId: VarInt,
        items: LenPrefixedSeq[VarInt, S],
        cursorItem: S
      )

      /**
       * WindowProperty changes the value of a property of a window. Properties vary depending
       * on the window type.
       */
      case class WindowProperty(id: UByte, property: Short, value: Short)

      /**
       * WindowSetSlot changes an itemstack in one of the slots in a window.
       */
      case class WindowSetSlot[S <: Slot](id: UByte, property: Short, item: S)

      /**
       * SetSlot is alternative to WindowSetSlot(since 1.17). StateId is used to ClickWindow.
       */
      case class SetSlot[S <: Slot](windowId: UByte, stateId: VarInt, slot: Short, item: S)

      /**
       * SetCooldown disables a set item (by id) for the set number of ticks
       */
      case class SetCooldown(itemId: VarInt, ticks: VarInt)

      /**
       * PluginMessageClientbound is used for custom messages between the client and server.
       * This is mainly for plugins/mods but vanilla has a few channels registered too.
       */
      case class PluginMessageClientbound(channel: String, data: UnspecifiedLengthByteArray)

      case class PluginMessageClientbound_i16(channel: String, data: LenPrefixedByteSeq[Short])

      /**
       * Plays a sound by name on the client
       */
      case class NamedSoundEffect(
        name: String,
        category: VarInt,
        x: Int,
        y: Int,
        z: Int,
        volume: Float,
        pitch: Float
      )

      case class NamedSoundEffect_u8(
        name: String,
        category: VarInt,
        x: Int,
        y: Int,
        z: Int,
        volume: Float,
        pitch: UByte
      )

      case class NamedSoundEffect_u8_NoCategory(
        name: String,
        x: Int,
        y: Int,
        z: Int,
        volume: Float,
        pitch: UByte
      )

      /**
       * Disconnect causes the client to disconnect displaying the passed reason.
       */
      case class Disconnect(reason: ChatComponent)

      /**
       * EntityAction causes an entity to preform an action based on the passed id.
       */
      case class EntityAction(entityId: Int, actionId: UByte)

      /**
       * Explosion is sent when an explosion is triggered (tnt, creeper etc). This plays the
       * effect and removes the effected blocks.
       */
      case class Explosion(
        x: Float,
        y: Float,
        z: Float,
        radius: Float,
        records: LenPrefixedSeq[Int, ExplosionRecord],
        velocityX: Float,
        velocityY: Float,
        velocityZ: Float
      )

      /**
       * ChunkUnload tells the client to unload the chunk at the specified position.
       */
      case class ChunkUnload(x: Int, z: Int)

      /**
       * SetCompression updates the compression threshold.
       */
      case class SetCompression(threshold: VarInt)

      /**
       * ChangeGameState is used to modify the game's state like gamemode or weather.
       */
      case class ChangeGameState(reason: UByte, value: Float)

      /**
       * KeepAliveClientbound is sent by a server to check if the client is still responding and
       * keep the connection open. The client should reply with the KeepAliveServerbound packet
       * setting ID to the same as this one.
       */
      case class KeepAliveClientbound_i64(id: Long)

      case class KeepAliveClientbound_VarInt(id: VarInt)

      case class KeepAliveClientbound_i32(id: Int)

      /**
       * ChunkData sends or updates a single chunk on the client. If New is set then biome data
       * should be sent too.
       */
      case class ChunkData_Biomes3D_VarInt(
        chunkX: Int,
        chunkZ: Int,
        isNew: Boolean,
        bitmask: VarInt,
        heightmaps: NBTCompound,
        biomes: Option[LenPrefixedSeq[VarInt, VarInt]],
        data: LenPrefixedByteSeq[VarInt],
        blockEntities: LenPrefixedSeq[VarInt, NBTCompound]
      ) {
        require(biomes.nonEmpty == (isNew))
      }

      case class ChunkData_Biomes3D_bool(
        chunkX: Int,
        chunkZ: Int,
        isNew: Boolean,
        ignoreOldData: Boolean,
        bitmask: VarInt,
        heightmaps: NBTCompound,
        biomes: Option[Biomes3D],
        data: LenPrefixedByteSeq[VarInt],
        blockEntities: LenPrefixedSeq[VarInt, NBTCompound]
      ) {
        require(biomes.nonEmpty == (isNew))
      }

      case class ChunkData_Biomes3D(
        chunkX: Int,
        chunkZ: Int,
        isNew: Boolean,
        bitmask: VarInt,
        heightmaps: NBTCompound,
        biomes: Option[Biomes3D],
        data: LenPrefixedByteSeq[VarInt],
        blockEntities: LenPrefixedSeq[VarInt, NBTCompound]
      ) {
        require(biomes.nonEmpty == (isNew))
      }

      case class ChunkData_HeightMap(
        chunkX: Int,
        chunkZ: Int,
        isNew: Boolean,
        bitmask: VarInt,
        heightmaps: NBTCompound,
        data: LenPrefixedByteSeq[VarInt],
        blockEntities: LenPrefixedSeq[VarInt, NBTCompound]
      )

      case class ChunkData(
        chunkX: Int,
        chunkZ: Int,
        isNew: Boolean,
        bitmask: VarInt,
        data: LenPrefixedByteSeq[VarInt],
        blockEntities: LenPrefixedSeq[VarInt, NBTCompound]
      )

      case class ChunkData_NoEntities(
        chunkX: Int,
        chunkZ: Int,
        isNew: Boolean,
        bitmask: VarInt,
        data: LenPrefixedByteSeq[VarInt]
      )

      case class ChunkData_NoEntities_u16(
        chunkX: Int,
        chunkZ: Int,
        isNew: Boolean,
        bitmask: UShort,
        data: LenPrefixedByteSeq[VarInt]
      )

      case class ChunkData_17(
        chunkX: Int,
        chunkZ: Int,
        isNew: Boolean,
        bitmask: UShort,
        addBitmask: UShort,
        compressedData: LenPrefixedByteSeq[Int]
      )

      case class ChunkData_withBlockEntity(
        chunkX: Int,
        chunkZ: Int,
        bitMask: LenPrefixedSeq[VarInt, Long],
        heightMaps: NBTCompound,
        biomes: LenPrefixedSeq[VarInt, VarInt],
        data: LenPrefixedByteSeq[VarInt],
        blockEntities: LenPrefixedSeq[VarInt, NBTCompound]
      )

      case class ChunkDataBulk(
        skylight: Boolean,
        chunkMeta: LenPrefixedSeq[VarInt, ChunkMeta],
        chunkData: UnspecifiedLengthByteArray
      )

      case class ChunkDataBulk_17(
        chunkColumnCount: UShort,
        dataLength: Int,
        skylight: Boolean,
        chunkDataAndMeta: UnspecifiedLengthByteArray
      )

      /**
       * Effect plays a sound effect or particle at the target location with the volume (of
       * sounds) being relative to the player's position unless DisableRelative is set to true.
       */
      case class Effect(effectId: Int, location: Position, data: Int, disableRelative: Boolean)

      case class Effect_u8y(
        effectId: Int,
        x: Int,
        y: UByte,
        z: Int,
        data: Int,
        disableRelative: Boolean
      )

      /**
       * Particle spawns particles at the target location with the various modifiers.
       */
      case class Particle_f64(
        particleId: Int,
        longDistance: Boolean,
        x: Double,
        y: Double,
        z: Double,
        offsetX: Float,
        offsetY: Float,
        offsetZ: Float,
        speed: Float,
        count: Int,
        blockState: Option[VarInt],
        red: Option[Float],
        green: Option[Float],
        blue: Option[Float],
        scale: Option[Float],
        item: Option[NBTCompound]
      ) {
        require(blockState.nonEmpty == (particleId == 3 || particleId == 23))
        require(red.nonEmpty == (particleId == 14))
        require(green.nonEmpty == (particleId == 14))
        require(blue.nonEmpty == (particleId == 14))
        require(scale.nonEmpty == (particleId == 14))
        require(item.nonEmpty == (particleId == 32))
      }

      case class Particle_Data(
        particleId: Int,
        longDistance: Boolean,
        x: Float,
        y: Float,
        z: Float,
        offsetX: Float,
        offsetY: Float,
        offsetZ: Float,
        speed: Float,
        count: Int,
        blockState: Option[VarInt],
        red: Option[Float],
        green: Option[Float],
        blue: Option[Float],
        scale: Option[Float],
        item: Option[NBTCompound]
      ) {
        require(blockState.nonEmpty == (particleId == 3 || particleId == 23))
        require(red.nonEmpty == (particleId == 14))
        require(green.nonEmpty == (particleId == 14))
        require(blue.nonEmpty == (particleId == 14))
        require(scale.nonEmpty == (particleId == 14))
        require(item.nonEmpty == (particleId == 32))
      }

      case class Particle_Data13(
        particleId: Int,
        longDistance: Boolean,
        x: Float,
        y: Float,
        z: Float,
        offsetX: Float,
        offsetY: Float,
        offsetZ: Float,
        speed: Float,
        count: Int,
        blockState: Option[VarInt],
        red: Option[Float],
        green: Option[Float],
        blue: Option[Float],
        scale: Option[Float],
        item: Option[NBTCompound]
      ) {
        require(blockState.nonEmpty == (particleId == 3 || particleId == 20))
        require(red.nonEmpty == (particleId == 11))
        require(green.nonEmpty == (particleId == 11))
        require(blue.nonEmpty == (particleId == 11))
        require(scale.nonEmpty == (particleId == 11))
        require(item.nonEmpty == (particleId == 27))
      }

      case class Particle_VarIntArray(
        particleId: Int,
        longDistance: Boolean,
        x: Float,
        y: Float,
        z: Float,
        offsetX: Float,
        offsetY: Float,
        offsetZ: Float,
        speed: Float,
        count: Int,
        data1: Option[VarInt],
        data2: Option[VarInt]
      ) {
        require(
          data1.nonEmpty == (particleId == 36 || particleId == 37 || particleId == 38 || particleId == 46)
        )
        require(data2.nonEmpty == (particleId == 36))
      }

      case class Particle_Named(
        particleId: String,
        x: Float,
        y: Float,
        z: Float,
        offsetX: Float,
        offsetY: Float,
        offsetZ: Float,
        speed: Float,
        count: Int
      )

      /**
       * JoinGame is sent after completing the login process. This sets the initial state for
       * the client.
       */
      case class JoinGame_WorldNames_IsHard(
        /**
         * The entity id the client will be referenced by
         */
        entityId: Int,
        /**
         * Whether hardcore mode is enabled
         */
        isHardcore: Boolean,
        /**
         * The starting gamemode of the client
         */
        gamemode: UByte,
        /**
         * The previous gamemode of the client
         */
        previousGamemode: UByte,
        /**
         * Identifiers for all worlds on the server
         */
        worldNames: LenPrefixedSeq[VarInt, String],
        /**
         * Represents a dimension registry
         */
        dimensionCodec: NBTCompound,
        /**
         * The dimension the client is starting in
         */
        dimension: NBTCompound,
        /**
         * The world being spawned into
         */
        worldName: String,
        /**
         * Truncated SHA-256 hash of world's seed
         */
        hashedSeed: Long,
        /**
         * The max number of players on the server
         */
        maxPlayers: VarInt,
        /**
         * The render distance (2-32)
         */
        viewDistance: VarInt,
        /**
         * Whether the client should reduce the amount of debug information it displays in F3
         * mode
         */
        reducedDebugInfo: Boolean,
        /**
         * Whether to prompt or immediately respawn
         */
        enableRespawnScreen: Boolean,
        /**
         * Whether the world is in debug mode
         */
        isDebug: Boolean,
        /**
         * Whether the world is a superflat world
         */
        isFlat: Boolean
      )

      case class JoinGame_WorldNames(
        /**
         * The entity id the client will be referenced by
         */
        entityId: Int,
        /**
         * The starting gamemode of the client
         */
        gamemode: UByte,
        /**
         * The previous gamemode of the client
         */
        previousGamemode: UByte,
        /**
         * Identifiers for all worlds on the server
         */
        worldNames: LenPrefixedSeq[VarInt, String],
        /**
         * Represents a dimension registry
         */
        dimensionCodec: NBTCompound,
        /**
         * The dimension the client is starting in
         */
        dimension: String,
        /**
         * The world being spawned into
         */
        worldName: String,
        /**
         * Truncated SHA-256 hash of world's seed
         */
        hashedSeed: Long,
        /**
         * The max number of players on the server
         */
        maxPlayers: UByte,
        /**
         * The render distance (2-32)
         */
        viewDistance: VarInt,
        /**
         * Whether the client should reduce the amount of debug information it displays in F3
         * mode
         */
        reducedDebugInfo: Boolean,
        /**
         * Whether to prompt or immediately respawn
         */
        enableRespawnScreen: Boolean,
        /**
         * Whether the world is in debug mode
         */
        isDebug: Boolean,
        /**
         * Whether the world is a superflat world
         */
        isFlat: Boolean
      )

      case class JoinGame_HashedSeed_Respawn(
        /**
         * The entity id the client will be referenced by
         */
        entityId: Int,
        /**
         * The starting gamemode of the client
         */
        gamemode: UByte,
        /**
         * The dimension the client is starting in
         */
        dimension: Int,
        /**
         * Truncated SHA-256 hash of world's seed
         */
        hashedSeed: Long,
        /**
         * The max number of players on the server
         */
        maxPlayers: UByte,
        /**
         * The level type of the server
         */
        levelType: String,
        /**
         * The render distance (2-32)
         */
        viewDistance: VarInt,
        /**
         * Whether the client should reduce the amount of debug information it displays in F3
         * mode
         */
        reducedDebugInfo: Boolean,
        /**
         * Whether to prompt or immediately respawn
         */
        enableRespawnScreen: Boolean
      )

      case class JoinGame_i32_ViewDistance(
        /**
         * The entity id the client will be referenced by
         */
        entityId: Int,
        /**
         * The starting gamemode of the client
         */
        gamemode: UByte,
        /**
         * The dimension the client is starting in
         */
        dimension: Int,
        /**
         * The max number of players on the server
         */
        maxPlayers: UByte,
        /**
         * The level type of the server
         */
        levelType: String,
        /**
         * The render distance (2-32)
         */
        viewDistance: VarInt,
        /**
         * Whether the client should reduce the amount of debug information it displays in F3
         * mode
         */
        reducedDebugInfo: Boolean
      )

      case class JoinGame_i32(
        /**
         * The entity id the client will be referenced by
         */
        entityId: Int,
        /**
         * The starting gamemode of the client
         */
        gamemode: UByte,
        /**
         * The dimension the client is starting in
         */
        dimension: Int,
        /**
         * The difficuilty setting for the server
         */
        difficulty: UByte,
        /**
         * The max number of players on the server
         */
        maxPlayers: UByte,
        /**
         * The level type of the server
         */
        levelType: String,
        /**
         * Whether the client should reduce the amount of debug information it displays in F3
         * mode
         */
        reducedDebugInfo: Boolean
      )

      case class JoinGame_i8(
        /**
         * The entity id the client will be referenced by
         */
        entityId: Int,
        /**
         * The starting gamemode of the client
         */
        gamemode: UByte,
        /**
         * The dimension the client is starting in
         */
        dimension: Byte,
        /**
         * The difficuilty setting for the server
         */
        difficulty: UByte,
        /**
         * The max number of players on the server
         */
        maxPlayers: UByte,
        /**
         * The level type of the server
         */
        levelType: String,
        /**
         * Whether the client should reduce the amount of debug information it displays in F3
         * mode
         */
        reducedDebugInfo: Boolean
      )

      case class JoinGame_i8_NoDebug(
        entityId: Int,
        gamemode: UByte,
        dimension: Byte,
        difficulty: UByte,
        maxPlayers: UByte,
        levelType: String
      )

      /**
       * Maps updates a single map's contents
       */
      case class Maps(
        itemDamage: VarInt,
        scale: Byte,
        trackingPosition: Boolean,
        locked: Boolean,
        icons: LenPrefixedSeq[VarInt, MapIcon],
        columns: UByte,
        rows: Option[UByte],
        x: Option[UByte],
        z: Option[UByte],
        data: Option[LenPrefixedByteSeq[VarInt]]
      ) {
        require(rows.nonEmpty == (columns != UByte(0)))
        require(x.nonEmpty == (columns != UByte(0)))
        require(z.nonEmpty == (columns != UByte(0)))
        require(data.nonEmpty == (columns != UByte(0)))
      }

      case class Maps_NoLocked(
        itemDamage: VarInt,
        scale: Byte,
        trackingPosition: Boolean,
        icons: LenPrefixedSeq[VarInt, MapIcon],
        columns: UByte,
        rows: Option[UByte],
        x: Option[UByte],
        z: Option[UByte],
        data: Option[LenPrefixedByteSeq[VarInt]]
      ) {
        require(rows.nonEmpty == (columns != UByte(0)))
        require(x.nonEmpty == (columns != UByte(0)))
        require(z.nonEmpty == (columns != UByte(0)))
        require(data.nonEmpty == (columns != UByte(0)))
      }

      case class Maps_NoTracking(
        itemDamage: VarInt,
        scale: Byte,
        icons: LenPrefixedSeq[VarInt, MapIcon],
        columns: UByte,
        rows: Option[UByte],
        x: Option[UByte],
        z: Option[UByte],
        data: Option[LenPrefixedByteSeq[VarInt]]
      ) {
        require(rows.nonEmpty == (columns != UByte(0)))
        require(x.nonEmpty == (columns != UByte(0)))
        require(z.nonEmpty == (columns != UByte(0)))
        require(data.nonEmpty == (columns != UByte(0)))
      }

      case class Maps_NoTracking_Data(itemDamage: VarInt, data: LenPrefixedByteSeq[Short])

      /**
       * EntityMove moves the entity with the id by the offsets provided.
       */
      case class EntityMove_i16(
        entityId: VarInt,
        deltaX: FixedPoint12[Short],
        deltaY: FixedPoint12[Short],
        deltaZ: FixedPoint12[Short],
        onGround: Boolean
      )

      case class EntityMove_i8(
        entityId: VarInt,
        deltaX: FixedPoint5[Byte],
        deltaY: FixedPoint5[Byte],
        deltaZ: FixedPoint5[Byte],
        onGround: Boolean
      )

      case class EntityMove_i8_i32_NoGround(
        entityId: Int,
        deltaX: FixedPoint5[Byte],
        deltaY: FixedPoint5[Byte],
        deltaZ: FixedPoint5[Byte]
      )

      /**
       * EntityLookAndMove is a combination of EntityMove and EntityLook.
       */
      case class EntityLookAndMove_i16(
        entityId: VarInt,
        deltaX: FixedPoint12[Short],
        deltaY: FixedPoint12[Short],
        deltaZ: FixedPoint12[Short],
        yaw: Byte,
        pitch: Byte,
        onGround: Boolean
      )

      case class EntityLookAndMove_i8(
        entityId: VarInt,
        deltaX: FixedPoint5[Byte],
        deltaY: FixedPoint5[Byte],
        deltaZ: FixedPoint5[Byte],
        yaw: Byte,
        pitch: Byte,
        onGround: Boolean
      )

      case class EntityLookAndMove_i8_i32_NoGround(
        entityId: Int,
        deltaX: FixedPoint5[Byte],
        deltaY: FixedPoint5[Byte],
        deltaZ: FixedPoint5[Byte],
        yaw: Byte,
        pitch: Byte
      )

      /**
       * EntityLook rotates the entity to the new angles provided.
       */
      case class EntityLook_VarInt(entityId: VarInt, yaw: Byte, pitch: Byte, onGround: Boolean)

      case class EntityLook_i32_NoGround(entityId: Int, yaw: Byte, pitch: Byte)

      /**
       * Entity does nothing. It is a result of subclassing used in Minecraft.
       */
      case class Entity(entityId: VarInt)

      case class Entity_i32(entityId: Int)

      /**
       * EntityUpdateNBT updates the entity named binary tag.
       */
      case class EntityUpdateNBT(entityId: VarInt, nbt: NBTCompoundOrEnd)

      /**
       * Teleports the player's vehicle
       */
      case class VehicleTeleport(x: Double, y: Double, z: Double, yaw: Float, pitch: Float)

      /**
       * Opens the book GUI.
       */
      case class OpenBook(hand: VarInt)

      /**
       * SignEditorOpen causes the client to open the editor for a sign so that it can write to
       * it. Only sent in vanilla when the player places a sign.
       */
      case class SignEditorOpen(location: Position)

      case class SignEditorOpen_i32(x: Int, y: Int, z: Int)

      /**
       * When Ping received, client needs to responds with a Pong packet with the same id.
       */
      case class Ping(id: Int)

      /**
       * CraftRecipeResponse is a response to CraftRecipeRequest, notifies the UI.
       */
      case class CraftRecipeResponse(windowId: UByte, recipe: VarInt)

      /**
       * PlayerAbilities is used to modify the players current abilities. Flying, creative, god
       * mode etc.
       */
      case class PlayerAbilities(flags: UByte, flyingSpeed: Float, walkingSpeed: Float)

      /**
       * CombatEvent is used for... you know, I never checked. I have no clue.
       */
      case class CombatEvent(
        event: VarInt,
        direction: Option[VarInt],
        playerId: Option[VarInt],
        entityId: Option[Int],
        message: Option[ChatComponent]
      ) {
        require(direction.nonEmpty == (event == VarInt(1)))
        require(playerId.nonEmpty == (event == VarInt(2)))
        require(entityId.nonEmpty == (event == VarInt(1) || event == VarInt(2)))
        require(message.nonEmpty == (event == VarInt(2)))
      }

      /**
       * EndCombatEvent is used to notify player ends combat. Duration is tick time since last
       * entity attack.
       */
      case class EndCombatEvent(duration: VarInt, entityId: Int)

      /**
       * EnterCombatEvent is used to notify player is in combat.
       */
      case class EnterCombatEvent()

      /**
       * DeathCombatEvent is used to send a respawn screen.
       */
      case class DeathCombatEvent(playerId: VarInt, entityId: Int, message: ChatComponent)

      /**
       * PlayerInfo is sent by the server for every player connected to the server to provide
       * skin and username information as well as ping and gamemode info.
       */
      case class PlayerInfo(inner: PlayerInfoData)

      case class PlayerInfo_String(name: String, online: Boolean, ping: UShort)

      case class FacePlayer(
        feetEyes: VarInt,
        targetX: Double,
        targetY: Double,
        targetZ: Double,
        isEntity: Boolean,
        entityId: Option[VarInt],
        entityFeetEyes: Option[VarInt]
      ) {
        require(entityId.nonEmpty == (isEntity))
        require(entityFeetEyes.nonEmpty == (isEntity))
      }

      /**
       * TeleportPlayer is sent to change the player's position. The client is expected to reply
       * to the server with the same positions as contained in this packet otherwise will reject
       * future packets.
       */
      case class TeleportPlayer_WithConfirm(
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        flags: UByte,
        teleportId: VarInt
      )

      case class TeleportPlayer_WithDismount(
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        flags: UByte,
        teleportId: VarInt,
        dismount: Boolean
      )

      case class TeleportPlayer_NoConfirm(
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        flags: UByte
      )

      case class TeleportPlayer_OnGround(
        x: Double,
        eyesY: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
      )

      /**
       * EntityUsedBed is sent by the server when a player goes to bed.
       */
      case class EntityUsedBed(entityId: VarInt, location: Position)

      case class EntityUsedBed_i32(entityId: Int, x: Int, y: UByte, z: Int)

      case class UnlockRecipes_NoSmelting(
        action: VarInt,
        craftingBookOpen: Boolean,
        filteringCraftable: Boolean,
        recipeIds: LenPrefixedSeq[VarInt, VarInt],
        recipeIds2: Option[LenPrefixedSeq[VarInt, VarInt]]
      ) {
        require(recipeIds2.nonEmpty == (action == VarInt(0)))
      }

      case class UnlockRecipes_WithSmelting(
        action: VarInt,
        craftingBookOpen: Boolean,
        filteringCraftable: Boolean,
        smeltingBookOpen: Boolean,
        filteringSmeltable: Boolean,
        recipeIds: LenPrefixedSeq[VarInt, String],
        recipeIds2: Option[LenPrefixedSeq[VarInt, String]]
      ) {
        require(recipeIds2.nonEmpty == (action == VarInt(0)))
      }

      case class UnlockRecipes_WithBlastSmoker(
        action: VarInt,
        craftingBookOpen: Boolean,
        filteringCraftable: Boolean,
        smeltingBookOpen: Boolean,
        filteringSmeltable: Boolean,
        blastFurnaceOpen: Boolean,
        filteringBlastFurnace: Boolean,
        smokerOpen: Boolean,
        filteringSmoker: Boolean,
        recipeIds: LenPrefixedSeq[VarInt, String],
        recipeIds2: Option[LenPrefixedSeq[VarInt, String]]
      ) {
        require(recipeIds2.nonEmpty == (action == VarInt(0)))
      }

      /**
       * EntityDestroy destroys the entities with the ids in the provided slice.
       */
      case class EntityDestroy(entityIds: LenPrefixedSeq[VarInt, VarInt])

      case class EntityDestroy_u8(entityIds: LenPrefixedSeq[UByte, Int])

      /**
       * EntityRemoveEffect removes an effect from an entity.
       */
      case class EntityRemoveEffect(entityId: VarInt, effectId: Byte)

      case class EntityRemoveEffect_i32(entityId: Int, effectId: Byte)

      /**
       * ResourcePackSend causes the client to check its cache for the requested resource packet
       * and download it if its missing. Once the resource pack is obtained the client will use
       * it.
       */
      case class ResourcePackSend(url: String, hash: String)

      /**
       * Respawn is sent to respawn the player after death or when they move worlds.
       */
      case class Respawn_Gamemode(
        dimension: Int,
        difficulty: UByte,
        gamemode: UByte,
        levelType: String
      )

      case class Respawn_HashedSeed(
        dimension: Int,
        hashedSeed: Long,
        difficulty: UByte,
        gamemode: UByte,
        levelType: String
      )

      case class Respawn_NBT(
        dimension: NBTCompound,
        worldName: String,
        hashedSeed: Long,
        gamemode: UByte,
        previousGamemode: UByte,
        isDebug: Boolean,
        isFlat: Boolean,
        copyMetadata: Boolean
      )

      case class Respawn_WorldName(
        dimension: String,
        worldName: String,
        hashedSeed: Long,
        gamemode: UByte,
        previousGamemode: UByte,
        isDebug: Boolean,
        isFlat: Boolean,
        copyMetadata: Boolean
      )

      /**
       * EntityHeadLook rotates an entity's head to the new angle.
       */
      case class EntityHeadLook(entityId: VarInt, headYaw: Byte)

      case class EntityHeadLook_i32(entityId: Int, headYaw: Byte)

      case class EntityStatus(entityId: Int, entityStatus: Byte)

      case class NBTQueryResponse(transactionId: VarInt, nbt: NBTCompoundOrEnd)

      /**
       * SelectAdvancementTab indicates the client should switch the advancement tab.
       */
      case class SelectAdvancementTab(hasId: Boolean, tabId: Option[String]) {
        require(tabId.nonEmpty == (hasId))
      }

      /**
       * ActionBar displays a message above the hotbar
       */
      case class ActionBar(chat: ChatComponent)

      /**
       * WorldBorder configures the world's border.
       */
      case class WorldBorder(
        action: VarInt,
        oldRadius: Option[Double],
        newRadius: Option[Double],
        speed: Option[VarLong],
        x: Option[Double],
        z: Option[Double],
        portalBoundary: Option[VarInt],
        warningTime: Option[VarInt],
        warningBlocks: Option[VarInt]
      ) {
        require(oldRadius.nonEmpty == (action == VarInt(3) || action == VarInt(1)))
        require(
          newRadius.nonEmpty == (action == VarInt(3) || action == VarInt(1) || action == VarInt(
            0
          ))
        )
        require(speed.nonEmpty == (action == VarInt(3) || action == VarInt(1)))
        require(x.nonEmpty == (action == VarInt(3) || action == VarInt(2)))
        require(z.nonEmpty == (action == VarInt(3) || action == VarInt(2)))
        require(portalBoundary.nonEmpty == (action == VarInt(3)))
        require(warningTime.nonEmpty == (action == VarInt(3) || action == VarInt(4)))
        require(warningBlocks.nonEmpty == (action == VarInt(3) || action == VarInt(5)))
      }

      /**
       * WorldBorderInitialize is used to create world's border
       */
      case class WorldBorderInitialize(
        x: Double,
        z: Double,
        oldDiameter: Double,
        newDiameter: Double,
        speed: VarLong,
        portalTeleportBoundary: VarInt,
        warningBlocks: VarInt,
        warningTime: VarInt
      )

      /**
       * WorldBorderCenter changes world's border center location.
       */
      case class WorldBorderCenter(x: Double, z: Double)

      /**
       * WorldBorderLerpSize changes world's border size.(Border will move smoothly.) Speed is
       * number of real-time milliseconds.
       */
      case class WorldBorderLerpSize(oldDiameter: Double, newDiameter: Double, speed: VarLong)

      /**
       * WorldBorderSize changes world's border size.
       */
      case class WorldBorderSize(diameter: Double)

      /**
       * WorldBorderWarningDelay changes world's border warning time.(seconds)
       */
      case class WorldBorderWarningDelay(warningTime: VarInt)

      /**
       * WorldBorderWarningReach changes world's border warning length from world's border.
       */
      case class WorldBorderWarningReach(warningBlocks: VarInt)

      /**
       * Camera causes the client to spectate the entity with the passed id. Use the player's id
       * to de-spectate.
       */
      case class Camera(targetId: VarInt)

      /**
       * SetCurrentHotbarSlot changes the player's currently selected hotbar item.
       */
      case class SetCurrentHotbarSlot(slot: UByte)

      /**
       * UpdateViewPosition is used to determine what chunks should be remain loaded.
       */
      case class UpdateViewPosition(chunkX: VarInt, chunkZ: VarInt)

      /**
       * UpdateViewDistance is sent by the integrated server when changing render distance.
       */
      case class UpdateViewDistance(viewDistance: VarInt)

      /**
       * ScoreboardDisplay is used to set the display position of a scoreboard.
       */
      case class ScoreboardDisplay(position: UByte, name: String)

      /**
       * EntityMetadata updates the metadata for an entity.
       */
      case class EntityMetadata(entityId: VarInt, metadata: Metadata)

      case class EntityMetadata_i32(entityId: Int, metadata: Metadata)

      /**
       * EntityAttach attaches to entities together, either by mounting or leashing.
       * -1 can be used at the EntityID to deattach.
       */
      case class EntityAttach(entityId: Int, vehicle: Int)

      case class EntityAttach_leashed(entityId: Int, vehicle: Int, leash: Boolean)

      /**
       * EntityVelocity sets the velocity of an entity in 1/8000 of a block per a tick.
       */
      case class EntityVelocity(
        entityId: VarInt,
        velocityX: Short,
        velocityY: Short,
        velocityZ: Short
      )

      case class EntityVelocity_i32(
        entityId: Int,
        velocityX: Short,
        velocityY: Short,
        velocityZ: Short
      )

      /**
       * EntityEquipment is sent to display an item on an entity, like a sword or armor. Slot 0
       * is the held item and slots 1 to 4 are boots, leggings chestplate and helmet
       * respectively.
       */
      case class EntityEquipment_Array(entityId: VarInt, equipments: EntityEquipments)

      case class EntityEquipment_VarInt[S <: Slot](entityId: VarInt, slot: VarInt, item: S)

      case class EntityEquipment_u16(entityId: VarInt, slot: UShort, item: Slot.Upto_1_12_2)

      case class EntityEquipment_u16_i32(entityId: Int, slot: UShort, item: Slot.Upto_1_12_2)

      /**
       * SetExperience updates the experience bar on the client.
       */
      case class SetExperience(experienceBar: Float, level: VarInt, totalExperience: VarInt)

      case class SetExperience_i16(experienceBar: Float, level: Short, totalExperience: Short)

      /**
       * UpdateHealth is sent by the server to update the player's health and food.
       */
      case class UpdateHealth(health: Float, food: VarInt, foodSaturation: Float)

      case class UpdateHealth_u16(health: Float, food: UShort, foodSaturation: Float)

      /**
       * ScoreboardObjective creates/updates a scoreboard objective.
       */
      case class ScoreboardObjective(
        name: String,
        mode: UByte,
        value: Option[String],
        ty: Option[String]
      ) {
        require(value.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(ty.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
      }

      case class ScoreboardObjective_NoMode(name: String, value: String, ty: UByte)

      /**
       * SetPassengers mounts entities to an entity
       */
      case class SetPassengers(entityId: VarInt, passengers: LenPrefixedSeq[VarInt, VarInt])

      /**
       * Teams creates and updates teams
       */
      case class Teams_VarInt(
        name: String,
        mode: UByte,
        displayName: Option[String],
        flags: Option[UByte],
        nameTagVisibility: Option[String],
        collisionRule: Option[String],
        formatting: Option[VarInt],
        prefix: Option[String],
        suffix: Option[String],
        players: Option[LenPrefixedSeq[VarInt, String]]
      ) {
        require(displayName.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(flags.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(nameTagVisibility.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(collisionRule.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(formatting.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(prefix.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(suffix.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(players.nonEmpty == (mode == UByte(0) || mode == UByte(3) || mode == UByte(4)))
      }

      case class Teams_u8(
        name: String,
        mode: UByte,
        displayName: Option[String],
        prefix: Option[String],
        suffix: Option[String],
        flags: Option[UByte],
        nameTagVisibility: Option[String],
        collisionRule: Option[String],
        color: Option[Byte],
        players: Option[LenPrefixedSeq[VarInt, String]]
      ) {
        require(displayName.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(prefix.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(suffix.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(flags.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(nameTagVisibility.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(collisionRule.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(color.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(players.nonEmpty == (mode == UByte(0) || mode == UByte(3) || mode == UByte(4)))
      }

      case class Teams_NoVisColor(
        name: String,
        mode: UByte,
        displayName: Option[String],
        prefix: Option[String],
        suffix: Option[String],
        flags: Option[UByte],
        players: Option[LenPrefixedSeq[VarInt, String]]
      ) {
        require(displayName.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(prefix.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(suffix.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(flags.nonEmpty == (mode == UByte(0) || mode == UByte(2)))
        require(players.nonEmpty == (mode == UByte(0) || mode == UByte(3) || mode == UByte(4)))
      }

      /**
       * UpdateScore is used to update or remove an item from a scoreboard objective.
       */
      case class UpdateScore(
        name: String,
        action: UByte,
        objectName: String,
        value: Option[VarInt]
      ) {
        require(value.nonEmpty == (action != UByte(1)))
      }

      case class UpdateScore_i32(
        name: String,
        action: UByte,
        objectName: String,
        value: Option[Int]
      ) {
        require(value.nonEmpty == (action != UByte(1)))
      }

      /**
       * SpawnPosition is sent to change the player's current spawn point. Currently only used
       * by the client for the compass.
       *
       * This packet intent is used for 1.8.9 ~ 1.16.4
       */
      case class SpawnPosition(location: Position)

      /**
       * This packet intent is used for 1.7.10
       */
      case class SpawnPosition_i32(x: Int, y: Int, z: Int)

      /**
       * This packet intent is used for 1.17.1 ~
       */
      case class SpawnPositionWithAngle(location: Position, angle: Float)

      /**
       * TimeUpdate is sent to sync the world's time to the client, the client will manually
       * tick the time itself so this doesn't need to sent repeatedly but if the server or
       * client has issues keeping up this can fall out of sync so it is a good idea to send
       * this now and again
       */
      case class TimeUpdate(worldAge: Long, timeOfDay: Long)

      case class StopSound(flags: UByte, source: Option[VarInt], sound: Option[String]) {
        require(source.nonEmpty == ((flags & 0x01) != UByte(0)))
        require(sound.nonEmpty == ((flags & 0x02) != UByte(0)))
      }

      /**
       * Title configures an on-screen title.
       */
      case class Title(
        action: VarInt,
        title: Option[ChatComponent],
        subTitle: Option[ChatComponent],
        actionBarText: Option[String],
        fadeIn: Option[Int],
        fadeStay: Option[Int],
        fadeOut: Option[Int]
      ) {
        require(title.nonEmpty == (action == VarInt(0)))
        require(subTitle.nonEmpty == (action == VarInt(1)))
        require(actionBarText.nonEmpty == (action == VarInt(2)))
        require(fadeIn.nonEmpty == (action == VarInt(3)))
        require(fadeStay.nonEmpty == (action == VarInt(3)))
        require(fadeOut.nonEmpty == (action == VarInt(3)))
      }

      case class Title_notext(
        action: VarInt,
        title: Option[ChatComponent],
        subTitle: Option[ChatComponent],
        fadeIn: Option[Int],
        fadeStay: Option[Int],
        fadeOut: Option[Int]
      ) {
        require(title.nonEmpty == (action == VarInt(0)))
        require(subTitle.nonEmpty == (action == VarInt(1)))
        require(fadeIn.nonEmpty == (action == VarInt(2)))
        require(fadeStay.nonEmpty == (action == VarInt(2)))
        require(fadeOut.nonEmpty == (action == VarInt(2)))
      }

      case class Title_notext_component(
        action: VarInt,
        title: Option[ChatComponent],
        subTitle: Option[ChatComponent],
        fadeIn: Option[ChatComponent],
        fadeStay: Option[ChatComponent],
        fadeOut: Option[ChatComponent]
      ) {
        require(title.nonEmpty == (action == VarInt(0)))
        require(subTitle.nonEmpty == (action == VarInt(1)))
        require(fadeIn.nonEmpty == (action == VarInt(2)))
        require(fadeStay.nonEmpty == (action == VarInt(2)))
        require(fadeOut.nonEmpty == (action == VarInt(2)))
      }

      case class Title_onlytext(title: ChatComponent)

      case class SubTitle(subtitle: ChatComponent)

      case class TitleFade(
        fadeIn: Int,
        fadeStay: Int,
        fadeOut: Int
      )

      case class ClearTitle(reset: Boolean)

      /**
       * UpdateSign sets or changes the text on a sign.
       */
      case class UpdateSign(
        location: Position,
        line1: ChatComponent,
        line2: ChatComponent,
        line3: ChatComponent,
        line4: ChatComponent
      )

      case class UpdateSign_u16(
        x: Int,
        y: UShort,
        z: Int,
        line1: ChatComponent,
        line2: ChatComponent,
        line3: ChatComponent,
        line4: ChatComponent
      )

      /**
       * SoundEffect plays the named sound at the target location.
       */
      case class SoundEffect(
        name: VarInt,
        category: VarInt,
        x: Int,
        y: Int,
        z: Int,
        volume: Float,
        pitch: Float
      )

      case class SoundEffect_u8(
        name: VarInt,
        category: VarInt,
        x: Int,
        y: Int,
        z: Int,
        volume: Float,
        pitch: UByte
      )

      /**
       * Plays a sound effect from an entity.
       */
      case class EntitySoundEffect(
        soundId: VarInt,
        soundCategory: VarInt,
        entityId: VarInt,
        volume: Float,
        pitch: Float
      )

      /**
       * PlayerListHeaderFooter updates the header/footer of the player list.
       */
      case class PlayerListHeaderFooter(header: ChatComponent, footer: ChatComponent)

      /**
       * CollectItem causes the collected item to fly towards the collector. This does not
       * destroy the entity.
       */
      case class CollectItem(
        collectedEntityId: VarInt,
        collectorEntityId: VarInt,
        numberOfItems: VarInt
      )

      case class CollectItem_nocount(collectedEntityId: VarInt, collectorEntityId: VarInt)

      case class CollectItem_nocount_i32(collectedEntityId: Int, collectorEntityId: Int)

      /**
       * EntityTeleport teleports the entity to the target location. This is sent if the entity
       * moves further than EntityMove allows.
       */
      case class EntityTeleport_f64(
        entityId: VarInt,
        x: Double,
        y: Double,
        z: Double,
        yaw: Byte,
        pitch: Byte,
        onGround: Boolean
      )

      case class EntityTeleport_i32(
        entityId: VarInt,
        x: FixedPoint5[Int],
        y: FixedPoint5[Int],
        z: FixedPoint5[Int],
        yaw: Byte,
        pitch: Byte,
        onGround: Boolean
      )

      case class EntityTeleport_i32_i32_NoGround(
        entityId: Int,
        x: FixedPoint5[Int],
        y: FixedPoint5[Int],
        z: FixedPoint5[Int],
        yaw: Byte,
        pitch: Byte
      )

      case class Advancements(
        data: UnspecifiedLengthByteArray
        /**
         * TODO: fix parsing modded advancements 1.12.2 (e.g. SevTech Ages) see
         * https://github.com/iceiix/stevenarella/issues/148 field reset_clear: bool =, field
         * mapping: LenPrefixed<VarInt, packet::Advancement> =, field identifiers:
         * LenPrefixed<VarInt, String> =, field progress: LenPrefixed<VarInt,
         * packet::AdvancementProgress> =,
         */
      )

      /**
       * EntityProperties updates the properties for an entity.
       *
       * Used in versions 1.8 ~ 1.16
       */
      case class EntityProperties(
        entityId: VarInt,
        properties: LenPrefixedSeq[Int, EntityProperty]
      )

      /**
       * Used in versions 1.17 ~
       */
      case class EntityProperties_VarIntLength(
        entityId: VarInt,
        properties: LenPrefixedSeq[VarInt, EntityProperty]
      )

      /**
       * Used in version 1.7
       */
      case class EntityProperties_i32(
        entityId: Int,
        properties: LenPrefixedSeq[Int, EntityPropertyShort]
      )

      /**
       * EntityEffect applies a status effect to an entity for a given duration.
       */
      case class EntityEffect(
        entityId: VarInt,
        effectId: Byte,
        amplifier: Byte,
        duration: VarInt,
        hideParticles: Boolean
      )

      case class EntityEffect_i32(
        entityId: Int,
        effectId: Byte,
        amplifier: Byte,
        duration: Short
      )

      case class DeclareRecipes(recipes: LenPrefixedSeq[VarInt, Recipe])

      // "Tags" on 1.13
      case class Tags(blockTags: TagArray, itemTags: TagArray, fluidTags: TagArray)

      // "Tags" from 1.14 until 1.16
      case class TagsWithEntities(
        blockTags: TagArray,
        itemTags: TagArray,
        fluidTags: TagArray,
        entityTags: TagArray
      )

      // "Tags" from 1.17. See https://wiki.vg/index.php?title=Protocol&oldid=16866#Tags for more details
      case class TagsWithTypes(tags: LenPrefixedSeq[VarInt, TagArrayWithType])

      case class AcknowledgePlayerDigging(
        location: Position,
        block: VarInt,
        status: VarInt,
        successful: Boolean
      )

      case class UpdateLight_WithTrust(
        chunkX: VarInt,
        chunkZ: VarInt,
        trustEdges: Boolean,
        skyLightMask: VarInt,
        blockLightMask: VarInt,
        emptySkyLightMask: VarInt,
        lightArrays: UnspecifiedLengthByteArray
      )

      case class UpdateLight_NoTrust(
        chunkX: VarInt,
        chunkZ: VarInt,
        skyLightMask: VarInt,
        blockLightMask: VarInt,
        emptySkyLightMask: VarInt,
        lightArrays: UnspecifiedLengthByteArray
      )

      case class TradeList_WithoutRestock(
        id: VarInt,
        trades: LenPrefixedSeq[UByte, Trade],
        villagerLevel: VarInt,
        experience: VarInt,
        isRegularVillager: Boolean
      )

      case class TradeList_WithRestock(
        id: VarInt,
        trades: LenPrefixedSeq[UByte, Trade],
        villagerLevel: VarInt,
        experience: VarInt,
        isRegularVillager: Boolean,
        canRestock: Boolean
      )

      case class CoFHLib_SendUUID(playerUuid: UUID)
    }
  }
  object Login {
    object ServerBound {

      /**
       * LoginStart is sent immeditately after switching into the login state. The passed
       * username is used by the server to authenticate the player in online mode.
       */
      case class LoginStart(username: String)

      /**
       * EncryptionResponse is sent as a reply to EncryptionRequest. All packets following this
       * one must be encrypted with AES/CFB8 encryption.
       */
      case class EncryptionResponse(
        /**
         * The key for the AES/CFB8 cipher encrypted with the public key
         */
        sharedSecret: LenPrefixedByteSeq[VarInt],
        /**
         * The verify token from the request encrypted with the public key
         */
        verifyToken: LenPrefixedByteSeq[VarInt]
      )

      case class EncryptionResponse_i16(
        sharedSecret: LenPrefixedByteSeq[Short],
        verifyToken: LenPrefixedByteSeq[Short]
      )

      case class LoginPluginResponse(
        messageId: VarInt,
        successful: Boolean,
        data: UnspecifiedLengthByteArray
      )
    }
    object ClientBound {

      /**
       * LoginDisconnect is sent by the server if there was any issues authenticating the player
       * during login or the general server issues (e.g. too many players).
       */
      case class LoginDisconnect(reason: ChatComponent)

      /**
       * EncryptionRequest is sent by the server if the server is in online mode. If it is not
       * sent then its assumed the server is in offline mode.
       */
      case class EncryptionRequest(
        /**
         * Generally empty, left in from legacy auth but is still used by the client if provided
         */
        serverId: String,
        /**
         * A RSA Public key serialized in x.509 PRIX format
         */
        publicKey: LenPrefixedByteSeq[VarInt],
        /**
         * Token used by the server to verify encryption is working correctly
         */
        verifyToken: LenPrefixedByteSeq[VarInt]
      )

      case class EncryptionRequest_i16(
        serverId: String,
        publicKey: LenPrefixedByteSeq[Short],
        verifyToken: LenPrefixedByteSeq[Short]
      )

      /**
       * LoginSuccess is sent by the server if the player successfully authenicates with the
       * session servers (online mode) or straight after LoginStart (offline mode).
       */
      case class LoginSuccess_String(
        /**
         * String encoding of a uuid (with hyphens)
         */
        uuid: String,
        username: String
      )

      case class LoginSuccess_UUID(uuid: UUID, username: String)

      /**
       * SetInitialCompression sets the compression threshold during the login state.
       */
      case class SetInitialCompression(
        /**
         * Threshold where a packet should be sent compressed
         */
        threshold: VarInt
      )

      case class LoginPluginRequest(
        messageId: VarInt,
        channel: String,
        data: UnspecifiedLengthByteArray
      )
    }
  }
  object Status {
    object ServerBound {

      /**
       * StatusRequest is sent by the client instantly after switching to the Status protocol
       * state and is used to signal the server to send a StatusResponse to the client
       */
      case class StatusRequest(empty: Unit)

      /**
       * StatusPing is sent by the client after recieving a StatusResponse. The client uses the
       * time from sending the ping until the time of recieving a pong to measure the latency
       * between the client and the server.
       */
      case class StatusPing(ping: Long)
    }
    object ClientBound {

      /**
       * StatusResponse is sent as a reply to a StatusRequest. The Status should contain a json
       * encoded structure with version information, a player sample, a description/MOTD and
       * optionally a favicon.
       *
       * The structure is as follows
       *
       * ```json
       * {
       *     "version": {
       *         "name": "1.8.3",
       *         "protocol": 47,
       *     },
       *     "players": {
       *         "max": 20,
       *         "online": 1,
       *         "sample": [
       *           packet  {"name": "Thinkofdeath", "id": "4566e69f-c907-48ee-8d71-d7ba5aa00d20"}
       *         ]
       *     },
       *     "description": "Hello world",
       *     "favicon": "data:image/png;base64,<data>"
       * }
       * ```
       */
      case class StatusResponse(status: String)

      /**
       * StatusPong is sent as a reply to a StatusPing. The Time field should be exactly the
       * same as the one sent by the client.
       */
      case class StatusPong(ping: Long)
    }
  }
}
