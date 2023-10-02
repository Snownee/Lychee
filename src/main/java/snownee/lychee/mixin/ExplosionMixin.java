package snownee.lychee.mixin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.RecipeTypes;
import snownee.lychee.block_exploding.BlockExplodingContext;
import snownee.lychee.item_exploding.ItemExplodingRecipe;

@Mixin(value = Explosion.class, priority = 700)
public abstract class ExplosionMixin {

	@Shadow
	public float radius;
	@Shadow
	@Nullable
	public Entity source;
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
	@Unique
	private Map<BlockPos, BlockExplodingContext.Builder> lychee$contexts;

	@Shadow
	private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList, ItemStack itemStack, BlockPos blockPos) {
		throw new AssertionError();
	}

	@Inject(
			at = @At(
					"TAIL"
			), method = "explode()V", locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void lychee_explode(CallbackInfo ci, Set<BlockPos> set, int i, float q, int k, int l, int r, int s, int t, int u, List<Entity> list, Vec3 vec3, int v) {
		ItemExplodingRecipe.on(level, x, y, z, list, radius);
	}

	@Inject(method = "finalizeExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void lychee_removeBlockPre(boolean bl, CallbackInfo ci, boolean bl2, ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList, boolean bl3, ObjectListIterator<Pair<ItemStack, BlockPos>> var5, BlockPos blockPos, BlockState state, Block block) {
		if (RecipeTypes.BLOCK_EXPLODING.isEmpty() || !RecipeTypes.BLOCK_EXPLODING.has(state)) {
			return;
		}
		if (lychee$contexts == null) {
			lychee$contexts = new HashMap<>();
		}
		lychee$contexts.clear();
		BlockExplodingContext.Builder builder = new BlockExplodingContext.Builder(level);
		builder.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos));
		builder.withParameter(LootContextParams.BLOCK_STATE, state);
		BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(blockPos) : null;
		builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
		builder.withOptionalParameter(LootContextParams.THIS_ENTITY, source);
		if (blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
			builder.withParameter(LootContextParams.EXPLOSION_RADIUS, radius);
		}
		lychee$contexts.put(blockPos, builder);
	}

	@Inject(method = "finalizeExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;wasExploded(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	private void lychee_removeBlockPost(boolean bl, CallbackInfo ci, boolean bl2, ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList, boolean bl3, ObjectListIterator<Pair<ItemStack, BlockPos>> var5, BlockPos blockPos, BlockState state, Block block) {
		if (lychee$contexts == null) {
			return;
		}
		BlockExplodingContext.Builder ctxBuilder = lychee$contexts.remove(blockPos);
		if (ctxBuilder == null) {
			return;
		}
		var result = RecipeTypes.BLOCK_EXPLODING.process(level, state, () -> ctxBuilder.create(RecipeTypes.BLOCK_EXPLODING.contextParamSet));
		if (result == null) {
			return;
		}
		BlockExplodingContext ctx = result.getFirst();
		if (!ctx.runtime.doDefault) {
			while (!objectArrayList.isEmpty() && objectArrayList.get(objectArrayList.size() - 1).getSecond().equals(blockPos)) {
				objectArrayList.remove(objectArrayList.size() - 1);
			}
		}
		for (ItemStack stack : ctx.itemHolders.tempList) {
			addBlockDrops(objectArrayList, stack, blockPos);
		}
	}
}
