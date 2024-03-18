package snownee.lychee.mixin.recipes.blockexploding;

import java.util.List;
import java.util.function.BiConsumer;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.input.ItemStackHolderCollection;

@Mixin(value = Explosion.class, priority = 700)
public abstract class ExplosionMixin {

	@Final
	@Shadow
	private float radius;
	@Final
	@Shadow
	@Nullable
	private Entity source;
	@Final
	@Shadow
	private Level level;
	@Shadow
	@Final
	private Explosion.BlockInteraction blockInteraction;

	@Shadow
	private static void addOrAppendStack(List<Pair<ItemStack, BlockPos>> list, ItemStack itemStack, BlockPos blockPos) {
	}

	/**
	 * The drops are added in {@link BlockBehaviour.BlockStateBase#onExplosionHit}.
	 * We need to avoid the default drops conditional after {@link BlockBehaviour.BlockStateBase#onExplosionHit}.
	 */
	@ModifyReceiver(
			method = "finalizeExplosion",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;onExplosionHit(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;Ljava/util/function/BiConsumer;)V"
			)
	)
	private BlockState lychee_beforeOnExplosionHit(
			final BlockState state,
			final Level level,
			final BlockPos blockPos,
			final Explosion explosion,
			final BiConsumer<ItemStack, BlockPos> biConsumer,
			@Share("state") LocalRef<BlockState> stateRef,
			@Share("context") LocalRef<LycheeContext> contextRef,
			@Share("currentDrops") LocalRef<List<Pair<ItemStack, BlockPos>>> currentDropsRef) {
		if (level.isClientSide || RecipeTypes.BLOCK_EXPLODING.isEmpty() || !RecipeTypes.BLOCK_EXPLODING.has(state)) {
			contextRef.set(null);
			return state;
		}
		contextRef.set(new LycheeContext());
		contextRef.get().put(LycheeContextKey.LEVEL, level);
		var lootParamsContext = contextRef.get().get(LycheeContextKey.LOOT_PARAMS);
		lootParamsContext.setParam(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos));
		lootParamsContext.setParam(LootContextParams.BLOCK_STATE, state);
		var blockEntity = state.hasBlockEntity() ? level.getBlockEntity(blockPos) : null;
		lootParamsContext.setParam(LootContextParams.BLOCK_ENTITY, blockEntity);
		lootParamsContext.setParam(LootContextParams.THIS_ENTITY, source);
		if (blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
			lootParamsContext.setParam(LootContextParams.EXPLOSION_RADIUS, radius);
		}
		stateRef.set(state);
		currentDropsRef.set(Lists.newArrayList());
		return state;
	}

	@ModifyArg(
			method = "finalizeExplosion",
			index = 3,
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;onExplosionHit(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;Ljava/util/function/BiConsumer;)V"))
	private BiConsumer<ItemStack, BlockPos> lychee_redirectDrops(
			BiConsumer<ItemStack, BlockPos> original,
			@Share("currentDrops") LocalRef<List<Pair<ItemStack, BlockPos>>> currentDropsRef) {
		return currentDropsRef.get() == null ?
				original :
				(itemStack, blockPos) -> addOrAppendStack(currentDropsRef.get(), itemStack, blockPos);
	}

	@Inject(
			method = "finalizeExplosion",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;onExplosionHit(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;Ljava/util/function/BiConsumer;)V",
					shift = At.Shift.AFTER
			)
	)
	private void lychee_afterOnExplosionHit(
			final boolean spawnParticles,
			final CallbackInfo ci,
			@Local BlockPos blockPos,
			@Local List<Pair<ItemStack, BlockPos>> allDrops,
			@Share("state") LocalRef<BlockState> stateRef,
			@Share("context") LocalRef<LycheeContext> contextRef,
			@Share("currentDrops") LocalRef<List<Pair<ItemStack, BlockPos>>> currentDropsRef) {
		if (level.isClientSide) {
			return;
		}
		var context = contextRef.get();
		if (context == null) {
			return;
		}
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParamsContext.validate(RecipeTypes.BLOCK_EXPLODING.contextParamSet);
		var itemHolders = ItemStackHolderCollection.InWorld.of();
		context.put(LycheeContextKey.ITEM, itemHolders);
		var state = stateRef.get();
		var recipe = RecipeTypes.BLOCK_EXPLODING.process(level, state, context);
		if (recipe == null) {
			if (currentDropsRef.get() != null) {
				allDrops.addAll(currentDropsRef.get());
			}
			return;
		}
		var actionContext = context.get(LycheeContextKey.ACTION);
		if (!actionContext.avoidDefault && currentDropsRef.get() != null) {
			allDrops.addAll(currentDropsRef.get());
		}
		currentDropsRef.set(null);
		for (var stack : itemHolders.stacksNeedHandle) {
			addOrAppendStack(allDrops, stack, blockPos);
		}
	}
}
