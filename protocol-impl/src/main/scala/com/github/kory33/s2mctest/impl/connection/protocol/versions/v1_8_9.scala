package com.github.kory33.s2mctest.impl.connection.protocol.versions

import com.github.kory33.s2mctest.core.connection.codec.ByteCodec
import com.github.kory33.s2mctest.core.connection.protocol.{PacketIdBindings, Protocol}
import com.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.VarInt
import com.github.kory33.s2mctest.impl.connection.protocol.PacketIntentCodecCache

object v1_8_9 {
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.*
  import com.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.PositionCodecBefore1_14.given

  val protocolVersion: VarInt = VarInt(47)

  private val codecCache = new PacketIntentCodecCache
  import codecCache.given

  // noinspection TypeAnnotation
  // format: off
  val playProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec[KeepAliveServerbound_VarInt],
      0x01 -> ByteCodec[ChatMessage],
      0x02 -> ByteCodec[UseEntity_Handsfree],
      0x03 -> ByteCodec[Player],
      0x04 -> ByteCodec[PlayerPosition],
      0x05 -> ByteCodec[PlayerLook],
      0x06 -> ByteCodec[PlayerPositionLook],
      0x07 -> ByteCodec[PlayerDigging_u8],
      0x08 -> ByteCodec[PlayerBlockPlacement_u8_Item],
      0x09 -> ByteCodec[HeldItemChange],
      0x0a -> ByteCodec[ArmSwing_Handsfree],
      0x0b -> ByteCodec[PlayerAction],
      0x0c -> ByteCodec[SteerVehicle],
      0x0d -> ByteCodec[CloseWindow],
      0x0e -> ByteCodec[ClickWindow_u8],
      0x0f -> ByteCodec[ConfirmTransactionServerbound],
      0x10 -> ByteCodec[CreativeInventoryAction],
      0x11 -> ByteCodec[EnchantItem],
      0x12 -> ByteCodec[SetSign],
      0x13 -> ByteCodec[ClientAbilities_f32],
      0x14 -> ByteCodec[TabComplete_NoAssume],
      0x15 -> ByteCodec[ClientSettings_u8_Handsfree],
      0x16 -> ByteCodec[ClientStatus],
      0x17 -> ByteCodec[PluginMessageServerbound],
      0x18 -> ByteCodec[SpectateTeleport],
      0x19 -> ByteCodec[ResourcePackStatus],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec[KeepAliveClientbound_VarInt],
      0x01 -> ByteCodec[JoinGame_i8],
      0x02 -> ByteCodec[ServerMessage_Position],
      0x03 -> ByteCodec[TimeUpdate],
      0x04 -> ByteCodec[EntityEquipment_u16],
      0x05 -> ByteCodec[SpawnPosition],
      0x06 -> ByteCodec[UpdateHealth],
      0x07 -> ByteCodec[Respawn_Gamemode],
      0x08 -> ByteCodec[TeleportPlayer_NoConfirm],
      0x09 -> ByteCodec[SetCurrentHotbarSlot],
      0x0a -> ByteCodec[EntityUsedBed],
      0x0b -> ByteCodec[Animation],
      0x0c -> ByteCodec[SpawnPlayer_i32_HeldItem],
      0x0d -> ByteCodec[CollectItem_nocount],
      0x0e -> ByteCodec[SpawnObject_i32_NoUUID],
      0x0f -> ByteCodec[SpawnMob_u8_i32_NoUUID],
      0x10 -> ByteCodec[SpawnPainting_NoUUID],
      0x11 -> ByteCodec[SpawnExperienceOrb_i32],
      0x12 -> ByteCodec[EntityVelocity],
      0x13 -> ByteCodec[EntityDestroy],
      0x14 -> ByteCodec[Entity],
      0x15 -> ByteCodec[EntityMove_i8],
      0x16 -> ByteCodec[EntityLook_VarInt],
      0x17 -> ByteCodec[EntityLookAndMove_i8],
      0x18 -> ByteCodec[EntityTeleport_i32],
      0x19 -> ByteCodec[EntityHeadLook],
      0x1a -> ByteCodec[EntityStatus],
      0x1b -> ByteCodec[EntityAttach_leashed],
      0x1c -> ByteCodec[EntityMetadata],
      0x1d -> ByteCodec[EntityEffect],
      0x1e -> ByteCodec[EntityRemoveEffect],
      0x1f -> ByteCodec[SetExperience],
      0x20 -> ByteCodec[EntityProperties],
      0x21 -> ByteCodec[ChunkData_NoEntities_u16],
      0x22 -> ByteCodec[MultiBlockChange_VarInt],
      0x23 -> ByteCodec[BlockChange_VarInt],
      0x24 -> ByteCodec[BlockAction],
      0x25 -> ByteCodec[BlockBreakAnimation],
      0x26 -> ByteCodec[ChunkDataBulk],
      0x27 -> ByteCodec[Explosion],
      0x28 -> ByteCodec[Effect],
      0x29 -> ByteCodec[NamedSoundEffect_u8_NoCategory],
      0x2a -> ByteCodec[Particle_VarIntArray],
      0x2b -> ByteCodec[ChangeGameState],
      0x2c -> ByteCodec[SpawnGlobalEntity_i32],
      0x2d -> ByteCodec[WindowOpen],
      0x2e -> ByteCodec[WindowClose],
      0x2f -> ByteCodec[WindowSetSlot],
      0x30 -> ByteCodec[WindowItems],
      0x31 -> ByteCodec[WindowProperty],
      0x32 -> ByteCodec[ConfirmTransaction],
      0x33 -> ByteCodec[UpdateSign],
      0x34 -> ByteCodec[Maps_NoTracking],
      0x35 -> ByteCodec[UpdateBlockEntity],
      0x36 -> ByteCodec[SignEditorOpen],
      0x37 -> ByteCodec[Statistics],
      0x38 -> ByteCodec[PlayerInfo],
      0x39 -> ByteCodec[PlayerAbilities],
      0x3a -> ByteCodec[TabCompleteReply],
      0x3b -> ByteCodec[ScoreboardObjective],
      0x3c -> ByteCodec[UpdateScore],
      0x3d -> ByteCodec[ScoreboardDisplay],
      0x3e -> ByteCodec[Teams_u8],
      0x3f -> ByteCodec[PluginMessageClientbound],
      0x40 -> ByteCodec[Disconnect],
      0x41 -> ByteCodec[ServerDifficulty],
      0x42 -> ByteCodec[CombatEvent],
      0x43 -> ByteCodec[Camera],
      0x44 -> ByteCodec[WorldBorder],
      0x45 -> ByteCodec[Title_notext_component],
      0x46 -> ByteCodec[SetCompression],
      0x47 -> ByteCodec[PlayerListHeaderFooter],
      0x48 -> ByteCodec[ResourcePackSend],
      0x49 -> ByteCodec[EntityUpdateNBT],
    ))
  )
  // format: on

  // noinspection TypeAnnotation
  // format: off
  val loginProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec[LoginStart],
      0x01 -> ByteCodec[EncryptionResponse],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec[LoginDisconnect],
      0x01 -> ByteCodec[EncryptionRequest],
      0x02 -> ByteCodec[LoginSuccess_String],
      0x03 -> ByteCodec[SetInitialCompression],
    ))
  )
  // format: on
}
