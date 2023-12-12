package snownee.lychee.mixin;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.RecipeTypes;
import snownee.lychee.block_exploding.BlockExplodingContext;
import snownee.lychee.item_exploding.ItemExplodingRecipe;

@Mixin(value = Explosion.class, priority = 700)
public abstract class ExplosionMixin {

	@Unique
	private static final ThreadLocal<snownee.lychee.util.Pair<BlockExplodingContext.Builder, List<ItemStack>>> CONTEXT = ThreadLocal.withInitial(() -> snownee.lychee.util.Pair.of(null, null));
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
	// the field "position" is added by forge
	@Final
	@Shadow
	private double x;
	@Final
	@Shadow
	private double y;
	@Final
	@Shadow
	private double z;
	@Shadow
	@Final
	private Explosion.BlockInteraction blockInteraction;

	@Shadow
	private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList, ItemStack itemStack, BlockPos blockPos) {
		throw new AssertionError();
	}

	@Inject(method = {"lambda$finalizeExplosion$0", "m_46071_"}, remap = false, at = @At("HEAD"), cancellable = true)
	private static void lychee_deferAddingDrops(
			final ObjectArrayList p_46072_,
			final BlockPos p_46073_,
			final ItemStack itemStack,
			final CallbackInfo ci) {
		var pair = CONTEXT.get();
		if (pair.getSecond() != null) {
			pair.getSecond().add(itemStack);
			ci.cancel();
		}
	}

	@Inject(method = "finalizeExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDrops(Lnet/minecraft/world/level/storage/loot/LootContext$Builder;)Ljava/util/List;"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void initDeferring(
			final boolean p_46076_,
			final CallbackInfo ci,
			final boolean flag,
			final ObjectArrayList objectarraylist,
			final boolean flag1,
			final ObjectListIterator var5,
			final BlockPos blockpos,
			final BlockState state,
			final Block block,
			final BlockPos blockpos1,
			final Level $$9,
			final ServerLevel serverlevel,
			final BlockEntity blockentity,
			final LootContext.Builder lootcontext$builder) {
		var pair = CONTEXT.get();
		if (RecipeTypes.BLOCK_EXPLODING.isEmpty() || !RecipeTypes.BLOCK_EXPLODING.has(state)) {
			pair.setSecond(null);
		} else if (pair.getFirst() == null) {
			pair.setSecond(new ObjectArrayList<>());
		} else {
			pair.getSecond().clear();
		}
	}

	@Inject(
			at = @At(
					"TAIL"
			), method = "explode()V", locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void lychee_explode(CallbackInfo ci, Set set, int i, float f2, int k1, int l1, int i2, int i1, int j2, int j1, List list, Vec3 vec3, int k2) {
		ItemExplodingRecipe.on(level, x, y, z, list, radius);
	}

	@Inject(method = "finalizeExplosion",
			at = @At(value = "INVOKE",
					 remap = false,
					 target = "Lnet/minecraft/world/level/block/state/BlockState;onBlockExploded(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;)V"),
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void lychee_removeBlockPre(boolean p_46076_, CallbackInfo ci, boolean flag, ObjectArrayList objectarraylist, boolean flag1, ObjectListIterator var5, BlockPos blockPos, BlockState state, Block block, BlockPos blockpos1) {
		if (level.isClientSide || RecipeTypes.BLOCK_EXPLODING.isEmpty() || !RecipeTypes.BLOCK_EXPLODING.has(state)) {
			return;
		}
		BlockExplodingContext.Builder builder = new BlockExplodingContext.Builder(level);
		builder.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos));
		builder.withParameter(LootContextParams.BLOCK_STATE, state);
		BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(blockPos) : null;
		builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
		builder.withOptionalParameter(LootContextParams.THIS_ENTITY, source);
		if (blockInteraction == Explosion.BlockInteraction.DESTROY) {
			builder.withParameter(LootContextParams.EXPLOSION_RADIUS, radius);
		}
		CONTEXT.get().setFirst(builder);
	}

	@Inject(method = "finalizeExplosion",
			at = @At(value = "INVOKE",
					 remap = false,
					 target = "Lnet/minecraft/world/level/block/state/BlockState;onBlockExploded(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;)V",
					 shift = At.Shift.AFTER),
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void lychee_removeBlockPost(boolean bl, CallbackInfo ci, boolean bl2, ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList, boolean bl3, ObjectListIterator<Pair<ItemStack, BlockPos>> var5, BlockPos blockPos, BlockState state, Block block) {
		if (level.isClientSide) {
			return;
		}
		var pair = CONTEXT.get();
		BlockExplodingContext.Builder ctxBuilder = pair.getFirst();
		if (ctxBuilder == null) {
			return;
		}
		pair.setFirst(null);
		var result = RecipeTypes.BLOCK_EXPLODING.process(level, state, () -> ctxBuilder.create(RecipeTypes.BLOCK_EXPLODING.contextParamSet));
		if (result == null) {
			return;
		}
		BlockExplodingContext ctx = result.getFirst();
		if (ctx.runtime.doDefault && pair.getSecond() != null) {
			for (ItemStack stack : pair.getSecond()) {
				addBlockDrops(objectArrayList, stack, blockPos);
			}
		}
		for (ItemStack stack : ctx.itemHolders.tempList) {
			addBlockDrops(objectArrayList, stack, blockPos);
		}
	}
}
