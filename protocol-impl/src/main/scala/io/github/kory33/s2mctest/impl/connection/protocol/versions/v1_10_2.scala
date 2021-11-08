package io.github.kory33.s2mctest.impl.connection.protocol.versions

import io.github.kory33.s2mctest.core.connection.codec.ByteCodec
import io.github.kory33.s2mctest.core.connection.protocol.{PacketIdBindings, Protocol}
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.VarInt
import io.github.kory33.s2mctest.impl.connection.protocol.PacketIntentCodecCache

object v1_10_2 {
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.*
  import io.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.PositionCodecBefore1_14.given
  import io.github.kory33.s2mctest.impl.connection.packets.PacketDataCompoundTypes.Slot.Upto_1_12_2 as VersionSpecificSlot

  val protocolVersion: VarInt = VarInt(210)

  private val codecCache = new PacketIntentCodecCache
  import codecCache.given

  // noinspection TypeAnnotation
  // format: off
  val playProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec[TeleportConfirm],
      0x01 -> ByteCodec[TabComplete],
      0x02 -> ByteCodec[ChatMessage],
      0x03 -> ByteCodec[ClientStatus],
      0x04 -> ByteCodec[ClientSettings],
      0x05 -> ByteCodec[ConfirmTransactionServerbound],
      0x06 -> ByteCodec[EnchantItem],
      0x07 -> ByteCodec[ClickWindow[VersionSpecificSlot]],
      0x08 -> ByteCodec[CloseWindow],
      0x09 -> ByteCodec[PluginMessageServerbound],
      0x0a -> ByteCodec[UseEntity_Hand],
      0x0b -> ByteCodec[KeepAliveServerbound_VarInt],
      0x0c -> ByteCodec[PlayerPosition],
      0x0d -> ByteCodec[PlayerPositionLook],
      0x0e -> ByteCodec[PlayerLook],
      0x0f -> ByteCodec[Player],
      0x10 -> ByteCodec[VehicleMove],
      0x11 -> ByteCodec[SteerBoat],
      0x12 -> ByteCodec[ClientAbilities_f32],
      0x13 -> ByteCodec[PlayerDigging],
      0x14 -> ByteCodec[PlayerAction],
      0x15 -> ByteCodec[SteerVehicle],
      0x16 -> ByteCodec[ResourcePackStatus],
      0x17 -> ByteCodec[HeldItemChange],
      0x18 -> ByteCodec[CreativeInventoryAction[VersionSpecificSlot]],
      0x19 -> ByteCodec[SetSign],
      0x1a -> ByteCodec[ArmSwing],
      0x1b -> ByteCodec[SpectateTeleport],
      0x1c -> ByteCodec[PlayerBlockPlacement_u8],
      0x1d -> ByteCodec[UseItem],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec[SpawnObject],
      0x01 -> ByteCodec[SpawnExperienceOrb],
      0x02 -> ByteCodec[SpawnGlobalEntity],
      0x03 -> ByteCodec[SpawnMob_u8],
      0x04 -> ByteCodec[SpawnPainting_String],
      0x05 -> ByteCodec[SpawnPlayer_f64],
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
      0x14 -> ByteCodec[WindowItems[VersionSpecificSlot]],
      0x15 -> ByteCodec[WindowProperty],
      0x16 -> ByteCodec[WindowSetSlot[VersionSpecificSlot]],
      0x17 -> ByteCodec[SetCooldown],
      0x18 -> ByteCodec[PluginMessageClientbound],
      0x19 -> ByteCodec[NamedSoundEffect],
      0x1a -> ByteCodec[Disconnect],
      0x1b -> ByteCodec[EntityAction],
      0x1c -> ByteCodec[Explosion],
      0x1d -> ByteCodec[ChunkUnload],
      0x1e -> ByteCodec[ChangeGameState],
      0x1f -> ByteCodec[KeepAliveClientbound_VarInt],
      0x20 -> ByteCodec[ChunkData],
      0x21 -> ByteCodec[Effect],
      0x22 -> ByteCodec[Particle_VarIntArray],
      0x23 -> ByteCodec[JoinGame_i32],
      0x24 -> ByteCodec[Maps_NoLocked],
      0x25 -> ByteCodec[EntityMove_i16],
      0x26 -> ByteCodec[EntityLookAndMove_i16],
      0x27 -> ByteCodec[EntityLook_VarInt],
      0x28 -> ByteCodec[Entity],
      0x29 -> ByteCodec[VehicleTeleport],
      0x2a -> ByteCodec[SignEditorOpen],
      0x2b -> ByteCodec[PlayerAbilities],
      0x2c -> ByteCodec[CombatEvent],
      0x2d -> ByteCodec[PlayerInfo],
      0x2e -> ByteCodec[TeleportPlayer_WithConfirm],
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
      0x3a -> ByteCodec[EntityAttach],
      0x3b -> ByteCodec[EntityVelocity],
      0x3c -> ByteCodec[EntityEquipment_VarInt[VersionSpecificSlot]],
      0x3d -> ByteCodec[SetExperience],
      0x3e -> ByteCodec[UpdateHealth],
      0x3f -> ByteCodec[ScoreboardObjective],
      0x40 -> ByteCodec[SetPassengers],
      0x41 -> ByteCodec[Teams_u8],
      0x42 -> ByteCodec[UpdateScore],
      0x43 -> ByteCodec[SpawnPosition],
      0x44 -> ByteCodec[TimeUpdate],
      0x45 -> ByteCodec[Title_notext],
      0x46 -> ByteCodec[SoundEffect],
      0x47 -> ByteCodec[PlayerListHeaderFooter],
      0x48 -> ByteCodec[CollectItem_nocount],
      0x49 -> ByteCodec[EntityTeleport_f64],
      0x4a -> ByteCodec[EntityProperties],
      0x4b -> ByteCodec[EntityEffect],
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
