package com.github.kory33.s2mctest.impl.connection.protocol.versions

import com.github.kory33.s2mctest.core.connection.codec.ByteCodec
import com.github.kory33.s2mctest.core.connection.protocol.{PacketIdBindings, Protocol}
import com.github.kory33.s2mctest.impl.connection.packets.PacketDataPrimitives.VarInt

object v1_12_2 {
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.*
  import com.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.Common.given
  import com.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.PositionCodecBefore1_14.given
  import com.github.kory33.s2mctest.impl.connection.codec.decode.macros.GenByteDecode.given

  val protocolVersion: VarInt = VarInt(340)

  // noinspection TypeAnnotation
  // format: off
  val playProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[TeleportConfirm],
      0x01 -> ByteCodec.summonPair[TabComplete],
      0x02 -> ByteCodec.summonPair[ChatMessage],
      0x03 -> ByteCodec.summonPair[ClientStatus],
      0x04 -> ByteCodec.summonPair[ClientSettings],
      0x05 -> ByteCodec.summonPair[ConfirmTransactionServerbound],
      0x06 -> ByteCodec.summonPair[EnchantItem],
      0x07 -> ByteCodec.summonPair[ClickWindow],
      0x08 -> ByteCodec.summonPair[CloseWindow],
      0x09 -> ByteCodec.summonPair[PluginMessageServerbound],
      0x0a -> ByteCodec.summonPair[UseEntity_Hand],
      0x0b -> ByteCodec.summonPair[KeepAliveServerbound_i64],
      0x0c -> ByteCodec.summonPair[Player],
      0x0d -> ByteCodec.summonPair[PlayerPosition],
      0x0e -> ByteCodec.summonPair[PlayerPositionLook],
      0x0f -> ByteCodec.summonPair[PlayerLook],
      0x10 -> ByteCodec.summonPair[VehicleMove],
      0x11 -> ByteCodec.summonPair[SteerBoat],
      0x12 -> ByteCodec.summonPair[CraftRecipeRequest],
      0x13 -> ByteCodec.summonPair[ClientAbilities_f32],
      0x14 -> ByteCodec.summonPair[PlayerDigging],
      0x15 -> ByteCodec.summonPair[PlayerAction],
      0x16 -> ByteCodec.summonPair[SteerVehicle],
      0x17 -> ByteCodec.summonPair[CraftingBookData],
      0x18 -> ByteCodec.summonPair[ResourcePackStatus],
      0x19 -> ByteCodec.summonPair[AdvancementTab],
      0x1a -> ByteCodec.summonPair[HeldItemChange],
      0x1b -> ByteCodec.summonPair[CreativeInventoryAction],
      0x1c -> ByteCodec.summonPair[SetSign],
      0x1d -> ByteCodec.summonPair[ArmSwing],
      0x1e -> ByteCodec.summonPair[SpectateTeleport],
      0x1f -> ByteCodec.summonPair[PlayerBlockPlacement_f32],
      0x20 -> ByteCodec.summonPair[UseItem],
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
      0x0e -> ByteCodec.summonPair[TabCompleteReply],
      0x0f -> ByteCodec.summonPair[ServerMessage_Position],
      0x10 -> ByteCodec.summonPair[MultiBlockChange_VarInt],
      0x11 -> ByteCodec.summonPair[ConfirmTransaction],
      0x12 -> ByteCodec.summonPair[WindowClose],
      0x13 -> ByteCodec.summonPair[WindowOpen],
      0x14 -> ByteCodec.summonPair[WindowItems],
      0x15 -> ByteCodec.summonPair[WindowProperty],
      0x16 -> ByteCodec.summonPair[WindowSetSlot],
      0x17 -> ByteCodec.summonPair[SetCooldown],
      0x18 -> ByteCodec.summonPair[PluginMessageClientbound],
      0x19 -> ByteCodec.summonPair[NamedSoundEffect],
      0x1a -> ByteCodec.summonPair[Disconnect],
      0x1b -> ByteCodec.summonPair[EntityAction],
      0x1c -> ByteCodec.summonPair[Explosion],
      0x1d -> ByteCodec.summonPair[ChunkUnload],
      0x1e -> ByteCodec.summonPair[ChangeGameState],
      0x1f -> ByteCodec.summonPair[KeepAliveClientbound_i64],
      0x20 -> ByteCodec.summonPair[ChunkData],
      0x21 -> ByteCodec.summonPair[Effect],
      0x22 -> ByteCodec.summonPair[Particle_VarIntArray],
      0x23 -> ByteCodec.summonPair[JoinGame_i32],
      0x24 -> ByteCodec.summonPair[Maps_NoLocked],
      0x25 -> ByteCodec.summonPair[Entity],
      0x26 -> ByteCodec.summonPair[EntityMove_i16],
      0x27 -> ByteCodec.summonPair[EntityLookAndMove_i16],
      0x28 -> ByteCodec.summonPair[EntityLook_VarInt],
      0x29 -> ByteCodec.summonPair[VehicleTeleport],
      0x2a -> ByteCodec.summonPair[SignEditorOpen],
      0x2b -> ByteCodec.summonPair[CraftRecipeResponse],
      0x2c -> ByteCodec.summonPair[PlayerAbilities],
      0x2d -> ByteCodec.summonPair[CombatEvent],
      0x2e -> ByteCodec.summonPair[PlayerInfo],
      0x2f -> ByteCodec.summonPair[TeleportPlayer_WithConfirm],
      0x30 -> ByteCodec.summonPair[EntityUsedBed],
      0x31 -> ByteCodec.summonPair[UnlockRecipes_NoSmelting],
      0x32 -> ByteCodec.summonPair[EntityDestroy],
      0x33 -> ByteCodec.summonPair[EntityRemoveEffect],
      0x34 -> ByteCodec.summonPair[ResourcePackSend],
      0x35 -> ByteCodec.summonPair[Respawn_Gamemode],
      0x36 -> ByteCodec.summonPair[EntityHeadLook],
      0x37 -> ByteCodec.summonPair[SelectAdvancementTab],
      0x38 -> ByteCodec.summonPair[WorldBorder],
      0x39 -> ByteCodec.summonPair[Camera],
      0x3a -> ByteCodec.summonPair[SetCurrentHotbarSlot],
      0x3b -> ByteCodec.summonPair[ScoreboardDisplay],
      0x3c -> ByteCodec.summonPair[EntityMetadata],
      0x3d -> ByteCodec.summonPair[EntityAttach],
      0x3e -> ByteCodec.summonPair[EntityVelocity],
      0x3f -> ByteCodec.summonPair[EntityEquipment_VarInt],
      0x40 -> ByteCodec.summonPair[SetExperience],
      0x41 -> ByteCodec.summonPair[UpdateHealth],
      0x42 -> ByteCodec.summonPair[ScoreboardObjective],
      0x43 -> ByteCodec.summonPair[SetPassengers],
      0x44 -> ByteCodec.summonPair[Teams_u8],
      0x45 -> ByteCodec.summonPair[UpdateScore],
      0x46 -> ByteCodec.summonPair[SpawnPosition],
      0x47 -> ByteCodec.summonPair[TimeUpdate],
      0x48 -> ByteCodec.summonPair[Title],
      0x49 -> ByteCodec.summonPair[SoundEffect],
      0x4a -> ByteCodec.summonPair[PlayerListHeaderFooter],
      0x4b -> ByteCodec.summonPair[CollectItem],
      0x4c -> ByteCodec.summonPair[EntityTeleport_f64],
      0x4d -> ByteCodec.summonPair[Advancements],
      0x4e -> ByteCodec.summonPair[EntityProperties],
      0x4f -> ByteCodec.summonPair[EntityEffect],
    ))
  )
  // format: on

  // noinspection TypeAnnotation
  // format: off
  val loginProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[LoginStart],
      0x01 -> ByteCodec.summonPair[EncryptionResponse],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[LoginDisconnect],
      0x01 -> ByteCodec.summonPair[EncryptionRequest],
      0x02 -> ByteCodec.summonPair[LoginSuccess_String],
      0x03 -> ByteCodec.summonPair[SetInitialCompression],
    ))
  )
  // format: on
}
