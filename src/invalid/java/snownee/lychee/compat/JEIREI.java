package snownee.lychee.compat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.CustomLightingSettings;
import snownee.lychee.client.gui.ILightingSettings;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.Pair;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class JEIREI {

	/* off */
	public static ILightingSettings BLOCK_LIGHTING = CustomLightingSettings.builder()
			.firstLightRotation(-30, 45)
			.secondLightRotation(0, 65)
			.build();

	public static ILightingSettings SIDE_ICON_LIGHTING = CustomLightingSettings.builder()
			.firstLightRotation(135, 35)
			.secondLightRotation(-20, 50)
			.build();

	public static ILightingSettings FUSED_TNT_LIGHTING = CustomLightingSettings.builder()
			.firstLightRotation(-120, 20)
			.secondLightRotation(200, 45)
			.build();
	/* on */

	public static List<IngredientInfo> generateShapelessInputs(OldLycheeRecipe<?> recipe) {
		/* off */
		List<IngredientInfo> ingredients = recipe.getIngredients()
				.stream()
				.map(IngredientInfo::new)
				.collect(Collectors.toCollection(ArrayList::new));
		/* on */
		recipe.getPostActions().forEach(action -> action.loadCatalystsInfo(recipe, ingredients));
		if (!recipe.getType().compactInputs) {
			addIngredientTips(recipe, ingredients);
			return ingredients;
		}
		List<IngredientInfo> newIngredients = Lists.newArrayList();
		for (var ingredient : ingredients) {
			IngredientInfo match = null;
			if (CommonProxy.isSimpleIngredient(ingredient.ingredient)) {
				for (var toCompare : newIngredients) {
					if (Objects.equals(toCompare.tooltips, ingredient.tooltips) && CommonProxy.isSimpleIngredient(
							toCompare.ingredient) &&
							toCompare.ingredient.getStackingIds().equals(ingredient.ingredient.getStackingIds())) {
						match = toCompare;
						break;
					}
				}
			}
			if (match == null) {
				newIngredients.add(ingredient);
			} else {
				match.count += ingredient.count;
			}
		}
		addIngredientTips(recipe, newIngredients);
		return newIngredients;
	}

	public static void addIngredientTips(OldLycheeRecipe<?> recipe, List<IngredientInfo> ingredients) {
		for (IngredientInfo ingredient : ingredients) {
			IngredientInfo.Type type = CommonProxy.getIngredientType(ingredient.ingredient);
			if (type != IngredientInfo.Type.NORMAL) {
				ingredient.addTooltip(Component.translatable(
						"tip.lychee.ingredient." + type.name().toLowerCase(Locale.ROOT)));
			}
		}
	}

	public static ResourceLocation composeCategoryIdentifier(ResourceLocation categoryId, ResourceLocation group) {
		return new ResourceLocation(
				categoryId.getNamespace(),
				"%s/%s/%s".formatted(categoryId.getPath(), group.getNamespace(), group.getPath())
		);
	}

	public static MutableComponent makeTitle(ResourceLocation id) {
		String key = id.toLanguageKey("recipeType");
		int i = key.indexOf('/');
		if ("/minecraft/default".equals(key.substring(i))) {
			key = key.substring(0, i);
		}
		return Component.translatable(key);
	}

	public static <T extends OldLycheeRecipe<?>> Pair<BlockState, Integer> getMostUsedBlock(List<T> recipes) {
		Object2IntMap<Block> blockStateCount = new Object2IntOpenHashMap<>();
		Map<Block, BlockPredicate> blockPredicateMap = Maps.newHashMap();
		for (T object : recipes) {
			BlockKeyableRecipe<?> recipe = (BlockKeyableRecipe<?>) object;
			for (Block block : BlockPredicateExtensions.matchedBlocks(recipe.blockPredicate())) {
				if (block.defaultBlockState().isAir()) {
					continue;
				}
				blockStateCount.mergeInt(block, 1, Integer::sum);
				blockPredicateMap.putIfAbsent(block, recipe.blockPredicate());
			}
		}
		if (blockStateCount.isEmpty()) {
			return Pair.of(Blocks.AIR.defaultBlockState(), 0);
		}
		/* off */
		return blockStateCount.object2IntEntrySet().stream()
				.max(Comparator.comparingInt(Object2IntMap.Entry::getIntValue))
				.map($ -> Pair.of(
						BlockPredicateHelper.anyBlockState(blockPredicateMap.get($.getKey())),
						$.getIntValue()
				))
				.orElseGet(() -> Pair.of(Blocks.AIR.defaultBlockState(), 0));
		/* on */
	}

	public static void registerCategories(
			Predicate<ResourceLocation> categoryIdValidator,
			BiConsumer<ResourceLocation, CategoryCreationContext> registrar
	) {
		Map<ResourceLocation, Map<ResourceLocation, List<OldLycheeRecipe<?>>>> recipes = Maps.newHashMap();
		for (LycheeRecipeType<?, ?> recipeType : RecipeTypes.ALL) {
			if (!recipeType.hasStandaloneCategory) {
				continue;
			}
			for (OldLycheeRecipe<?> recipe : recipeType.inViewerRecipes()) {
				recipes.computeIfAbsent(recipeType.categoryId, $ -> Maps.newHashMap())
						.computeIfAbsent(new ResourceLocation(recipe.group), $ -> Lists.newArrayList())
						.add(recipe);
			}
		}
		recipes.forEach((categoryId, map) -> {
			Preconditions.checkArgument(
					categoryIdValidator.test(categoryId),
					"Category factory %s does not exist",
					categoryId
			);
			map.forEach((group, groupRecipes) -> {
				CategoryCreationContext context = new CategoryCreationContext(group, groupRecipes);
				registrar.accept(categoryId, context);
			});
		});
	}

	public static List<Component> getRecipeTooltip(ILycheeRecipe recipe) {
		List<Component> list = Lists.newArrayList();
		if (!Strings.isNullOrEmpty(recipe.getComment())) {
			String comment = recipe.getComment();
			if (I18n.exists(comment)) {
				comment = I18n.get(comment);
			}
			Splitter.on('\n').splitToStream(comment).map(Component::literal).forEach(list::add);
		}
		Minecraft mc = Minecraft.getInstance();
		recipe.getContextualHolder().getConditionTooltips(list, 0, mc.level, mc.player);
		return list;
	}

	public record CategoryCreationContext(ResourceLocation group, List<OldLycheeRecipe<?>> recipes) {
	}

}
