package io.github.kory33.s2mctest.impl.connection.protocol.versions

import io.github.kory33.s2mctest.core.connection.codec.ByteCodec
import io.github.kory33.s2mctest.core.connection.protocol.{PacketIdBindings, Protocol}
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.VarInt
import io.github.kory33.s2mctest.impl.connection.protocol.{
  PacketIntentCodecCache,
  WithVersionNumber
}

object v1_15 extends WithVersionNumber {
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.*
  import io.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.PositionCodec.given
  import io.github.kory33.s2mctest.impl.connection.packets.PacketDataCompoundTypes.Slot.Upto_1_17_1 as VersionSpecificSlot

  val protocolVersion: VarInt = VarInt(573)

  private val codecCache = new PacketIntentCodecCache
  import codecCache.given

  // noinspection TypeAnnotation
  // format: off
  val playProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec[TeleportConfirm],
      0x01 -> ByteCodec[QueryBlockNBT],
      0x02 -> ByteCodec[SetDifficulty],
      0x03 -> ByteCodec[ChatMessage],
      0x04 -> ByteCodec[ClientStatus],
      0x05 -> ByteCodec[ClientSettings],
      0x06 -> ByteCodec[TabComplete],
      0x07 -> ByteCodec[ConfirmTransactionServerbound],
      0x08 -> ByteCodec[ClickWindowButton],
      0x09 -> ByteCodec[ClickWindow[VersionSpecificSlot]],
      0x0a -> ByteCodec[CloseWindow],
      0x0b -> ByteCodec[PluginMessageServerbound],
      0x0c -> ByteCodec[EditBook],
      0x0d -> ByteCodec[QueryEntityNBT],
      0x0e -> ByteCodec[UseEntity_Hand],
      0x0f -> ByteCodec[KeepAliveServerbound_i64],
      0x10 -> ByteCodec[LockDifficulty],
      0x11 -> ByteCodec[PlayerPosition],
      0x12 -> ByteCodec[PlayerPositionLook],
      0x13 -> ByteCodec[PlayerLook],
      0x14 -> ByteCodec[Player],
      0x15 -> ByteCodec[VehicleMove],
      0x16 -> ByteCodec[SteerBoat],
      0x17 -> ByteCodec[PickItem],
      0x18 -> ByteCodec[CraftRecipeRequest],
      0x19 -> ByteCodec[ClientAbilities_f32],
      0x1a -> ByteCodec[PlayerDigging],
      0x1b -> ByteCodec[PlayerAction],
      0x1c -> ByteCodec[SteerVehicle],
      0x1d -> ByteCodec[CraftingBookData],
      0x1e -> ByteCodec[NameItem],
      0x1f -> ByteCodec[ResourcePackStatus],
      0x20 -> ByteCodec[AdvancementTab],
      0x21 -> ByteCodec[SelectTrade],
      0x22 -> ByteCodec[SetBeaconEffect],
      0x23 -> ByteCodec[HeldItemChange],
      0x24 -> ByteCodec[UpdateCommandBlock],
      0x25 -> ByteCodec[UpdateCommandBlockMinecart],
      0x26 -> ByteCodec[CreativeInventoryAction[VersionSpecificSlot]],
      0x27 -> ByteCodec[UpdateJigsawBlock_Type],
      0x28 -> ByteCodec[UpdateStructureBlock],
      0x29 -> ByteCodec[SetSign],
      0x2a -> ByteCodec[ArmSwing],
      0x2b -> ByteCodec[SpectateTeleport],
      0x2c -> ByteCodec[PlayerBlockPlacement_insideblock],
      0x2d -> ByteCodec[UseItem],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec[SpawnObject_VarInt],
      0x01 -> ByteCodec[SpawnExperienceOrb],
      0x02 -> ByteCodec[SpawnGlobalEntity],
      0x03 -> ByteCodec[SpawnMob_NoMeta],
      0x04 -> ByteCodec[SpawnPainting_VarInt],
      0x05 -> ByteCodec[SpawnPlayer_f64_NoMeta],
      0x06 -> ByteCodec[Animation],
      0x07 -> ByteCodec[Statistics],
      0x08 -> ByteCodec[AcknowledgePlayerDigging],
      0x09 -> ByteCodec[BlockBreakAnimation],
      0x0a -> ByteCodec[UpdateBlockEntity],
      0x0b -> ByteCodec[BlockAction],
      0x0c -> ByteCodec[BlockChange_VarInt],
      0x0d -> ByteCodec[BossBar],
      0x0e -> ByteCodec[ServerDifficulty_Locked],
      0x0f -> ByteCodec[ServerMessage_Position],
      0x10 -> ByteCodec[MultiBlockChange_VarInt],
      0x11 -> ByteCodec[TabCompleteReply],
      0x12 -> ByteCodec[DeclareCommands],
      0x13 -> ByteCodec[ConfirmTransaction],
      0x14 -> ByteCodec[WindowClose],
      0x15 -> ByteCodec[WindowItems[VersionSpecificSlot]],
      0x16 -> ByteCodec[WindowProperty],
      0x17 -> ByteCodec[WindowSetSlot[VersionSpecificSlot]],
      0x18 -> ByteCodec[SetCooldown],
      0x19 -> ByteCodec[PluginMessageClientbound],
      0x1a -> ByteCodec[NamedSoundEffect],
      0x1b -> ByteCodec[Disconnect],
      0x1c -> ByteCodec[EntityAction],
      0x1d -> ByteCodec[Explosion],
      0x1e -> ByteCodec[ChunkUnload],
      0x1f -> ByteCodec[ChangeGameState],
      0x20 -> ByteCodec[WindowOpenHorse],
      0x21 -> ByteCodec[KeepAliveClientbound_i64],
      0x22 -> ByteCodec[ChunkData_Biomes3D],
      0x23 -> ByteCodec[Effect],
      0x24 -> ByteCodec[Particle_f64],
      0x25 -> ByteCodec[UpdateLight_NoTrust],
      0x26 -> ByteCodec[JoinGame_HashedSeed_Respawn],
      0x27 -> ByteCodec[Maps],
      0x28 -> ByteCodec[TradeList_WithRestock],
      0x29 -> ByteCodec[EntityMove_i16],
      0x2a -> ByteCodec[EntityLookAndMove_i16],
      0x2b -> ByteCodec[EntityLook_VarInt],
      0x2c -> ByteCodec[Entity],
      0x2d -> ByteCodec[VehicleTeleport],
      0x2e -> ByteCodec[OpenBook],
      0x2f -> ByteCodec[WindowOpen_VarInt],
      0x30 -> ByteCodec[SignEditorOpen],
      0x31 -> ByteCodec[CraftRecipeResponse],
      0x32 -> ByteCodec[PlayerAbilities],
      0x33 -> ByteCodec[CombatEvent],
      0x34 -> ByteCodec[PlayerInfo],
      0x35 -> ByteCodec[FacePlayer],
      0x36 -> ByteCodec[TeleportPlayer_WithConfirm],
      0x37 -> ByteCodec[UnlockRecipes_WithSmelting],
      0x38 -> ByteCodec[EntityDestroy],
      0x39 -> ByteCodec[EntityRemoveEffect],
      0x3a -> ByteCodec[ResourcePackSend],
      0x3b -> ByteCodec[Respawn_HashedSeed],
      0x3c -> ByteCodec[EntityHeadLook],
      0x3d -> ByteCodec[SelectAdvancementTab],
      0x3e -> ByteCodec[WorldBorder],
      0x3f -> ByteCodec[Camera],
      0x40 -> ByteCodec[SetCurrentHotbarSlot],
      0x41 -> ByteCodec[UpdateViewPosition],
      0x42 -> ByteCodec[UpdateViewDistance],
      0x43 -> ByteCodec[ScoreboardDisplay],
      0x44 -> ByteCodec[EntityMetadata],
      0x45 -> ByteCodec[EntityAttach],
      0x46 -> ByteCodec[EntityVelocity],
      0x47 -> ByteCodec[EntityEquipment_VarInt[VersionSpecificSlot]],
      0x48 -> ByteCodec[SetExperience],
      0x49 -> ByteCodec[UpdateHealth],
      0x4a -> ByteCodec[ScoreboardObjective],
      0x4b -> ByteCodec[SetPassengers],
      0x4c -> ByteCodec[Teams_VarInt],
      0x4d -> ByteCodec[UpdateScore],
      0x4e -> ByteCodec[SpawnPosition],
      0x4f -> ByteCodec[TimeUpdate],
      0x50 -> ByteCodec[Title],
      0x51 -> ByteCodec[EntitySoundEffect],
      0x52 -> ByteCodec[SoundEffect],
      0x53 -> ByteCodec[StopSound],
      0x54 -> ByteCodec[PlayerListHeaderFooter],
      0x55 -> ByteCodec[NBTQueryResponse],
      0x56 -> ByteCodec[CollectItem],
      0x57 -> ByteCodec[EntityTeleport_f64],
      0x58 -> ByteCodec[Advancements],
      0x59 -> ByteCodec[EntityProperties],
      0x5a -> ByteCodec[EntityEffect],
      0x5b -> ByteCodec[DeclareRecipes],
      0x5c -> ByteCodec[TagsWithEntities],
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
