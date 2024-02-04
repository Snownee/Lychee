package snownee.lychee.recipes.dripstone_dripping;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParamSets;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.action.Break;
import snownee.lychee.action.Delay;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.core.contextual.Location;
import snownee.lychee.core.contextual.Not;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.recipe.ChanceRecipe;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;
import snownee.lychee.mixin.PointedDripstoneBlockAccess;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class DripstoneRecipe extends OldLycheeRecipe<DripstoneContext>
    implements BlockKeyableRecipe<DripstoneRecipe>, ChanceRecipe {

	private float chance = 1;
	protected BlockPredicate sourceBlock;
	protected BlockPredicate targetBlock;

	public DripstoneRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public boolean matches(DripstoneContext ctx, Level level) {
		if (!BlockPredicateHelper.matches(targetBlock, ctx)) {
			return false;
		}
		return BlockPredicateHelper.matches(
				sourceBlock,
				ctx.source,
				() -> level.getBlockEntity(ctx.getParam(LycheeLootContextParams.BLOCK_POS))
		);
	}

	@Override
	public float getChance() {
		return chance;
	}

	@Override
	public void setChance(float chance) {
		this.chance = chance;
	}

	@Override
	public OldLycheeRecipe.@NotNull Serializer<?> getSerializer() {
		return RecipeSerializers.DRIPSTONE_DRIPPING;
	}

	@Override
	public @NotNull LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.DRIPSTONE_DRIPPING;
	}

	@Override
	public int compareTo(DripstoneRecipe that) {
		int i;
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0)
			return i;
		i = Integer.compare(targetBlock == BlockPredicate.ANY ? 1 : 0, that.targetBlock == BlockPredicate.ANY ? 1 : 0);
		if (i != 0)
			return i;
		return getId().compareTo(that.getId());
	}

	@Override
	public BlockPredicate blockPredicate() {
		return targetBlock;
	}

	public BlockPredicate getSourceBlock() {
		return sourceBlock;
	}

	@Override
	public List<BlockPredicate> getBlockInputs() {
		return List.of(sourceBlock, targetBlock);
	}

	@Override
	public void applyPostActions(LycheeRecipeContext ctx, int times) {
		if (!ctx.getLevel().isClientSide) {
			ctx.enqueueActions(getPostActions(), times, true);
		}
	}

	public static boolean safeTick(
			BlockState state,
			ServerLevel serverLevel,
			BlockPos blockPos,
			RandomSource randomSource
	) {
		if (!PointedDripstoneBlockAccess.callIsStalactiteStartPos(state, serverLevel, blockPos)) {
			return false;
		}
		float f = randomSource.nextFloat();
		if (f > 0.17578125f && f > 0.05859375f) {
			return false;
		}
		return on(state, serverLevel, blockPos);
	}

	public static boolean on(BlockState blockState, ServerLevel level, BlockPos blockPos) {
		if (RecipeTypes.DRIPSTONE_DRIPPING.isEmpty()) {
			return false;
		}
		BlockPos tipPos = PointedDripstoneBlockAccess.callFindTip(blockState, level, blockPos, 11, false);
		if (tipPos == null) {
			return false;
		}
		BlockPos targetPos = findTargetBelowStalactiteTip(level, tipPos);
		if (targetPos == null) {
			return false;
		}
		BlockState sourceBlock = getBlockAboveStalactite(level, blockPos, blockState);
		if (sourceBlock == null) {
			return false;
		}
		BlockState targetBlock = level.getBlockState(targetPos);
		var result = RecipeTypes.DRIPSTONE_DRIPPING.process(level, targetBlock, () -> {
			var builder = new DripstoneContext.Builder(level, sourceBlock);
			builder.withParameter(LootContextParams.BLOCK_STATE, targetBlock);
			Vec3 origin = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 0.99, targetPos.getZ() + 0.5);
			builder.withParameter(LootContextParams.ORIGIN, origin);
			builder.withParameter(LycheeLootContextParams.BLOCK_POS, targetPos);
			return builder.create(LycheeLootContextParamSets.BLOCK_ONLY);
		});
		if (result == null) {
			return false;
		}
		DripstoneContext ctx = result.getFirst();
		DripstoneRecipe recipe = result.getSecond();
		level.levelEvent(LevelEvent.DRIPSTONE_DRIP, tipPos, 0);
		int i = tipPos.getY() - targetPos.getY();
		int j = 50 + i;
		Break breakAction = new Break();
		var builder = new LocationPredicate.Builder().setBlock(recipe.targetBlock);
		LocationCheck check = (LocationCheck) LocationCheck.checkLocation(builder).build();
		breakAction.withCondition(new Not(new Location(check)));
		ctx.runtime.jobs.push(new Job(breakAction, 1));
		ctx.runtime.jobs.push(new Job(new Delay(j / 20F), 1));
		ctx.runtime.run(recipe, ctx);
		return true;
	}

	// PointedDripstoneBlock
	@Nullable
	private static BlockPos findTargetBelowStalactiteTip(Level level, BlockPos blockPos2) {
		Predicate<BlockState> predicate = blockState -> !blockState.isAir() && RecipeTypes.DRIPSTONE_DRIPPING.has(
				blockState);
		BiPredicate<BlockPos, BlockState> biPredicate =
				(blockPos, blockState) -> PointedDripstoneBlockAccess.callCanDripThrough(level, blockPos, blockState);
		return PointedDripstoneBlockAccess.callFindBlockVertical(
				level,
				blockPos2,
				Direction.DOWN.getAxisDirection(),
				biPredicate,
				predicate,
				11
		).orElse(null);
	}

	public static BlockState getBlockAboveStalactite(Level level, BlockPos blockPos2, BlockState blockState) {
		//		if (!PointedDripstoneBlock.isStalactite(blockState)) {
		//			return Optional.empty();
		//		}
		return PointedDripstoneBlockAccess.callFindRootBlock(level, blockPos2, blockState, 11).map(blockPos -> {
			return level.getBlockState(blockPos.above());
		}).orElse(null);
	}

	public static class Serializer extends OldLycheeRecipe.Serializer<DripstoneRecipe> {

		public Serializer() {
			super(DripstoneRecipe::new);
		}

		@Override
		public void fromJson(DripstoneRecipe pRecipe, JsonObject pSerializedRecipe) {
			pRecipe.sourceBlock = BlockPredicateHelper.fromJson(pSerializedRecipe.get("source_block"));
			pRecipe.targetBlock = BlockPredicateHelper.fromJson(pSerializedRecipe.get("target_block"));
			Preconditions.checkArgument(pRecipe.sourceBlock != BlockPredicate.ANY, "source_block can't be wildcard");
			Preconditions.checkArgument(pRecipe.targetBlock != BlockPredicate.ANY, "target_block can't be wildcard");
		}

		@Override
		public void fromNetwork(DripstoneRecipe pRecipe, FriendlyByteBuf pBuffer) {
			pRecipe.sourceBlock = BlockPredicateHelper.fromNetwork(pBuffer);
			pRecipe.targetBlock = BlockPredicateHelper.fromNetwork(pBuffer);
		}

		@Override
		public void toNetwork0(FriendlyByteBuf pBuffer, DripstoneRecipe pRecipe) {
			BlockPredicateHelper.toNetwork(pRecipe.sourceBlock, pBuffer);
			BlockPredicateHelper.toNetwork(pRecipe.targetBlock, pBuffer);
		}

	}

}
