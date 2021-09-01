package com.github.kory33.s2mctest
package com.github.kory33.s2mctest.connection.protocol.versions

import connection.protocol.{Protocol, PacketIdBindings}
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

object v1_11_2 {
  val playProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summon[TeleportConfirm],
      0x01 -> ByteCodec.summon[TabComplete],
      0x02 -> ByteCodec.summon[ChatMessage],
      0x03 -> ByteCodec.summon[ClientStatus],
      0x04 -> ByteCodec.summon[ClientSettings],
      0x05 -> ByteCodec.summon[ConfirmTransactionServerbound],
      0x06 -> ByteCodec.summon[EnchantItem],
      0x07 -> ByteCodec.summon[ClickWindow],
      0x08 -> ByteCodec.summon[CloseWindow],
      0x09 -> ByteCodec.summon[PluginMessageServerbound],
      0x0a -> ByteCodec.summon[UseEntity_Hand],
      0x0b -> ByteCodec.summon[KeepAliveServerbound_VarInt],
      0x0c -> ByteCodec.summon[PlayerPosition],
      0x0d -> ByteCodec.summon[PlayerPositionLook],
      0x0e -> ByteCodec.summon[PlayerLook],
      0x0f -> ByteCodec.summon[Player],
      0x10 -> ByteCodec.summon[VehicleMove],
      0x11 -> ByteCodec.summon[SteerBoat],
      0x12 -> ByteCodec.summon[ClientAbilities_f32],
      0x13 -> ByteCodec.summon[PlayerDigging],
      0x14 -> ByteCodec.summon[PlayerAction],
      0x15 -> ByteCodec.summon[SteerVehicle],
      0x16 -> ByteCodec.summon[ResourcePackStatus],
      0x17 -> ByteCodec.summon[HeldItemChange],
      0x18 -> ByteCodec.summon[CreativeInventoryAction],
      0x19 -> ByteCodec.summon[SetSign],
      0x1a -> ByteCodec.summon[ArmSwing],
      0x1b -> ByteCodec.summon[SpectateTeleport],
      0x1c -> ByteCodec.summon[PlayerBlockPlacement_f32],
      0x1d -> ByteCodec.summon[UseItem],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summon[SpawnObject],
      0x01 -> ByteCodec.summon[SpawnExperienceOrb],
      0x02 -> ByteCodec.summon[SpawnGlobalEntity],
      0x03 -> ByteCodec.summon[SpawnMob_WithMeta],
      0x04 -> ByteCodec.summon[SpawnPainting_String],
      0x05 -> ByteCodec.summon[SpawnPlayer_f64],
      0x06 -> ByteCodec.summon[Animation],
      0x07 -> ByteCodec.summon[Statistics],
      0x08 -> ByteCodec.summon[BlockBreakAnimation],
      0x09 -> ByteCodec.summon[UpdateBlockEntity],
      0x0a -> ByteCodec.summon[BlockAction],
      0x0b -> ByteCodec.summon[BlockChange_VarInt],
      0x0c -> ByteCodec.summon[BossBar],
      0x0d -> ByteCodec.summon[ServerDifficulty],
      0x0e -> ByteCodec.summon[TabCompleteReply],
      0x0f -> ByteCodec.summon[ServerMessage_Position],
      0x10 -> ByteCodec.summon[MultiBlockChange_VarInt],
      0x11 -> ByteCodec.summon[ConfirmTransaction],
      0x12 -> ByteCodec.summon[WindowClose],
      0x13 -> ByteCodec.summon[WindowOpen],
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
      0x1f -> ByteCodec.summon[KeepAliveClientbound_VarInt],
      0x20 -> ByteCodec.summon[ChunkData],
      0x21 -> ByteCodec.summon[Effect],
      0x22 -> ByteCodec.summon[Particle_VarIntArray],
      0x23 -> ByteCodec.summon[JoinGame_i32],
      0x24 -> ByteCodec.summon[Maps_NoLocked],
      0x25 -> ByteCodec.summon[EntityMove_i16],
      0x26 -> ByteCodec.summon[EntityLookAndMove_i16],
      0x27 -> ByteCodec.summon[EntityLook_VarInt],
      0x28 -> ByteCodec.summon[Entity],
      0x29 -> ByteCodec.summon[VehicleTeleport],
      0x2a -> ByteCodec.summon[SignEditorOpen],
      0x2b -> ByteCodec.summon[PlayerAbilities],
      0x2c -> ByteCodec.summon[CombatEvent],
      0x2d -> ByteCodec.summon[PlayerInfo],
      0x2e -> ByteCodec.summon[TeleportPlayer_WithConfirm],
      0x2f -> ByteCodec.summon[EntityUsedBed],
      0x30 -> ByteCodec.summon[EntityDestroy],
      0x31 -> ByteCodec.summon[EntityRemoveEffect],
      0x32 -> ByteCodec.summon[ResourcePackSend],
      0x33 -> ByteCodec.summon[Respawn_Gamemode],
      0x34 -> ByteCodec.summon[EntityHeadLook],
      0x35 -> ByteCodec.summon[WorldBorder],
      0x36 -> ByteCodec.summon[Camera],
      0x37 -> ByteCodec.summon[SetCurrentHotbarSlot],
      0x38 -> ByteCodec.summon[ScoreboardDisplay],
      0x39 -> ByteCodec.summon[EntityMetadata],
      0x3a -> ByteCodec.summon[EntityAttach],
      0x3b -> ByteCodec.summon[EntityVelocity],
      0x3c -> ByteCodec.summon[EntityEquipment_VarInt],
      0x3d -> ByteCodec.summon[SetExperience],
      0x3e -> ByteCodec.summon[UpdateHealth],
      0x3f -> ByteCodec.summon[ScoreboardObjective],
      0x40 -> ByteCodec.summon[SetPassengers],
      0x41 -> ByteCodec.summon[Teams_u8],
      0x42 -> ByteCodec.summon[UpdateScore],
      0x43 -> ByteCodec.summon[SpawnPosition],
      0x44 -> ByteCodec.summon[TimeUpdate],
      0x45 -> ByteCodec.summon[Title],
      0x46 -> ByteCodec.summon[SoundEffect],
      0x47 -> ByteCodec.summon[PlayerListHeaderFooter],
      0x48 -> ByteCodec.summon[CollectItem],
      0x49 -> ByteCodec.summon[EntityTeleport_f64],
      0x4a -> ByteCodec.summon[EntityProperties],
      0x4b -> ByteCodec.summon[EntityEffect],
    ))
  )

  val loginProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summon[LoginStart],
      0x01 -> ByteCodec.summon[EncryptionResponse],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summon[LoginDisconnect],
      0x01 -> ByteCodec.summon[EncryptionRequest],
      0x02 -> ByteCodec.summon[LoginSuccess_String],
      0x03 -> ByteCodec.summon[SetInitialCompression],
    ))
  )
}
