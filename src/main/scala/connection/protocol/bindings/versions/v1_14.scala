package com.github.kory33.s2mctest
package com.github.kory33.s2mctest.connection.protocol.bindings.versions

import connection.protocol.bindings.{Protocol, PacketIdBindings}
import connection.protocol.codec.ByteCodec
import connection.protocol.codec.ByteCodecs.Common.given
import connection.protocol.codec.macros.GenByteDecode.given
import connection.protocol.packets.PacketIntent
import connection.protocol.packets.PacketIntent.Handshaking.ServerBound.*
import connection.protocol.packets.PacketIntent.Login.ClientBound.*
import connection.protocol.packets.PacketIntent.Login.ServerBound.*
import connection.protocol.packets.PacketIntent.Play.ClientBound.*
import connection.protocol.packets.PacketIntent.Play.ServerBound.*
import connection.protocol.packets.PacketIntent.Status.ClientBound.*
import connection.protocol.packets.PacketIntent.Status.ServerBound.*

object v1_14 {
  val playProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summon[TeleportConfirm],
      0x01 -> ByteCodec.summon[QueryBlockNBT],
      0x02 -> ByteCodec.summon[SetDifficulty],
      0x03 -> ByteCodec.summon[ChatMessage],
      0x04 -> ByteCodec.summon[ClientStatus],
      0x05 -> ByteCodec.summon[ClientSettings],
      0x06 -> ByteCodec.summon[TabComplete],
      0x07 -> ByteCodec.summon[ConfirmTransactionServerbound],
      0x08 -> ByteCodec.summon[ClickWindowButton],
      0x09 -> ByteCodec.summon[ClickWindow],
      0x0a -> ByteCodec.summon[CloseWindow],
      0x0b -> ByteCodec.summon[PluginMessageServerbound],
      0x0c -> ByteCodec.summon[EditBook],
      0x0d -> ByteCodec.summon[QueryEntityNBT],
      0x0e -> ByteCodec.summon[UseEntity_Hand],
      0x0f -> ByteCodec.summon[KeepAliveServerbound_i64],
      0x10 -> ByteCodec.summon[LockDifficulty],
      0x11 -> ByteCodec.summon[PlayerPosition],
      0x12 -> ByteCodec.summon[PlayerPositionLook],
      0x13 -> ByteCodec.summon[PlayerLook],
      0x14 -> ByteCodec.summon[Player],
      0x15 -> ByteCodec.summon[VehicleMove],
      0x16 -> ByteCodec.summon[SteerBoat],
      0x17 -> ByteCodec.summon[PickItem],
      0x18 -> ByteCodec.summon[CraftRecipeRequest],
      0x19 -> ByteCodec.summon[ClientAbilities_f32],
      0x1a -> ByteCodec.summon[PlayerDigging],
      0x1b -> ByteCodec.summon[PlayerAction],
      0x1c -> ByteCodec.summon[SteerVehicle],
      0x1d -> ByteCodec.summon[CraftingBookData],
      0x1e -> ByteCodec.summon[NameItem],
      0x1f -> ByteCodec.summon[ResourcePackStatus],
      0x20 -> ByteCodec.summon[AdvancementTab],
      0x21 -> ByteCodec.summon[SelectTrade],
      0x22 -> ByteCodec.summon[SetBeaconEffect],
      0x23 -> ByteCodec.summon[HeldItemChange],
      0x24 -> ByteCodec.summon[UpdateCommandBlock],
      0x25 -> ByteCodec.summon[UpdateCommandBlockMinecart],
      0x26 -> ByteCodec.summon[CreativeInventoryAction],
      0x27 -> ByteCodec.summon[UpdateJigsawBlock_Type],
      0x28 -> ByteCodec.summon[UpdateStructureBlock],
      0x29 -> ByteCodec.summon[SetSign],
      0x2a -> ByteCodec.summon[ArmSwing],
      0x2b -> ByteCodec.summon[SpectateTeleport],
      0x2c -> ByteCodec.summon[PlayerBlockPlacement_insideblock],
      0x2d -> ByteCodec.summon[UseItem],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summon[SpawnObject_VarInt],
      0x01 -> ByteCodec.summon[SpawnExperienceOrb],
      0x02 -> ByteCodec.summon[SpawnGlobalEntity],
      0x03 -> ByteCodec.summon[SpawnMob_WithMeta],
      0x04 -> ByteCodec.summon[SpawnPainting_VarInt],
      0x05 -> ByteCodec.summon[SpawnPlayer_f64],
      0x06 -> ByteCodec.summon[Animation],
      0x07 -> ByteCodec.summon[Statistics],
      0x08 -> ByteCodec.summon[BlockBreakAnimation],
      0x09 -> ByteCodec.summon[UpdateBlockEntity],
      0x0a -> ByteCodec.summon[BlockAction],
      0x0b -> ByteCodec.summon[BlockChange_VarInt],
      0x0c -> ByteCodec.summon[BossBar],
      0x0d -> ByteCodec.summon[ServerDifficulty_Locked],
      0x0e -> ByteCodec.summon[ServerMessage_Position],
      0x0f -> ByteCodec.summon[MultiBlockChange_VarInt],
      0x10 -> ByteCodec.summon[TabCompleteReply],
      0x11 -> ByteCodec.summon[DeclareCommands],
      0x12 -> ByteCodec.summon[ConfirmTransaction],
      0x13 -> ByteCodec.summon[WindowClose],
      0x14 -> ByteCodec.summon[WindowItems],
      0x15 -> ByteCodec.summon[WindowProperty],
      0x16 -> ByteCodec.summon[WindowSetSlot],
      0x17 -> ByteCodec.summon[SetCooldown],
      0x18 -> ByteCodec.summon[PluginMessageClientbound],
      0x19 -> ByteCodec.summon[NamedSoundEffect],
      0x1a -> ByteCodec.summon[Disconnect],
      0x1b -> ByteCodec.summon[EntityAction],
      0x1c -> ByteCodec.summon[Explosion],
      0x1d -> ByteCodec.summon[ChunkUnload],
      0x1e -> ByteCodec.summon[ChangeGameState],
      0x1f -> ByteCodec.summon[WindowOpenHorse],
      0x20 -> ByteCodec.summon[KeepAliveClientbound_i64],
      0x21 -> ByteCodec.summon[ChunkData_HeightMap],
      0x22 -> ByteCodec.summon[Effect],
      0x23 -> ByteCodec.summon[Particle_Data],
      0x24 -> ByteCodec.summon[UpdateLight_NoTrust],
      0x25 -> ByteCodec.summon[JoinGame_i32_ViewDistance],
      0x26 -> ByteCodec.summon[Maps],
      0x27 -> ByteCodec.summon[TradeList_WithoutRestock],
      0x28 -> ByteCodec.summon[EntityMove_i16],
      0x29 -> ByteCodec.summon[EntityLookAndMove_i16],
      0x2a -> ByteCodec.summon[EntityLook_VarInt],
      0x2b -> ByteCodec.summon[Entity],
      0x2c -> ByteCodec.summon[VehicleTeleport],
      0x2d -> ByteCodec.summon[OpenBook],
      0x2e -> ByteCodec.summon[WindowOpen_VarInt],
      0x2f -> ByteCodec.summon[SignEditorOpen],
      0x30 -> ByteCodec.summon[CraftRecipeResponse],
      0x31 -> ByteCodec.summon[PlayerAbilities],
      0x32 -> ByteCodec.summon[CombatEvent],
      0x33 -> ByteCodec.summon[PlayerInfo],
      0x34 -> ByteCodec.summon[FacePlayer],
      0x35 -> ByteCodec.summon[TeleportPlayer_WithConfirm],
      0x36 -> ByteCodec.summon[UnlockRecipes_WithSmelting],
      0x37 -> ByteCodec.summon[EntityDestroy],
      0x38 -> ByteCodec.summon[EntityRemoveEffect],
      0x39 -> ByteCodec.summon[ResourcePackSend],
      0x3a -> ByteCodec.summon[Respawn_Gamemode],
      0x3b -> ByteCodec.summon[EntityHeadLook],
      0x3c -> ByteCodec.summon[SelectAdvancementTab],
      0x3d -> ByteCodec.summon[WorldBorder],
      0x3e -> ByteCodec.summon[Camera],
      0x3f -> ByteCodec.summon[SetCurrentHotbarSlot],
      0x40 -> ByteCodec.summon[UpdateViewPosition],
      0x41 -> ByteCodec.summon[UpdateViewDistance],
      0x42 -> ByteCodec.summon[ScoreboardDisplay],
      0x43 -> ByteCodec.summon[EntityMetadata],
      0x44 -> ByteCodec.summon[EntityAttach],
      0x45 -> ByteCodec.summon[EntityVelocity],
      0x46 -> ByteCodec.summon[EntityEquipment_VarInt],
      0x47 -> ByteCodec.summon[SetExperience],
      0x48 -> ByteCodec.summon[UpdateHealth],
      0x49 -> ByteCodec.summon[ScoreboardObjective],
      0x4a -> ByteCodec.summon[SetPassengers],
      0x4b -> ByteCodec.summon[Teams_VarInt],
      0x4c -> ByteCodec.summon[UpdateScore],
      0x4d -> ByteCodec.summon[SpawnPosition],
      0x4e -> ByteCodec.summon[TimeUpdate],
      0x4f -> ByteCodec.summon[Title],
      0x50 -> ByteCodec.summon[EntitySoundEffect],
      0x51 -> ByteCodec.summon[SoundEffect],
      0x52 -> ByteCodec.summon[StopSound],
      0x53 -> ByteCodec.summon[PlayerListHeaderFooter],
      0x54 -> ByteCodec.summon[NBTQueryResponse],
      0x55 -> ByteCodec.summon[CollectItem],
      0x56 -> ByteCodec.summon[EntityTeleport_f64],
      0x57 -> ByteCodec.summon[Advancements],
      0x58 -> ByteCodec.summon[EntityProperties],
      0x59 -> ByteCodec.summon[EntityEffect],
      0x5a -> ByteCodec.summon[DeclareRecipes],
      0x5b -> ByteCodec.summon[TagsWithEntities],
    ))
  )

  val loginProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summon[LoginStart],
      0x01 -> ByteCodec.summon[EncryptionResponse],
      0x02 -> ByteCodec.summon[LoginPluginResponse],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summon[LoginDisconnect],
      0x01 -> ByteCodec.summon[EncryptionRequest],
      0x02 -> ByteCodec.summon[LoginSuccess_String],
      0x03 -> ByteCodec.summon[SetInitialCompression],
      0x04 -> ByteCodec.summon[LoginPluginRequest],
    ))
  )
}
