package com.github.kory33.s2mctest
package com.github.kory33.s2mctest.connection.protocol.bindings.versions

import connection.protocol.bindings.{Protocol, PacketIdBindings}
import connection.protocol.codec.ByteCodec
import connection.protocol.codec.ByteCodecs.Common.given
import connection.protocol.codec.macros.GenByteDecode.given
import connection.protocol.packets.PacketIntent

import PacketIntent.Handshaking.ServerBound.*
import PacketIntent.Play.ServerBound.*
import PacketIntent.Play.ClientBound.*
import PacketIntent.Login.ServerBound.*
import PacketIntent.Login.ClientBound.*
import PacketIntent.Status.ClientBound.*
import PacketIntent.Status.ServerBound.*

object v1_7_10 {
  val playProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summon[KeepAliveServerbound_i32],
      0x01 -> ByteCodec.summon[ChatMessage],
      0x02 -> ByteCodec.summon[UseEntity_Handsfree_i32],
      0x03 -> ByteCodec.summon[Player],
      0x04 -> ByteCodec.summon[PlayerPosition_HeadY],
      0x05 -> ByteCodec.summon[PlayerLook],
      0x06 -> ByteCodec.summon[PlayerPositionLook_HeadY],
      0x07 -> ByteCodec.summon[PlayerDigging_u8_u8y],
      0x08 -> ByteCodec.summon[PlayerBlockPlacement_u8_Item_u8y],
      0x09 -> ByteCodec.summon[HeldItemChange],
      0x0a -> ByteCodec.summon[ArmSwing_Handsfree_ID],
      0x0b -> ByteCodec.summon[PlayerAction_i32],
      0x0c -> ByteCodec.summon[SteerVehicle_jump_unmount],
      0x0d -> ByteCodec.summon[CloseWindow],
      0x0e -> ByteCodec.summon[ClickWindow_u8],
      0x0f -> ByteCodec.summon[ConfirmTransactionServerbound],
      0x10 -> ByteCodec.summon[CreativeInventoryAction],
      0x11 -> ByteCodec.summon[EnchantItem],
      0x12 -> ByteCodec.summon[SetSign_i16y],
      0x13 -> ByteCodec.summon[ClientAbilities_f32],
      0x14 -> ByteCodec.summon[TabComplete_NoAssume_NoTarget],
      0x15 -> ByteCodec.summon[ClientSettings_u8_Handsfree_Difficulty],
      0x16 -> ByteCodec.summon[ClientStatus_u8],
      0x17 -> ByteCodec.summon[PluginMessageServerbound_i16],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summon[KeepAliveClientbound_i32],
      0x01 -> ByteCodec.summon[JoinGame_i8_NoDebug],
      0x02 -> ByteCodec.summon[ServerMessage_NoPosition],
      0x03 -> ByteCodec.summon[TimeUpdate],
      0x04 -> ByteCodec.summon[EntityEquipment_u16_i32],
      0x05 -> ByteCodec.summon[SpawnPosition_i32],
      0x06 -> ByteCodec.summon[UpdateHealth_u16],
      0x07 -> ByteCodec.summon[Respawn_Gamemode],
      0x08 -> ByteCodec.summon[TeleportPlayer_OnGround],
      0x09 -> ByteCodec.summon[SetCurrentHotbarSlot],
      0x0a -> ByteCodec.summon[EntityUsedBed_i32],
      0x0b -> ByteCodec.summon[Animation],
      0x0c -> ByteCodec.summon[SpawnPlayer_i32_HeldItem_String],
      0x0d -> ByteCodec.summon[CollectItem_nocount_i32],
      0x0e -> ByteCodec.summon[SpawnObject_i32_NoUUID],
      0x0f -> ByteCodec.summon[SpawnMob_u8_i32_NoUUID],
      0x10 -> ByteCodec.summon[SpawnPainting_NoUUID_i32],
      0x11 -> ByteCodec.summon[SpawnExperienceOrb_i32],
      0x12 -> ByteCodec.summon[EntityVelocity_i32],
      0x13 -> ByteCodec.summon[EntityDestroy_u8],
      0x14 -> ByteCodec.summon[Entity_i32],
      0x15 -> ByteCodec.summon[EntityMove_i8_i32_NoGround],
      0x16 -> ByteCodec.summon[EntityLook_i32_NoGround],
      0x17 -> ByteCodec.summon[EntityLookAndMove_i8_i32_NoGround],
      0x18 -> ByteCodec.summon[EntityTeleport_i32_i32_NoGround],
      0x19 -> ByteCodec.summon[EntityHeadLook_i32],
      0x1a -> ByteCodec.summon[EntityStatus],
      0x1b -> ByteCodec.summon[EntityAttach_leashed],
      0x1c -> ByteCodec.summon[EntityMetadata_i32],
      0x1d -> ByteCodec.summon[EntityEffect_i32],
      0x1e -> ByteCodec.summon[EntityRemoveEffect_i32],
      0x1f -> ByteCodec.summon[SetExperience_i16],
      0x20 -> ByteCodec.summon[EntityProperties_i32],
      0x21 -> ByteCodec.summon[ChunkData_17],
      0x22 -> ByteCodec.summon[MultiBlockChange_u16],
      0x23 -> ByteCodec.summon[BlockChange_u8],
      0x24 -> ByteCodec.summon[BlockAction_u16],
      0x25 -> ByteCodec.summon[BlockBreakAnimation_i32],
      0x26 -> ByteCodec.summon[ChunkDataBulk_17],
      0x27 -> ByteCodec.summon[Explosion],
      0x28 -> ByteCodec.summon[Effect_u8y],
      0x29 -> ByteCodec.summon[NamedSoundEffect_u8_NoCategory],
      0x2a -> ByteCodec.summon[Particle_Named],
      0x2b -> ByteCodec.summon[ChangeGameState],
      0x2c -> ByteCodec.summon[SpawnGlobalEntity_i32],
      0x2d -> ByteCodec.summon[WindowOpen_u8],
      0x2e -> ByteCodec.summon[WindowClose],
      0x2f -> ByteCodec.summon[WindowSetSlot],
      0x30 -> ByteCodec.summon[WindowItems],
      0x31 -> ByteCodec.summon[WindowProperty],
      0x32 -> ByteCodec.summon[ConfirmTransaction],
      0x33 -> ByteCodec.summon[UpdateSign_u16],
      0x34 -> ByteCodec.summon[Maps_NoTracking_Data],
      0x35 -> ByteCodec.summon[UpdateBlockEntity_Data],
      0x36 -> ByteCodec.summon[SignEditorOpen_i32],
      0x37 -> ByteCodec.summon[Statistics],
      0x38 -> ByteCodec.summon[PlayerInfo_String],
      0x39 -> ByteCodec.summon[PlayerAbilities],
      0x3a -> ByteCodec.summon[TabCompleteReply],
      0x3b -> ByteCodec.summon[ScoreboardObjective_NoMode],
      0x3c -> ByteCodec.summon[UpdateScore_i32],
      0x3d -> ByteCodec.summon[ScoreboardDisplay],
      0x3e -> ByteCodec.summon[Teams_NoVisColor],
      0x3f -> ByteCodec.summon[PluginMessageClientbound_i16],
      0x40 -> ByteCodec.summon[Disconnect],
      -0x1a -> ByteCodec.summon[CoFHLib_SendUUID],
    ))
  )

  val loginProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summon[LoginStart],
      0x01 -> ByteCodec.summon[EncryptionResponse_i16],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summon[LoginDisconnect],
      0x01 -> ByteCodec.summon[EncryptionRequest_i16],
      0x02 -> ByteCodec.summon[LoginSuccess_String],
    ))
  )
}
