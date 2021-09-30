package com.github.kory33.s2mctest.protocol.impl.versions

import com.github.kory33.s2mctest.connection.protocol.Protocol
import com.github.kory33.s2mctest.connection.protocol.PacketIdBindings
import com.github.kory33.s2mctest.connection.protocol.codec.ByteCodec


object v15w39c {
  import com.github.kory33.s2mctest.protocol.impl.packets.PacketIntent.Play.ClientBound.*
  import com.github.kory33.s2mctest.protocol.impl.packets.PacketIntent.Play.ServerBound.*

  import com.github.kory33.s2mctest.protocol.impl.packets.PacketIntent.Login.ClientBound.*
  import com.github.kory33.s2mctest.protocol.impl.packets.PacketIntent.Login.ServerBound.*

  import com.github.kory33.s2mctest.protocol.impl.codec.ByteCodecs.Common.given
  import com.github.kory33.s2mctest.connection.protocol.macros.GenByteDecode.given
  import com.github.kory33.s2mctest.protocol.impl.codec.ByteCodecs.PositionCodecBefore1_14.given

  val playProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[TabComplete_NoAssume],
      0x01 -> ByteCodec.summonPair[ChatMessage],
      0x02 -> ByteCodec.summonPair[ClientStatus],
      0x03 -> ByteCodec.summonPair[ClientSettings_u8],
      0x04 -> ByteCodec.summonPair[ConfirmTransactionServerbound],
      0x05 -> ByteCodec.summonPair[EnchantItem],
      0x06 -> ByteCodec.summonPair[ClickWindow_u8],
      0x07 -> ByteCodec.summonPair[CloseWindow],
      0x08 -> ByteCodec.summonPair[PluginMessageServerbound],
      0x09 -> ByteCodec.summonPair[UseEntity_Hand],
      0x0a -> ByteCodec.summonPair[KeepAliveServerbound_VarInt],
      0x0b -> ByteCodec.summonPair[PlayerPosition],
      0x0c -> ByteCodec.summonPair[PlayerPositionLook],
      0x0d -> ByteCodec.summonPair[PlayerLook],
      0x0e -> ByteCodec.summonPair[Player],
      0x0f -> ByteCodec.summonPair[ClientAbilities_f32],
      0x10 -> ByteCodec.summonPair[PlayerDigging_u8],
      0x11 -> ByteCodec.summonPair[PlayerAction],
      0x12 -> ByteCodec.summonPair[SteerVehicle],
      0x13 -> ByteCodec.summonPair[ResourcePackStatus],
      0x14 -> ByteCodec.summonPair[HeldItemChange],
      0x15 -> ByteCodec.summonPair[CreativeInventoryAction],
      0x16 -> ByteCodec.summonPair[SetSign],
      0x17 -> ByteCodec.summonPair[ArmSwing],
      0x18 -> ByteCodec.summonPair[SpectateTeleport],
      0x19 -> ByteCodec.summonPair[PlayerBlockPlacement_u8],
      0x1a -> ByteCodec.summonPair[UseItem],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[SpawnObject_i32],
      0x01 -> ByteCodec.summonPair[SpawnExperienceOrb_i32],
      0x02 -> ByteCodec.summonPair[SpawnGlobalEntity_i32],
      0x03 -> ByteCodec.summonPair[SpawnMob_u8_i32],
      0x04 -> ByteCodec.summonPair[SpawnPainting_NoUUID],
      0x05 -> ByteCodec.summonPair[SpawnPlayer_i32],
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
      0x19 -> ByteCodec.summonPair[Disconnect],
      0x1a -> ByteCodec.summonPair[EntityAction],
      0x1b -> ByteCodec.summonPair[Explosion],
      0x1c -> ByteCodec.summonPair[ChunkUnload],
      0x1d -> ByteCodec.summonPair[SetCompression],
      0x1e -> ByteCodec.summonPair[ChangeGameState],
      0x1f -> ByteCodec.summonPair[KeepAliveClientbound_VarInt],
      0x20 -> ByteCodec.summonPair[ChunkData_NoEntities],
      0x21 -> ByteCodec.summonPair[Effect],
      0x22 -> ByteCodec.summonPair[Particle_VarIntArray],
      0x23 -> ByteCodec.summonPair[NamedSoundEffect_u8_NoCategory],
      0x24 -> ByteCodec.summonPair[JoinGame_i8],
      0x25 -> ByteCodec.summonPair[Maps_NoLocked],
      0x26 -> ByteCodec.summonPair[EntityMove_i8],
      0x27 -> ByteCodec.summonPair[EntityLookAndMove_i8],
      0x28 -> ByteCodec.summonPair[EntityLook_VarInt],
      0x29 -> ByteCodec.summonPair[Entity],
      0x2a -> ByteCodec.summonPair[SignEditorOpen],
      0x2b -> ByteCodec.summonPair[PlayerAbilities],
      0x2c -> ByteCodec.summonPair[CombatEvent],
      0x2d -> ByteCodec.summonPair[PlayerInfo],
      0x2e -> ByteCodec.summonPair[TeleportPlayer_NoConfirm],
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
      0x3a -> ByteCodec.summonPair[EntityAttach_leashed],
      0x3b -> ByteCodec.summonPair[EntityVelocity],
      0x3c -> ByteCodec.summonPair[EntityEquipment_VarInt],
      0x3d -> ByteCodec.summonPair[SetExperience],
      0x3e -> ByteCodec.summonPair[UpdateHealth],
      0x3f -> ByteCodec.summonPair[ScoreboardObjective],
      0x40 -> ByteCodec.summonPair[Teams_u8],
      0x41 -> ByteCodec.summonPair[UpdateScore],
      0x42 -> ByteCodec.summonPair[SpawnPosition],
      0x43 -> ByteCodec.summonPair[TimeUpdate],
      0x44 -> ByteCodec.summonPair[Title_notext_component],
      0x45 -> ByteCodec.summonPair[UpdateSign],
      0x46 -> ByteCodec.summonPair[PlayerListHeaderFooter],
      0x47 -> ByteCodec.summonPair[CollectItem_nocount],
      0x48 -> ByteCodec.summonPair[EntityTeleport_i32],
      0x49 -> ByteCodec.summonPair[EntityProperties],
      0x4a -> ByteCodec.summonPair[EntityEffect],
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
