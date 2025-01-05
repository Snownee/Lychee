package snownee.lychee.compat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joml.Quaternionf;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.CustomLightingSettings;
import snownee.lychee.client.gui.ILightingSettings;
import snownee.lychee.util.CachedRenderingEntity;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostActionRenderer;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public final class JEIREI {
	public static final CachedRenderingEntity<PrimedTnt> TNT_ENTITY = CachedRenderingEntity.ofFactory(EntityType.TNT::create);
	public static ILightingSettings BLOCK_LIGHTING = CustomLightingSettings.builder()
			.firstLightRotation(-45, -45)
			.secondLightRotation(15, -60)
			.build();
	public static ILightingSettings SIDE_ICON_LIGHTING = CustomLightingSettings.builder()
			.firstLightRotation(-30, -60)
			.secondLightRotation(30, -60)
			.build();
	public static ILightingSettings FUSED_TNT_LIGHTING = CustomLightingSettings.builder()
			.firstLightRotation(-120, 20)
			.secondLightRotation(200, 45)
			.build();

	public static List<IngredientInfo> generateShapelessInputs(ILycheeRecipe<LycheeContext> recipe) {
		var ingredients = recipe.getIngredients()
				.stream()
				.map(IngredientInfo::new)
				.collect(Collectors.toCollection(ArrayList::new));
		recipe.postActions().forEach(action -> PostActionRenderer.of(action).loadCatalystsInfo(action, recipe, ingredients));
		var type = (LycheeRecipeType<?>) recipe.getType();
		if (!type.compactInputs) {
			addIngredientTips(recipe, ingredients);
			return ingredients;
		}
		List<IngredientInfo> newIngredients = Lists.newArrayList();
		for (var ingredient : ingredients) {
			IngredientInfo match = null;
			boolean simpleA = CommonProxy.isSimpleIngredient(ingredient.ingredient);
			for (var toCompare : newIngredients) {
				boolean simpleB = CommonProxy.isSimpleIngredient(toCompare.ingredient);
				if (simpleA != simpleB) {
					continue;
				}
				if (ingredient.isCatalyst != toCompare.isCatalyst) {
					continue;
				}
				if (!Objects.equals(toCompare.tooltips, ingredient.tooltips)) {
					continue;
				}
				if (simpleA) {
					if (toCompare.ingredient.getStackingIds().equals(ingredient.ingredient.getStackingIds())) {
						match = toCompare;
						break;
					}
				} else {
					ItemStack[] itemsA = ingredient.ingredient.getItems();
					ItemStack[] itemsB = toCompare.ingredient.getItems();
					if (itemsA.length == 0 || itemsA.length != itemsB.length) {
						continue;
					}
					boolean matchAll = true;
					for (int i = 0; i < itemsA.length; i++) {
						if (!ItemStack.isSameItemSameComponents(itemsA[i], itemsB[i])) {
							matchAll = false;
							break;
						}
					}
					if (matchAll) {
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

	public static void addIngredientTips(ILycheeRecipe<LycheeContext> recipe, List<IngredientInfo> ingredients) {
		for (IngredientInfo ingredient : ingredients) {
			IngredientInfo.Type type = CommonProxy.getIngredientType(ingredient.ingredient);
			if (type != IngredientInfo.Type.NORMAL) {
				ingredient.addTooltip(Component.translatable("tip.lychee.ingredient." + type.name().toLowerCase(Locale.ROOT)));
			}
		}
	}

	public static MutableComponent makeTitle(ResourceLocation id) {
		var key = id.toLanguageKey("recipeType");
		var i = key.indexOf('/');
		if ("/minecraft/default".equals(key.substring(i))) {
			key = key.substring(0, i);
		}
		return Component.translatable(key);
	}

	public static List<Component> getRecipeTooltip(ILycheeRecipe<?> recipe) {
		var list = Lists.<Component>newArrayList();
		if (recipe.comment().map(it -> !Strings.isNullOrEmpty(it)).orElse(false)) {
			var comment = recipe.comment().orElseThrow();
			if (I18n.exists(comment)) {
				comment = I18n.get(comment);
			}
			Splitter.on('\n').splitToStream(comment).map(Component::literal).forEach(list::add);
		}
		var mc = Minecraft.getInstance();
		recipe.conditions().appendToTooltips(list, mc.level, mc.player, 0);
		return list;
	}

	public static <T extends BlockKeyableRecipe<?>> Pair<BlockState, Integer> getMostUsedBlock(Collection<RecipeHolder<? extends T>> recipes) {
		var blockStateCount = new Object2IntOpenHashMap<Block>();
		var blockPredicateMap = Maps.<Block, BlockPredicate>newHashMap();
		for (var object : recipes) {
			var recipe = object.value();
			for (var block : BlockPredicateExtensions.matchedBlocks(recipe.blockPredicate())) {
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
		return blockStateCount.object2IntEntrySet().stream()
				.max(Comparator.comparingInt(Object2IntMap.Entry::getIntValue))
				.map($ -> Pair.of(BlockPredicateExtensions.anyBlockState(blockPredicateMap.get($.getKey())), $.getIntValue()))
				.orElseGet(() -> Pair.of(Blocks.AIR.defaultBlockState(), 0));
	}

	public static void renderTnt(GuiGraphics graphics, float x, float y) {
		PrimedTnt tnt = TNT_ENTITY.getEntity();
		int fuse = 80 - tnt.tickCount % 80;
		if (fuse >= 40) {
			return;
		}
		TNT_ENTITY.earlySetLevel();
		tnt.setFuse(fuse);
		float toRad = 0.01745329251F;
		Quaternionf quaternion = new Quaternionf().rotateXYZ(200 * toRad, -20 * toRad, 0);
		FUSED_TNT_LIGHTING.applyLighting();
		TNT_ENTITY.render(graphics.pose(), x, y, 20, quaternion);
	}

	public static <T> ImmutableMap<T, List<RecipeHolder<? extends ILycheeRecipe<LycheeContext>>>> generateCategories(
			LycheeRecipeType<? extends ILycheeRecipe<LycheeContext>> recipeType,
			Function<ResourceLocation, T> categoryFactory) {
		return recipeType
				.inViewerRecipes()
				.stream()
				.reduce(
						ImmutableMultimap.<ResourceLocation, RecipeHolder<? extends ILycheeRecipe<LycheeContext>>>builder(),
						(map, recipeHolder) -> {
							map.put(
									ResourceLocation.parse(recipeHolder.value().group()),
									recipeHolder
							);
							return map;
						},
						(map, ignored) -> map)
				.build()
				.asMap()
				.entrySet()
				.stream()
				.collect(ImmutableMap.toImmutableMap(
						entry -> categoryFactory.apply(composeCategoryIdentifier(recipeType.categoryId, entry.getKey())),
						it -> List.copyOf(it.getValue())));
	}

	public static ResourceLocation composeCategoryIdentifier(ResourceLocation categoryId, ResourceLocation group) {
		return ResourceLocation.fromNamespaceAndPath(
				categoryId.getNamespace(),
				"%s/%s/%s".formatted(categoryId.getPath(), group.getNamespace(), group.getPath()));
	}
}
