package com.github.kory33.s2mctest.impl.connection.protocol.versions

import com.github.kory33.s2mctest.core.connection.codec.ByteCodec
import com.github.kory33.s2mctest.core.connection.protocol.{PacketIdBindings, Protocol}
import com.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.VarInt

object v1_7_10 {
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.*
  import com.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.Common.given
  import com.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.PositionCodecBefore1_14.given
  import com.github.kory33.s2mctest.impl.connection.codec.decode.macros.GenByteDecode.given

  val protocolVersion: VarInt = VarInt(5)

  // noinspection TypeAnnotation
  // format: off
  val playProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[KeepAliveServerbound_i32],
      0x01 -> ByteCodec.summonPair[ChatMessage],
      0x02 -> ByteCodec.summonPair[UseEntity_Handsfree_i32],
      0x03 -> ByteCodec.summonPair[Player],
      0x04 -> ByteCodec.summonPair[PlayerPosition_HeadY],
      0x05 -> ByteCodec.summonPair[PlayerLook],
      0x06 -> ByteCodec.summonPair[PlayerPositionLook_HeadY],
      0x07 -> ByteCodec.summonPair[PlayerDigging_u8_u8y],
      0x08 -> ByteCodec.summonPair[PlayerBlockPlacement_u8_Item_u8y],
      0x09 -> ByteCodec.summonPair[HeldItemChange],
      0x0a -> ByteCodec.summonPair[ArmSwing_Handsfree_ID],
      0x0b -> ByteCodec.summonPair[PlayerAction_i32],
      0x0c -> ByteCodec.summonPair[SteerVehicle_jump_unmount],
      0x0d -> ByteCodec.summonPair[CloseWindow],
      0x0e -> ByteCodec.summonPair[ClickWindow_u8],
      0x0f -> ByteCodec.summonPair[ConfirmTransactionServerbound],
      0x10 -> ByteCodec.summonPair[CreativeInventoryAction],
      0x11 -> ByteCodec.summonPair[EnchantItem],
      0x12 -> ByteCodec.summonPair[SetSign_i16y],
      0x13 -> ByteCodec.summonPair[ClientAbilities_f32],
      0x14 -> ByteCodec.summonPair[TabComplete_NoAssume_NoTarget],
      0x15 -> ByteCodec.summonPair[ClientSettings_u8_Handsfree_Difficulty],
      0x16 -> ByteCodec.summonPair[ClientStatus_u8],
      0x17 -> ByteCodec.summonPair[PluginMessageServerbound_i16],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[KeepAliveClientbound_i32],
      0x01 -> ByteCodec.summonPair[JoinGame_i8_NoDebug],
      0x02 -> ByteCodec.summonPair[ServerMessage_NoPosition],
      0x03 -> ByteCodec.summonPair[TimeUpdate],
      0x04 -> ByteCodec.summonPair[EntityEquipment_u16_i32],
      0x05 -> ByteCodec.summonPair[SpawnPosition_i32],
      0x06 -> ByteCodec.summonPair[UpdateHealth_u16],
      0x07 -> ByteCodec.summonPair[Respawn_Gamemode],
      0x08 -> ByteCodec.summonPair[TeleportPlayer_OnGround],
      0x09 -> ByteCodec.summonPair[SetCurrentHotbarSlot],
      0x0a -> ByteCodec.summonPair[EntityUsedBed_i32],
      0x0b -> ByteCodec.summonPair[Animation],
      0x0c -> ByteCodec.summonPair[SpawnPlayer_i32_HeldItem_String],
      0x0d -> ByteCodec.summonPair[CollectItem_nocount_i32],
      0x0e -> ByteCodec.summonPair[SpawnObject_i32_NoUUID],
      0x0f -> ByteCodec.summonPair[SpawnMob_u8_i32_NoUUID],
      0x10 -> ByteCodec.summonPair[SpawnPainting_NoUUID_i32],
      0x11 -> ByteCodec.summonPair[SpawnExperienceOrb_i32],
      0x12 -> ByteCodec.summonPair[EntityVelocity_i32],
      0x13 -> ByteCodec.summonPair[EntityDestroy_u8],
      0x14 -> ByteCodec.summonPair[Entity_i32],
      0x15 -> ByteCodec.summonPair[EntityMove_i8_i32_NoGround],
      0x16 -> ByteCodec.summonPair[EntityLook_i32_NoGround],
      0x17 -> ByteCodec.summonPair[EntityLookAndMove_i8_i32_NoGround],
      0x18 -> ByteCodec.summonPair[EntityTeleport_i32_i32_NoGround],
      0x19 -> ByteCodec.summonPair[EntityHeadLook_i32],
      0x1a -> ByteCodec.summonPair[EntityStatus],
      0x1b -> ByteCodec.summonPair[EntityAttach_leashed],
      0x1c -> ByteCodec.summonPair[EntityMetadata_i32],
      0x1d -> ByteCodec.summonPair[EntityEffect_i32],
      0x1e -> ByteCodec.summonPair[EntityRemoveEffect_i32],
      0x1f -> ByteCodec.summonPair[SetExperience_i16],
      0x20 -> ByteCodec.summonPair[EntityProperties_i32],
      0x21 -> ByteCodec.summonPair[ChunkData_17],
      0x22 -> ByteCodec.summonPair[MultiBlockChange_u16],
      0x23 -> ByteCodec.summonPair[BlockChange_u8],
      0x24 -> ByteCodec.summonPair[BlockAction_u16],
      0x25 -> ByteCodec.summonPair[BlockBreakAnimation_i32],
      0x26 -> ByteCodec.summonPair[ChunkDataBulk_17],
      0x27 -> ByteCodec.summonPair[Explosion],
      0x28 -> ByteCodec.summonPair[Effect_u8y],
      0x29 -> ByteCodec.summonPair[NamedSoundEffect_u8_NoCategory],
      0x2a -> ByteCodec.summonPair[Particle_Named],
      0x2b -> ByteCodec.summonPair[ChangeGameState],
      0x2c -> ByteCodec.summonPair[SpawnGlobalEntity_i32],
      0x2d -> ByteCodec.summonPair[WindowOpen_u8],
      0x2e -> ByteCodec.summonPair[WindowClose],
      0x2f -> ByteCodec.summonPair[WindowSetSlot],
      0x30 -> ByteCodec.summonPair[WindowItems],
      0x31 -> ByteCodec.summonPair[WindowProperty],
      0x32 -> ByteCodec.summonPair[ConfirmTransaction],
      0x33 -> ByteCodec.summonPair[UpdateSign_u16],
      0x34 -> ByteCodec.summonPair[Maps_NoTracking_Data],
      0x35 -> ByteCodec.summonPair[UpdateBlockEntity_Data],
      0x36 -> ByteCodec.summonPair[SignEditorOpen_i32],
      0x37 -> ByteCodec.summonPair[Statistics],
      0x38 -> ByteCodec.summonPair[PlayerInfo_String],
      0x39 -> ByteCodec.summonPair[PlayerAbilities],
      0x3a -> ByteCodec.summonPair[TabCompleteReply],
      0x3b -> ByteCodec.summonPair[ScoreboardObjective_NoMode],
      0x3c -> ByteCodec.summonPair[UpdateScore_i32],
      0x3d -> ByteCodec.summonPair[ScoreboardDisplay],
      0x3e -> ByteCodec.summonPair[Teams_NoVisColor],
      0x3f -> ByteCodec.summonPair[PluginMessageClientbound_i16],
      0x40 -> ByteCodec.summonPair[Disconnect],
      -0x1a -> ByteCodec.summonPair[CoFHLib_SendUUID],
    ))
  )
  // format: on

  // noinspection TypeAnnotation
  // format: off
  val loginProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[LoginStart],
      0x01 -> ByteCodec.summonPair[EncryptionResponse_i16],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[LoginDisconnect],
      0x01 -> ByteCodec.summonPair[EncryptionRequest_i16],
      0x02 -> ByteCodec.summonPair[LoginSuccess_String],
    ))
  )
  // format: on
}
