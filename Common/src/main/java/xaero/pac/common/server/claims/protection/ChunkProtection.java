/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU Lesser General Public License
 * (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received copies of the GNU Lesser General Public License
 * and the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.server.claims.protection;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.platform.Services;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ChunkProtection
<
	CM extends IServerClaimsManager<?, ?, ?>,
	M extends IPartyMember, 
	I extends IPartyPlayerInfo, 
	P extends IServerParty<M, I>
> {

	private static final String FORCE_PREFIX = "force$";
	private static final String BREAK_PREFIX = "break$";
	private static final String FORCE_BREAK_PREFIX = "force_break$";
	
	private final Component CANT_INTERACT_BLOCK_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_block", new TranslatableComponent("gui.xaero_claims_protection_main_hand")).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component BLOCK_TRY_EMPTY_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_block_try_empty", new TranslatableComponent("gui.xaero_claims_protection_main_hand")).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component USE_ITEM_MAIN = new TranslatableComponent("gui.xaero_claims_protection_use_item", new TranslatableComponent("gui.xaero_claims_protection_main_hand")).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_INTERACT_ENTITY_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_entity", new TranslatableComponent("gui.xaero_claims_protection_main_hand")).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component ENTITY_TRY_EMPTY_MAIN = new TranslatableComponent("gui.xaero_claims_protection_interact_entity_try_empty", new TranslatableComponent("gui.xaero_claims_protection_main_hand")).withStyle(s -> s.withColor(ChatFormatting.RED));

	private final Component CANT_INTERACT_BLOCK_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_block", new TranslatableComponent("gui.xaero_claims_protection_off_hand")).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component BLOCK_TRY_EMPTY_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_block_try_empty", new TranslatableComponent("gui.xaero_claims_protection_off_hand")).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component USE_ITEM_OFF = new TranslatableComponent("gui.xaero_claims_protection_use_item", new TranslatableComponent("gui.xaero_claims_protection_off_hand")).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component CANT_INTERACT_ENTITY_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_entity", new TranslatableComponent("gui.xaero_claims_protection_off_hand")).withStyle(s -> s.withColor(ChatFormatting.RED));
	private final Component ENTITY_TRY_EMPTY_OFF = new TranslatableComponent("gui.xaero_claims_protection_interact_entity_try_empty", new TranslatableComponent("gui.xaero_claims_protection_off_hand")).withStyle(s -> s.withColor(ChatFormatting.RED));

	private final Component CANT_CHORUS = new TranslatableComponent("gui.xaero_claims_protection_chorus").withStyle(s -> s.withColor(ChatFormatting.RED));

	private final ChunkProtectionEntityHelper entityHelper;
	private final CM claimsManager;
	private final IPartyManager<P> partyManager;
	private final Set<EntityType<?>> friendlyEntityList;
	private final Set<EntityType<?>> hostileEntityList;
	private final Set<Block> optionalEmptyHandExceptionBlocks;
	private final Set<Block> optionalBreakExceptionBlocks;
	private final Set<Block> forcedEmptyHandExceptionBlocks;
	private final Set<Block> forcedBreakExceptionBlocks;
	private final Set<EntityType<?>> optionalEmptyHandExceptionEntities;
	private final Set<EntityType<?>> optionalKillExceptionEntities;
	private final Set<EntityType<?>> forcedEmptyHandExceptionEntities;
	private final Set<EntityType<?>> forcedKillExceptionEntities;
	
	public ChunkProtection(CM claimsManager, IPartyManager<P> partyManager, ChunkProtectionEntityHelper entityHelper) {
		this.claimsManager = claimsManager;
		this.partyManager = partyManager;
		this.entityHelper = entityHelper;
		friendlyEntityList = new HashSet<>();
		hostileEntityList = new HashSet<>();
		optionalEmptyHandExceptionBlocks = new HashSet<>();
		optionalBreakExceptionBlocks = new HashSet<>();
		forcedEmptyHandExceptionBlocks = new HashSet<>();
		forcedBreakExceptionBlocks = new HashSet<>();
		optionalEmptyHandExceptionEntities = new HashSet<>();
		optionalKillExceptionEntities = new HashSet<>();
		forcedEmptyHandExceptionEntities = new HashSet<>();
		forcedKillExceptionEntities = new HashSet<>();
		ServerConfig.CONFIG.friendlyChunkProtectedEntityList.get().forEach(s -> EntityType.byString(s).ifPresent(friendlyEntityList::add));
		ServerConfig.CONFIG.hostileChunkProtectedEntityList.get().forEach(s -> EntityType.byString(s).ifPresent(hostileEntityList::add));

		Function<ResourceLocation, Block> blockGetter = Services.PLATFORM.getBlockRegistry()::getValue;
		Function<ResourceLocation, EntityType<?>> entityGetter = rl -> EntityType.byString(rl.toString()).orElse(null);
		ServerConfig.CONFIG.blockProtectionExceptionList.get()
				.forEach(s -> onExceptionListElement(
						s,
						optionalEmptyHandExceptionBlocks,
						optionalBreakExceptionBlocks,
						forcedEmptyHandExceptionBlocks,
						forcedBreakExceptionBlocks,
						blockGetter
				));
		ServerConfig.CONFIG.entityProtectionExceptionList.get()
				.forEach(s -> onExceptionListElement(
						s,
						optionalEmptyHandExceptionEntities,
						optionalKillExceptionEntities,
						forcedEmptyHandExceptionEntities,
						forcedKillExceptionEntities,
						entityGetter
				));
	}

	private <T> void onExceptionListElement(String element, Set<T> optionalEmptyHandException, Set<T> optionalBreakException, Set<T> forcedEmptyHandException, Set<T> forcedBreakException, Function<ResourceLocation, T> objectGetter){
		String id = element;
		Set<T> destination = optionalEmptyHandException;
		if(element.startsWith(BREAK_PREFIX) || element.startsWith(FORCE_PREFIX) || element.startsWith(FORCE_BREAK_PREFIX)){
			if(element.startsWith(BREAK_PREFIX))
				destination = optionalBreakException;
			else if(element.startsWith(FORCE_PREFIX))
				destination = forcedEmptyHandException;
			else
				destination = forcedBreakException;
			id = element.substring(element.indexOf("$") + 1);
		}
		T object = objectGetter.apply(new ResourceLocation(id));
		if(object != null)
			destination.add(object);
	}
	
	private boolean shouldProtectEntity(IPlayerConfig claimConfig, Entity e, Entity from) {
		if(claimConfig == null || !claimConfig.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS))
			return false;
		return isProtectable(e) &&
				(
					from != null && !(from instanceof Player && entityHelper.isTamed(e, (Player)from)) &&
					(
						from instanceof LivingEntity &&
						(
							from instanceof Player && claimConfig.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_PLAYERS) && !hasChunkAccess(claimConfig, from)
						||
							!(from instanceof Player) && claimConfig.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_MOBS)
						)
					)
				||
					!(from instanceof LivingEntity) && claimConfig.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_ANONYMOUS_ATTACKS)
				);
	}
	
	private boolean isIncludedByLists(Entity e) {
		if(entityHelper.isHostile(e))
			return ServerConfig.CONFIG.hostileChunkProtectedEntityListType.get() == ServerConfig.ConfigListType.ALL_BUT && !hostileEntityList.contains(e.getType()) ||
					ServerConfig.CONFIG.hostileChunkProtectedEntityListType.get() == ServerConfig.ConfigListType.ONLY && hostileEntityList.contains(e.getType());
		return ServerConfig.CONFIG.friendlyChunkProtectedEntityListType.get() == ServerConfig.ConfigListType.ALL_BUT && !friendlyEntityList.contains(e.getType()) ||
				ServerConfig.CONFIG.friendlyChunkProtectedEntityListType.get() == ServerConfig.ConfigListType.ONLY && friendlyEntityList.contains(e.getType());
	}
	
	private boolean isProtectable(Entity e) {
		return !(e instanceof Player) && isIncludedByLists(e);
	}
	
	public boolean hasChunkAccess(IPlayerConfig claimConfig, Entity e) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return true;
		if(claimConfig == null || !claimConfig.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS))
			return true;
		if(e instanceof Player && ServerPlayerDataAPI.from((ServerPlayer) e).isClaimsNonallyMode())
			return false;
		if(e.getUUID().equals(claimConfig.getPlayerId()) || e instanceof Player && ServerPlayerDataAPI.from((ServerPlayer) e).isClaimsAdminMode())
			return true;
		if(!(e instanceof Player) || claimConfig.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS_FROM_PARTY) && claimConfig.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS_FROM_ALLY_PARTIES))
			return false;
		P claimParty = partyManager.getPartyByMember(claimConfig.getPlayerId());
		if(claimParty == null)
			return false;
		P accessorParty = partyManager.getPartyByMember(e.getUUID());
		return accessorParty == claimParty && !claimConfig.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS_FROM_PARTY) || accessorParty != null && !claimConfig.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS_FROM_ALLY_PARTIES) && claimParty.isAlly(accessorParty.getId());
	}
	
	public boolean onLeftClickBlockServer(IServerData<CM,P> serverData, BlockPos pos, Player player) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		return onBlockAccess(serverData, pos, player, InteractionHand.MAIN_HAND, false, true, null);
	}
	
	public boolean onDestroyBlock(IServerData<CM,P> serverData, BlockPos pos, Player player) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		return onBlockAccess(serverData, pos, player, InteractionHand.MAIN_HAND, false, true, null);
	}
	
	public IPlayerConfig getClaimConfig(IPlayerConfigManager<?> playerConfigs, IPlayerChunkClaim claim) {
		return playerConfigs.getLoadedConfig(claim == null ? null : claim.getPlayerId());
	}
	
	private boolean blockAccessCheck(IServerData<CM,P> serverData, BlockPos pos, Player player, boolean emptyHand, boolean leftClick) {
		ChunkPos chunkPos = new ChunkPos(pos);
		IPlayerChunkClaim claim = claimsManager.get(player.getLevel().dimension().location(), chunkPos);
		IPlayerConfigManager<?> playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		boolean chunkAccess = hasChunkAccess(config, player);
		if(chunkAccess)
			return false;
		else {
			Block block = player.getLevel().getBlockState(pos).getBlock();
			if(leftClick && forcedBreakExceptionBlocks.contains(block) || !leftClick && emptyHand && forcedEmptyHandExceptionBlocks.contains(block))
				return false;
			if(
				leftClick && config.getEffective(PlayerConfig.ALLOW_SOME_BLOCK_BREAKING) && optionalBreakExceptionBlocks.contains(block)
			||
				!leftClick && emptyHand && config.getEffective(PlayerConfig.ALLOW_SOME_BLOCK_INTERACTIONS) && optionalEmptyHandExceptionBlocks.contains(block)
			)
				return false;
			return true;
		}
	}
	
	private boolean onBlockAccess(IServerData<CM,P> serverData, BlockPos pos, Player player, InteractionHand hand, boolean emptyHand, boolean leftClick, Component message) {
		if(blockAccessCheck(serverData, pos, player, emptyHand, leftClick)) {
			player.sendMessage(hand == InteractionHand.MAIN_HAND ? CANT_INTERACT_BLOCK_MAIN : CANT_INTERACT_BLOCK_OFF, player.getUUID());
			if(message != null)
				player.sendMessage(message, player.getUUID());
			return true;
		}
		return false;
	}

	public boolean onRightClickBlock(IServerData<CM,P> serverData, Player player, InteractionHand hand, BlockPos pos, BlockHitResult blockHit) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		ItemStack stack = player.getItemInHand(hand);
		boolean emptyHand = stack.getItem() == Items.AIR;
		if(emptyHand)
			return onBlockAccess(serverData, pos, player, hand, emptyHand, false, null);
		BlockPos placePos = pos.offset(blockHit.getDirection().getNormal());
		Component message = hand == InteractionHand.MAIN_HAND ? BLOCK_TRY_EMPTY_MAIN : BLOCK_TRY_EMPTY_OFF;
		return onBlockAccess(serverData, pos, player, hand, emptyHand, false, message) || onBlockAccess(serverData, placePos, player, hand, emptyHand, false, message);
	}
	
	public boolean onItemRightClick(IServerData<CM,P> serverData, InteractionHand hand, ItemStack itemStack, BlockPos pos, Player player) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		boolean shouldProtect = false;
		IPlayerConfigManager<?> playerConfigs = serverData.getPlayerConfigs();
		ChunkPos chunkPos = new ChunkPos(pos);
		if(itemStack.getItem().getFoodProperties() == null &&
				!(itemStack.getItem() instanceof PotionItem) &&
				!(itemStack.getItem() instanceof ProjectileWeaponItem) &&
				!(itemStack.getItem() instanceof TridentItem)
		) {
			IPlayerChunkClaim claim = claimsManager.get(player.getLevel().dimension().location(), chunkPos);
			if(!hasChunkAccess(getClaimConfig(playerConfigs, claim), player))
				shouldProtect = true;
		}
		if(shouldProtect)
			player.sendMessage(hand == InteractionHand.MAIN_HAND ? USE_ITEM_MAIN : USE_ITEM_OFF, player.getUUID());
		return shouldProtect;
	}
	
	public boolean onMobGrief(IServerData<CM,P> serverData, Entity entity) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerConfigManager<?> playerConfigs = serverData.getPlayerConfigs();
		for(int i = -1; i < 2; i++)
			for(int j = -1; j < 2; j++) {
				ChunkPos chunkPos = new ChunkPos(entity.chunkPosition().x + i, entity.chunkPosition().z + j);
				IPlayerChunkClaim claim = claimsManager.get(entity.getLevel().dimension().location(), chunkPos);
				if(i == 0 && j == 0 || claim != null) {//wilderness neighbors don't have to be protected this much
					IPlayerConfig config = getClaimConfig(playerConfigs, claim);
					if (config.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS_FROM_MOB_GRIEFING) && !hasChunkAccess(config, entity))
						return true;
				}
			}
		return false;
	}

	private boolean isEntityException(Entity entity, IPlayerConfig config, boolean emptyHand, boolean attack){
		EntityType<?> entityType = entity.getType();
		if(attack && forcedKillExceptionEntities.contains(entityType) || emptyHand && !attack && forcedEmptyHandExceptionEntities.contains(entityType))
			return true;
		if(
			attack && config.getEffective(PlayerConfig.ALLOW_SOME_ENTITY_KILLING) && optionalKillExceptionEntities.contains(entityType)
		||
			!attack && emptyHand && config.getEffective(PlayerConfig.ALLOW_SOME_ENTITY_INTERACTIONS) && optionalEmptyHandExceptionEntities.contains(entityType)
		)
			return true;
		return false;
	}
	
	public boolean onEntityInteract(IServerData<CM,P> serverData, Entity entity, Entity target, InteractionHand hand, boolean direct, boolean attack) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerConfigManager<?> playerConfigs = serverData.getPlayerConfigs();
		ChunkPos chunkPos = new ChunkPos(new BlockPos(target.getBlockX(), target.getBlockY(), target.getBlockZ()));
		IPlayerChunkClaim claim = claimsManager.get(target.getLevel().dimension().location(), chunkPos);
		IPlayerConfig config = getClaimConfig(playerConfigs, claim);
		if(shouldProtectEntity(config, target, entity)) {
			if(direct && entity instanceof Player player) {
				ItemStack stack = player.getItemInHand(hand);
				boolean emptyHand = stack.getItem() == Items.AIR;
				if(isEntityException(target, config, emptyHand, attack))
					return false;
				entity.sendMessage(hand == InteractionHand.MAIN_HAND ? CANT_INTERACT_ENTITY_MAIN : CANT_INTERACT_ENTITY_OFF, entity.getUUID());
				if(!attack && !emptyHand){
					Component message = hand == InteractionHand.MAIN_HAND ? ENTITY_TRY_EMPTY_MAIN : ENTITY_TRY_EMPTY_OFF;
					entity.sendMessage(message, entity.getUUID());
				}
			}
			//OpenPartiesAndClaims.LOGGER.info("stopped {} interacting with {}", entity, target);
			return true;
		}
		return false;
	}
	
	public void onExplosionDetonate(IServerData<CM,P> serverData, ServerLevel world, Explosion explosion, List<Entity> affectedEntities, List<BlockPos> affectedBlocks) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		IPlayerConfigManager<?> playerConfigs = serverData.getPlayerConfigs();
		DamageSource damageSource = explosion.getDamageSource();
		Iterator<BlockPos> positions = affectedBlocks.iterator();
		while(positions.hasNext()) {
			BlockPos blockPos = positions.next();
			ChunkPos chunkPos = new ChunkPos(blockPos);
			IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), chunkPos);
			IPlayerConfig config = getClaimConfig(playerConfigs, claim);
			if(config != null && (!config.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS) || !config.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS_BLOCKS_FROM_EXPLOSIONS)))
				continue;
			if(config != null)
				positions.remove();
		}
		Iterator<Entity> entities = affectedEntities.iterator();
		while(entities.hasNext()) {
			Entity entity = entities.next();
			BlockPos blockPos = new BlockPos(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ());
			ChunkPos chunkPos = new ChunkPos(blockPos);
			IPlayerChunkClaim claim = claimsManager.get(world.dimension().location(), chunkPos);
			IPlayerConfig config = getClaimConfig(playerConfigs, claim);
			if(config != null && !config.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS))
				config = null;
			if(config != null && config.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS_ENTITIES_FROM_EXPLOSIONS) && 
					((damageSource.getEntity() == null || !(damageSource.getEntity() instanceof Player)) && isProtectable(entity) || shouldProtectEntity(config, entity, damageSource.getEntity()))
					) {
				entities.remove();
			}
		}
	}
	
	public boolean onChorusFruitTeleport(IServerData<CM,P> serverData, Vec3 pos, Entity entity) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		ChunkPos chunkPos = new ChunkPos(new BlockPos(pos));
		IPlayerChunkClaim claim = claimsManager.get(entity.getLevel().dimension().location(), chunkPos);
		IPlayerConfigManager<?> playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig claimConfig = getClaimConfig(playerConfigs, claim);
		if(claimConfig.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS_CHORUS_FRUIT) && !hasChunkAccess(claimConfig, entity)) {
			if(entity instanceof Player)
				entity.sendMessage(CANT_CHORUS, entity.getUUID());
			//OpenPartiesAndClaims.LOGGER.info("stopped {} from teleporting to {}", entity, pos);
			return true;
		}
		return false;
	}

	public void onLightningBolt(IServerData<CM,P> serverData, LightningBolt bolt) {
		if(!ServerConfig.CONFIG.claimsEnabled.get() || bolt.getCause() == null)
			return;
		IPlayerConfigManager<?> playerConfigs = serverData.getPlayerConfigs();
		for(int i = -1; i < 2; i++)
			for(int j = -1; j < 2; j++) {
				ChunkPos chunkPos = new ChunkPos(bolt.chunkPosition().x + i, bolt.chunkPosition().z + j);
				IPlayerChunkClaim claim = claimsManager.get(bolt.getLevel().dimension().location(), chunkPos);
				if(i == 0 && j == 0 || claim != null) {//wilderness neighbors don't have to be protected this much
					IPlayerConfig config = getClaimConfig(playerConfigs, claim);
					if (config.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS_PLAYER_LIGHTNING) && !hasChunkAccess(config, bolt.getCause())) {
						bolt.setVisualOnly(true);
						break;
					}
				}
			}
	}

	public boolean onFireSpread(IServerData<CM,P> serverData, ServerLevel level, BlockPos pos){
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return false;
		IPlayerChunkClaim claim = claimsManager.get(level.dimension().location(), new ChunkPos(pos));
		IPlayerConfigManager<?> playerConfigs = serverData.getPlayerConfigs();
		IPlayerConfig claimConfig = getClaimConfig(playerConfigs, claim);
		return claimConfig.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS) && claimConfig.getEffective(PlayerConfig.PROTECT_CLAIMED_CHUNKS_FROM_FIRE_SPREAD);
	}

}
