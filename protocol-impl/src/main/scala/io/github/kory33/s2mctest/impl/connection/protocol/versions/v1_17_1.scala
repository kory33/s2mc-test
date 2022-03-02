package io.github.kory33.s2mctest.impl.connection.protocol.versions

import io.github.kory33.s2mctest.core.connection.codec.ByteCodec
import io.github.kory33.s2mctest.core.connection.protocol.{PacketIdBindings, Protocol}
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.VarInt
import io.github.kory33.s2mctest.impl.connection.protocol.PacketIntentCodecCache

object v1_17_1 {
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.*
  import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.*
  import io.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.PositionCodec.given
  import io.github.kory33.s2mctest.impl.connection.packets.PacketDataCompoundTypes.Slot.Upto_1_17_1 as VersionSpecificSlot

  val protocolVersion: VarInt = VarInt(756)

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
      0x07 -> ByteCodec[ClickWindowButton],
      0x08 -> ByteCodec[ClickWindow[VersionSpecificSlot]],
      0x09 -> ByteCodec[CloseWindow],
      0x0a -> ByteCodec[PluginMessageServerbound],
      0x0b -> ByteCodec[EditBook],
      0x0c -> ByteCodec[QueryEntityNBT],
      0x0d -> ByteCodec[UseEntity_Sneakflag],
      0x0e -> ByteCodec[GenerateStructure],
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
      0x19 -> ByteCodec[ClientAbilities_u8],
      0x1a -> ByteCodec[PlayerDigging],
      0x1b -> ByteCodec[PlayerAction],
      0x1c -> ByteCodec[SteerVehicle],
      0x1d -> ByteCodec[Pong],
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
      0x28 -> ByteCodec[CreativeInventoryAction[VersionSpecificSlot]],
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
      0x05 -> ByteCodec[SculkVibrationSignal],
      0x06 -> ByteCodec[Animation],
      0x07 -> ByteCodec[Statistics],
      0x08 -> ByteCodec[AcknowledgePlayerDigging],
      0x09 -> ByteCodec[BlockBreakAnimation],
      0x0a -> ByteCodec[UpdateBlockEntity],
      0x0b -> ByteCodec[BlockAction],
      0x0c -> ByteCodec[BlockChange_VarInt],
      0x0d -> ByteCodec[BossBar],
      0x0e -> ByteCodec[ServerDifficulty_Locked],
      0x0f -> ByteCodec[ServerMessage_Sender],
      0x10 -> ByteCodec[ClearTitle],
      0x11 -> ByteCodec[TabCompleteReply],
      0x12 -> ByteCodec[DeclareCommands],
      0x13 -> ByteCodec[WindowClose],
      0x14 -> ByteCodec[WindowItems_withState[VersionSpecificSlot]],
      0x15 -> ByteCodec[WindowProperty],
      0x16 -> ByteCodec[SetSlot[VersionSpecificSlot]],
      0x17 -> ByteCodec[SetCooldown],
      0x18 -> ByteCodec[PluginMessageClientbound],
      0x19 -> ByteCodec[NamedSoundEffect],
      0x1a -> ByteCodec[Disconnect],
      0x1b -> ByteCodec[EntityAction],
      0x1c -> ByteCodec[Explosion],
      0x1d -> ByteCodec[ChunkUnload],
      0x1e -> ByteCodec[ChangeGameState],
      0x1f -> ByteCodec[WindowOpenHorse],
      0x20 -> ByteCodec[WorldBorderInitialize],
      0x21 -> ByteCodec[KeepAliveClientbound_i64],
      0x22 -> ByteCodec[ChunkData_withBlockEntity],
      0x23 -> ByteCodec[Effect],
      0x24 -> ByteCodec[Particle_f64],
      0x25 -> ByteCodec[UpdateLight_WithTrust],
      0x26 -> ByteCodec[JoinGame_WorldNames_IsHard],
      0x27 -> ByteCodec[Maps],
      0x28 -> ByteCodec[TradeList_WithRestock],
      0x29 -> ByteCodec[EntityMove_i16],
      0x2a -> ByteCodec[EntityLookAndMove_i16],
      0x2b -> ByteCodec[EntityLook_VarInt],
      0x2c -> ByteCodec[VehicleTeleport],
      0x2d -> ByteCodec[OpenBook],
      0x2e -> ByteCodec[WindowOpen_VarInt],
      0x2f -> ByteCodec[SignEditorOpen],
      0x30 -> ByteCodec[Ping],
      0x31 -> ByteCodec[CraftRecipeResponse],
      0x32 -> ByteCodec[PlayerAbilities],
      0x33 -> ByteCodec[EndCombatEvent],
      0x34 -> ByteCodec[EnterCombatEvent],
      0x35 -> ByteCodec[DeathCombatEvent],
      0x36 -> ByteCodec[PlayerInfo],
      0x37 -> ByteCodec[FacePlayer],
      0x38 -> ByteCodec[TeleportPlayer_WithDismount],
      0x39 -> ByteCodec[UnlockRecipes_WithBlastSmoker],
      0x3a -> ByteCodec[EntityDestroy],
      0x3b -> ByteCodec[EntityRemoveEffect],
      0x3c -> ByteCodec[ResourcePackSend],
      0x3d -> ByteCodec[Respawn_NBT],
      0x3e -> ByteCodec[EntityHeadLook],
      0x3f -> ByteCodec[MultiBlockChange_Packed],
      0x40 -> ByteCodec[SelectAdvancementTab],
      0x41 -> ByteCodec[ActionBar],
      0x42 -> ByteCodec[WorldBorderCenter],
      0x43 -> ByteCodec[WorldBorderLerpSize],
      0x44 -> ByteCodec[WorldBorderSize],
      0x45 -> ByteCodec[WorldBorderWarningDelay],
      0x46 -> ByteCodec[WorldBorderWarningReach],
      0x47 -> ByteCodec[Camera],
      0x48 -> ByteCodec[SetCurrentHotbarSlot],
      0x49 -> ByteCodec[UpdateViewPosition],
      0x4a -> ByteCodec[UpdateViewDistance],
      0x4b -> ByteCodec[SpawnPosition],
      0x4c -> ByteCodec[ScoreboardDisplay],
      0x4d -> ByteCodec[EntityMetadata],
      0x4e -> ByteCodec[EntityAttach],
      0x4f -> ByteCodec[EntityVelocity],
      0x50 -> ByteCodec[EntityEquipment_Array],
      0x51 -> ByteCodec[SetExperience],
      0x52 -> ByteCodec[UpdateHealth],
      0x53 -> ByteCodec[ScoreboardObjective],
      0x54 -> ByteCodec[SetPassengers],
      0x55 -> ByteCodec[Teams_VarInt],
      0x56 -> ByteCodec[UpdateScore],
      0x57 -> ByteCodec[SubTitle],
      0x58 -> ByteCodec[TimeUpdate],
      0x59 -> ByteCodec[Title_onlytext],
      0x5a -> ByteCodec[TitleFade],
      0x5b -> ByteCodec[EntitySoundEffect],
      0x5c -> ByteCodec[SoundEffect],
      0x5d -> ByteCodec[StopSound],
      0x5e -> ByteCodec[PlayerListHeaderFooter],
      0x5f -> ByteCodec[NBTQueryResponse],
      0x60 -> ByteCodec[CollectItem],
      0x61 -> ByteCodec[EntityTeleport_f64],
      0x62 -> ByteCodec[Advancements],
      0x63 -> ByteCodec[EntityProperties],
      0x64 -> ByteCodec[EntityEffect],
      0x65 -> ByteCodec[DeclareRecipes],
      0x66 -> ByteCodec[TagsWithTypes],
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
