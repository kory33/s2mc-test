package com.github.kory33.s2mctest.impl.connection.protocol.versions

import com.github.kory33.s2mctest.core.connection.codec.ByteCodec
import com.github.kory33.s2mctest.core.connection.protocol.{PacketIdBindings, Protocol}
import com.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.VarInt

object v1_13_2 {
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.*
  import com.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.Common.given
  import com.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.PositionCodecBefore1_14.given
  import com.github.kory33.s2mctest.impl.connection.codec.decode.macros.GenByteDecode.given

  val protocolVersion: VarInt = VarInt(404)

  // noinspection TypeAnnotation
  // format: off
  val playProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[TeleportConfirm],
      0x01 -> ByteCodec.summonPair[QueryBlockNBT],
      0x02 -> ByteCodec.summonPair[ChatMessage],
      0x03 -> ByteCodec.summonPair[ClientStatus],
      0x04 -> ByteCodec.summonPair[ClientSettings],
      0x05 -> ByteCodec.summonPair[TabComplete],
      0x06 -> ByteCodec.summonPair[ConfirmTransactionServerbound],
      0x07 -> ByteCodec.summonPair[EnchantItem],
      0x08 -> ByteCodec.summonPair[ClickWindow],
      0x09 -> ByteCodec.summonPair[CloseWindow],
      0x0a -> ByteCodec.summonPair[PluginMessageServerbound],
      0x0b -> ByteCodec.summonPair[EditBook],
      0x0c -> ByteCodec.summonPair[QueryEntityNBT],
      0x0d -> ByteCodec.summonPair[UseEntity_Hand],
      0x0e -> ByteCodec.summonPair[KeepAliveServerbound_i64],
      0x0f -> ByteCodec.summonPair[Player],
      0x10 -> ByteCodec.summonPair[PlayerPosition],
      0x11 -> ByteCodec.summonPair[PlayerPositionLook],
      0x12 -> ByteCodec.summonPair[PlayerLook],
      0x13 -> ByteCodec.summonPair[VehicleMove],
      0x14 -> ByteCodec.summonPair[SteerBoat],
      0x15 -> ByteCodec.summonPair[PickItem],
      0x16 -> ByteCodec.summonPair[CraftRecipeRequest],
      0x17 -> ByteCodec.summonPair[ClientAbilities_f32],
      0x18 -> ByteCodec.summonPair[PlayerDigging],
      0x19 -> ByteCodec.summonPair[PlayerAction],
      0x1a -> ByteCodec.summonPair[SteerVehicle],
      0x1b -> ByteCodec.summonPair[CraftingBookData],
      0x1c -> ByteCodec.summonPair[NameItem],
      0x1d -> ByteCodec.summonPair[ResourcePackStatus],
      0x1e -> ByteCodec.summonPair[AdvancementTab],
      0x1f -> ByteCodec.summonPair[SelectTrade],
      0x20 -> ByteCodec.summonPair[SetBeaconEffect],
      0x21 -> ByteCodec.summonPair[HeldItemChange],
      0x22 -> ByteCodec.summonPair[UpdateCommandBlock],
      0x23 -> ByteCodec.summonPair[UpdateCommandBlockMinecart],
      0x24 -> ByteCodec.summonPair[CreativeInventoryAction],
      0x25 -> ByteCodec.summonPair[UpdateStructureBlock],
      0x26 -> ByteCodec.summonPair[SetSign],
      0x27 -> ByteCodec.summonPair[ArmSwing],
      0x28 -> ByteCodec.summonPair[SpectateTeleport],
      0x29 -> ByteCodec.summonPair[PlayerBlockPlacement_f32],
      0x2a -> ByteCodec.summonPair[UseItem],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[SpawnObject],
      0x01 -> ByteCodec.summonPair[SpawnExperienceOrb],
      0x02 -> ByteCodec.summonPair[SpawnGlobalEntity],
      0x03 -> ByteCodec.summonPair[SpawnMob_WithMeta],
      0x04 -> ByteCodec.summonPair[SpawnPainting_VarInt],
      0x05 -> ByteCodec.summonPair[SpawnPlayer_f64],
      0x06 -> ByteCodec.summonPair[Animation],
      0x07 -> ByteCodec.summonPair[Statistics],
      0x08 -> ByteCodec.summonPair[BlockBreakAnimation],
      0x09 -> ByteCodec.summonPair[UpdateBlockEntity],
      0x0a -> ByteCodec.summonPair[BlockAction],
      0x0b -> ByteCodec.summonPair[BlockChange_VarInt],
      0x0c -> ByteCodec.summonPair[BossBar],
      0x0d -> ByteCodec.summonPair[ServerDifficulty],
      0x0e -> ByteCodec.summonPair[ServerMessage_Position],
      0x0f -> ByteCodec.summonPair[MultiBlockChange_VarInt],
      0x10 -> ByteCodec.summonPair[TabCompleteReply],
      0x11 -> ByteCodec.summonPair[DeclareCommands],
      0x12 -> ByteCodec.summonPair[ConfirmTransaction],
      0x13 -> ByteCodec.summonPair[WindowClose],
      0x14 -> ByteCodec.summonPair[WindowOpen],
      0x15 -> ByteCodec.summonPair[WindowItems],
      0x16 -> ByteCodec.summonPair[WindowProperty],
      0x17 -> ByteCodec.summonPair[WindowSetSlot],
      0x18 -> ByteCodec.summonPair[SetCooldown],
      0x19 -> ByteCodec.summonPair[PluginMessageClientbound],
      0x1a -> ByteCodec.summonPair[NamedSoundEffect],
      0x1b -> ByteCodec.summonPair[Disconnect],
      0x1c -> ByteCodec.summonPair[EntityAction],
      0x1d -> ByteCodec.summonPair[NBTQueryResponse],
      0x1e -> ByteCodec.summonPair[Explosion],
      0x1f -> ByteCodec.summonPair[ChunkUnload],
      0x20 -> ByteCodec.summonPair[ChangeGameState],
      0x21 -> ByteCodec.summonPair[KeepAliveClientbound_i64],
      0x22 -> ByteCodec.summonPair[ChunkData],
      0x23 -> ByteCodec.summonPair[Effect],
      0x24 -> ByteCodec.summonPair[Particle_Data13],
      0x25 -> ByteCodec.summonPair[JoinGame_i32],
      0x26 -> ByteCodec.summonPair[Maps_NoLocked],
      0x27 -> ByteCodec.summonPair[Entity],
      0x28 -> ByteCodec.summonPair[EntityMove_i16],
      0x29 -> ByteCodec.summonPair[EntityLookAndMove_i16],
      0x2a -> ByteCodec.summonPair[EntityLook_VarInt],
      0x2b -> ByteCodec.summonPair[VehicleTeleport],
      0x2c -> ByteCodec.summonPair[SignEditorOpen],
      0x2d -> ByteCodec.summonPair[CraftRecipeResponse],
      0x2e -> ByteCodec.summonPair[PlayerAbilities],
      0x2f -> ByteCodec.summonPair[CombatEvent],
      0x30 -> ByteCodec.summonPair[PlayerInfo],
      0x31 -> ByteCodec.summonPair[FacePlayer],
      0x32 -> ByteCodec.summonPair[TeleportPlayer_WithConfirm],
      0x33 -> ByteCodec.summonPair[EntityUsedBed],
      0x34 -> ByteCodec.summonPair[UnlockRecipes_WithSmelting],
      0x35 -> ByteCodec.summonPair[EntityDestroy],
      0x36 -> ByteCodec.summonPair[EntityRemoveEffect],
      0x37 -> ByteCodec.summonPair[ResourcePackSend],
      0x38 -> ByteCodec.summonPair[Respawn_Gamemode],
      0x39 -> ByteCodec.summonPair[EntityHeadLook],
      0x3a -> ByteCodec.summonPair[SelectAdvancementTab],
      0x3b -> ByteCodec.summonPair[WorldBorder],
      0x3c -> ByteCodec.summonPair[Camera],
      0x3d -> ByteCodec.summonPair[SetCurrentHotbarSlot],
      0x3e -> ByteCodec.summonPair[ScoreboardDisplay],
      0x3f -> ByteCodec.summonPair[EntityMetadata],
      0x40 -> ByteCodec.summonPair[EntityAttach],
      0x41 -> ByteCodec.summonPair[EntityVelocity],
      0x42 -> ByteCodec.summonPair[EntityEquipment_VarInt],
      0x43 -> ByteCodec.summonPair[SetExperience],
      0x44 -> ByteCodec.summonPair[UpdateHealth],
      0x45 -> ByteCodec.summonPair[ScoreboardObjective],
      0x46 -> ByteCodec.summonPair[SetPassengers],
      0x47 -> ByteCodec.summonPair[Teams_VarInt],
      0x48 -> ByteCodec.summonPair[UpdateScore],
      0x49 -> ByteCodec.summonPair[SpawnPosition],
      0x4a -> ByteCodec.summonPair[TimeUpdate],
      0x4c -> ByteCodec.summonPair[StopSound],
      0x4d -> ByteCodec.summonPair[SoundEffect],
      0x4e -> ByteCodec.summonPair[PlayerListHeaderFooter],
      0x4f -> ByteCodec.summonPair[CollectItem],
      0x50 -> ByteCodec.summonPair[EntityTeleport_f64],
      0x51 -> ByteCodec.summonPair[Advancements],
      0x52 -> ByteCodec.summonPair[EntityProperties],
      0x53 -> ByteCodec.summonPair[EntityEffect],
      0x54 -> ByteCodec.summonPair[DeclareRecipes],
      0x55 -> ByteCodec.summonPair[Tags],
    ))
  )
  // format: on

  // noinspection TypeAnnotation
  // format: off
  val loginProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[LoginStart],
      0x01 -> ByteCodec.summonPair[EncryptionResponse],
      0x02 -> ByteCodec.summonPair[LoginPluginResponse],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[LoginDisconnect],
      0x01 -> ByteCodec.summonPair[EncryptionRequest],
      0x02 -> ByteCodec.summonPair[LoginSuccess_String],
      0x03 -> ByteCodec.summonPair[SetInitialCompression],
      0x04 -> ByteCodec.summonPair[LoginPluginRequest],
    ))
  )
  // format: on
}
