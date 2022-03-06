package io.github.kory33.s2mctest.impl.connection.protocol

import io.github.kory33.s2mctest.core.connection.codec.{ByteCodec, ByteEncode}
import io.github.kory33.s2mctest.impl.connection.packets.PacketDataCompoundTypes.{
  Position,
  Slot
}
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Handshaking.ClientBound.*
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Handshaking.ServerBound.*
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ClientBound.*
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Login.ServerBound.*
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ClientBound.*
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Play.ServerBound.*
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Status.ClientBound.*
import io.github.kory33.s2mctest.impl.connection.packets.PacketIntent.Status.ServerBound.*

private class PacketIntentCodecCacheNonGiven(using ByteCodec[Position]) {
  import io.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.Common.given
  import io.github.kory33.s2mctest.impl.connection.codec.ByteCodecs.autogenerateFor
  import io.github.kory33.s2mctest.impl.connection.codec.decode.macros.GenByteDecode
  import io.github.kory33.s2mctest.impl.connection.codec.decode.macros.GenByteDecode.given

  // format: off
  val codec_Handshake: ByteCodec[Handshake] = autogenerateFor[Handshake]
  val codec_TeleportConfirm: ByteCodec[TeleportConfirm] = autogenerateFor[TeleportConfirm]
  val codec_QueryBlockNBT: ByteCodec[QueryBlockNBT] = autogenerateFor[QueryBlockNBT]
  val codec_SetDifficulty: ByteCodec[SetDifficulty] = autogenerateFor[SetDifficulty]
  val codec_TabComplete: ByteCodec[TabComplete] = autogenerateFor[TabComplete]
  val codec_TabComplete_NoAssume: ByteCodec[TabComplete_NoAssume] = autogenerateFor[TabComplete_NoAssume]
  val codec_TabComplete_NoAssume_NoTarget: ByteCodec[TabComplete_NoAssume_NoTarget] = autogenerateFor[TabComplete_NoAssume_NoTarget]
  val codec_ChatMessage: ByteCodec[ChatMessage] = autogenerateFor[ChatMessage]
  val codec_ClientStatus: ByteCodec[ClientStatus] = autogenerateFor[ClientStatus]
  val codec_ClientStatus_u8: ByteCodec[ClientStatus_u8] = autogenerateFor[ClientStatus_u8]
  val codec_ClientSettings: ByteCodec[ClientSettings] = autogenerateFor[ClientSettings]
  val codec_ClientSettings_u8: ByteCodec[ClientSettings_u8] = autogenerateFor[ClientSettings_u8]
  val codec_ClientSettings_u8_Handsfree: ByteCodec[ClientSettings_u8_Handsfree] = autogenerateFor[ClientSettings_u8_Handsfree]
  val codec_ClientSettings_u8_Handsfree_Difficulty: ByteCodec[ClientSettings_u8_Handsfree_Difficulty] = autogenerateFor[ClientSettings_u8_Handsfree_Difficulty]
  val codec_ConfirmTransactionServerbound: ByteCodec[ConfirmTransactionServerbound] = autogenerateFor[ConfirmTransactionServerbound]
  val codec_EnchantItem: ByteCodec[EnchantItem] = autogenerateFor[EnchantItem]
  val codec_ClickWindowButton: ByteCodec[ClickWindowButton] = autogenerateFor[ClickWindowButton]
  val codec_ClickWindow_u8: ByteCodec[ClickWindow_u8] = autogenerateFor[ClickWindow_u8]
  val codec_CloseWindow: ByteCodec[CloseWindow] = autogenerateFor[CloseWindow]
  val codec_PluginMessageServerbound: ByteCodec[PluginMessageServerbound] = autogenerateFor[PluginMessageServerbound]
  val codec_PluginMessageServerbound_i16: ByteCodec[PluginMessageServerbound_i16] = autogenerateFor[PluginMessageServerbound_i16]
  val codec_EditBook: ByteCodec[EditBook] = autogenerateFor[EditBook]
  val codec_QueryEntityNBT: ByteCodec[QueryEntityNBT] = autogenerateFor[QueryEntityNBT]
  val codec_UseEntity_Sneakflag: ByteCodec[UseEntity_Sneakflag] = autogenerateFor[UseEntity_Sneakflag]
  val codec_UseEntity_Hand: ByteCodec[UseEntity_Hand] = autogenerateFor[UseEntity_Hand]
  val codec_UseEntity_Handsfree: ByteCodec[UseEntity_Handsfree] = autogenerateFor[UseEntity_Handsfree]
  val codec_UseEntity_Handsfree_i32: ByteCodec[UseEntity_Handsfree_i32] = autogenerateFor[UseEntity_Handsfree_i32]
  val codec_GenerateStructure: ByteCodec[GenerateStructure] = autogenerateFor[GenerateStructure]
  val codec_KeepAliveServerbound_i64: ByteCodec[KeepAliveServerbound_i64] = autogenerateFor[KeepAliveServerbound_i64]
  val codec_KeepAliveServerbound_VarInt: ByteCodec[KeepAliveServerbound_VarInt] = autogenerateFor[KeepAliveServerbound_VarInt]
  val codec_KeepAliveServerbound_i32: ByteCodec[KeepAliveServerbound_i32] = autogenerateFor[KeepAliveServerbound_i32]
  val codec_LockDifficulty: ByteCodec[LockDifficulty] = autogenerateFor[LockDifficulty]
  val codec_PlayerPosition: ByteCodec[PlayerPosition] = autogenerateFor[PlayerPosition]
  val codec_PlayerPosition_HeadY: ByteCodec[PlayerPosition_HeadY] = autogenerateFor[PlayerPosition_HeadY]
  val codec_PlayerPositionLook: ByteCodec[PlayerPositionLook] = autogenerateFor[PlayerPositionLook]
  val codec_PlayerPositionLook_HeadY: ByteCodec[PlayerPositionLook_HeadY] = autogenerateFor[PlayerPositionLook_HeadY]
  val codec_PlayerLook: ByteCodec[PlayerLook] = autogenerateFor[PlayerLook]
  val codec_Player: ByteCodec[Player] = autogenerateFor[Player]
  val codec_VehicleMove: ByteCodec[VehicleMove] = autogenerateFor[VehicleMove]
  val codec_SteerBoat: ByteCodec[SteerBoat] = autogenerateFor[SteerBoat]
  val codec_PickItem: ByteCodec[PickItem] = autogenerateFor[PickItem]
  val codec_CraftRecipeRequest: ByteCodec[CraftRecipeRequest] = autogenerateFor[CraftRecipeRequest]
  val codec_ClientAbilities_f32: ByteCodec[ClientAbilities_f32] = autogenerateFor[ClientAbilities_f32]
  val codec_ClientAbilities_u8: ByteCodec[ClientAbilities_u8] = autogenerateFor[ClientAbilities_u8]
  val codec_PlayerDigging: ByteCodec[PlayerDigging] = autogenerateFor[PlayerDigging]
  val codec_PlayerDigging_u8: ByteCodec[PlayerDigging_u8] = autogenerateFor[PlayerDigging_u8]
  val codec_PlayerDigging_u8_u8y: ByteCodec[PlayerDigging_u8_u8y] = autogenerateFor[PlayerDigging_u8_u8y]
  val codec_PlayerAction: ByteCodec[PlayerAction] = autogenerateFor[PlayerAction]
  val codec_PlayerAction_i32: ByteCodec[PlayerAction_i32] = autogenerateFor[PlayerAction_i32]
  val codec_SteerVehicle: ByteCodec[SteerVehicle] = autogenerateFor[SteerVehicle]
  val codec_SteerVehicle_jump_unmount: ByteCodec[SteerVehicle_jump_unmount] = autogenerateFor[SteerVehicle_jump_unmount]
  val codec_CraftingBookData: ByteCodec[CraftingBookData] = autogenerateFor[CraftingBookData]
  val codec_Pong: ByteCodec[Pong] = autogenerateFor[Pong]
  val codec_SetDisplayedRecipe: ByteCodec[SetDisplayedRecipe] = autogenerateFor[SetDisplayedRecipe]
  val codec_SetRecipeBookState: ByteCodec[SetRecipeBookState] = autogenerateFor[SetRecipeBookState]
  val codec_NameItem: ByteCodec[NameItem] = autogenerateFor[NameItem]
  val codec_ResourcePackStatus: ByteCodec[ResourcePackStatus] = autogenerateFor[ResourcePackStatus]
  val codec_ResourcePackStatus_hash: ByteCodec[ResourcePackStatus_hash] = autogenerateFor[ResourcePackStatus_hash]
  val codec_AdvancementTab: ByteCodec[AdvancementTab] = autogenerateFor[AdvancementTab]
  val codec_SelectTrade: ByteCodec[SelectTrade] = autogenerateFor[SelectTrade]
  val codec_SetBeaconEffect: ByteCodec[SetBeaconEffect] = autogenerateFor[SetBeaconEffect]
  val codec_HeldItemChange: ByteCodec[HeldItemChange] = autogenerateFor[HeldItemChange]
  val codec_UpdateCommandBlock: ByteCodec[UpdateCommandBlock] = autogenerateFor[UpdateCommandBlock]
  val codec_UpdateCommandBlockMinecart: ByteCodec[UpdateCommandBlockMinecart] = autogenerateFor[UpdateCommandBlockMinecart]
  val codec_UpdateJigsawBlock_Joint: ByteCodec[UpdateJigsawBlock_Joint] = autogenerateFor[UpdateJigsawBlock_Joint]
  val codec_UpdateJigsawBlock_Type: ByteCodec[UpdateJigsawBlock_Type] = autogenerateFor[UpdateJigsawBlock_Type]
  val codec_UpdateStructureBlock: ByteCodec[UpdateStructureBlock] = autogenerateFor[UpdateStructureBlock]
  val codec_SetSign: ByteCodec[SetSign] = autogenerateFor[SetSign]
  val codec_SetSign_i16y: ByteCodec[SetSign_i16y] = autogenerateFor[SetSign_i16y]
  val codec_ArmSwing: ByteCodec[ArmSwing] = autogenerateFor[ArmSwing]
  val codec_ArmSwing_Handsfree: ByteCodec[ArmSwing_Handsfree] = autogenerateFor[ArmSwing_Handsfree]
  val codec_ArmSwing_Handsfree_ID: ByteCodec[ArmSwing_Handsfree_ID] = autogenerateFor[ArmSwing_Handsfree_ID]
  val codec_SpectateTeleport: ByteCodec[SpectateTeleport] = autogenerateFor[SpectateTeleport]
  val codec_PlayerBlockPlacement_f32: ByteCodec[PlayerBlockPlacement_f32] = autogenerateFor[PlayerBlockPlacement_f32]
  val codec_PlayerBlockPlacement_u8: ByteCodec[PlayerBlockPlacement_u8] = autogenerateFor[PlayerBlockPlacement_u8]
  val codec_PlayerBlockPlacement_u8_Item: ByteCodec[PlayerBlockPlacement_u8_Item] = autogenerateFor[PlayerBlockPlacement_u8_Item]
  val codec_PlayerBlockPlacement_u8_Item_u8y: ByteCodec[PlayerBlockPlacement_u8_Item_u8y] = autogenerateFor[PlayerBlockPlacement_u8_Item_u8y]
  val codec_PlayerBlockPlacement_insideblock: ByteCodec[PlayerBlockPlacement_insideblock] = autogenerateFor[PlayerBlockPlacement_insideblock]
  val codec_UseItem: ByteCodec[UseItem] = autogenerateFor[UseItem]
  val codec_SpawnObject: ByteCodec[SpawnObject] = autogenerateFor[SpawnObject]
  val codec_SpawnObject_i32: ByteCodec[SpawnObject_i32] = autogenerateFor[SpawnObject_i32]
  val codec_SpawnObject_i32_NoUUID: ByteCodec[SpawnObject_i32_NoUUID] = autogenerateFor[SpawnObject_i32_NoUUID]
  val codec_SpawnObject_VarInt: ByteCodec[SpawnObject_VarInt] = autogenerateFor[SpawnObject_VarInt]
  val codec_SpawnExperienceOrb: ByteCodec[SpawnExperienceOrb] = autogenerateFor[SpawnExperienceOrb]
  val codec_SpawnExperienceOrb_i32: ByteCodec[SpawnExperienceOrb_i32] = autogenerateFor[SpawnExperienceOrb_i32]
  val codec_SpawnGlobalEntity: ByteCodec[SpawnGlobalEntity] = autogenerateFor[SpawnGlobalEntity]
  val codec_SpawnGlobalEntity_i32: ByteCodec[SpawnGlobalEntity_i32] = autogenerateFor[SpawnGlobalEntity_i32]
  val codec_SpawnMob_NoMeta: ByteCodec[SpawnMob_NoMeta] = autogenerateFor[SpawnMob_NoMeta]
  val codec_SpawnMob_WithMeta: ByteCodec[SpawnMob_WithMeta] = autogenerateFor[SpawnMob_WithMeta]
  val codec_SpawnMob_u8: ByteCodec[SpawnMob_u8] = autogenerateFor[SpawnMob_u8]
  val codec_SpawnMob_u8_i32: ByteCodec[SpawnMob_u8_i32] = autogenerateFor[SpawnMob_u8_i32]
  val codec_SpawnMob_u8_i32_NoUUID: ByteCodec[SpawnMob_u8_i32_NoUUID] = autogenerateFor[SpawnMob_u8_i32_NoUUID]
  val codec_SpawnPainting_VarInt: ByteCodec[SpawnPainting_VarInt] = autogenerateFor[SpawnPainting_VarInt]
  val codec_SpawnPainting_String: ByteCodec[SpawnPainting_String] = autogenerateFor[SpawnPainting_String]
  val codec_SpawnPainting_NoUUID: ByteCodec[SpawnPainting_NoUUID] = autogenerateFor[SpawnPainting_NoUUID]
  val codec_SpawnPainting_NoUUID_i32: ByteCodec[SpawnPainting_NoUUID_i32] = autogenerateFor[SpawnPainting_NoUUID_i32]
  val codec_SpawnPlayer_f64_NoMeta: ByteCodec[SpawnPlayer_f64_NoMeta] = autogenerateFor[SpawnPlayer_f64_NoMeta]
  val codec_SpawnPlayer_f64: ByteCodec[SpawnPlayer_f64] = autogenerateFor[SpawnPlayer_f64]
  val codec_SpawnPlayer_i32: ByteCodec[SpawnPlayer_i32] = autogenerateFor[SpawnPlayer_i32]
  val codec_SpawnPlayer_i32_HeldItem: ByteCodec[SpawnPlayer_i32_HeldItem] = autogenerateFor[SpawnPlayer_i32_HeldItem]
  val codec_SpawnPlayer_i32_HeldItem_String: ByteCodec[SpawnPlayer_i32_HeldItem_String] = autogenerateFor[SpawnPlayer_i32_HeldItem_String]
  val codec_Animation: ByteCodec[Animation] = autogenerateFor[Animation]
  val codec_SculkVibrationSignal: ByteCodec[SculkVibrationSignal] = autogenerateFor[SculkVibrationSignal]
  val codec_Statistics: ByteCodec[Statistics] = autogenerateFor[Statistics]
  val codec_BlockBreakAnimation: ByteCodec[BlockBreakAnimation] = autogenerateFor[BlockBreakAnimation]
  val codec_BlockBreakAnimation_i32: ByteCodec[BlockBreakAnimation_i32] = autogenerateFor[BlockBreakAnimation_i32]
  val codec_UpdateBlockEntity: ByteCodec[UpdateBlockEntity] = autogenerateFor[UpdateBlockEntity]
  val codec_UpdateBlockEntity_Data: ByteCodec[UpdateBlockEntity_Data] = autogenerateFor[UpdateBlockEntity_Data]
  val codec_BlockAction: ByteCodec[BlockAction] = autogenerateFor[BlockAction]
  val codec_BlockAction_u16: ByteCodec[BlockAction_u16] = autogenerateFor[BlockAction_u16]
  val codec_BlockChange_VarInt: ByteCodec[BlockChange_VarInt] = autogenerateFor[BlockChange_VarInt]
  val codec_BlockChange_u8: ByteCodec[BlockChange_u8] = autogenerateFor[BlockChange_u8]
  val codec_BossBar: ByteCodec[BossBar] = autogenerateFor[BossBar]
  val codec_ServerDifficulty: ByteCodec[ServerDifficulty] = autogenerateFor[ServerDifficulty]
  val codec_ServerDifficulty_Locked: ByteCodec[ServerDifficulty_Locked] = autogenerateFor[ServerDifficulty_Locked]
  val codec_TabCompleteReply: ByteCodec[TabCompleteReply] = autogenerateFor[TabCompleteReply]
  val codec_DeclareCommands: ByteCodec[DeclareCommands] = autogenerateFor[DeclareCommands]
  val codec_ServerMessage_Sender: ByteCodec[ServerMessage_Sender] = autogenerateFor[ServerMessage_Sender]
  val codec_ServerMessage_Position: ByteCodec[ServerMessage_Position] = autogenerateFor[ServerMessage_Position]
  val codec_ServerMessage_NoPosition: ByteCodec[ServerMessage_NoPosition] = autogenerateFor[ServerMessage_NoPosition]
  val codec_MultiBlockChange_Packed: ByteCodec[MultiBlockChange_Packed] = autogenerateFor[MultiBlockChange_Packed]
  val codec_MultiBlockChange_VarInt: ByteCodec[MultiBlockChange_VarInt] = autogenerateFor[MultiBlockChange_VarInt]
  val codec_MultiBlockChange_u16: ByteCodec[MultiBlockChange_u16] = autogenerateFor[MultiBlockChange_u16]
  val codec_ConfirmTransaction: ByteCodec[ConfirmTransaction] = autogenerateFor[ConfirmTransaction]
  val codec_WindowClose: ByteCodec[WindowClose] = autogenerateFor[WindowClose]
  val codec_WindowOpen: ByteCodec[WindowOpen] = autogenerateFor[WindowOpen]
  val codec_WindowOpenHorse: ByteCodec[WindowOpenHorse] = autogenerateFor[WindowOpenHorse]
  val codec_WindowOpen_u8: ByteCodec[WindowOpen_u8] = autogenerateFor[WindowOpen_u8]
  val codec_WindowOpen_VarInt: ByteCodec[WindowOpen_VarInt] = autogenerateFor[WindowOpen_VarInt]
  val codec_WindowProperty: ByteCodec[WindowProperty] = autogenerateFor[WindowProperty]
  val codec_SetCooldown: ByteCodec[SetCooldown] = autogenerateFor[SetCooldown]
  val codec_PluginMessageClientbound: ByteCodec[PluginMessageClientbound] = autogenerateFor[PluginMessageClientbound]
  val codec_PluginMessageClientbound_i16: ByteCodec[PluginMessageClientbound_i16] = autogenerateFor[PluginMessageClientbound_i16]
  val codec_NamedSoundEffect: ByteCodec[NamedSoundEffect] = autogenerateFor[NamedSoundEffect]
  val codec_NamedSoundEffect_u8: ByteCodec[NamedSoundEffect_u8] = autogenerateFor[NamedSoundEffect_u8]
  val codec_NamedSoundEffect_u8_NoCategory: ByteCodec[NamedSoundEffect_u8_NoCategory] = autogenerateFor[NamedSoundEffect_u8_NoCategory]
  val codec_Disconnect: ByteCodec[Disconnect] = autogenerateFor[Disconnect]
  val codec_EntityAction: ByteCodec[EntityAction] = autogenerateFor[EntityAction]
  val codec_Explosion: ByteCodec[Explosion] = autogenerateFor[Explosion]
  val codec_ChunkUnload: ByteCodec[ChunkUnload] = autogenerateFor[ChunkUnload]
  val codec_SetCompression: ByteCodec[SetCompression] = autogenerateFor[SetCompression]
  val codec_ChangeGameState: ByteCodec[ChangeGameState] = autogenerateFor[ChangeGameState]
  val codec_KeepAliveClientbound_i64: ByteCodec[KeepAliveClientbound_i64] = autogenerateFor[KeepAliveClientbound_i64]
  val codec_KeepAliveClientbound_VarInt: ByteCodec[KeepAliveClientbound_VarInt] = autogenerateFor[KeepAliveClientbound_VarInt]
  val codec_KeepAliveClientbound_i32: ByteCodec[KeepAliveClientbound_i32] = autogenerateFor[KeepAliveClientbound_i32]
  val codec_ChunkData_Biomes3D_VarInt: ByteCodec[ChunkData_Biomes3D_VarInt] = autogenerateFor[ChunkData_Biomes3D_VarInt]
  val codec_ChunkData_Biomes3D_bool: ByteCodec[ChunkData_Biomes3D_bool] = autogenerateFor[ChunkData_Biomes3D_bool]
  val codec_ChunkData_Biomes3D: ByteCodec[ChunkData_Biomes3D] = autogenerateFor[ChunkData_Biomes3D]
  val codec_ChunkData_HeightMap: ByteCodec[ChunkData_HeightMap] = autogenerateFor[ChunkData_HeightMap]
  val codec_ChunkData: ByteCodec[ChunkData] = autogenerateFor[ChunkData]
  val codec_ChunkData_NoEntities: ByteCodec[ChunkData_NoEntities] = autogenerateFor[ChunkData_NoEntities]
  val codec_ChunkData_NoEntities_u16: ByteCodec[ChunkData_NoEntities_u16] = autogenerateFor[ChunkData_NoEntities_u16]
  val codec_ChunkData_17: ByteCodec[ChunkData_17] = autogenerateFor[ChunkData_17]
  val codec_ChunkData_withBlockEntity: ByteCodec[ChunkData_withBlockEntity] = autogenerateFor[ChunkData_withBlockEntity]
  val codec_ChunkDataBulk: ByteCodec[ChunkDataBulk] = autogenerateFor[ChunkDataBulk]
  val codec_ChunkDataBulk_17: ByteCodec[ChunkDataBulk_17] = autogenerateFor[ChunkDataBulk_17]
  val codec_Effect: ByteCodec[Effect] = autogenerateFor[Effect]
  val codec_Effect_u8y: ByteCodec[Effect_u8y] = autogenerateFor[Effect_u8y]

  /**
   * NOTE: Some compiler error relating to https://github.com/lampepfl/dotty/issues/13406
   * prevents us from calling `autogenerateFor` method with these three types.
   *
   * This seems to happen only with a type with more than 15 fields in which at
   * least one Option field is present.
   */
  val codec_Particle_f64: ByteCodec[Particle_f64] = ByteCodec(GenByteDecode.gen[Particle_f64], ByteEncode.forADT[Particle_f64])
  val codec_Particle_Data: ByteCodec[Particle_Data] = ByteCodec(GenByteDecode.gen[Particle_Data], ByteEncode.forADT[Particle_Data])
  val codec_Particle_Data13: ByteCodec[Particle_Data13] = ByteCodec(GenByteDecode.gen[Particle_Data13], ByteEncode.forADT[Particle_Data13])

  val codec_Particle_VarIntArray: ByteCodec[Particle_VarIntArray] = autogenerateFor[Particle_VarIntArray]
  val codec_Particle_Named: ByteCodec[Particle_Named] = autogenerateFor[Particle_Named]
  val codec_JoinGame_WorldNames_IsHard: ByteCodec[JoinGame_WorldNames_IsHard] = autogenerateFor[JoinGame_WorldNames_IsHard]
  val codec_JoinGame_WorldNames: ByteCodec[JoinGame_WorldNames] = autogenerateFor[JoinGame_WorldNames]
  val codec_JoinGame_HashedSeed_Respawn: ByteCodec[JoinGame_HashedSeed_Respawn] = autogenerateFor[JoinGame_HashedSeed_Respawn]
  val codec_JoinGame_i32_ViewDistance: ByteCodec[JoinGame_i32_ViewDistance] = autogenerateFor[JoinGame_i32_ViewDistance]
  val codec_JoinGame_i32: ByteCodec[JoinGame_i32] = autogenerateFor[JoinGame_i32]
  val codec_JoinGame_i8: ByteCodec[JoinGame_i8] = autogenerateFor[JoinGame_i8]
  val codec_JoinGame_i8_NoDebug: ByteCodec[JoinGame_i8_NoDebug] = autogenerateFor[JoinGame_i8_NoDebug]
  val codec_Maps: ByteCodec[Maps] = autogenerateFor[Maps]
  val codec_Maps_NoLocked: ByteCodec[Maps_NoLocked] = autogenerateFor[Maps_NoLocked]
  val codec_Maps_NoTracking: ByteCodec[Maps_NoTracking] = autogenerateFor[Maps_NoTracking]
  val codec_Maps_NoTracking_Data: ByteCodec[Maps_NoTracking_Data] = autogenerateFor[Maps_NoTracking_Data]
  val codec_EntityMove_i16: ByteCodec[EntityMove_i16] = autogenerateFor[EntityMove_i16]
  val codec_EntityMove_i8: ByteCodec[EntityMove_i8] = autogenerateFor[EntityMove_i8]
  val codec_EntityMove_i8_i32_NoGround: ByteCodec[EntityMove_i8_i32_NoGround] = autogenerateFor[EntityMove_i8_i32_NoGround]
  val codec_EntityLookAndMove_i16: ByteCodec[EntityLookAndMove_i16] = autogenerateFor[EntityLookAndMove_i16]
  val codec_EntityLookAndMove_i8: ByteCodec[EntityLookAndMove_i8] = autogenerateFor[EntityLookAndMove_i8]
  val codec_EntityLookAndMove_i8_i32_NoGround: ByteCodec[EntityLookAndMove_i8_i32_NoGround] = autogenerateFor[EntityLookAndMove_i8_i32_NoGround]
  val codec_EntityLook_VarInt: ByteCodec[EntityLook_VarInt] = autogenerateFor[EntityLook_VarInt]
  val codec_EntityLook_i32_NoGround: ByteCodec[EntityLook_i32_NoGround] = autogenerateFor[EntityLook_i32_NoGround]
  val codec_Entity: ByteCodec[Entity] = autogenerateFor[Entity]
  val codec_Entity_i32: ByteCodec[Entity_i32] = autogenerateFor[Entity_i32]
  val codec_EntityUpdateNBT: ByteCodec[EntityUpdateNBT] = autogenerateFor[EntityUpdateNBT]
  val codec_VehicleTeleport: ByteCodec[VehicleTeleport] = autogenerateFor[VehicleTeleport]
  val codec_OpenBook: ByteCodec[OpenBook] = autogenerateFor[OpenBook]
  val codec_SignEditorOpen: ByteCodec[SignEditorOpen] = autogenerateFor[SignEditorOpen]
  val codec_SignEditorOpen_i32: ByteCodec[SignEditorOpen_i32] = autogenerateFor[SignEditorOpen_i32]
  val codec_Ping: ByteCodec[Ping] = autogenerateFor[Ping]
  val codec_CraftRecipeResponse: ByteCodec[CraftRecipeResponse] = autogenerateFor[CraftRecipeResponse]
  val codec_PlayerAbilities: ByteCodec[PlayerAbilities] = autogenerateFor[PlayerAbilities]
  val codec_CombatEvent: ByteCodec[CombatEvent] = autogenerateFor[CombatEvent]
  val codec_EndCombatEvent: ByteCodec[EndCombatEvent] = autogenerateFor[EndCombatEvent]
  val codec_EnterCombatEvent: ByteCodec[EnterCombatEvent] = autogenerateFor[EnterCombatEvent]
  val codec_DeathCombatEvent: ByteCodec[DeathCombatEvent] = autogenerateFor[DeathCombatEvent]
  val codec_PlayerInfo: ByteCodec[PlayerInfo] = autogenerateFor[PlayerInfo]
  val codec_PlayerInfo_String: ByteCodec[PlayerInfo_String] = autogenerateFor[PlayerInfo_String]
  val codec_FacePlayer: ByteCodec[FacePlayer] = autogenerateFor[FacePlayer]
  val codec_TeleportPlayer_WithConfirm: ByteCodec[TeleportPlayer_WithConfirm] = autogenerateFor[TeleportPlayer_WithConfirm]
  val codec_TeleportPlayer_WithDismount: ByteCodec[TeleportPlayer_WithDismount] = autogenerateFor[TeleportPlayer_WithDismount]
  val codec_TeleportPlayer_NoConfirm: ByteCodec[TeleportPlayer_NoConfirm] = autogenerateFor[TeleportPlayer_NoConfirm]
  val codec_TeleportPlayer_OnGround: ByteCodec[TeleportPlayer_OnGround] = autogenerateFor[TeleportPlayer_OnGround]
  val codec_EntityUsedBed: ByteCodec[EntityUsedBed] = autogenerateFor[EntityUsedBed]
  val codec_EntityUsedBed_i32: ByteCodec[EntityUsedBed_i32] = autogenerateFor[EntityUsedBed_i32]
  val codec_UnlockRecipes_NoSmelting: ByteCodec[UnlockRecipes_NoSmelting] = autogenerateFor[UnlockRecipes_NoSmelting]
  val codec_UnlockRecipes_WithSmelting: ByteCodec[UnlockRecipes_WithSmelting] = autogenerateFor[UnlockRecipes_WithSmelting]
  val codec_UnlockRecipes_WithBlastSmoker: ByteCodec[UnlockRecipes_WithBlastSmoker] = autogenerateFor[UnlockRecipes_WithBlastSmoker]
  val codec_EntityDestroy: ByteCodec[EntityDestroy] = autogenerateFor[EntityDestroy]
  val codec_EntityDestroy_u8: ByteCodec[EntityDestroy_u8] = autogenerateFor[EntityDestroy_u8]
  val codec_EntityRemoveEffect: ByteCodec[EntityRemoveEffect] = autogenerateFor[EntityRemoveEffect]
  val codec_EntityRemoveEffect_i32: ByteCodec[EntityRemoveEffect_i32] = autogenerateFor[EntityRemoveEffect_i32]
  val codec_ResourcePackSend: ByteCodec[ResourcePackSend] = autogenerateFor[ResourcePackSend]
  val codec_Respawn_Gamemode: ByteCodec[Respawn_Gamemode] = autogenerateFor[Respawn_Gamemode]
  val codec_Respawn_HashedSeed: ByteCodec[Respawn_HashedSeed] = autogenerateFor[Respawn_HashedSeed]
  val codec_Respawn_NBT: ByteCodec[Respawn_NBT] = autogenerateFor[Respawn_NBT]
  val codec_Respawn_WorldName: ByteCodec[Respawn_WorldName] = autogenerateFor[Respawn_WorldName]
  val codec_EntityHeadLook: ByteCodec[EntityHeadLook] = autogenerateFor[EntityHeadLook]
  val codec_EntityHeadLook_i32: ByteCodec[EntityHeadLook_i32] = autogenerateFor[EntityHeadLook_i32]
  val codec_EntityStatus: ByteCodec[EntityStatus] = autogenerateFor[EntityStatus]
  val codec_NBTQueryResponse: ByteCodec[NBTQueryResponse] = autogenerateFor[NBTQueryResponse]
  val codec_SelectAdvancementTab: ByteCodec[SelectAdvancementTab] = autogenerateFor[SelectAdvancementTab]
  val codec_ActionBar: ByteCodec[ActionBar] = autogenerateFor[ActionBar]
  val codec_WorldBorder: ByteCodec[WorldBorder] = autogenerateFor[WorldBorder]
  val codec_WorldBorderInitialize: ByteCodec[WorldBorderInitialize] = autogenerateFor[WorldBorderInitialize]
  val codec_WorldBorderCenter: ByteCodec[WorldBorderCenter] = autogenerateFor[WorldBorderCenter]
  val codec_WorldBorderLerpSize: ByteCodec[WorldBorderLerpSize] = autogenerateFor[WorldBorderLerpSize]
  val codec_WorldBorderSize: ByteCodec[WorldBorderSize] = autogenerateFor[WorldBorderSize]
  val codec_WorldBorderWarningDelay: ByteCodec[WorldBorderWarningDelay] = autogenerateFor[WorldBorderWarningDelay]
  val codec_WorldBorderWarningReach: ByteCodec[WorldBorderWarningReach] = autogenerateFor[WorldBorderWarningReach]
  val codec_Camera: ByteCodec[Camera] = autogenerateFor[Camera]
  val codec_SetCurrentHotbarSlot: ByteCodec[SetCurrentHotbarSlot] = autogenerateFor[SetCurrentHotbarSlot]
  val codec_UpdateViewPosition: ByteCodec[UpdateViewPosition] = autogenerateFor[UpdateViewPosition]
  val codec_UpdateViewDistance: ByteCodec[UpdateViewDistance] = autogenerateFor[UpdateViewDistance]
  val codec_ScoreboardDisplay: ByteCodec[ScoreboardDisplay] = autogenerateFor[ScoreboardDisplay]
  val codec_EntityMetadata: ByteCodec[EntityMetadata] = autogenerateFor[EntityMetadata]
  val codec_EntityMetadata_i32: ByteCodec[EntityMetadata_i32] = autogenerateFor[EntityMetadata_i32]
  val codec_EntityAttach: ByteCodec[EntityAttach] = autogenerateFor[EntityAttach]
  val codec_EntityAttach_leashed: ByteCodec[EntityAttach_leashed] = autogenerateFor[EntityAttach_leashed]
  val codec_EntityVelocity: ByteCodec[EntityVelocity] = autogenerateFor[EntityVelocity]
  val codec_EntityVelocity_i32: ByteCodec[EntityVelocity_i32] = autogenerateFor[EntityVelocity_i32]
  val codec_EntityEquipment_Array: ByteCodec[EntityEquipment_Array] = autogenerateFor[EntityEquipment_Array]
  val codec_EntityEquipment_u16: ByteCodec[EntityEquipment_u16] = autogenerateFor[EntityEquipment_u16]
  val codec_EntityEquipment_u16_i32: ByteCodec[EntityEquipment_u16_i32] = autogenerateFor[EntityEquipment_u16_i32]
  val codec_SetExperience: ByteCodec[SetExperience] = autogenerateFor[SetExperience]
  val codec_SetExperience_i16: ByteCodec[SetExperience_i16] = autogenerateFor[SetExperience_i16]
  val codec_UpdateHealth: ByteCodec[UpdateHealth] = autogenerateFor[UpdateHealth]
  val codec_UpdateHealth_u16: ByteCodec[UpdateHealth_u16] = autogenerateFor[UpdateHealth_u16]
  val codec_ScoreboardObjective: ByteCodec[ScoreboardObjective] = autogenerateFor[ScoreboardObjective]
  val codec_ScoreboardObjective_NoMode: ByteCodec[ScoreboardObjective_NoMode] = autogenerateFor[ScoreboardObjective_NoMode]
  val codec_SetPassengers: ByteCodec[SetPassengers] = autogenerateFor[SetPassengers]
  val codec_Teams_VarInt: ByteCodec[Teams_VarInt] = autogenerateFor[Teams_VarInt]
  val codec_Teams_u8: ByteCodec[Teams_u8] = autogenerateFor[Teams_u8]
  val codec_Teams_NoVisColor: ByteCodec[Teams_NoVisColor] = autogenerateFor[Teams_NoVisColor]
  val codec_UpdateScore: ByteCodec[UpdateScore] = autogenerateFor[UpdateScore]
  val codec_UpdateScore_i32: ByteCodec[UpdateScore_i32] = autogenerateFor[UpdateScore_i32]
  val codec_SpawnPosition: ByteCodec[SpawnPosition] = autogenerateFor[SpawnPosition]
  val codec_SpawnPosition_i32: ByteCodec[SpawnPosition_i32] = autogenerateFor[SpawnPosition_i32]
  val codec_SpawnPositionWithAngle: ByteCodec[SpawnPositionWithAngle] = autogenerateFor[SpawnPositionWithAngle]
  val codec_TimeUpdate: ByteCodec[TimeUpdate] = autogenerateFor[TimeUpdate]
  val codec_StopSound: ByteCodec[StopSound] = autogenerateFor[StopSound]
  val codec_Title: ByteCodec[Title] = autogenerateFor[Title]
  val codec_Title_notext: ByteCodec[Title_notext] = autogenerateFor[Title_notext]
  val codec_Title_notext_component: ByteCodec[Title_notext_component] = autogenerateFor[Title_notext_component]
  val codec_Title_onlytext: ByteCodec[Title_onlytext] = autogenerateFor[Title_onlytext]
  val codec_SubTitle: ByteCodec[SubTitle] = autogenerateFor[SubTitle]
  val codec_TitleFade: ByteCodec[TitleFade] = autogenerateFor[TitleFade]
  val codec_ClearTitle: ByteCodec[ClearTitle] = autogenerateFor[ClearTitle]
  val codec_UpdateSign: ByteCodec[UpdateSign] = autogenerateFor[UpdateSign]
  val codec_UpdateSign_u16: ByteCodec[UpdateSign_u16] = autogenerateFor[UpdateSign_u16]
  val codec_SoundEffect: ByteCodec[SoundEffect] = autogenerateFor[SoundEffect]
  val codec_SoundEffect_u8: ByteCodec[SoundEffect_u8] = autogenerateFor[SoundEffect_u8]
  val codec_EntitySoundEffect: ByteCodec[EntitySoundEffect] = autogenerateFor[EntitySoundEffect]
  val codec_PlayerListHeaderFooter: ByteCodec[PlayerListHeaderFooter] = autogenerateFor[PlayerListHeaderFooter]
  val codec_CollectItem: ByteCodec[CollectItem] = autogenerateFor[CollectItem]
  val codec_CollectItem_nocount: ByteCodec[CollectItem_nocount] = autogenerateFor[CollectItem_nocount]
  val codec_CollectItem_nocount_i32: ByteCodec[CollectItem_nocount_i32] = autogenerateFor[CollectItem_nocount_i32]
  val codec_EntityTeleport_f64: ByteCodec[EntityTeleport_f64] = autogenerateFor[EntityTeleport_f64]
  val codec_EntityTeleport_i32: ByteCodec[EntityTeleport_i32] = autogenerateFor[EntityTeleport_i32]
  val codec_EntityTeleport_i32_i32_NoGround: ByteCodec[EntityTeleport_i32_i32_NoGround] = autogenerateFor[EntityTeleport_i32_i32_NoGround]
  val codec_Advancements: ByteCodec[Advancements] = autogenerateFor[Advancements]
  val codec_EntityProperties: ByteCodec[EntityProperties] = autogenerateFor[EntityProperties]
  val codec_EntityProperties_VarIntLength: ByteCodec[EntityProperties_VarIntLength] = autogenerateFor[EntityProperties_VarIntLength]
  val codec_EntityProperties_i32: ByteCodec[EntityProperties_i32] = autogenerateFor[EntityProperties_i32]
  val codec_EntityEffect: ByteCodec[EntityEffect] = autogenerateFor[EntityEffect]
  val codec_EntityEffect_i32: ByteCodec[EntityEffect_i32] = autogenerateFor[EntityEffect_i32]
  val codec_DeclareRecipes: ByteCodec[DeclareRecipes] = autogenerateFor[DeclareRecipes]
  val codec_Tags: ByteCodec[Tags] = autogenerateFor[Tags]
  val codec_TagsWithEntities: ByteCodec[TagsWithEntities] = autogenerateFor[TagsWithEntities]
  val codec_TagsWithTypes: ByteCodec[TagsWithTypes] = autogenerateFor[TagsWithTypes]
  val codec_AcknowledgePlayerDigging: ByteCodec[AcknowledgePlayerDigging] = autogenerateFor[AcknowledgePlayerDigging]
  val codec_UpdateLight_WithTrust: ByteCodec[UpdateLight_WithTrust] = autogenerateFor[UpdateLight_WithTrust]
  val codec_UpdateLight_NoTrust: ByteCodec[UpdateLight_NoTrust] = autogenerateFor[UpdateLight_NoTrust]
  val codec_TradeList_WithoutRestock: ByteCodec[TradeList_WithoutRestock] = autogenerateFor[TradeList_WithoutRestock]
  val codec_TradeList_WithRestock: ByteCodec[TradeList_WithRestock] = autogenerateFor[TradeList_WithRestock]
  val codec_CoFHLib_SendUUID: ByteCodec[CoFHLib_SendUUID] = autogenerateFor[CoFHLib_SendUUID]
  val codec_LoginStart: ByteCodec[LoginStart] = autogenerateFor[LoginStart]
  val codec_EncryptionResponse: ByteCodec[EncryptionResponse] = autogenerateFor[EncryptionResponse]
  val codec_EncryptionResponse_i16: ByteCodec[EncryptionResponse_i16] = autogenerateFor[EncryptionResponse_i16]
  val codec_LoginPluginResponse: ByteCodec[LoginPluginResponse] = autogenerateFor[LoginPluginResponse]
  val codec_LoginDisconnect: ByteCodec[LoginDisconnect] = autogenerateFor[LoginDisconnect]
  val codec_EncryptionRequest: ByteCodec[EncryptionRequest] = autogenerateFor[EncryptionRequest]
  val codec_EncryptionRequest_i16: ByteCodec[EncryptionRequest_i16] = autogenerateFor[EncryptionRequest_i16]
  val codec_LoginSuccess_String: ByteCodec[LoginSuccess_String] = autogenerateFor[LoginSuccess_String]
  val codec_LoginSuccess_UUID: ByteCodec[LoginSuccess_UUID] = autogenerateFor[LoginSuccess_UUID]
  val codec_SetInitialCompression: ByteCodec[SetInitialCompression] = autogenerateFor[SetInitialCompression]
  val codec_LoginPluginRequest: ByteCodec[LoginPluginRequest] = autogenerateFor[LoginPluginRequest]
  val codec_StatusRequest: ByteCodec[StatusRequest] = autogenerateFor[StatusRequest]
  val codec_StatusPing: ByteCodec[StatusPing] = autogenerateFor[StatusPing]
  val codec_StatusResponse: ByteCodec[StatusResponse] = autogenerateFor[StatusResponse]
  val codec_StatusPong: ByteCodec[StatusPong] = autogenerateFor[StatusPong]

  val codec_WindowItems_withState_Slot_Upto_1_17_1: ByteCodec[WindowItems_withState[Slot.Upto_1_17_1]] = autogenerateFor[WindowItems_withState[Slot.Upto_1_17_1]]
  val codec_SetSlot_Slot_Upto_1_17_1: ByteCodec[SetSlot[Slot.Upto_1_17_1]] = autogenerateFor[SetSlot[Slot.Upto_1_17_1]]

  // region polymorphic givens
  private def clickWindow[S <: Slot: ByteCodec]: ByteCodec[ClickWindow[S]] = autogenerateFor[ClickWindow[S]]
  private def creativeInventoryActions[S <: Slot: ByteCodec]: ByteCodec[CreativeInventoryAction[S]] = autogenerateFor[CreativeInventoryAction[S]]
  private def windowSetSlot[S <: Slot: ByteCodec]: ByteCodec[WindowSetSlot[S]] = autogenerateFor[WindowSetSlot[S]]
  private def windowItems[T <: Slot: ByteCodec]: ByteCodec[WindowItems[T]] = autogenerateFor[WindowItems[T]]
  private def entityEquipment_VarInt[S <: Slot: ByteCodec]: ByteCodec[EntityEquipment_VarInt[S]] = autogenerateFor[EntityEquipment_VarInt[S]]
  // endregion

  val clickWindowUpto_1_12_2: ByteCodec[ClickWindow[Slot.Upto_1_12_2]] = clickWindow[Slot.Upto_1_12_2]
  val clickWindowUpto_1_17_1: ByteCodec[ClickWindow[Slot.Upto_1_17_1]] = clickWindow[Slot.Upto_1_17_1]

  val creativeInventoryActions_1_12_2: ByteCodec[CreativeInventoryAction[Slot.Upto_1_12_2]] = creativeInventoryActions[Slot.Upto_1_12_2]
  val creativeInventoryActions_1_17_1: ByteCodec[CreativeInventoryAction[Slot.Upto_1_17_1]] = creativeInventoryActions[Slot.Upto_1_17_1]

  val windowSetSlot_1_12_2: ByteCodec[WindowSetSlot[Slot.Upto_1_12_2]] = windowSetSlot[Slot.Upto_1_12_2]
  val windowSetSlot_1_17_1: ByteCodec[WindowSetSlot[Slot.Upto_1_17_1]] = windowSetSlot[Slot.Upto_1_17_1]

  val windowItems_1_12_2: ByteCodec[WindowItems[Slot.Upto_1_12_2]] = windowItems[Slot.Upto_1_12_2]
  val windowItems_1_17_1: ByteCodec[WindowItems[Slot.Upto_1_17_1]] = windowItems[Slot.Upto_1_17_1]

  val entityEquipment_VarInt_1_12_2: ByteCodec[EntityEquipment_VarInt[Slot.Upto_1_12_2]] = entityEquipment_VarInt[Slot.Upto_1_12_2]
  val entityEquipment_VarInt_1_17_1: ByteCodec[EntityEquipment_VarInt[Slot.Upto_1_17_1]] = entityEquipment_VarInt[Slot.Upto_1_17_1]
  
  // format: on
}

/**
 * The object that will hold codecs for PacketIntent datatypes.
 *
 * Version-specific protocols can import all given members of this object to gain access to
 * given instances of [[ByteCodec]] without constructing codec instances (hence saves some
 * compilation time).
 */
class PacketIntentCodecCache(using ByteCodec[Position]) {
  private val expansion: PacketIntentCodecCacheNonGiven = new PacketIntentCodecCacheNonGiven
  
  // format: off
  given ByteCodec[Handshake] = expansion.codec_Handshake
  given ByteCodec[TeleportConfirm] = expansion.codec_TeleportConfirm
  given ByteCodec[QueryBlockNBT] = expansion.codec_QueryBlockNBT
  given ByteCodec[SetDifficulty] = expansion.codec_SetDifficulty
  given ByteCodec[TabComplete] = expansion.codec_TabComplete
  given ByteCodec[TabComplete_NoAssume] = expansion.codec_TabComplete_NoAssume
  given ByteCodec[TabComplete_NoAssume_NoTarget] = expansion.codec_TabComplete_NoAssume_NoTarget
  given ByteCodec[ChatMessage] = expansion.codec_ChatMessage
  given ByteCodec[ClientStatus] = expansion.codec_ClientStatus
  given ByteCodec[ClientStatus_u8] = expansion.codec_ClientStatus_u8
  given ByteCodec[ClientSettings] = expansion.codec_ClientSettings
  given ByteCodec[ClientSettings_u8] = expansion.codec_ClientSettings_u8
  given ByteCodec[ClientSettings_u8_Handsfree] = expansion.codec_ClientSettings_u8_Handsfree
  given ByteCodec[ClientSettings_u8_Handsfree_Difficulty] = expansion.codec_ClientSettings_u8_Handsfree_Difficulty
  given ByteCodec[ConfirmTransactionServerbound] = expansion.codec_ConfirmTransactionServerbound
  given ByteCodec[EnchantItem] = expansion.codec_EnchantItem
  given ByteCodec[ClickWindowButton] = expansion.codec_ClickWindowButton
  given ByteCodec[ClickWindow_u8] = expansion.codec_ClickWindow_u8
  given ByteCodec[CloseWindow] = expansion.codec_CloseWindow
  given ByteCodec[PluginMessageServerbound] = expansion.codec_PluginMessageServerbound
  given ByteCodec[PluginMessageServerbound_i16] = expansion.codec_PluginMessageServerbound_i16
  given ByteCodec[EditBook] = expansion.codec_EditBook
  given ByteCodec[QueryEntityNBT] = expansion.codec_QueryEntityNBT
  given ByteCodec[UseEntity_Sneakflag] = expansion.codec_UseEntity_Sneakflag
  given ByteCodec[UseEntity_Hand] = expansion.codec_UseEntity_Hand
  given ByteCodec[UseEntity_Handsfree] = expansion.codec_UseEntity_Handsfree
  given ByteCodec[UseEntity_Handsfree_i32] = expansion.codec_UseEntity_Handsfree_i32
  given ByteCodec[GenerateStructure] = expansion.codec_GenerateStructure
  given ByteCodec[KeepAliveServerbound_i64] = expansion.codec_KeepAliveServerbound_i64
  given ByteCodec[KeepAliveServerbound_VarInt] = expansion.codec_KeepAliveServerbound_VarInt
  given ByteCodec[KeepAliveServerbound_i32] = expansion.codec_KeepAliveServerbound_i32
  given ByteCodec[LockDifficulty] = expansion.codec_LockDifficulty
  given ByteCodec[PlayerPosition] = expansion.codec_PlayerPosition
  given ByteCodec[PlayerPosition_HeadY] = expansion.codec_PlayerPosition_HeadY
  given ByteCodec[PlayerPositionLook] = expansion.codec_PlayerPositionLook
  given ByteCodec[PlayerPositionLook_HeadY] = expansion.codec_PlayerPositionLook_HeadY
  given ByteCodec[PlayerLook] = expansion.codec_PlayerLook
  given ByteCodec[Player] = expansion.codec_Player
  given ByteCodec[VehicleMove] = expansion.codec_VehicleMove
  given ByteCodec[SteerBoat] = expansion.codec_SteerBoat
  given ByteCodec[PickItem] = expansion.codec_PickItem
  given ByteCodec[CraftRecipeRequest] = expansion.codec_CraftRecipeRequest
  given ByteCodec[ClientAbilities_f32] = expansion.codec_ClientAbilities_f32
  given ByteCodec[ClientAbilities_u8] = expansion.codec_ClientAbilities_u8
  given ByteCodec[PlayerDigging] = expansion.codec_PlayerDigging
  given ByteCodec[PlayerDigging_u8] = expansion.codec_PlayerDigging_u8
  given ByteCodec[PlayerDigging_u8_u8y] = expansion.codec_PlayerDigging_u8_u8y
  given ByteCodec[PlayerAction] = expansion.codec_PlayerAction
  given ByteCodec[PlayerAction_i32] = expansion.codec_PlayerAction_i32
  given ByteCodec[SteerVehicle] = expansion.codec_SteerVehicle
  given ByteCodec[SteerVehicle_jump_unmount] = expansion.codec_SteerVehicle_jump_unmount
  given ByteCodec[CraftingBookData] = expansion.codec_CraftingBookData
  given ByteCodec[Pong] = expansion.codec_Pong
  given ByteCodec[SetDisplayedRecipe] = expansion.codec_SetDisplayedRecipe
  given ByteCodec[SetRecipeBookState] = expansion.codec_SetRecipeBookState
  given ByteCodec[NameItem] = expansion.codec_NameItem
  given ByteCodec[ResourcePackStatus] = expansion.codec_ResourcePackStatus
  given ByteCodec[ResourcePackStatus_hash] = expansion.codec_ResourcePackStatus_hash
  given ByteCodec[AdvancementTab] = expansion.codec_AdvancementTab
  given ByteCodec[SelectTrade] = expansion.codec_SelectTrade
  given ByteCodec[SetBeaconEffect] = expansion.codec_SetBeaconEffect
  given ByteCodec[HeldItemChange] = expansion.codec_HeldItemChange
  given ByteCodec[UpdateCommandBlock] = expansion.codec_UpdateCommandBlock
  given ByteCodec[UpdateCommandBlockMinecart] = expansion.codec_UpdateCommandBlockMinecart
  given ByteCodec[UpdateJigsawBlock_Joint] = expansion.codec_UpdateJigsawBlock_Joint
  given ByteCodec[UpdateJigsawBlock_Type] = expansion.codec_UpdateJigsawBlock_Type
  given ByteCodec[UpdateStructureBlock] = expansion.codec_UpdateStructureBlock
  given ByteCodec[SetSign] = expansion.codec_SetSign
  given ByteCodec[SetSign_i16y] = expansion.codec_SetSign_i16y
  given ByteCodec[ArmSwing] = expansion.codec_ArmSwing
  given ByteCodec[ArmSwing_Handsfree] = expansion.codec_ArmSwing_Handsfree
  given ByteCodec[ArmSwing_Handsfree_ID] = expansion.codec_ArmSwing_Handsfree_ID
  given ByteCodec[SpectateTeleport] = expansion.codec_SpectateTeleport
  given ByteCodec[PlayerBlockPlacement_f32] = expansion.codec_PlayerBlockPlacement_f32
  given ByteCodec[PlayerBlockPlacement_u8] = expansion.codec_PlayerBlockPlacement_u8
  given ByteCodec[PlayerBlockPlacement_u8_Item] = expansion.codec_PlayerBlockPlacement_u8_Item
  given ByteCodec[PlayerBlockPlacement_u8_Item_u8y] = expansion.codec_PlayerBlockPlacement_u8_Item_u8y
  given ByteCodec[PlayerBlockPlacement_insideblock] = expansion.codec_PlayerBlockPlacement_insideblock
  given ByteCodec[UseItem] = expansion.codec_UseItem
  given ByteCodec[SpawnObject] = expansion.codec_SpawnObject
  given ByteCodec[SpawnObject_i32] = expansion.codec_SpawnObject_i32
  given ByteCodec[SpawnObject_i32_NoUUID] = expansion.codec_SpawnObject_i32_NoUUID
  given ByteCodec[SpawnObject_VarInt] = expansion.codec_SpawnObject_VarInt
  given ByteCodec[SpawnExperienceOrb] = expansion.codec_SpawnExperienceOrb
  given ByteCodec[SpawnExperienceOrb_i32] = expansion.codec_SpawnExperienceOrb_i32
  given ByteCodec[SpawnGlobalEntity] = expansion.codec_SpawnGlobalEntity
  given ByteCodec[SpawnGlobalEntity_i32] = expansion.codec_SpawnGlobalEntity_i32
  given ByteCodec[SpawnMob_NoMeta] = expansion.codec_SpawnMob_NoMeta
  given ByteCodec[SpawnMob_WithMeta] = expansion.codec_SpawnMob_WithMeta
  given ByteCodec[SpawnMob_u8] = expansion.codec_SpawnMob_u8
  given ByteCodec[SpawnMob_u8_i32] = expansion.codec_SpawnMob_u8_i32
  given ByteCodec[SpawnMob_u8_i32_NoUUID] = expansion.codec_SpawnMob_u8_i32_NoUUID
  given ByteCodec[SpawnPainting_VarInt] = expansion.codec_SpawnPainting_VarInt
  given ByteCodec[SpawnPainting_String] = expansion.codec_SpawnPainting_String
  given ByteCodec[SpawnPainting_NoUUID] = expansion.codec_SpawnPainting_NoUUID
  given ByteCodec[SpawnPainting_NoUUID_i32] = expansion.codec_SpawnPainting_NoUUID_i32
  given ByteCodec[SpawnPlayer_f64_NoMeta] = expansion.codec_SpawnPlayer_f64_NoMeta
  given ByteCodec[SpawnPlayer_f64] = expansion.codec_SpawnPlayer_f64
  given ByteCodec[SpawnPlayer_i32] = expansion.codec_SpawnPlayer_i32
  given ByteCodec[SpawnPlayer_i32_HeldItem] = expansion.codec_SpawnPlayer_i32_HeldItem
  given ByteCodec[SpawnPlayer_i32_HeldItem_String] = expansion.codec_SpawnPlayer_i32_HeldItem_String
  given ByteCodec[Animation] = expansion.codec_Animation
  given ByteCodec[SculkVibrationSignal] = expansion.codec_SculkVibrationSignal
  given ByteCodec[Statistics] = expansion.codec_Statistics
  given ByteCodec[BlockBreakAnimation] = expansion.codec_BlockBreakAnimation
  given ByteCodec[BlockBreakAnimation_i32] = expansion.codec_BlockBreakAnimation_i32
  given ByteCodec[UpdateBlockEntity] = expansion.codec_UpdateBlockEntity
  given ByteCodec[UpdateBlockEntity_Data] = expansion.codec_UpdateBlockEntity_Data
  given ByteCodec[BlockAction] = expansion.codec_BlockAction
  given ByteCodec[BlockAction_u16] = expansion.codec_BlockAction_u16
  given ByteCodec[BlockChange_VarInt] = expansion.codec_BlockChange_VarInt
  given ByteCodec[BlockChange_u8] = expansion.codec_BlockChange_u8
  given ByteCodec[BossBar] = expansion.codec_BossBar
  given ByteCodec[ServerDifficulty] = expansion.codec_ServerDifficulty
  given ByteCodec[ServerDifficulty_Locked] = expansion.codec_ServerDifficulty_Locked
  given ByteCodec[TabCompleteReply] = expansion.codec_TabCompleteReply
  given ByteCodec[DeclareCommands] = expansion.codec_DeclareCommands
  given ByteCodec[ServerMessage_Sender] = expansion.codec_ServerMessage_Sender
  given ByteCodec[ServerMessage_Position] = expansion.codec_ServerMessage_Position
  given ByteCodec[ServerMessage_NoPosition] = expansion.codec_ServerMessage_NoPosition
  given ByteCodec[MultiBlockChange_Packed] = expansion.codec_MultiBlockChange_Packed
  given ByteCodec[MultiBlockChange_VarInt] = expansion.codec_MultiBlockChange_VarInt
  given ByteCodec[MultiBlockChange_u16] = expansion.codec_MultiBlockChange_u16
  given ByteCodec[ConfirmTransaction] = expansion.codec_ConfirmTransaction
  given ByteCodec[WindowClose] = expansion.codec_WindowClose
  given ByteCodec[WindowOpen] = expansion.codec_WindowOpen
  given ByteCodec[WindowOpenHorse] = expansion.codec_WindowOpenHorse
  given ByteCodec[WindowOpen_u8] = expansion.codec_WindowOpen_u8
  given ByteCodec[WindowOpen_VarInt] = expansion.codec_WindowOpen_VarInt
  given ByteCodec[WindowProperty] = expansion.codec_WindowProperty
  given ByteCodec[SetCooldown] = expansion.codec_SetCooldown
  given ByteCodec[PluginMessageClientbound] = expansion.codec_PluginMessageClientbound
  given ByteCodec[PluginMessageClientbound_i16] = expansion.codec_PluginMessageClientbound_i16
  given ByteCodec[NamedSoundEffect] = expansion.codec_NamedSoundEffect
  given ByteCodec[NamedSoundEffect_u8] = expansion.codec_NamedSoundEffect_u8
  given ByteCodec[NamedSoundEffect_u8_NoCategory] = expansion.codec_NamedSoundEffect_u8_NoCategory
  given ByteCodec[Disconnect] = expansion.codec_Disconnect
  given ByteCodec[EntityAction] = expansion.codec_EntityAction
  given ByteCodec[Explosion] = expansion.codec_Explosion
  given ByteCodec[ChunkUnload] = expansion.codec_ChunkUnload
  given ByteCodec[SetCompression] = expansion.codec_SetCompression
  given ByteCodec[ChangeGameState] = expansion.codec_ChangeGameState
  given ByteCodec[KeepAliveClientbound_i64] = expansion.codec_KeepAliveClientbound_i64
  given ByteCodec[KeepAliveClientbound_VarInt] = expansion.codec_KeepAliveClientbound_VarInt
  given ByteCodec[KeepAliveClientbound_i32] = expansion.codec_KeepAliveClientbound_i32
  given ByteCodec[ChunkData_Biomes3D_VarInt] = expansion.codec_ChunkData_Biomes3D_VarInt
  given ByteCodec[ChunkData_Biomes3D_bool] = expansion.codec_ChunkData_Biomes3D_bool
  given ByteCodec[ChunkData_Biomes3D] = expansion.codec_ChunkData_Biomes3D
  given ByteCodec[ChunkData_HeightMap] = expansion.codec_ChunkData_HeightMap
  given ByteCodec[ChunkData] = expansion.codec_ChunkData
  given ByteCodec[ChunkData_NoEntities] = expansion.codec_ChunkData_NoEntities
  given ByteCodec[ChunkData_NoEntities_u16] = expansion.codec_ChunkData_NoEntities_u16
  given ByteCodec[ChunkData_17] = expansion.codec_ChunkData_17
  given ByteCodec[ChunkData_withBlockEntity] = expansion.codec_ChunkData_withBlockEntity
  given ByteCodec[ChunkDataBulk] = expansion.codec_ChunkDataBulk
  given ByteCodec[ChunkDataBulk_17] = expansion.codec_ChunkDataBulk_17
  given ByteCodec[Effect] = expansion.codec_Effect
  given ByteCodec[Effect_u8y] = expansion.codec_Effect_u8y

  /**
   * NOTE: Some compiler error relating to https://github.com/lampepfl/dotty/issues/13406
   * prevents us from calling `autogenerateFor` method with these three types.
   *
   * This seems to happen only with a type with more than 15 fields in which at
   * least one Option field is present.
   */
  given ByteCodec[Particle_f64] = expansion.codec_Particle_f64
  given ByteCodec[Particle_Data] = expansion.codec_Particle_Data
  given ByteCodec[Particle_Data13] = expansion.codec_Particle_Data13

  given ByteCodec[Particle_VarIntArray] = expansion.codec_Particle_VarIntArray
  given ByteCodec[Particle_Named] = expansion.codec_Particle_Named
  given ByteCodec[JoinGame_WorldNames_IsHard] = expansion.codec_JoinGame_WorldNames_IsHard
  given ByteCodec[JoinGame_WorldNames] = expansion.codec_JoinGame_WorldNames
  given ByteCodec[JoinGame_HashedSeed_Respawn] = expansion.codec_JoinGame_HashedSeed_Respawn
  given ByteCodec[JoinGame_i32_ViewDistance] = expansion.codec_JoinGame_i32_ViewDistance
  given ByteCodec[JoinGame_i32] = expansion.codec_JoinGame_i32
  given ByteCodec[JoinGame_i8] = expansion.codec_JoinGame_i8
  given ByteCodec[JoinGame_i8_NoDebug] = expansion.codec_JoinGame_i8_NoDebug
  given ByteCodec[Maps] = expansion.codec_Maps
  given ByteCodec[Maps_NoLocked] = expansion.codec_Maps_NoLocked
  given ByteCodec[Maps_NoTracking] = expansion.codec_Maps_NoTracking
  given ByteCodec[Maps_NoTracking_Data] = expansion.codec_Maps_NoTracking_Data
  given ByteCodec[EntityMove_i16] = expansion.codec_EntityMove_i16
  given ByteCodec[EntityMove_i8] = expansion.codec_EntityMove_i8
  given ByteCodec[EntityMove_i8_i32_NoGround] = expansion.codec_EntityMove_i8_i32_NoGround
  given ByteCodec[EntityLookAndMove_i16] = expansion.codec_EntityLookAndMove_i16
  given ByteCodec[EntityLookAndMove_i8] = expansion.codec_EntityLookAndMove_i8
  given ByteCodec[EntityLookAndMove_i8_i32_NoGround] = expansion.codec_EntityLookAndMove_i8_i32_NoGround
  given ByteCodec[EntityLook_VarInt] = expansion.codec_EntityLook_VarInt
  given ByteCodec[EntityLook_i32_NoGround] = expansion.codec_EntityLook_i32_NoGround
  given ByteCodec[Entity] = expansion.codec_Entity
  given ByteCodec[Entity_i32] = expansion.codec_Entity_i32
  given ByteCodec[EntityUpdateNBT] = expansion.codec_EntityUpdateNBT
  given ByteCodec[VehicleTeleport] = expansion.codec_VehicleTeleport
  given ByteCodec[OpenBook] = expansion.codec_OpenBook
  given ByteCodec[SignEditorOpen] = expansion.codec_SignEditorOpen
  given ByteCodec[SignEditorOpen_i32] = expansion.codec_SignEditorOpen_i32
  given ByteCodec[Ping] = expansion.codec_Ping
  given ByteCodec[CraftRecipeResponse] = expansion.codec_CraftRecipeResponse
  given ByteCodec[PlayerAbilities] = expansion.codec_PlayerAbilities
  given ByteCodec[CombatEvent] = expansion.codec_CombatEvent
  given ByteCodec[EndCombatEvent] = expansion.codec_EndCombatEvent
  given ByteCodec[EnterCombatEvent] = expansion.codec_EnterCombatEvent
  given ByteCodec[DeathCombatEvent] = expansion.codec_DeathCombatEvent
  given ByteCodec[PlayerInfo] = expansion.codec_PlayerInfo
  given ByteCodec[PlayerInfo_String] = expansion.codec_PlayerInfo_String
  given ByteCodec[FacePlayer] = expansion.codec_FacePlayer
  given ByteCodec[TeleportPlayer_WithConfirm] = expansion.codec_TeleportPlayer_WithConfirm
  given ByteCodec[TeleportPlayer_WithDismount] = expansion.codec_TeleportPlayer_WithDismount
  given ByteCodec[TeleportPlayer_NoConfirm] = expansion.codec_TeleportPlayer_NoConfirm
  given ByteCodec[TeleportPlayer_OnGround] = expansion.codec_TeleportPlayer_OnGround
  given ByteCodec[EntityUsedBed] = expansion.codec_EntityUsedBed
  given ByteCodec[EntityUsedBed_i32] = expansion.codec_EntityUsedBed_i32
  given ByteCodec[UnlockRecipes_NoSmelting] = expansion.codec_UnlockRecipes_NoSmelting
  given ByteCodec[UnlockRecipes_WithSmelting] = expansion.codec_UnlockRecipes_WithSmelting
  given ByteCodec[UnlockRecipes_WithBlastSmoker] = expansion.codec_UnlockRecipes_WithBlastSmoker
  given ByteCodec[EntityDestroy] = expansion.codec_EntityDestroy
  given ByteCodec[EntityDestroy_u8] = expansion.codec_EntityDestroy_u8
  given ByteCodec[EntityRemoveEffect] = expansion.codec_EntityRemoveEffect
  given ByteCodec[EntityRemoveEffect_i32] = expansion.codec_EntityRemoveEffect_i32
  given ByteCodec[ResourcePackSend] = expansion.codec_ResourcePackSend
  given ByteCodec[Respawn_Gamemode] = expansion.codec_Respawn_Gamemode
  given ByteCodec[Respawn_HashedSeed] = expansion.codec_Respawn_HashedSeed
  given ByteCodec[Respawn_NBT] = expansion.codec_Respawn_NBT
  given ByteCodec[Respawn_WorldName] = expansion.codec_Respawn_WorldName
  given ByteCodec[EntityHeadLook] = expansion.codec_EntityHeadLook
  given ByteCodec[EntityHeadLook_i32] = expansion.codec_EntityHeadLook_i32
  given ByteCodec[EntityStatus] = expansion.codec_EntityStatus
  given ByteCodec[NBTQueryResponse] = expansion.codec_NBTQueryResponse
  given ByteCodec[SelectAdvancementTab] = expansion.codec_SelectAdvancementTab
  given ByteCodec[ActionBar] = expansion.codec_ActionBar
  given ByteCodec[WorldBorder] = expansion.codec_WorldBorder
  given ByteCodec[WorldBorderInitialize] = expansion.codec_WorldBorderInitialize
  given ByteCodec[WorldBorderCenter] = expansion.codec_WorldBorderCenter
  given ByteCodec[WorldBorderLerpSize] = expansion.codec_WorldBorderLerpSize
  given ByteCodec[WorldBorderSize]  = expansion.codec_WorldBorderSize
  given ByteCodec[WorldBorderWarningDelay] = expansion.codec_WorldBorderWarningDelay
  given ByteCodec[WorldBorderWarningReach] = expansion.codec_WorldBorderWarningReach
  given ByteCodec[Camera] = expansion.codec_Camera
  given ByteCodec[SetCurrentHotbarSlot] = expansion.codec_SetCurrentHotbarSlot
  given ByteCodec[UpdateViewPosition] = expansion.codec_UpdateViewPosition
  given ByteCodec[UpdateViewDistance] = expansion.codec_UpdateViewDistance
  given ByteCodec[ScoreboardDisplay] = expansion.codec_ScoreboardDisplay
  given ByteCodec[EntityMetadata] = expansion.codec_EntityMetadata
  given ByteCodec[EntityMetadata_i32] = expansion.codec_EntityMetadata_i32
  given ByteCodec[EntityAttach] = expansion.codec_EntityAttach
  given ByteCodec[EntityAttach_leashed] = expansion.codec_EntityAttach_leashed
  given ByteCodec[EntityVelocity] = expansion.codec_EntityVelocity
  given ByteCodec[EntityVelocity_i32] = expansion.codec_EntityVelocity_i32
  given ByteCodec[EntityEquipment_Array] = expansion.codec_EntityEquipment_Array
  given ByteCodec[EntityEquipment_u16] = expansion.codec_EntityEquipment_u16
  given ByteCodec[EntityEquipment_u16_i32] = expansion.codec_EntityEquipment_u16_i32
  given ByteCodec[SetExperience] = expansion.codec_SetExperience
  given ByteCodec[SetExperience_i16] = expansion.codec_SetExperience_i16
  given ByteCodec[UpdateHealth] = expansion.codec_UpdateHealth
  given ByteCodec[UpdateHealth_u16] = expansion.codec_UpdateHealth_u16
  given ByteCodec[ScoreboardObjective] = expansion.codec_ScoreboardObjective
  given ByteCodec[ScoreboardObjective_NoMode] = expansion.codec_ScoreboardObjective_NoMode
  given ByteCodec[SetPassengers] = expansion.codec_SetPassengers
  given ByteCodec[Teams_VarInt] = expansion.codec_Teams_VarInt
  given ByteCodec[Teams_u8] = expansion.codec_Teams_u8
  given ByteCodec[Teams_NoVisColor] = expansion.codec_Teams_NoVisColor
  given ByteCodec[UpdateScore] = expansion.codec_UpdateScore
  given ByteCodec[UpdateScore_i32] = expansion.codec_UpdateScore_i32
  given ByteCodec[SpawnPosition] = expansion.codec_SpawnPosition
  given ByteCodec[SpawnPosition_i32] = expansion.codec_SpawnPosition_i32
  given ByteCodec[SpawnPositionWithAngle] = expansion.codec_SpawnPositionWithAngle
  given ByteCodec[TimeUpdate] = expansion.codec_TimeUpdate
  given ByteCodec[StopSound] = expansion.codec_StopSound
  given ByteCodec[Title] = expansion.codec_Title
  given ByteCodec[Title_notext] = expansion.codec_Title_notext
  given ByteCodec[Title_notext_component] = expansion.codec_Title_notext_component
  given ByteCodec[Title_onlytext] = expansion.codec_Title_onlytext
  given ByteCodec[SubTitle] = expansion.codec_SubTitle
  given ByteCodec[TitleFade] = expansion.codec_TitleFade
  given ByteCodec[ClearTitle] = expansion.codec_ClearTitle
  given ByteCodec[UpdateSign] = expansion.codec_UpdateSign
  given ByteCodec[UpdateSign_u16] = expansion.codec_UpdateSign_u16
  given ByteCodec[SoundEffect] = expansion.codec_SoundEffect
  given ByteCodec[SoundEffect_u8] = expansion.codec_SoundEffect_u8
  given ByteCodec[EntitySoundEffect] = expansion.codec_EntitySoundEffect
  given ByteCodec[PlayerListHeaderFooter] = expansion.codec_PlayerListHeaderFooter
  given ByteCodec[CollectItem] = expansion.codec_CollectItem
  given ByteCodec[CollectItem_nocount] = expansion.codec_CollectItem_nocount
  given ByteCodec[CollectItem_nocount_i32] = expansion.codec_CollectItem_nocount_i32
  given ByteCodec[EntityTeleport_f64] = expansion.codec_EntityTeleport_f64
  given ByteCodec[EntityTeleport_i32] = expansion.codec_EntityTeleport_i32
  given ByteCodec[EntityTeleport_i32_i32_NoGround] = expansion.codec_EntityTeleport_i32_i32_NoGround
  given ByteCodec[Advancements] = expansion.codec_Advancements
  given ByteCodec[EntityProperties] = expansion.codec_EntityProperties
  given ByteCodec[EntityProperties_VarIntLength] = expansion.codec_EntityProperties_VarIntLength
  given ByteCodec[EntityProperties_i32] = expansion.codec_EntityProperties_i32
  given ByteCodec[EntityEffect] = expansion.codec_EntityEffect
  given ByteCodec[EntityEffect_i32] = expansion.codec_EntityEffect_i32
  given ByteCodec[DeclareRecipes] = expansion.codec_DeclareRecipes
  given ByteCodec[Tags] = expansion.codec_Tags
  given ByteCodec[TagsWithEntities] = expansion.codec_TagsWithEntities
  given ByteCodec[TagsWithTypes] = expansion.codec_TagsWithTypes
  given ByteCodec[AcknowledgePlayerDigging] = expansion.codec_AcknowledgePlayerDigging
  given ByteCodec[UpdateLight_WithTrust] = expansion.codec_UpdateLight_WithTrust
  given ByteCodec[UpdateLight_NoTrust] = expansion.codec_UpdateLight_NoTrust
  given ByteCodec[TradeList_WithoutRestock] = expansion.codec_TradeList_WithoutRestock
  given ByteCodec[TradeList_WithRestock] = expansion.codec_TradeList_WithRestock
  given ByteCodec[CoFHLib_SendUUID] = expansion.codec_CoFHLib_SendUUID
  given ByteCodec[LoginStart] = expansion.codec_LoginStart
  given ByteCodec[EncryptionResponse] = expansion.codec_EncryptionResponse
  given ByteCodec[EncryptionResponse_i16] = expansion.codec_EncryptionResponse_i16
  given ByteCodec[LoginPluginResponse] = expansion.codec_LoginPluginResponse
  given ByteCodec[LoginDisconnect] = expansion.codec_LoginDisconnect
  given ByteCodec[EncryptionRequest] = expansion.codec_EncryptionRequest
  given ByteCodec[EncryptionRequest_i16] = expansion.codec_EncryptionRequest_i16
  given ByteCodec[LoginSuccess_String] = expansion.codec_LoginSuccess_String
  given ByteCodec[LoginSuccess_UUID] = expansion.codec_LoginSuccess_UUID
  given ByteCodec[SetInitialCompression] = expansion.codec_SetInitialCompression
  given ByteCodec[LoginPluginRequest] = expansion.codec_LoginPluginRequest
  given ByteCodec[StatusRequest] = expansion.codec_StatusRequest
  given ByteCodec[StatusPing] = expansion.codec_StatusPing
  given ByteCodec[StatusResponse] = expansion.codec_StatusResponse
  given ByteCodec[StatusPong] = expansion.codec_StatusPong
  given ByteCodec[WindowItems_withState[Slot.Upto_1_17_1]] = expansion.codec_WindowItems_withState_Slot_Upto_1_17_1
  given ByteCodec[SetSlot[Slot.Upto_1_17_1]] = expansion.codec_SetSlot_Slot_Upto_1_17_1
  
  // region monomorphic instantiations of polymorphic codecs
  given clickWindowUpto_1_12_2: ByteCodec[ClickWindow[Slot.Upto_1_12_2]] = expansion.clickWindowUpto_1_12_2
  given clickWindowUpto_1_17_1: ByteCodec[ClickWindow[Slot.Upto_1_17_1]] = expansion.clickWindowUpto_1_17_1

  given creativeInventoryActions_1_12_2: ByteCodec[CreativeInventoryAction[Slot.Upto_1_12_2]] = expansion.creativeInventoryActions_1_12_2
  given creativeInventoryActions_1_17_1: ByteCodec[CreativeInventoryAction[Slot.Upto_1_17_1]] = expansion.creativeInventoryActions_1_17_1

  given windowSetSlot_1_12_2: ByteCodec[WindowSetSlot[Slot.Upto_1_12_2]] = expansion.windowSetSlot_1_12_2
  given windowSetSlot_1_17_1: ByteCodec[WindowSetSlot[Slot.Upto_1_17_1]] = expansion.windowSetSlot_1_17_1

  given windowItems_1_12_2: ByteCodec[WindowItems[Slot.Upto_1_12_2]] = expansion.windowItems_1_12_2
  given windowItems_1_17_1: ByteCodec[WindowItems[Slot.Upto_1_17_1]] = expansion.windowItems_1_17_1

  given entityEquipment_VarInt_1_12_2: ByteCodec[EntityEquipment_VarInt[Slot.Upto_1_12_2]] = expansion.entityEquipment_VarInt_1_12_2
  given entityEquipment_VarInt_1_17_1: ByteCodec[EntityEquipment_VarInt[Slot.Upto_1_17_1]] = expansion.entityEquipment_VarInt_1_17_1
  // endregion

  // format: on
}
