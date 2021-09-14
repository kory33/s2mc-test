package com.github.kory33.s2mctest
package com.github.kory33.s2mctest.connection.protocol.versions

import connection.protocol.{Protocol, PacketIdBindings}
import connection.protocol.codec.ByteCodec
import impl.protocol.packets.PacketIntent

import PacketIntent.Handshaking.ServerBound.*
import PacketIntent.Play.ServerBound.*
import PacketIntent.Play.ClientBound.*
import PacketIntent.Login.ServerBound.*
import PacketIntent.Login.ClientBound.*
import PacketIntent.Status.ClientBound.*
import PacketIntent.Status.ServerBound.*

object v1_9_2 {
  import connection.protocol.codec.ByteCodecs.Common.given
  import connection.protocol.codec.ByteCodecs.PositionCodecBefore1_14.given
  import connection.protocol.macros.GenByteDecode.given

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
      0x0b -> ByteCodec.summonPair[KeepAliveServerbound_VarInt],
      0x0c -> ByteCodec.summonPair[PlayerPosition],
      0x0d -> ByteCodec.summonPair[PlayerPositionLook],
      0x0e -> ByteCodec.summonPair[PlayerLook],
      0x0f -> ByteCodec.summonPair[Player],
      0x10 -> ByteCodec.summonPair[VehicleMove],
      0x11 -> ByteCodec.summonPair[SteerBoat],
      0x12 -> ByteCodec.summonPair[ClientAbilities_f32],
      0x13 -> ByteCodec.summonPair[PlayerDigging],
      0x14 -> ByteCodec.summonPair[PlayerAction],
      0x15 -> ByteCodec.summonPair[SteerVehicle],
      0x16 -> ByteCodec.summonPair[ResourcePackStatus_hash],
      0x17 -> ByteCodec.summonPair[HeldItemChange],
      0x18 -> ByteCodec.summonPair[CreativeInventoryAction],
      0x19 -> ByteCodec.summonPair[SetSign],
      0x1a -> ByteCodec.summonPair[ArmSwing],
      0x1b -> ByteCodec.summonPair[SpectateTeleport],
      0x1c -> ByteCodec.summonPair[PlayerBlockPlacement_u8],
      0x1d -> ByteCodec.summonPair[UseItem],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[SpawnObject],
      0x01 -> ByteCodec.summonPair[SpawnExperienceOrb],
      0x02 -> ByteCodec.summonPair[SpawnGlobalEntity],
      0x03 -> ByteCodec.summonPair[SpawnMob_u8],
      0x04 -> ByteCodec.summonPair[SpawnPainting_String],
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
      0x19 -> ByteCodec.summonPair[NamedSoundEffect_u8],
      0x1a -> ByteCodec.summonPair[Disconnect],
      0x1b -> ByteCodec.summonPair[EntityAction],
      0x1c -> ByteCodec.summonPair[Explosion],
      0x1d -> ByteCodec.summonPair[ChunkUnload],
      0x1e -> ByteCodec.summonPair[ChangeGameState],
      0x1f -> ByteCodec.summonPair[KeepAliveClientbound_VarInt],
      0x20 -> ByteCodec.summonPair[ChunkData_NoEntities],
      0x21 -> ByteCodec.summonPair[Effect],
      0x22 -> ByteCodec.summonPair[Particle_VarIntArray],
      0x23 -> ByteCodec.summonPair[JoinGame_i32],
      0x24 -> ByteCodec.summonPair[Maps_NoLocked],
      0x25 -> ByteCodec.summonPair[EntityMove_i16],
      0x26 -> ByteCodec.summonPair[EntityLookAndMove_i16],
      0x27 -> ByteCodec.summonPair[EntityLook_VarInt],
      0x28 -> ByteCodec.summonPair[Entity],
      0x29 -> ByteCodec.summonPair[VehicleTeleport],
      0x2a -> ByteCodec.summonPair[SignEditorOpen],
      0x2b -> ByteCodec.summonPair[PlayerAbilities],
      0x2c -> ByteCodec.summonPair[CombatEvent],
      0x2d -> ByteCodec.summonPair[PlayerInfo],
      0x2e -> ByteCodec.summonPair[TeleportPlayer_WithConfirm],
      0x2f -> ByteCodec.summonPair[EntityUsedBed],
      0x30 -> ByteCodec.summonPair[EntityDestroy],
      0x31 -> ByteCodec.summonPair[EntityRemoveEffect],
      0x32 -> ByteCodec.summonPair[ResourcePackSend],
      0x33 -> ByteCodec.summonPair[Respawn_Gamemode],
      0x34 -> ByteCodec.summonPair[EntityHeadLook],
      0x35 -> ByteCodec.summonPair[WorldBorder],
      0x36 -> ByteCodec.summonPair[Camera],
      0x37 -> ByteCodec.summonPair[SetCurrentHotbarSlot],
      0x38 -> ByteCodec.summonPair[ScoreboardDisplay],
      0x39 -> ByteCodec.summonPair[EntityMetadata],
      0x3a -> ByteCodec.summonPair[EntityAttach],
      0x3b -> ByteCodec.summonPair[EntityVelocity],
      0x3c -> ByteCodec.summonPair[EntityEquipment_VarInt],
      0x3d -> ByteCodec.summonPair[SetExperience],
      0x3e -> ByteCodec.summonPair[UpdateHealth],
      0x3f -> ByteCodec.summonPair[ScoreboardObjective],
      0x40 -> ByteCodec.summonPair[SetPassengers],
      0x41 -> ByteCodec.summonPair[Teams_u8],
      0x42 -> ByteCodec.summonPair[UpdateScore],
      0x43 -> ByteCodec.summonPair[SpawnPosition],
      0x44 -> ByteCodec.summonPair[TimeUpdate],
      0x45 -> ByteCodec.summonPair[Title_notext],
      0x46 -> ByteCodec.summonPair[UpdateSign],
      0x47 -> ByteCodec.summonPair[SoundEffect_u8],
      0x48 -> ByteCodec.summonPair[PlayerListHeaderFooter],
      0x49 -> ByteCodec.summonPair[CollectItem_nocount],
      0x4a -> ByteCodec.summonPair[EntityTeleport_f64],
      0x4b -> ByteCodec.summonPair[EntityProperties],
      0x4c -> ByteCodec.summonPair[EntityEffect],
    ))
  )

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
}
