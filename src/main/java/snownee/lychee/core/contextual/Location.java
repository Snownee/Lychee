package snownee.lychee.core.contextual;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.FluidPredicate;
import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.def.BoundsHelper;
import snownee.lychee.core.def.LocationPredicateHelper;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.mixin.BlockPredicateAccess;
import snownee.lychee.mixin.LightPredicateAccess;
import snownee.lychee.mixin.LocationCheckAccess;
import snownee.lychee.mixin.LocationPredicateAccess;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.CommonProxy;

public record Location(LocationCheck check) implements ContextualCondition {

	private static final Rule X = new PosRule("x", LocationPredicateAccess::getX, Vec3::x);
	private static final Rule Y = new PosRule("y", LocationPredicateAccess::getY, Vec3::y);
	private static final Rule Z = new PosRule("z", LocationPredicateAccess::getZ, Vec3::z);
	private static final Rule DIMENSION = new DimensionRule();
	private static final Rule FEATURE = new FeatureRule();
	private static final Rule BIOME = new BiomeRule();
	private static final Rule BLOCK = new BlockRule();
	private static final Rule FLUID = new FluidRule();
	private static final Rule LIGHT = new LightRule();
	private static final Rule SMOKEY = new SmokeyRule();
	public static final Rule[] RULES = new Rule[]{X, Y, Z, DIMENSION, FEATURE, BIOME, BLOCK, FLUID, LIGHT, SMOKEY};

	@Override
	public ContextualConditionType<? extends ContextualCondition> getType() {
		return ContextualConditionTypes.LOCATION;
	}

	@Override
	public int test(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		if (ctx.getLevel().isClientSide) {
			return testClient(ctx.getLevel(), ctx.getParamOrNull(LycheeLootContextParams.BLOCK_POS), ctx.getParamOrNull(LootContextParams.ORIGIN)) == InteractionResult.SUCCESS ? times : 0;
		} else {
			return check.test(ctx.toLootContext()) ? times : 0;
		}
	}

	@Override
	public InteractionResult testInTooltips(Level level, @Nullable Player player) {
		if (player == null) {
			return InteractionResult.PASS;
		}
		LocationCheckAccess checkAccess = (LocationCheckAccess) check;
		if (!BlockPos.ZERO.equals(checkAccess.getOffset())) {
			return InteractionResult.PASS;
		}
		Vec3 vec = player.position();
		BlockPos pos = player.blockPosition();
		return testClient(level, pos, vec);
	}

	public InteractionResult testClient(Level level, BlockPos pos, Vec3 vec) {
		LocationCheckAccess checkAccess = (LocationCheckAccess) check;
		BlockPos offset = checkAccess.getOffset();
		if (!BlockPos.ZERO.equals(offset)) {
			pos = pos.offset(offset.getX(), offset.getY(), offset.getZ());
		}
		LocationPredicateAccess access = (LocationPredicateAccess) checkAccess.getPredicate();
		boolean hasPass = false;
		for (Rule rule : RULES) {
			if (rule.isAny(access))
				continue;
			InteractionResult result = rule.testClient(access, level, pos, vec);
			if (result == InteractionResult.FAIL) {
				return result;
			}
			if (result == InteractionResult.PASS) {
				hasPass = true;
			}
		}
		return hasPass ? InteractionResult.PASS : InteractionResult.SUCCESS;
	}

	@Override
	public void appendTooltips(List<Component> tooltips, Level level, @Nullable Player player, int indent, boolean inverted) {
		LocationCheckAccess checkAccess = (LocationCheckAccess) check;
		LocationPredicateAccess access = (LocationPredicateAccess) checkAccess.getPredicate();
		boolean test = false;
		Vec3 vec = null;
		BlockPos pos = null;
		String key = makeDescriptionId(inverted);
		boolean noOffset = BlockPos.ZERO.equals(checkAccess.getOffset());
		if (!noOffset) {
			BlockPos offset = checkAccess.getOffset();
			MutableComponent content = Component.translatable(key, offset.getX(), offset.getY(), offset.getZ()).withStyle(ChatFormatting.GRAY);
			InteractionResult result = testInTooltips(level, player);
			ContextualCondition.desc(tooltips, result, indent, content);
			++indent;
		}
		if (noOffset && player != null) {
			test = true;
			vec = player.position();
			pos = player.blockPosition();
		}
		for (Location.Rule rule : Location.RULES) {
			if (rule.isAny(access))
				continue;
			InteractionResult result = InteractionResult.PASS;
			if (test) {
				result = rule.testClient(access, level, pos, vec);
			}
			rule.appendTooltips(tooltips, indent, key, access, result);
		}
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return Component.translatable(makeDescriptionId(inverted));
	}

	@Override
	public int showingCount() {
		int c = 0;
		LocationCheckAccess checkAccess = (LocationCheckAccess) check;
		LocationPredicateAccess access = (LocationPredicateAccess) checkAccess.getPredicate();
		for (Rule rule : RULES) {
			if (!rule.isAny(access)) {
				++c;
			}
		}
		return c;
	}

	public interface Rule {
		String getName();

		boolean isAny(LocationPredicateAccess access);

		default InteractionResult testClient(LocationPredicateAccess access, Level level, BlockPos pos, Vec3 vec) {
			return InteractionResult.PASS;
		}

		void appendTooltips(List<Component> tooltips, int indent, String key, LocationPredicateAccess access, InteractionResult result);
	}

	private record PosRule(String name, Function<LocationPredicateAccess, Doubles> boundsGetter,
						   Function<Vec3, Double> valueGetter) implements Rule {
		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean isAny(LocationPredicateAccess access) {
			return boundsGetter.apply(access).isAny();
		}

		@Override
		public InteractionResult testClient(LocationPredicateAccess access, Level level, BlockPos pos, Vec3 vec) {
			return CommonProxy.interactionResult(boundsGetter.apply(access).matches(valueGetter.apply(vec)));
		}

		@Override
		public void appendTooltips(List<Component> tooltips, int indent, String key, LocationPredicateAccess access, InteractionResult result) {
			ContextualCondition.desc(tooltips, result, indent, Component.translatable(key + "." + getName(), BoundsHelper.getDescription(boundsGetter.apply(access)).withStyle(ChatFormatting.WHITE)));
		}
	}

	private static class BlockRule implements Rule {
		@Override
		public String getName() {
			return "block";
		}

		@Override
		public boolean isAny(LocationPredicateAccess access) {
			return access.getBlock() == BlockPredicate.ANY;
		}

		@Override
		public void appendTooltips(List<Component> tooltips, int indent, String key, LocationPredicateAccess access, InteractionResult result) {
			Block block = CommonProxy.getCycledItem(List.copyOf(BlockPredicateHelper.getMatchedBlocks(access.getBlock())), Blocks.AIR, 1000);
			MutableComponent name = block.getName().withStyle(ChatFormatting.WHITE);
			BlockPredicateAccess blockAccess = (BlockPredicateAccess) access.getBlock();
			if (blockAccess.getProperties() != StatePropertiesPredicate.ANY || blockAccess.getNbt() != NbtPredicate.ANY) {
				name.append("*");
			}
			ContextualCondition.desc(tooltips, result, indent, Component.translatable(key + "." + getName(), name));
		}

		@Override
		public InteractionResult testClient(LocationPredicateAccess access, Level level, BlockPos pos, Vec3 vec) {
			return BlockPredicateHelper.fastMatch(access.getBlock(), level.getBlockState(pos), () -> level.getBlockEntity(pos)) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
		}
	}

	private static class FluidRule implements Rule {
		@Override
		public String getName() {
			return "fluid";
		}

		@Override
		public boolean isAny(LocationPredicateAccess access) {
			return access.getFluid() == FluidPredicate.ANY;
		}

		@Override
		public void appendTooltips(List<Component> tooltips, int indent, String key, LocationPredicateAccess access, InteractionResult result) {
		}
	}

	private static class LightRule implements Rule {
		@Override
		public String getName() {
			return "light";
		}

		@Override
		public boolean isAny(LocationPredicateAccess access) {
			return access.getLight() == LightPredicate.ANY;
		}

		@Override
		public InteractionResult testClient(LocationPredicateAccess access, Level level, BlockPos pos, Vec3 vec) {
			int light = level.getMaxLocalRawBrightness(pos);
			return CommonProxy.interactionResult(((LightPredicateAccess) access.getLight()).getComposite().matches(light));
		}

		@Override
		public void appendTooltips(List<Component> tooltips, int indent, String key, LocationPredicateAccess access, InteractionResult result) {
			MutableComponent bounds = BoundsHelper.getDescription(((LightPredicateAccess) access.getLight()).getComposite()).withStyle(ChatFormatting.WHITE);
			ContextualCondition.desc(tooltips, result, indent, Component.translatable(key + "." + getName(), bounds));
		}
	}

	private static class DimensionRule implements Rule {
		@Override
		public String getName() {
			return "dimension";
		}

		@Override
		public boolean isAny(LocationPredicateAccess access) {
			return access.getDimension() == null;
		}

		@Override
		public InteractionResult testClient(LocationPredicateAccess access, Level level, BlockPos pos, Vec3 vec) {
			return CommonProxy.interactionResult(level.dimension() == access.getDimension());
		}

		@Override
		public void appendTooltips(List<Component> tooltips, int indent, String key, LocationPredicateAccess access, InteractionResult result) {
			MutableComponent name = ClientProxy.getDimensionDisplayName(access.getDimension()).withStyle(ChatFormatting.WHITE);
			ContextualCondition.desc(tooltips, result, indent, Component.translatable(key + "." + getName(), name));
		}
	}

	private static class BiomeRule implements Rule {
		@Override
		public String getName() {
			return "biome";
		}

		@Override
		public boolean isAny(LocationPredicateAccess access) {
			return access.getBiome() == null && ((LocationPredicateHelper) access).lychee$getBiomeTag() == null;
		}

		@Override
		public InteractionResult testClient(LocationPredicateAccess access, Level level, BlockPos pos, Vec3 vec) {
			Holder<Biome> biome = level.getBiome(pos);
			if (access.getBiome() != null && biome.is(access.getBiome().location())) {
				return InteractionResult.SUCCESS;
			}
			TagKey<Biome> tag = ((LocationPredicateHelper) access).lychee$getBiomeTag();
			if (tag != null && biome.is(tag)) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.FAIL;
		}

		@Override
		public void appendTooltips(List<Component> tooltips, int indent, String key, LocationPredicateAccess access, InteractionResult result) {
			String valueKey;
			if (access.getBiome() != null) {
				valueKey = Util.makeDescriptionId("biome", access.getBiome().location());
			} else {
				valueKey = Util.makeDescriptionId("biomeTag", ((LocationPredicateHelper) access).lychee$getBiomeTag().location());
			}
			MutableComponent name = Component.translatable(valueKey).withStyle(ChatFormatting.WHITE);
			ContextualCondition.desc(tooltips, result, indent, Component.translatable(key + "." + getName(), name));
		}
	}

	private static class SmokeyRule implements Rule {
		@Override
		public String getName() {
			return "smokey";
		}

		@Override
		public boolean isAny(LocationPredicateAccess access) {
			return access.getSmokey() == null;
		}

		@Override
		public void appendTooltips(List<Component> tooltips, int indent, String key, LocationPredicateAccess access, InteractionResult result) {
			key = key + "." + getName();
			if (access.getSmokey() == Boolean.FALSE) {
				key += ".not";
			}
			ContextualCondition.desc(tooltips, result, indent, Component.translatable(key));
		}
	}

	private static class FeatureRule implements Rule {
		@Override
		public String getName() {
			return "feature";
		}

		@Override
		public boolean isAny(LocationPredicateAccess access) {
			return access.getStructure() == null;
		}

		@Override
		public void appendTooltips(List<Component> tooltips, int indent, String key, LocationPredicateAccess access, InteractionResult result) {
			MutableComponent name = ClientProxy.getStructureDisplayName(access.getStructure().location()).withStyle(ChatFormatting.WHITE);
			ContextualCondition.desc(tooltips, result, indent, Component.translatable(key + "." + getName(), name));
		}
	}

	public static class Type extends ContextualConditionType<Location> {

		@Override
		public Location fromJson(JsonObject o) {
			LocationCheck check = (LocationCheck) LootItemConditions.LOCATION_CHECK.getSerializer().deserialize(o, gsonContext);
			return new Location(check);
		}

		@Override
		public void toJson(Location condition, JsonObject o) {
			new LocationCheck.Serializer().serialize(o, condition.check(), gsonContext);
		}

		@Override
		public Location fromNetwork(FriendlyByteBuf buf) {
			LocationPredicate.Builder builder = LocationPredicateHelper.fromNetwork(buf);
			LocationCheck check = (LocationCheck) LocationCheck.checkLocation(builder, buf.readBlockPos()).build();
			ResourceLocation biomeTag = CommonProxy.readNullableRL(buf);
			if (biomeTag != null) {
				LocationPredicateHelper access = (LocationPredicateHelper) ((LocationCheckAccess) check).getPredicate();
				access.lychee$setBiomeTag(TagKey.create(Registry.BIOME_REGISTRY, biomeTag));
			}
			return new Location(check);
		}

		@Override
		public void toNetwork(Location condition, FriendlyByteBuf buf) {
			LocationCheckAccess access = (LocationCheckAccess) condition.check();
			LocationPredicateHelper.toNetwork(access.getPredicate(), buf);
			buf.writeBlockPos(access.getOffset());
			ResourceLocation biomeTag = Optional.ofNullable(((LocationPredicateHelper) access.getPredicate()).lychee$getBiomeTag()).map(TagKey::location).orElse(null);
			CommonProxy.writeNullableRL(biomeTag, buf);
		}

	}

}
