package io.github.kory33.s2mctest.impl.connection.protocol.versions

import io.github.kory33.s2mctest.core.connection.codec.ByteCodec
import io.github.kory33.s2mctest.core.connection.protocol.{PacketIdBindings, Protocol}
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.VarInt
import io.github.kory33.s2mctest.impl.connection.protocol.PacketIntentCodecCache

object v1_12_2 {
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.*
  import io.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.PositionCodecBefore1_14.given
  import io.github.kory33.s2mctest.impl.connection.packets.PacketDataCompoundTypes.Slot.Upto_1_12_2 as VersionSpecificSlot

  val protocolVersion: VarInt = VarInt(340)

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
      0x0b -> ByteCodec[KeepAliveServerbound_i64],
      0x0c -> ByteCodec[Player],
      0x0d -> ByteCodec[PlayerPosition],
      0x0e -> ByteCodec[PlayerPositionLook],
      0x0f -> ByteCodec[PlayerLook],
      0x10 -> ByteCodec[VehicleMove],
      0x11 -> ByteCodec[SteerBoat],
      0x12 -> ByteCodec[CraftRecipeRequest],
      0x13 -> ByteCodec[ClientAbilities_f32],
      0x14 -> ByteCodec[PlayerDigging],
      0x15 -> ByteCodec[PlayerAction],
      0x16 -> ByteCodec[SteerVehicle],
      0x17 -> ByteCodec[CraftingBookData],
      0x18 -> ByteCodec[ResourcePackStatus],
      0x19 -> ByteCodec[AdvancementTab],
      0x1a -> ByteCodec[HeldItemChange],
      0x1b -> ByteCodec[CreativeInventoryAction[VersionSpecificSlot]],
      0x1c -> ByteCodec[SetSign],
      0x1d -> ByteCodec[ArmSwing],
      0x1e -> ByteCodec[SpectateTeleport],
      0x1f -> ByteCodec[PlayerBlockPlacement_f32],
      0x20 -> ByteCodec[UseItem],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec[SpawnObject],
      0x01 -> ByteCodec[SpawnExperienceOrb],
      0x02 -> ByteCodec[SpawnGlobalEntity],
      0x03 -> ByteCodec[SpawnMob_WithMeta],
      0x04 -> ByteCodec[SpawnPainting_VarInt],
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
      0x1f -> ByteCodec[KeepAliveClientbound_i64],
      0x20 -> ByteCodec[ChunkData],
      0x21 -> ByteCodec[Effect],
      0x22 -> ByteCodec[Particle_VarIntArray],
      0x23 -> ByteCodec[JoinGame_i32],
      0x24 -> ByteCodec[Maps_NoLocked],
      0x25 -> ByteCodec[Entity],
      0x26 -> ByteCodec[EntityMove_i16],
      0x27 -> ByteCodec[EntityLookAndMove_i16],
      0x28 -> ByteCodec[EntityLook_VarInt],
      0x29 -> ByteCodec[VehicleTeleport],
      0x2a -> ByteCodec[SignEditorOpen],
      0x2b -> ByteCodec[CraftRecipeResponse],
      0x2c -> ByteCodec[PlayerAbilities],
      0x2d -> ByteCodec[CombatEvent],
      0x2e -> ByteCodec[PlayerInfo],
      0x2f -> ByteCodec[TeleportPlayer_WithConfirm],
      0x30 -> ByteCodec[EntityUsedBed],
      0x31 -> ByteCodec[UnlockRecipes_NoSmelting],
      0x32 -> ByteCodec[EntityDestroy],
      0x33 -> ByteCodec[EntityRemoveEffect],
      0x34 -> ByteCodec[ResourcePackSend],
      0x35 -> ByteCodec[Respawn_Gamemode],
      0x36 -> ByteCodec[EntityHeadLook],
      0x37 -> ByteCodec[SelectAdvancementTab],
      0x38 -> ByteCodec[WorldBorder],
      0x39 -> ByteCodec[Camera],
      0x3a -> ByteCodec[SetCurrentHotbarSlot],
      0x3b -> ByteCodec[ScoreboardDisplay],
      0x3c -> ByteCodec[EntityMetadata],
      0x3d -> ByteCodec[EntityAttach],
      0x3e -> ByteCodec[EntityVelocity],
      0x3f -> ByteCodec[EntityEquipment_VarInt[VersionSpecificSlot]],
      0x40 -> ByteCodec[SetExperience],
      0x41 -> ByteCodec[UpdateHealth],
      0x42 -> ByteCodec[ScoreboardObjective],
      0x43 -> ByteCodec[SetPassengers],
      0x44 -> ByteCodec[Teams_u8],
      0x45 -> ByteCodec[UpdateScore],
      0x46 -> ByteCodec[SpawnPosition],
      0x47 -> ByteCodec[TimeUpdate],
      0x48 -> ByteCodec[Title],
      0x49 -> ByteCodec[SoundEffect],
      0x4a -> ByteCodec[PlayerListHeaderFooter],
      0x4b -> ByteCodec[CollectItem],
      0x4c -> ByteCodec[EntityTeleport_f64],
      0x4d -> ByteCodec[Advancements],
      0x4e -> ByteCodec[EntityProperties],
      0x4f -> ByteCodec[EntityEffect],
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
