package snownee.lychee;

import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import snownee.lychee.recipes.AnvilCraftingRecipe;
import snownee.lychee.recipes.BlockClickingRecipe;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.recipes.BlockCrushingRecipeType;
import snownee.lychee.recipes.BlockExplodingRecipe;
import snownee.lychee.recipes.BlockInteractingRecipe;
import snownee.lychee.recipes.DripstoneRecipe;
import snownee.lychee.recipes.DripstoneRecipeType;
import snownee.lychee.recipes.ItemBurningRecipe;
import snownee.lychee.recipes.ItemExplodingRecipe;
import snownee.lychee.recipes.ItemInsideRecipe;
import snownee.lychee.recipes.ItemInsideRecipeType;
import snownee.lychee.recipes.LightningChannelingRecipe;
import snownee.lychee.recipes.RandomBlockTickingRecipe;
import snownee.lychee.recipes.RandomBlockTickingRecipeType;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.ItemShapelessRecipeType;
import snownee.lychee.util.recipe.LycheeRecipeType;

public final class RecipeTypes {

	static {
		Objects.requireNonNull(LycheeLootContextParams.ALL);
		Objects.requireNonNull(LycheeLootContextParamSets.ALL);
	}

	public static void init() {
		BLOCK_INTERACTING.requiresClient = true;
		BLOCK_CLICKING.requiresClient = true;
		DRIPSTONE_DRIPPING.requiresClient = true; // we need the particle
		RANDOM_BLOCK_TICKING.extractChance = true;
		RANDOM_BLOCK_TICKING.hasStandaloneCategory = false;
		DRIPSTONE_DRIPPING.extractChance = true;
		BLOCK_INTERACTING.canPreventConsumeInputs = true;
		BLOCK_CLICKING.canPreventConsumeInputs = true;
		BLOCK_CLICKING.categoryId = BLOCK_INTERACTING.categoryId; // they share the same category
		ITEM_INSIDE.canPreventConsumeInputs = true;
		LIGHTNING_CHANNELING.canPreventConsumeInputs = true;
		ITEM_EXPLODING.canPreventConsumeInputs = true;
		ANVIL_CRAFTING.hasStandaloneCategory = false;
	}

	public static final Set<LycheeRecipeType<LycheeContext, ? extends ILycheeRecipe<LycheeContext>>> ALL = Sets.newLinkedHashSet();
	public static final LycheeRecipeType<LycheeContext, ItemBurningRecipe> ITEM_BURNING =
			register(new LycheeRecipeType<>("item_burning", ItemBurningRecipe.class, null));
	public static final ItemInsideRecipeType ITEM_INSIDE = register(new ItemInsideRecipeType(
			"item_inside",
			ItemInsideRecipe.class,
			null
	));
	public static final BlockKeyableRecipeType<BlockInteractingRecipe>
			BLOCK_INTERACTING =
			register(new BlockKeyableRecipeType<>(
					"block_interacting",
					BlockInteractingRecipe.class,
					LycheeLootContextParamSets.BLOCK_INTERACTION
			));
	public static final BlockKeyableRecipeType<BlockClickingRecipe> BLOCK_CLICKING =
			register(new BlockKeyableRecipeType<>(
					"block_clicking",
					BlockClickingRecipe.class,
					LycheeLootContextParamSets.BLOCK_INTERACTION
			));
	public static final LycheeRecipeType<LycheeContext, AnvilCraftingRecipe> ANVIL_CRAFTING =
			register(new LycheeRecipeType<>("anvil_crafting", AnvilCraftingRecipe.class, null));
	public static final BlockCrushingRecipeType BLOCK_CRUSHING = register(new BlockCrushingRecipeType(
			"block_crushing",
			BlockCrushingRecipe.class,
			null
	));
	public static final ItemShapelessRecipeType<LightningChannelingRecipe> LIGHTNING_CHANNELING =
			register(new ItemShapelessRecipeType<>("lightning_channeling", LightningChannelingRecipe.class, null));
	public static final ItemShapelessRecipeType<ItemExplodingRecipe> ITEM_EXPLODING =
			register(new ItemShapelessRecipeType<>("item_exploding", ItemExplodingRecipe.class, null));
	public static final BlockKeyableRecipeType<BlockExplodingRecipe> BLOCK_EXPLODING =
			register(new BlockKeyableRecipeType<>(
					"block_exploding",
					BlockExplodingRecipe.class,
					LycheeLootContextParamSets.BLOCK_ONLY
			));
	public static final RandomBlockTickingRecipeType RANDOM_BLOCK_TICKING = register(new RandomBlockTickingRecipeType(
			"random_block_ticking",
			RandomBlockTickingRecipe.class,
			LycheeLootContextParamSets.BLOCK_ONLY
	));
	public static final DripstoneRecipeType DRIPSTONE_DRIPPING = register(new DripstoneRecipeType(
			"dripstone_dripping",
			DripstoneRecipe.class,
			LycheeLootContextParamSets.BLOCK_ONLY
	));

	public static <T extends LycheeRecipeType<LycheeContext, ? extends ILycheeRecipe<LycheeContext>>> T register(T recipeType) {
		ALL.add(recipeType);
		return Registry.register(BuiltInRegistries.RECIPE_TYPE, recipeType.id, recipeType);
	}

	public static void buildCache() {
		ALL.forEach(LycheeRecipeType::refreshCache);
		ALL.forEach(LycheeRecipeType::updateEmptyState);
	}

}
