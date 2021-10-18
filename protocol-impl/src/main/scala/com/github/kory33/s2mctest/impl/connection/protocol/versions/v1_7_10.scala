package com.github.kory33.s2mctest.impl.connection.protocol.versions

import com.github.kory33.s2mctest.core.connection.codec.ByteCodec
import com.github.kory33.s2mctest.core.connection.protocol.{PacketIdBindings, Protocol}
import com.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.VarInt
import com.github.kory33.s2mctest.impl.connection.protocol.PacketIntentCodecCache

object v1_7_10 {
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.*
  import com.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.PositionCodecBefore1_14.given

  val protocolVersion: VarInt = VarInt(5)

  private val codecCache = new PacketIntentCodecCache
  import codecCache.given

  // noinspection TypeAnnotation
  // format: off
  val playProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec[KeepAliveServerbound_i32],
      0x01 -> ByteCodec[ChatMessage],
      0x02 -> ByteCodec[UseEntity_Handsfree_i32],
      0x03 -> ByteCodec[Player],
      0x04 -> ByteCodec[PlayerPosition_HeadY],
      0x05 -> ByteCodec[PlayerLook],
      0x06 -> ByteCodec[PlayerPositionLook_HeadY],
      0x07 -> ByteCodec[PlayerDigging_u8_u8y],
      0x08 -> ByteCodec[PlayerBlockPlacement_u8_Item_u8y],
      0x09 -> ByteCodec[HeldItemChange],
      0x0a -> ByteCodec[ArmSwing_Handsfree_ID],
      0x0b -> ByteCodec[PlayerAction_i32],
      0x0c -> ByteCodec[SteerVehicle_jump_unmount],
      0x0d -> ByteCodec[CloseWindow],
      0x0e -> ByteCodec[ClickWindow_u8],
      0x0f -> ByteCodec[ConfirmTransactionServerbound],
      0x10 -> ByteCodec[CreativeInventoryAction],
      0x11 -> ByteCodec[EnchantItem],
      0x12 -> ByteCodec[SetSign_i16y],
      0x13 -> ByteCodec[ClientAbilities_f32],
      0x14 -> ByteCodec[TabComplete_NoAssume_NoTarget],
      0x15 -> ByteCodec[ClientSettings_u8_Handsfree_Difficulty],
      0x16 -> ByteCodec[ClientStatus_u8],
      0x17 -> ByteCodec[PluginMessageServerbound_i16],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec[KeepAliveClientbound_i32],
      0x01 -> ByteCodec[JoinGame_i8_NoDebug],
      0x02 -> ByteCodec[ServerMessage_NoPosition],
      0x03 -> ByteCodec[TimeUpdate],
      0x04 -> ByteCodec[EntityEquipment_u16_i32],
      0x05 -> ByteCodec[SpawnPosition_i32],
      0x06 -> ByteCodec[UpdateHealth_u16],
      0x07 -> ByteCodec[Respawn_Gamemode],
      0x08 -> ByteCodec[TeleportPlayer_OnGround],
      0x09 -> ByteCodec[SetCurrentHotbarSlot],
      0x0a -> ByteCodec[EntityUsedBed_i32],
      0x0b -> ByteCodec[Animation],
      0x0c -> ByteCodec[SpawnPlayer_i32_HeldItem_String],
      0x0d -> ByteCodec[CollectItem_nocount_i32],
      0x0e -> ByteCodec[SpawnObject_i32_NoUUID],
      0x0f -> ByteCodec[SpawnMob_u8_i32_NoUUID],
      0x10 -> ByteCodec[SpawnPainting_NoUUID_i32],
      0x11 -> ByteCodec[SpawnExperienceOrb_i32],
      0x12 -> ByteCodec[EntityVelocity_i32],
      0x13 -> ByteCodec[EntityDestroy_u8],
      0x14 -> ByteCodec[Entity_i32],
      0x15 -> ByteCodec[EntityMove_i8_i32_NoGround],
      0x16 -> ByteCodec[EntityLook_i32_NoGround],
      0x17 -> ByteCodec[EntityLookAndMove_i8_i32_NoGround],
      0x18 -> ByteCodec[EntityTeleport_i32_i32_NoGround],
      0x19 -> ByteCodec[EntityHeadLook_i32],
      0x1a -> ByteCodec[EntityStatus],
      0x1b -> ByteCodec[EntityAttach_leashed],
      0x1c -> ByteCodec[EntityMetadata_i32],
      0x1d -> ByteCodec[EntityEffect_i32],
      0x1e -> ByteCodec[EntityRemoveEffect_i32],
      0x1f -> ByteCodec[SetExperience_i16],
      0x20 -> ByteCodec[EntityProperties_i32],
      0x21 -> ByteCodec[ChunkData_17],
      0x22 -> ByteCodec[MultiBlockChange_u16],
      0x23 -> ByteCodec[BlockChange_u8],
      0x24 -> ByteCodec[BlockAction_u16],
      0x25 -> ByteCodec[BlockBreakAnimation_i32],
      0x26 -> ByteCodec[ChunkDataBulk_17],
      0x27 -> ByteCodec[Explosion],
      0x28 -> ByteCodec[Effect_u8y],
      0x29 -> ByteCodec[NamedSoundEffect_u8_NoCategory],
      0x2a -> ByteCodec[Particle_Named],
      0x2b -> ByteCodec[ChangeGameState],
      0x2c -> ByteCodec[SpawnGlobalEntity_i32],
      0x2d -> ByteCodec[WindowOpen_u8],
      0x2e -> ByteCodec[WindowClose],
      0x2f -> ByteCodec[WindowSetSlot],
      0x30 -> ByteCodec[WindowItems],
      0x31 -> ByteCodec[WindowProperty],
      0x32 -> ByteCodec[ConfirmTransaction],
      0x33 -> ByteCodec[UpdateSign_u16],
      0x34 -> ByteCodec[Maps_NoTracking_Data],
      0x35 -> ByteCodec[UpdateBlockEntity_Data],
      0x36 -> ByteCodec[SignEditorOpen_i32],
      0x37 -> ByteCodec[Statistics],
      0x38 -> ByteCodec[PlayerInfo_String],
      0x39 -> ByteCodec[PlayerAbilities],
      0x3a -> ByteCodec[TabCompleteReply],
      0x3b -> ByteCodec[ScoreboardObjective_NoMode],
      0x3c -> ByteCodec[UpdateScore_i32],
      0x3d -> ByteCodec[ScoreboardDisplay],
      0x3e -> ByteCodec[Teams_NoVisColor],
      0x3f -> ByteCodec[PluginMessageClientbound_i16],
      0x40 -> ByteCodec[Disconnect],
      -0x1a -> ByteCodec[CoFHLib_SendUUID],
    ))
  )
  // format: on

  // noinspection TypeAnnotation
  // format: off
  val loginProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec[LoginStart],
      0x01 -> ByteCodec[EncryptionResponse_i16],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec[LoginDisconnect],
      0x01 -> ByteCodec[EncryptionRequest_i16],
      0x02 -> ByteCodec[LoginSuccess_String],
    ))
  )
  // format: on
}
