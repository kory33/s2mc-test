package io.github.kory33.s2mctest.impl.connection.protocol.versions

import io.github.kory33.s2mctest.core.connection.codec.ByteCodec
import io.github.kory33.s2mctest.core.connection.protocol.{PacketIdBindings, Protocol}
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.VarInt
import io.github.kory33.s2mctest.impl.connection.protocol.{
  PacketIntentCodecCache,
  WithVersionNumber
}

object v1_16_4 extends WithVersionNumber {
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.*
  import io.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.PositionCodec.given

  val protocolVersion: VarInt = VarInt(754)

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
      0x09 -> ByteCodec[ClickWindow],
      0x0a -> ByteCodec[CloseWindow],
      0x0b -> ByteCodec[PluginMessageServerbound],
      0x0c -> ByteCodec[EditBook],
      0x0d -> ByteCodec[QueryEntityNBT],
      0x0e -> ByteCodec[UseEntity_Sneakflag],
      0x0f -> ByteCodec[GenerateStructure],
      0x10 -> ByteCodec[KeepAliveServerbound_i64],
      0x11 -> ByteCodec[LockDifficulty],
      0x12 -> ByteCodec[PlayerPosition],
      0x13 -> ByteCodec[PlayerPositionLook],
      0x14 -> ByteCodec[PlayerLook],
      0x15 -> ByteCodec[Player],
      0x16 -> ByteCodec[VehicleMove],
      0x17 -> ByteCodec[SteerBoat],
      0x18 -> ByteCodec[PickItem],
      0x19 -> ByteCodec[CraftRecipeRequest],
      0x1a -> ByteCodec[ClientAbilities_u8],
      0x1b -> ByteCodec[PlayerDigging],
      0x1c -> ByteCodec[PlayerAction],
      0x1d -> ByteCodec[SteerVehicle],
      0x1e -> ByteCodec[SetDisplayedRecipe],
      0x1f -> ByteCodec[SetRecipeBookState],
      0x20 -> ByteCodec[NameItem],
      0x21 -> ByteCodec[ResourcePackStatus],
      0x22 -> ByteCodec[AdvancementTab],
      0x23 -> ByteCodec[SelectTrade],
      0x24 -> ByteCodec[SetBeaconEffect],
      0x25 -> ByteCodec[HeldItemChange],
      0x26 -> ByteCodec[UpdateCommandBlock],
      0x27 -> ByteCodec[UpdateCommandBlockMinecart],
      0x28 -> ByteCodec[CreativeInventoryAction],
      0x29 -> ByteCodec[UpdateJigsawBlock_Joint],
      0x2a -> ByteCodec[UpdateStructureBlock],
      0x2b -> ByteCodec[SetSign],
      0x2c -> ByteCodec[ArmSwing],
      0x2d -> ByteCodec[SpectateTeleport],
      0x2e -> ByteCodec[PlayerBlockPlacement_insideblock],
      0x2f -> ByteCodec[UseItem],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec[SpawnObject_VarInt],
      0x01 -> ByteCodec[SpawnExperienceOrb],
      0x02 -> ByteCodec[SpawnMob_NoMeta],
      0x03 -> ByteCodec[SpawnPainting_VarInt],
      0x04 -> ByteCodec[SpawnPlayer_f64_NoMeta],
      0x05 -> ByteCodec[Animation],
      0x06 -> ByteCodec[Statistics],
      0x07 -> ByteCodec[AcknowledgePlayerDigging],
      0x08 -> ByteCodec[BlockBreakAnimation],
      0x09 -> ByteCodec[UpdateBlockEntity],
      0x0a -> ByteCodec[BlockAction],
      0x0b -> ByteCodec[BlockChange_VarInt],
      0x0c -> ByteCodec[BossBar],
      0x0d -> ByteCodec[ServerDifficulty_Locked],
      0x0e -> ByteCodec[ServerMessage_Sender],
      0x0f -> ByteCodec[TabCompleteReply],
      0x10 -> ByteCodec[DeclareCommands],
      0x11 -> ByteCodec[ConfirmTransaction],
      0x12 -> ByteCodec[WindowClose],
      0x13 -> ByteCodec[WindowItems],
      0x14 -> ByteCodec[WindowProperty],
      0x15 -> ByteCodec[WindowSetSlot],
      0x16 -> ByteCodec[SetCooldown],
      0x17 -> ByteCodec[PluginMessageClientbound],
      0x18 -> ByteCodec[NamedSoundEffect],
      0x19 -> ByteCodec[Disconnect],
      0x1a -> ByteCodec[EntityAction],
      0x1b -> ByteCodec[Explosion],
      0x1c -> ByteCodec[ChunkUnload],
      0x1d -> ByteCodec[ChangeGameState],
      0x1e -> ByteCodec[WindowOpenHorse],
      0x1f -> ByteCodec[KeepAliveClientbound_i64],
      0x20 -> ByteCodec[ChunkData_Biomes3D_VarInt],
      0x21 -> ByteCodec[Effect],
      0x22 -> ByteCodec[Particle_f64],
      0x23 -> ByteCodec[UpdateLight_WithTrust],
      0x24 -> ByteCodec[JoinGame_WorldNames_IsHard],
      0x25 -> ByteCodec[Maps],
      0x26 -> ByteCodec[TradeList_WithRestock],
      0x27 -> ByteCodec[EntityMove_i16],
      0x28 -> ByteCodec[EntityLookAndMove_i16],
      0x29 -> ByteCodec[EntityLook_VarInt],
      0x2a -> ByteCodec[Entity],
      0x2b -> ByteCodec[VehicleTeleport],
      0x2c -> ByteCodec[OpenBook],
      0x2d -> ByteCodec[WindowOpen_VarInt],
      0x2e -> ByteCodec[SignEditorOpen],
      0x2f -> ByteCodec[CraftRecipeResponse],
      0x30 -> ByteCodec[PlayerAbilities],
      0x31 -> ByteCodec[CombatEvent],
      0x32 -> ByteCodec[PlayerInfo],
      0x33 -> ByteCodec[FacePlayer],
      0x34 -> ByteCodec[TeleportPlayer_WithConfirm],
      0x35 -> ByteCodec[UnlockRecipes_WithBlastSmoker],
      0x36 -> ByteCodec[EntityDestroy],
      0x37 -> ByteCodec[EntityRemoveEffect],
      0x38 -> ByteCodec[ResourcePackSend],
      0x39 -> ByteCodec[Respawn_NBT],
      0x3a -> ByteCodec[EntityHeadLook],
      0x3b -> ByteCodec[MultiBlockChange_Packed],
      0x3c -> ByteCodec[SelectAdvancementTab],
      0x3d -> ByteCodec[WorldBorder],
      0x3e -> ByteCodec[Camera],
      0x3f -> ByteCodec[SetCurrentHotbarSlot],
      0x40 -> ByteCodec[UpdateViewPosition],
      0x41 -> ByteCodec[UpdateViewDistance],
      0x42 -> ByteCodec[SpawnPosition],
      0x43 -> ByteCodec[ScoreboardDisplay],
      0x44 -> ByteCodec[EntityMetadata],
      0x45 -> ByteCodec[EntityAttach],
      0x46 -> ByteCodec[EntityVelocity],
      0x47 -> ByteCodec[EntityEquipment_Array],
      0x48 -> ByteCodec[SetExperience],
      0x49 -> ByteCodec[UpdateHealth],
      0x4a -> ByteCodec[ScoreboardObjective],
      0x4b -> ByteCodec[SetPassengers],
      0x4c -> ByteCodec[Teams_VarInt],
      0x4d -> ByteCodec[UpdateScore],
      0x4e -> ByteCodec[TimeUpdate],
      0x4f -> ByteCodec[Title],
      0x50 -> ByteCodec[EntitySoundEffect],
      0x51 -> ByteCodec[SoundEffect],
      0x52 -> ByteCodec[StopSound],
      0x53 -> ByteCodec[PlayerListHeaderFooter],
      0x54 -> ByteCodec[NBTQueryResponse],
      0x55 -> ByteCodec[CollectItem],
      0x56 -> ByteCodec[EntityTeleport_f64],
      0x57 -> ByteCodec[Advancements],
      0x58 -> ByteCodec[EntityProperties],
      0x59 -> ByteCodec[EntityEffect],
      0x5a -> ByteCodec[DeclareRecipes],
      0x5b -> ByteCodec[TagsWithEntities],
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
      0x02 -> ByteCodec[LoginSuccess_UUID],
      0x03 -> ByteCodec[SetInitialCompression],
      0x04 -> ByteCodec[LoginPluginRequest],
    ))
  )
  // format: on
}
