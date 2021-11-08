package io.github.kory33.s2mctest.impl.connection.protocol.versions

import io.github.kory33.s2mctest.core.connection.codec.ByteCodec
import io.github.kory33.s2mctest.core.connection.protocol.{PacketIdBindings, Protocol}
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.VarInt
import io.github.kory33.s2mctest.impl.connection.protocol.PacketIntentCodecCache

object v1_13_2 {
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.*
  import io.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.PositionCodecBefore1_14.given
  import io.github.kory33.s2mctest.impl.connection.packets.PacketDataCompoundTypes.Slot.Upto_1_17_1 as VersionSpecificSlot

  val protocolVersion: VarInt = VarInt(404)

  private val codecCache = new PacketIntentCodecCache
  import codecCache.given

  // noinspection TypeAnnotation
  // format: off
  val playProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec[TeleportConfirm],
      0x01 -> ByteCodec[QueryBlockNBT],
      0x02 -> ByteCodec[ChatMessage],
      0x03 -> ByteCodec[ClientStatus],
      0x04 -> ByteCodec[ClientSettings],
      0x05 -> ByteCodec[TabComplete],
      0x06 -> ByteCodec[ConfirmTransactionServerbound],
      0x07 -> ByteCodec[EnchantItem],
      0x08 -> ByteCodec[ClickWindow[VersionSpecificSlot]],
      0x09 -> ByteCodec[CloseWindow],
      0x0a -> ByteCodec[PluginMessageServerbound],
      0x0b -> ByteCodec[EditBook],
      0x0c -> ByteCodec[QueryEntityNBT],
      0x0d -> ByteCodec[UseEntity_Hand],
      0x0e -> ByteCodec[KeepAliveServerbound_i64],
      0x0f -> ByteCodec[Player],
      0x10 -> ByteCodec[PlayerPosition],
      0x11 -> ByteCodec[PlayerPositionLook],
      0x12 -> ByteCodec[PlayerLook],
      0x13 -> ByteCodec[VehicleMove],
      0x14 -> ByteCodec[SteerBoat],
      0x15 -> ByteCodec[PickItem],
      0x16 -> ByteCodec[CraftRecipeRequest],
      0x17 -> ByteCodec[ClientAbilities_f32],
      0x18 -> ByteCodec[PlayerDigging],
      0x19 -> ByteCodec[PlayerAction],
      0x1a -> ByteCodec[SteerVehicle],
      0x1b -> ByteCodec[CraftingBookData],
      0x1c -> ByteCodec[NameItem],
      0x1d -> ByteCodec[ResourcePackStatus],
      0x1e -> ByteCodec[AdvancementTab],
      0x1f -> ByteCodec[SelectTrade],
      0x20 -> ByteCodec[SetBeaconEffect],
      0x21 -> ByteCodec[HeldItemChange],
      0x22 -> ByteCodec[UpdateCommandBlock],
      0x23 -> ByteCodec[UpdateCommandBlockMinecart],
      0x24 -> ByteCodec[CreativeInventoryAction[VersionSpecificSlot]],
      0x25 -> ByteCodec[UpdateStructureBlock],
      0x26 -> ByteCodec[SetSign],
      0x27 -> ByteCodec[ArmSwing],
      0x28 -> ByteCodec[SpectateTeleport],
      0x29 -> ByteCodec[PlayerBlockPlacement_f32],
      0x2a -> ByteCodec[UseItem],
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
      0x0e -> ByteCodec[ServerMessage_Position],
      0x0f -> ByteCodec[MultiBlockChange_VarInt],
      0x10 -> ByteCodec[TabCompleteReply],
      0x11 -> ByteCodec[DeclareCommands],
      0x12 -> ByteCodec[ConfirmTransaction],
      0x13 -> ByteCodec[WindowClose],
      0x14 -> ByteCodec[WindowOpen],
      0x15 -> ByteCodec[WindowItems[VersionSpecificSlot]],
      0x16 -> ByteCodec[WindowProperty],
      0x17 -> ByteCodec[WindowSetSlot[VersionSpecificSlot]],
      0x18 -> ByteCodec[SetCooldown],
      0x19 -> ByteCodec[PluginMessageClientbound],
      0x1a -> ByteCodec[NamedSoundEffect],
      0x1b -> ByteCodec[Disconnect],
      0x1c -> ByteCodec[EntityAction],
      0x1d -> ByteCodec[NBTQueryResponse],
      0x1e -> ByteCodec[Explosion],
      0x1f -> ByteCodec[ChunkUnload],
      0x20 -> ByteCodec[ChangeGameState],
      0x21 -> ByteCodec[KeepAliveClientbound_i64],
      0x22 -> ByteCodec[ChunkData],
      0x23 -> ByteCodec[Effect],
      0x24 -> ByteCodec[Particle_Data13],
      0x25 -> ByteCodec[JoinGame_i32],
      0x26 -> ByteCodec[Maps_NoLocked],
      0x27 -> ByteCodec[Entity],
      0x28 -> ByteCodec[EntityMove_i16],
      0x29 -> ByteCodec[EntityLookAndMove_i16],
      0x2a -> ByteCodec[EntityLook_VarInt],
      0x2b -> ByteCodec[VehicleTeleport],
      0x2c -> ByteCodec[SignEditorOpen],
      0x2d -> ByteCodec[CraftRecipeResponse],
      0x2e -> ByteCodec[PlayerAbilities],
      0x2f -> ByteCodec[CombatEvent],
      0x30 -> ByteCodec[PlayerInfo],
      0x31 -> ByteCodec[FacePlayer],
      0x32 -> ByteCodec[TeleportPlayer_WithConfirm],
      0x33 -> ByteCodec[EntityUsedBed],
      0x34 -> ByteCodec[UnlockRecipes_WithSmelting],
      0x35 -> ByteCodec[EntityDestroy],
      0x36 -> ByteCodec[EntityRemoveEffect],
      0x37 -> ByteCodec[ResourcePackSend],
      0x38 -> ByteCodec[Respawn_Gamemode],
      0x39 -> ByteCodec[EntityHeadLook],
      0x3a -> ByteCodec[SelectAdvancementTab],
      0x3b -> ByteCodec[WorldBorder],
      0x3c -> ByteCodec[Camera],
      0x3d -> ByteCodec[SetCurrentHotbarSlot],
      0x3e -> ByteCodec[ScoreboardDisplay],
      0x3f -> ByteCodec[EntityMetadata],
      0x40 -> ByteCodec[EntityAttach],
      0x41 -> ByteCodec[EntityVelocity],
      0x42 -> ByteCodec[EntityEquipment_VarInt[VersionSpecificSlot]],
      0x43 -> ByteCodec[SetExperience],
      0x44 -> ByteCodec[UpdateHealth],
      0x45 -> ByteCodec[ScoreboardObjective],
      0x46 -> ByteCodec[SetPassengers],
      0x47 -> ByteCodec[Teams_VarInt],
      0x48 -> ByteCodec[UpdateScore],
      0x49 -> ByteCodec[SpawnPosition],
      0x4a -> ByteCodec[TimeUpdate],
      0x4c -> ByteCodec[StopSound],
      0x4d -> ByteCodec[SoundEffect],
      0x4e -> ByteCodec[PlayerListHeaderFooter],
      0x4f -> ByteCodec[CollectItem],
      0x50 -> ByteCodec[EntityTeleport_f64],
      0x51 -> ByteCodec[Advancements],
      0x52 -> ByteCodec[EntityProperties],
      0x53 -> ByteCodec[EntityEffect],
      0x54 -> ByteCodec[DeclareRecipes],
      0x55 -> ByteCodec[Tags],
    ))
  )
  // format: on

  // noinspection TypeAnnotation
  // format: off
  val loginProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec[LoginStart],
      0x01 -> ByteCodec[EncryptionResponse],
      0x02 -> ByteCodec[LoginPluginResponse],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec[LoginDisconnect],
      0x01 -> ByteCodec[EncryptionRequest],
      0x02 -> ByteCodec[LoginSuccess_String],
      0x03 -> ByteCodec[SetInitialCompression],
      0x04 -> ByteCodec[LoginPluginRequest],
    ))
  )
  // format: on
}
