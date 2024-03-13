package snownee.lychee.recipes;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.RecipeSerializer;
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
import snownee.lychee.contextual.Location;
import snownee.lychee.contextual.Not;
import snownee.lychee.mixin.particles.PointedDripstoneBlockAccess;
import snownee.lychee.mixin.predicates.LocationPredicate$BuilderAccess;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.particles.dripstone.DripstoneParticleService;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ChanceRecipe;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class DripstoneRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe<DripstoneRecipe>, ChanceRecipe {
	public static boolean invoke(BlockState blockState, ServerLevel level, BlockPos blockPos) {
		if (RecipeTypes.DRIPSTONE_DRIPPING.isEmpty()) {
			return false;
		}
		var tipPos = PointedDripstoneBlockAccess.callFindTip(blockState, level, blockPos, 11, false);
		if (tipPos == null) {
			return false;
		}
		var targetPos = findTargetBelowStalactiteTip(level, tipPos);
		if (targetPos == null) {
			return false;
		}
		var sourceBlock = DripstoneParticleService.findBlockAboveStalactite(level, blockPos, blockState);
		if (sourceBlock == null) {
			return false;
		}
		var targetBlock = level.getBlockState(targetPos);
		var context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, level);
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParamsContext.setParam(LootContextParams.BLOCK_STATE, targetBlock);
		var origin = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 0.99, targetPos.getZ() + 0.5);
		lootParamsContext.setParam(LootContextParams.ORIGIN, origin);
		lootParamsContext.setParam(LycheeLootContextParams.BLOCK_POS, targetPos);
		lootParamsContext.validate(LycheeLootContextParamSets.BLOCK_ONLY);
		var recipe = RecipeTypes.DRIPSTONE_DRIPPING.process(level, targetBlock, context);
		if (recipe == null) {
			return false;
		}
		level.levelEvent(LevelEvent.DRIPSTONE_DRIP, tipPos, 0);
		var i = tipPos.getY() - targetPos.getY();
		var j = 50 + i;
		var breakAction = new Break();
		var builder = new LocationPredicate.Builder();
		((LocationPredicate$BuilderAccess) builder).setBlock(Optional.ofNullable(recipe.value().targetBlock));
		var check = (LocationCheck) LocationCheck.checkLocation(builder).build();
		breakAction.conditions().conditions().add(new Not(new Location(check)));
		var actionContext = context.get(LycheeContextKey.ACTION);
		actionContext.jobs.offer(new Job(breakAction, 1));
		actionContext.jobs.offer(new Job(new Delay(j / 20F), 1));
		actionContext.run(context);
		return true;
	}

	private float chance = 1;
	protected final @Nullable BlockPredicate sourceBlock;
	protected final @Nullable BlockPredicate targetBlock;

	protected DripstoneRecipe(
			LycheeRecipeCommonProperties commonProperties,
			@Nullable BlockPredicate sourceBlock,
			@Nullable BlockPredicate targetBlock
	) {
		super(commonProperties);
		this.sourceBlock = sourceBlock;
		this.targetBlock = targetBlock;
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		if (targetBlock != null && !BlockPredicateExtensions.matches(targetBlock, context)) {
			return false;
		}

		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);

		return sourceBlock != null &&
				BlockPredicateExtensions.unsafeMatches(
						level,
						sourceBlock,
						context.get(LycheeContextKey.DRIPSTONE_ROOT),
						() -> level.getBlockEntity(lootParamsContext.get(LycheeLootContextParams.BLOCK_POS))
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
	public @NotNull RecipeSerializer<DripstoneRecipe> getSerializer() {
		return RecipeSerializers.DRIPSTONE_DRIPPING;
	}

	@Override
	public @NotNull LycheeRecipeType<LycheeContext, DripstoneRecipe> getType() {
		return RecipeTypes.DRIPSTONE_DRIPPING;
	}

	@Override
	public int compareTo(DripstoneRecipe that) {
		int i;
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(targetBlock == null ? 1 : 0, that.targetBlock == null ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(sourceBlock == null ? 1 : 0, that.sourceBlock == null ? 1 : 0);
		return i;
	}

	@Override
	public Optional<BlockPredicate> blockPredicate() {
		return Optional.ofNullable(targetBlock);
	}

	public Optional<BlockPredicate> sourceBlock() {
		return Optional.ofNullable(sourceBlock);
	}

	@Override
	public List<BlockPredicate> getBlockInputs() {
		var list = Lists.<BlockPredicate>newArrayList();
		if (sourceBlock != null) {
			list.add(sourceBlock);
		}

		if (targetBlock != null) {
			list.add(targetBlock);
		}
		return list;
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
		var f = randomSource.nextFloat();
		if (f > 0.17578125f && f > 0.05859375f) {
			return false;
		}
		return invoke(state, serverLevel, blockPos);
	}

	// PointedDripstoneBlock
	@Nullable
	private static BlockPos findTargetBelowStalactiteTip(Level level, BlockPos blockPos2) {
		Predicate<BlockState> predicate = blockState -> !blockState.isAir() && RecipeTypes.DRIPSTONE_DRIPPING.has(blockState);
		BiPredicate<BlockPos, BlockState> biPredicate =
				(blockPos, blockState) -> PointedDripstoneBlockAccess.callCanDripThrough(level, blockPos, blockState);
		return PointedDripstoneBlockAccess.callFindBlockVertical(
				level,
				blockPos2,
				Direction.DOWN.getAxisDirection(),
				biPredicate,
				predicate,
				11).orElse(null);
	}

	public static class Serializer implements LycheeRecipeSerializer<DripstoneRecipe> {
		public static final Codec<DripstoneRecipe> CODEC =
				RecordCodecBuilder.create(instance -> instance.group(
						LycheeRecipeCommonProperties.MAP_CODEC.forGetter(DripstoneRecipe::commonProperties),
						BlockPredicateExtensions.CODEC.fieldOf("source_block").forGetter(it -> it.sourceBlock),
						BlockPredicateExtensions.CODEC.fieldOf("target_block").forGetter(it -> it.targetBlock)
				).apply(instance, DripstoneRecipe::new));

		@Override
		public @NotNull Codec<DripstoneRecipe> codec() {
			return CODEC;
		}
	}
}
