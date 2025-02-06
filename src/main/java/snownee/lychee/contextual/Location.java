package snownee.lychee.contextual;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.FluidPredicate;
import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.mixin.predicates.LocationCheckAccess;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.RegistryEntryDisplay;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionDisplay;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record Location(LocationCheck check) implements ContextualCondition {
	public static final ImmutableList<Rule<?>> RULES = bootstrapRules();

	private static ImmutableList<Rule<?>> bootstrapRules() {
		ImmutableList.Builder<Rule<?>> builder = ImmutableList.builder();
		builder.add(new PosRule("x", it -> it.position().map(LocationPredicate.PositionPredicate::x), Vec3::x));
		builder.add(new PosRule("y", it -> it.position().map(LocationPredicate.PositionPredicate::y), Vec3::y));
		builder.add(new PosRule("z", it -> it.position().map(LocationPredicate.PositionPredicate::z), Vec3::z));
		builder.add(new DimensionRule());
		builder.add(new FeatureRule());
		builder.add(new BiomeRule());
		builder.add(new BlockRule());
		builder.add(new FluidRule());
		builder.add(new LightRule());
		builder.add(new SmokeyRule());
		builder.add(new CanSeeSkyRule());
		return builder.build();
	}

	@Override
	public ContextualConditionType<Location> type() {
		return ContextualConditionType.LOCATION;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		final var level = ctx.level();
		final var lootParamsContext = ctx.get(LycheeContextKey.LOOT_PARAMS);
		if (level.isClientSide) {
			return testClient(
					level,
					lootParamsContext.getOrNull(LycheeLootContextParams.BLOCK_POS),
					lootParamsContext.getOrNull(LootContextParams.ORIGIN)
			).get() ? times : 0;
		} else {
			return check.test(lootParamsContext.asLootContext()) ? times : 0;
		}
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		if (player == null) {
			return TriState.DEFAULT;
		}
		if (!BlockPos.ZERO.equals(check.offset())) {
			return TriState.DEFAULT;
		}
		final var vec = player.position();
		final var pos = player.blockPosition();
		return testClient(level, pos, vec);
	}

	public TriState testClient(Level level, BlockPos pos, Vec3 vec) {
		if (check.predicate().isEmpty()) {
			return TriState.TRUE;
		}
		final var offset = check.offset();
		if (!BlockPos.ZERO.equals(offset)) {
			pos = pos.offset(offset.getX(), offset.getY(), offset.getZ());
		}
		var predicate = check.predicate().get();
		var finalResult = TriState.TRUE;
		for (var rule : RULES) {
			if (rule.isEmpty(predicate)) {
				continue;
			}
			final var result = rule.testClient(rule.cast(predicate), level, pos, vec);
			if (result == TriState.FALSE) {
				return result;
			}
			if (result == TriState.DEFAULT) {
				finalResult = TriState.DEFAULT;
			}
		}
		return finalResult;
	}

	@Override
	public void appendToTooltips(List<Component> tooltips, Level level, @Nullable Player player, int indent, boolean inverted) {
		if (check.predicate().isEmpty()) {
			return;
		}
		final var predicate = check.predicate().get();
		var test = false;
		Vec3 vec = null;
		BlockPos pos = null;
		final var key = getDescriptionId(inverted);
		var noOffset = BlockPos.ZERO.equals(check.offset());
		if (!noOffset) {
			final var offset = check.offset();
			final var content = Component.translatable(key, offset.getX(), offset.getY(), offset.getZ()).withStyle(ChatFormatting.GRAY);
			final var result = testForTooltips(level, player);
			ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, content);
			++indent;
		}
		if (noOffset && player != null) {
			test = true;
			vec = player.position();
			pos = player.blockPosition();
		}
		for (var rule : Location.RULES) {
			if (rule.isEmpty(predicate)) {
				continue;
			}
			var result = TriState.DEFAULT;
			if (test) {
				result = rule.testClient(rule.cast(predicate), level, pos, vec);
			}
			rule.appendToTooltips(tooltips, indent, key, rule.cast(predicate), result);
		}
	}

	@Override
	public int showingCount() {
		var c = 0;
		if (check.predicate().isEmpty()) {
			return c;
		}
		final var predicate = check.predicate().get();
		for (var rule : RULES) {
			if (!rule.isEmpty(predicate)) {
				++c;
			}
		}
		return c;
	}

	public abstract static class Rule<T> {
		public final String name;
		protected final Function<LocationPredicate, Optional<T>> getter;

		protected Rule(String name, Function<LocationPredicate, Optional<T>> getter) {
			this.name = name;
			this.getter = getter;
		}

		public boolean isEmpty(LocationPredicate predicate) {
			return getter.apply(predicate).isEmpty();
		}

		@SuppressWarnings("unchecked")
		public final <Any> Any cast(LocationPredicate predicate) {
			return (Any) getter.apply(predicate).orElseThrow();
		}

		public TriState testClient(T value, Level level, BlockPos pos, Vec3 vec) {
			return TriState.DEFAULT;
		}

		public abstract void appendToTooltips(List<Component> tooltips, int indent, String key, T value, TriState result);
	}

	private static class PosRule extends Rule<Doubles> {
		private final Function<Vec3, Double> valueGetter;

		private PosRule(String name, Function<LocationPredicate, Optional<Doubles>> boundsGetter, Function<Vec3, Double> valueGetter) {
			super(name, boundsGetter);
			this.valueGetter = valueGetter;
		}

		@Override
		public boolean isEmpty(LocationPredicate predicate) {
			return super.isEmpty(predicate) || getter.apply(predicate).orElseThrow().isAny();
		}

		@Override
		public TriState testClient(Doubles value, Level level, BlockPos pos, Vec3 vec) {
			return TriState.of(value.matches(valueGetter.apply(vec)));
		}

		@Override
		public void appendToTooltips(List<Component> tooltips, int indent, String key, Doubles value, TriState result) {
			ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, Component.translatable(
					key + "." + name,
					BoundsExtensions.getDescription(value).withStyle(ChatFormatting.WHITE)
			));
		}
	}

	private static class BlockRule extends Rule<BlockPredicate> {
		private BlockRule() {
			super("block", LocationPredicate::block);
		}

		@Override
		public void appendToTooltips(List<Component> tooltips, int indent, String key, BlockPredicate value, TriState result) {
			final var block = CommonProxy.getCycledItem(List.copyOf(BlockPredicateExtensions.matchedBlocks(value)), Blocks.AIR, 1000);
			final var displayName = block.getName().withStyle(ChatFormatting.WHITE);
			if (value.properties().isPresent() || value.nbt().isPresent()) {
				displayName.append("*");
			}
			ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, Component.translatable(key + "." + name, displayName));
		}

		@Override
		public TriState testClient(BlockPredicate value, Level level, BlockPos pos, Vec3 vec) {
			return TriState.of(BlockPredicateExtensions.unsafeMatches(
					level,
					value,
					level.getBlockState(pos),
					() -> level.getBlockEntity(pos)));
		}
	}

	private static class FluidRule extends Rule<FluidPredicate> {
		private FluidRule() {
			super("fluid", LocationPredicate::fluid);
		}

		@Override
		public void appendToTooltips(List<Component> tooltips, int indent, String key, FluidPredicate value, TriState result) {
		}
	}

	private static class LightRule extends Rule<LightPredicate> {
		private LightRule() {
			super("light", LocationPredicate::light);
		}

		@Override
		public TriState testClient(LightPredicate value, Level level, BlockPos pos, Vec3 vec) {
			var brightness = level.getMaxLocalRawBrightness(pos);
			return TriState.of(value.composite().matches(brightness));
		}

		@Override
		public void appendToTooltips(List<Component> tooltips, int indent, String key, LightPredicate value, TriState result) {
			var displayName = BoundsExtensions.getDescription(value.composite()).withStyle(ChatFormatting.WHITE);
			ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, Component.translatable(key + "." + name, displayName));
		}
	}

	private static class DimensionRule extends Rule<ResourceKey<Level>> {
		private DimensionRule() {
			super("dimension", LocationPredicate::dimension);
		}

		@Override
		public TriState testClient(ResourceKey<Level> value, Level level, BlockPos pos, Vec3 vec) {
			return TriState.of(value == level.dimension());
		}

		@Override
		public void appendToTooltips(List<Component> tooltips, int indent, String key, ResourceKey<Level> value, TriState result) {
			final var displayName = RegistryEntryDisplay.of(value, Registries.DIMENSION).withStyle(ChatFormatting.WHITE);
			ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, Component.translatable(key + "." + name, displayName));
		}
	}

	private static class BiomeRule extends Rule<HolderSet<Biome>> {
		private BiomeRule() {
			super("biome", LocationPredicate::biomes);
		}

		@Override
		public TriState testClient(HolderSet<Biome> value, Level level, BlockPos pos, Vec3 vec) {
			return TriState.of(value.contains(level.getBiome(pos)));
		}

		@Override
		public void appendToTooltips(List<Component> tooltips, int indent, String key, HolderSet<Biome> value, TriState result) {
			var displayName = RegistryEntryDisplay.of(value, Registries.BIOME).withStyle(ChatFormatting.WHITE);
			ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, Component.translatable(key + "." + name, displayName));
		}
	}

	private static class SmokeyRule extends Rule<Boolean> {
		private SmokeyRule() {
			super("smokey", LocationPredicate::smokey);
		}

		@Override
		public void appendToTooltips(List<Component> tooltips, int indent, String key, Boolean value, TriState result) {
			key = key + "." + name;
			if (!value) {
				key += ".not";
			}
			ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, Component.translatable(key));
		}
	}

	private static class CanSeeSkyRule extends Rule<Boolean> {
		private CanSeeSkyRule() {
			super("can_see_sky", LocationPredicate::canSeeSky);
		}

		@Override
		public void appendToTooltips(List<Component> tooltips, int indent, String key, Boolean value, TriState result) {
			key = key + "." + name;
			if (!value) {
				key += ".not";
			}
			ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, Component.translatable(key));
		}
	}

	private static class FeatureRule extends Rule<HolderSet<Structure>> {
		private FeatureRule() {
			super("feature", LocationPredicate::structures);
		}

		@Override
		public void appendToTooltips(
				List<Component> tooltips,
				int indent,
				String key,
				HolderSet<Structure> value,
				TriState result) {
			var displayName = RegistryEntryDisplay.of(value, Registries.STRUCTURE).withStyle(ChatFormatting.WHITE);
			ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, Component.translatable(key + "." + name, displayName));
		}
	}

	public static class Type implements ContextualConditionType<Location> {
		private static final MapCodec<LocationCheck> LOCATION_CHECK_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				LocationPredicate.CODEC.optionalFieldOf("predicate").forGetter(LocationCheck::predicate),
				LocationCheckAccess.getOffsetCodec().forGetter(LocationCheck::offset)
		).apply(instance, LocationCheck::new));

		public static final MapCodec<Location> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				LOCATION_CHECK_CODEC.forGetter(Location::check)
		).apply(instance, Location::new));

		@Override
		public @NotNull MapCodec<Location> codec() {
			return CODEC;
		}
	}
}
