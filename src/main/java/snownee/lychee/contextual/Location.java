package snownee.lychee.contextual;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.recipe.OldLycheeRecipe;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.TriState;
import snownee.lychee.util.contextual.ContextualConditionDisplay;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualConditionTypes;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.predicates.LocationCheck;
import snownee.lychee.util.predicates.LocationPredicate;

public record Location(LocationCheck check) implements ContextualCondition<Location> {

	private static final Rule X = new PosRule("x", it -> it.position().orElseThrow().x(), Vec3::x);
	private static final Rule Y = new PosRule("y", it -> it.position().orElseThrow().y(), Vec3::y);
	private static final Rule Z = new PosRule("z", it -> it.position().orElseThrow().z(), Vec3::z);
	private static final Rule DIMENSION = new DimensionRule();
	private static final Rule FEATURE = new FeatureRule();
	private static final Rule BIOME = new BiomeRule();
	private static final Rule BLOCK = new BlockRule();
	private static final Rule FLUID = new FluidRule();
	private static final Rule LIGHT = new LightRule();
	private static final Rule SMOKEY = new SmokeyRule();
	public static final Rule[] RULES = new Rule[]{X, Y, Z, DIMENSION, FEATURE, BIOME, BLOCK, FLUID, LIGHT, SMOKEY};

	@Override
	public ContextualConditionType<Location> type() {
		return ContextualConditionTypes.LOCATION;
	}

	@Override
	public int test(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times) {
		if (ctx.level().isClientSide) {
			return testClient(
					ctx.level(),
					ctx.getOrNull(LycheeLootContextParams.BLOCK_POS),
					ctx.getOrNull(LootContextParams.ORIGIN)).get() ? times : 0;
		} else {
			return check.test(ctx.asLootContext()) ? times : 0;
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
		final var offset = check.offset();
		if (!BlockPos.ZERO.equals(offset)) {
			pos = pos.offset(offset.getX(), offset.getY(), offset.getZ());
		}
		if (check.predicate().isEmpty()) return TriState.DEFAULT;
		final var predicate = check.predicate().orElseThrow();
		var finalResult = TriState.TRUE;
		for (Rule rule : RULES) {
			if (rule.shouldSkip(predicate)) continue;
			final var result = rule.testClient(predicate, level, pos, vec);
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
	public void appendToTooltips(
			List<Component> tooltips,
			Level level,
			@Nullable Player player,
			int indent,
			boolean inverted) {
		final var predicate = check.predicate().orElseThrow();
		var test = false;
		Vec3 vec = null;
		BlockPos pos = null;
		final var key = getDescriptionId(inverted);
		boolean noOffset = BlockPos.ZERO.equals(check.offset());
		if (!noOffset) {
			final var offset = check.offset();
			final var content = Component.translatable(key, offset.getX(), offset.getY(), offset.getZ()).withStyle(
					ChatFormatting.GRAY);
			final var result = testForTooltips(level, player);
			ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, content);
			++indent;
		}
		if (noOffset && player != null) {
			test = true;
			vec = player.position();
			pos = player.blockPosition();
		}
		for (Rule rule : Location.RULES) {
			if (rule.shouldSkip(predicate)) continue;
			var result = TriState.DEFAULT;
			if (test) {
				result = rule.testClient(predicate, level, pos, vec);
			}
			rule.appendToTooltips(tooltips, indent, key, predicate, result);
		}
	}

	@Override
	public int showingCount() {
		var c = 0;
		final var predicate = check.predicate().orElseThrow();
		for (Rule rule : RULES) {
			if (!rule.shouldSkip(predicate)) {
				++c;
			}
		}
		return c;
	}

	public interface Rule {
		String name();

		boolean shouldSkip(LocationPredicate predicate);

		default TriState testClient(LocationPredicate predicate, Level level, BlockPos pos, Vec3 vec) {
			return TriState.DEFAULT;
		}

		void appendToTooltips(
				List<Component> tooltips,
				int indent,
				String key,
				LocationPredicate predicate,
				TriState result);
	}

	private record PosRule(String name,
						   Function<LocationPredicate, Doubles> boundsGetter,
						   Function<Vec3, Double> valueGetter) implements Rule {

		@Override
		public boolean shouldSkip(LocationPredicate predicate) {
			return boundsGetter.apply(predicate).isAny();
		}

		@Override
		public TriState testClient(LocationPredicate predicate, Level level, BlockPos pos, Vec3 vec) {
			return TriState.of(boundsGetter.apply(predicate).matches(valueGetter.apply(vec)));
		}

		@Override
		public void appendToTooltips(
				List<Component> tooltips,
				int indent,
				String key,
				LocationPredicate predicate,
				TriState result) {
			ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, Component.translatable(
					key + "." + name(),
					BoundsExtensions.getDescription(boundsGetter.apply(predicate)).withStyle(ChatFormatting.WHITE)));
		}
	}

	private static class BlockRule implements Rule {
		@Override
		public String name() {
			return "block";
		}

		@Override
		public boolean shouldSkip(LocationPredicate predicate) {
			return predicate.block().isEmpty();
		}

		@Override
		public void appendToTooltips(
				List<Component> tooltips,
				int indent,
				String key,
				LocationPredicate predicate,
				TriState result) {
			final var block =
					CommonProxy.getCycledItem(
							List.copyOf(BlockPredicateExtensions.matchedBlocks(predicate.block()
																						.orElseThrow())),
							Blocks.AIR,
							1000);
			final var name = block.getName().withStyle(ChatFormatting.WHITE);
			final var blockPredicate = predicate.block().orElseThrow();
			if (blockPredicate.properties().isPresent() || blockPredicate.nbt().isPresent()) {
				name.append("*");
			}
			ContextualConditionDisplay.appendToTooltips(
					tooltips,
					result,
					indent,
					Component.translatable(key + "." + name(), name));
		}

		@Override
		public TriState testClient(LocationPredicate predicate, Level level, BlockPos pos, Vec3 vec) {
			return TriState.of(predicate.block()
										.orElseThrow()
										.unsafeMatches(level.getBlockState(pos), () -> level.getBlockEntity(pos)));
		}
	}

	private static class FluidRule implements Rule {
		@Override
		public String name() {
			return "fluid";
		}

		@Override
		public boolean shouldSkip(LocationPredicate predicate) {
			return predicate.fluid().isEmpty();
		}

		@Override
		public void appendToTooltips(
				List<Component> tooltips, int indent, String key, LocationPredicate predicate, TriState result) {}
	}

	private static class LightRule implements Rule {
		@Override
		public String name() {
			return "light";
		}

		@Override
		public boolean shouldSkip(LocationPredicate predicate) {
			return predicate.light().isEmpty();
		}

		@Override
		public TriState testClient(LocationPredicate predicate, Level level, BlockPos pos, Vec3 vec) {
			int brightness = level.getMaxLocalRawBrightness(pos);
			return TriState.of(predicate.light().orElseThrow().composite().matches(brightness));
		}

		@Override
		public void appendToTooltips(
				List<Component> tooltips,
				int indent,
				String key,
				LocationPredicate predicate,
				TriState result) {
			MutableComponent bounds = BoundsExtensions.getDescription(predicate.light().orElseThrow().composite())
													  .withStyle(ChatFormatting.WHITE);
			ContextualConditionDisplay.appendToTooltips(
					tooltips,
					result,
					indent,
					Component.translatable(key + "." + name(), bounds));
		}
	}

	private static class DimensionRule implements Rule {
		@Override
		public String name() {
			return "dimension";
		}

		@Override
		public boolean shouldSkip(LocationPredicate predicate) {
			return predicate.dimension().isEmpty();
		}

		@Override
		public TriState testClient(LocationPredicate predicate, Level level, BlockPos pos, Vec3 vec) {
			return TriState.of(predicate.dimension()
										.orElseThrow()
										.get(((Registry<Registry<Level>>) BuiltInRegistries.REGISTRY).getOrThrow(
												Registries.DIMENSION))
										.contains(Holder.direct(level)));
		}

		@Override
		public void appendToTooltips(
				List<Component> tooltips,
				int indent,
				String key,
				LocationPredicate predicate,
				TriState result) {
			final var name =
					predicate
							.dimension()
							.map(it -> it.tag()
									   ?
									   Component.literal(
											   TagKey.hashedCodec(Registries.DIMENSION)
													 .encodeStart(JsonOps.INSTANCE,
																  TagKey.create(Registries.DIMENSION, it.id()))
													 .get()
													 .orThrow()
													 .getAsString())
									   :
									   ClientProxy.getDimensionDisplayName(
											   ResourceKey.create(Registries.DIMENSION, it.id()))
							)
							.orElseThrow()
							.withStyle(ChatFormatting.WHITE);
			ContextualConditionDisplay.appendToTooltips(
					tooltips,
					result,
					indent,
					Component.translatable(key + "." + name(), name));
		}
	}

	private static class BiomeRule implements Rule {
		@Override
		public String name() {
			return "biome";
		}

		@Override
		public boolean shouldSkip(LocationPredicate predicate) {
			return predicate.biome().isPresent();
		}

		@Override
		public TriState testClient(LocationPredicate predicate, Level level, BlockPos pos, Vec3 vec) {
			final var biome = level.getBiome(pos);
			final var biomeTagOrElementHolder = predicate.biome().orElseThrow();

			final var nonTagAndEquals = !biomeTagOrElementHolder.tag() && biome.is(biomeTagOrElementHolder.id());
			if (nonTagAndEquals) {
				return TriState.TRUE;
			}

			final var isTagAndIn = biomeTagOrElementHolder.tag()
								   && biome.is(TagKey.create(Registries.BIOME, biomeTagOrElementHolder.id()));
			if (isTagAndIn) {
				return TriState.TRUE;
			}
			return TriState.FALSE;
		}

		@Override
		public void appendToTooltips(
				List<Component> tooltips,
				int indent,
				String key,
				LocationPredicate predicate,
				TriState result) {
			String valueKey;
			if (predicate.biome().orElseThrow().tag()) {
				valueKey = Util.makeDescriptionId(
						"biomeTag",
						predicate.biome().orElseThrow().id());
			} else {
				valueKey = Util.makeDescriptionId("biome", predicate.biome().orElseThrow().id());
			}

			final var name = Component.translatable(valueKey).withStyle(ChatFormatting.WHITE);
			ContextualConditionDisplay.appendToTooltips(
					tooltips,
					result,
					indent,
					Component.translatable(key + "." + name(), name));
		}
	}

	private static class SmokeyRule implements Rule {
		@Override
		public String name() {
			return "smokey";
		}

		@Override
		public boolean shouldSkip(LocationPredicate predicate) {
			return predicate.smokey().isEmpty();
		}

		@Override
		public void appendToTooltips(
				List<Component> tooltips,
				int indent,
				String key,
				LocationPredicate predicate,
				TriState result) {
			key = key + "." + name();
			if (!predicate.smokey().orElseThrow()) {
				key += ".not";
			}
			ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, Component.translatable(key));
		}
	}

	private static class FeatureRule implements Rule {
		@Override
		public String name() {
			return "feature";
		}

		@Override
		public boolean shouldSkip(LocationPredicate predicate) {
			return predicate.structure().isEmpty();
		}

		@Override
		public void appendToTooltips(
				List<Component> tooltips, int indent, String key, LocationPredicate predicate, TriState result) {
			final var name = ClientProxy.getStructureDisplayName(predicate.structure().orElseThrow().id())
										.withStyle(ChatFormatting.WHITE);
			ContextualConditionDisplay.appendToTooltips(
					tooltips,
					result,
					indent,
					Component.translatable(key + "." + name(), name));
		}
	}

	public static class Type implements ContextualConditionType<Location> {
		public static final Codec<Location> CODEC =
				RecordCodecBuilder.create(instance ->
												  instance.group(
														  LocationCheck.CODEC
																  .fieldOf("check")
																  .forGetter(Location::check)
												  ).apply(instance, Location::new));

		@Override
		public Codec<Location> codec() {
			return CODEC;
		}
	}
}
