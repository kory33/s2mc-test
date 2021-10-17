package com.github.kory33.s2mctest.impl.connection.protocol.versions

import com.github.kory33.s2mctest.core.connection.codec.ByteCodec
import com.github.kory33.s2mctest.core.connection.protocol.{PacketIdBindings, Protocol}

object v1_8_9 {
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.*
  import com.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.*
  import com.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.Common.given
  import com.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.PositionCodecBefore1_14.given
  import com.github.kory33.s2mctest.impl.connection.codec.decode.macros.GenByteDecode.given

  // noinspection TypeAnnotation
  // format: off
  val playProtocol = Protocol(
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[KeepAliveServerbound_VarInt],
      0x01 -> ByteCodec.summonPair[ChatMessage],
      0x02 -> ByteCodec.summonPair[UseEntity_Handsfree],
      0x03 -> ByteCodec.summonPair[Player],
      0x04 -> ByteCodec.summonPair[PlayerPosition],
      0x05 -> ByteCodec.summonPair[PlayerLook],
      0x06 -> ByteCodec.summonPair[PlayerPositionLook],
      0x07 -> ByteCodec.summonPair[PlayerDigging_u8],
      0x08 -> ByteCodec.summonPair[PlayerBlockPlacement_u8_Item],
      0x09 -> ByteCodec.summonPair[HeldItemChange],
      0x0a -> ByteCodec.summonPair[ArmSwing_Handsfree],
      0x0b -> ByteCodec.summonPair[PlayerAction],
      0x0c -> ByteCodec.summonPair[SteerVehicle],
      0x0d -> ByteCodec.summonPair[CloseWindow],
      0x0e -> ByteCodec.summonPair[ClickWindow_u8],
      0x0f -> ByteCodec.summonPair[ConfirmTransactionServerbound],
      0x10 -> ByteCodec.summonPair[CreativeInventoryAction],
      0x11 -> ByteCodec.summonPair[EnchantItem],
      0x12 -> ByteCodec.summonPair[SetSign],
      0x13 -> ByteCodec.summonPair[ClientAbilities_f32],
      0x14 -> ByteCodec.summonPair[TabComplete_NoAssume],
      0x15 -> ByteCodec.summonPair[ClientSettings_u8_Handsfree],
      0x16 -> ByteCodec.summonPair[ClientStatus],
      0x17 -> ByteCodec.summonPair[PluginMessageServerbound],
      0x18 -> ByteCodec.summonPair[SpectateTeleport],
      0x19 -> ByteCodec.summonPair[ResourcePackStatus],
    )),
    PacketIdBindings((
      0x00 -> ByteCodec.summonPair[KeepAliveClientbound_VarInt],
      0x01 -> ByteCodec.summonPair[JoinGame_i8],
      0x02 -> ByteCodec.summonPair[ServerMessage_Position],
      0x03 -> ByteCodec.summonPair[TimeUpdate],
      0x04 -> ByteCodec.summonPair[EntityEquipment_u16],
      0x05 -> ByteCodec.summonPair[SpawnPosition],
      0x06 -> ByteCodec.summonPair[UpdateHealth],
      0x07 -> ByteCodec.summonPair[Respawn_Gamemode],
      0x08 -> ByteCodec.summonPair[TeleportPlayer_NoConfirm],
      0x09 -> ByteCodec.summonPair[SetCurrentHotbarSlot],
      0x0a -> ByteCodec.summonPair[EntityUsedBed],
      0x0b -> ByteCodec.summonPair[Animation],
      0x0c -> ByteCodec.summonPair[SpawnPlayer_i32_HeldItem],
      0x0d -> ByteCodec.summonPair[CollectItem_nocount],
      0x0e -> ByteCodec.summonPair[SpawnObject_i32_NoUUID],
      0x0f -> ByteCodec.summonPair[SpawnMob_u8_i32_NoUUID],
      0x10 -> ByteCodec.summonPair[SpawnPainting_NoUUID],
      0x11 -> ByteCodec.summonPair[SpawnExperienceOrb_i32],
      0x12 -> ByteCodec.summonPair[EntityVelocity],
      0x13 -> ByteCodec.summonPair[EntityDestroy],
      0x14 -> ByteCodec.summonPair[Entity],
      0x15 -> ByteCodec.summonPair[EntityMove_i8],
      0x16 -> ByteCodec.summonPair[EntityLook_VarInt],
      0x17 -> ByteCodec.summonPair[EntityLookAndMove_i8],
      0x18 -> ByteCodec.summonPair[EntityTeleport_i32],
      0x19 -> ByteCodec.summonPair[EntityHeadLook],
      0x1a -> ByteCodec.summonPair[EntityStatus],
      0x1b -> ByteCodec.summonPair[EntityAttach_leashed],
      0x1c -> ByteCodec.summonPair[EntityMetadata],
      0x1d -> ByteCodec.summonPair[EntityEffect],
      0x1e -> ByteCodec.summonPair[EntityRemoveEffect],
      0x1f -> ByteCodec.summonPair[SetExperience],
      0x20 -> ByteCodec.summonPair[EntityProperties],
      0x21 -> ByteCodec.summonPair[ChunkData_NoEntities_u16],
      0x22 -> ByteCodec.summonPair[MultiBlockChange_VarInt],
      0x23 -> ByteCodec.summonPair[BlockChange_VarInt],
      0x24 -> ByteCodec.summonPair[BlockAction],
      0x25 -> ByteCodec.summonPair[BlockBreakAnimation],
      0x26 -> ByteCodec.summonPair[ChunkDataBulk],
      0x27 -> ByteCodec.summonPair[Explosion],
      0x28 -> ByteCodec.summonPair[Effect],
      0x29 -> ByteCodec.summonPair[NamedSoundEffect_u8_NoCategory],
      0x2a -> ByteCodec.summonPair[Particle_VarIntArray],
      0x2b -> ByteCodec.summonPair[ChangeGameState],
      0x2c -> ByteCodec.summonPair[SpawnGlobalEntity_i32],
      0x2d -> ByteCodec.summonPair[WindowOpen],
      0x2e -> ByteCodec.summonPair[WindowClose],
      0x2f -> ByteCodec.summonPair[WindowSetSlot],
      0x30 -> ByteCodec.summonPair[WindowItems],
      0x31 -> ByteCodec.summonPair[WindowProperty],
      0x32 -> ByteCodec.summonPair[ConfirmTransaction],
      0x33 -> ByteCodec.summonPair[UpdateSign],
      0x34 -> ByteCodec.summonPair[Maps_NoTracking],
      0x35 -> ByteCodec.summonPair[UpdateBlockEntity],
      0x36 -> ByteCodec.summonPair[SignEditorOpen],
      0x37 -> ByteCodec.summonPair[Statistics],
      0x38 -> ByteCodec.summonPair[PlayerInfo],
      0x39 -> ByteCodec.summonPair[PlayerAbilities],
      0x3a -> ByteCodec.summonPair[TabCompleteReply],
      0x3b -> ByteCodec.summonPair[ScoreboardObjective],
      0x3c -> ByteCodec.summonPair[UpdateScore],
      0x3d -> ByteCodec.summonPair[ScoreboardDisplay],
      0x3e -> ByteCodec.summonPair[Teams_u8],
      0x3f -> ByteCodec.summonPair[PluginMessageClientbound],
      0x40 -> ByteCodec.summonPair[Disconnect],
      0x41 -> ByteCodec.summonPair[ServerDifficulty],
      0x42 -> ByteCodec.summonPair[CombatEvent],
      0x43 -> ByteCodec.summonPair[Camera],
      0x44 -> ByteCodec.summonPair[WorldBorder],
      0x45 -> ByteCodec.summonPair[Title_notext_component],
      0x46 -> ByteCodec.summonPair[SetCompression],
      0x47 -> ByteCodec.summonPair[PlayerListHeaderFooter],
      0x48 -> ByteCodec.summonPair[ResourcePackSend],
      0x49 -> ByteCodec.summonPair[EntityUpdateNBT],
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
