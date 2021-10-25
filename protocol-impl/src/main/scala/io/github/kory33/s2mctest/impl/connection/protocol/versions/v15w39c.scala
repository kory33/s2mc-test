package io.github.kory33.s2mctest.impl.connection.protocol.versions

import io.github.kory33.s2mctest.core.connection.codec.ByteCodec
import io.github.kory33.s2mctest.core.connection.protocol.{PacketIdBindings, Protocol}
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.VarInt
import io.github.kory33.s2mctest.impl.connection.protocol.PacketIntentCodecCache

object v15w39c {
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.*
  import io.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.PositionCodecBefore1_14.given

  val protocolVersion: VarInt = VarInt(74)

  private val codecCache = new PacketIntentCodecCache
  import codecCache.given

  // noinspection TypeAnnotation
  // format: off
  val playProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec[TabComplete_NoAssume],
      0x01 -> ByteCodec[ChatMessage],
      0x02 -> ByteCodec[ClientStatus],
      0x03 -> ByteCodec[ClientSettings_u8],
      0x04 -> ByteCodec[ConfirmTransactionServerbound],
      0x05 -> ByteCodec[EnchantItem],
      0x06 -> ByteCodec[ClickWindow_u8],
      0x07 -> ByteCodec[CloseWindow],
      0x08 -> ByteCodec[PluginMessageServerbound],
      0x09 -> ByteCodec[UseEntity_Hand],
      0x0a -> ByteCodec[KeepAliveServerbound_VarInt],
      0x0b -> ByteCodec[PlayerPosition],
      0x0c -> ByteCodec[PlayerPositionLook],
      0x0d -> ByteCodec[PlayerLook],
      0x0e -> ByteCodec[Player],
      0x0f -> ByteCodec[ClientAbilities_f32],
      0x10 -> ByteCodec[PlayerDigging_u8],
      0x11 -> ByteCodec[PlayerAction],
      0x12 -> ByteCodec[SteerVehicle],
      0x13 -> ByteCodec[ResourcePackStatus],
      0x14 -> ByteCodec[HeldItemChange],
      0x15 -> ByteCodec[CreativeInventoryAction],
      0x16 -> ByteCodec[SetSign],
      0x17 -> ByteCodec[ArmSwing],
      0x18 -> ByteCodec[SpectateTeleport],
      0x19 -> ByteCodec[PlayerBlockPlacement_u8],
      0x1a -> ByteCodec[UseItem],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec[SpawnObject_i32],
      0x01 -> ByteCodec[SpawnExperienceOrb_i32],
      0x02 -> ByteCodec[SpawnGlobalEntity_i32],
      0x03 -> ByteCodec[SpawnMob_u8_i32],
      0x04 -> ByteCodec[SpawnPainting_NoUUID],
      0x05 -> ByteCodec[SpawnPlayer_i32],
      0x06 -> ByteCodec[Animation],
      0x07 -> ByteCodec[Statistics],
      0x08 -> ByteCodec[BlockBreakAnimation],
      0x09 -> ByteCodec[UpdateBlockEntity],
      0x0a -> ByteCodec[BlockAction],
      0x0b -> ByteCodec[BlockChange_VarInt],
      0x0c -> ByteCodec[BossBar],
      0x0d -> ByteCodec[ServerDifficulty],
      0x0e -> ByteCodec[TabCompleteReply],
      0x0f -> ByteCodec[ServerMessage_Position],
      0x10 -> ByteCodec[MultiBlockChange_VarInt],
      0x11 -> ByteCodec[ConfirmTransaction],
      0x12 -> ByteCodec[WindowClose],
      0x13 -> ByteCodec[WindowOpen],
      0x14 -> ByteCodec[WindowItems],
      0x15 -> ByteCodec[WindowProperty],
      0x16 -> ByteCodec[WindowSetSlot],
      0x17 -> ByteCodec[SetCooldown],
      0x18 -> ByteCodec[PluginMessageClientbound],
      0x19 -> ByteCodec[Disconnect],
      0x1a -> ByteCodec[EntityAction],
      0x1b -> ByteCodec[Explosion],
      0x1c -> ByteCodec[ChunkUnload],
      0x1d -> ByteCodec[SetCompression],
      0x1e -> ByteCodec[ChangeGameState],
      0x1f -> ByteCodec[KeepAliveClientbound_VarInt],
      0x20 -> ByteCodec[ChunkData_NoEntities],
      0x21 -> ByteCodec[Effect],
      0x22 -> ByteCodec[Particle_VarIntArray],
      0x23 -> ByteCodec[NamedSoundEffect_u8_NoCategory],
      0x24 -> ByteCodec[JoinGame_i8],
      0x25 -> ByteCodec[Maps_NoLocked],
      0x26 -> ByteCodec[EntityMove_i8],
      0x27 -> ByteCodec[EntityLookAndMove_i8],
      0x28 -> ByteCodec[EntityLook_VarInt],
      0x29 -> ByteCodec[Entity],
      0x2a -> ByteCodec[SignEditorOpen],
      0x2b -> ByteCodec[PlayerAbilities],
      0x2c -> ByteCodec[CombatEvent],
      0x2d -> ByteCodec[PlayerInfo],
      0x2e -> ByteCodec[TeleportPlayer_NoConfirm],
      0x2f -> ByteCodec[EntityUsedBed],
      0x30 -> ByteCodec[EntityDestroy],
      0x31 -> ByteCodec[EntityRemoveEffect],
      0x32 -> ByteCodec[ResourcePackSend],
      0x33 -> ByteCodec[Respawn_Gamemode],
      0x34 -> ByteCodec[EntityHeadLook],
      0x35 -> ByteCodec[WorldBorder],
      0x36 -> ByteCodec[Camera],
      0x37 -> ByteCodec[SetCurrentHotbarSlot],
      0x38 -> ByteCodec[ScoreboardDisplay],
      0x39 -> ByteCodec[EntityMetadata],
      0x3a -> ByteCodec[EntityAttach_leashed],
      0x3b -> ByteCodec[EntityVelocity],
      0x3c -> ByteCodec[EntityEquipment_VarInt],
      0x3d -> ByteCodec[SetExperience],
      0x3e -> ByteCodec[UpdateHealth],
      0x3f -> ByteCodec[ScoreboardObjective],
      0x40 -> ByteCodec[Teams_u8],
      0x41 -> ByteCodec[UpdateScore],
      0x42 -> ByteCodec[SpawnPosition],
      0x43 -> ByteCodec[TimeUpdate],
      0x44 -> ByteCodec[Title_notext_component],
      0x45 -> ByteCodec[UpdateSign],
      0x46 -> ByteCodec[PlayerListHeaderFooter],
      0x47 -> ByteCodec[CollectItem_nocount],
      0x48 -> ByteCodec[EntityTeleport_i32],
      0x49 -> ByteCodec[EntityProperties],
      0x4a -> ByteCodec[EntityEffect],
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
