package snownee.lychee;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import snownee.lychee.anvil_crafting.AnvilContext;
import snownee.lychee.anvil_crafting.AnvilCraftingRecipe;
import snownee.lychee.block_crushing.BlockCrushingRecipe;
import snownee.lychee.block_crushing.BlockCrushingRecipeType;
import snownee.lychee.block_exploding.BlockExplodingContext;
import snownee.lychee.block_exploding.BlockExplodingRecipe;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.type.BlockKeyRecipeType;
import snownee.lychee.core.recipe.type.ItemShapelessRecipeType;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.dripstone_dripping.DripstoneRecipe;
import snownee.lychee.dripstone_dripping.DripstoneRecipeType;
import snownee.lychee.interaction.BlockClickingRecipe;
import snownee.lychee.interaction.BlockInteractingRecipe;
import snownee.lychee.item_burning.ItemBurningRecipe;
import snownee.lychee.item_exploding.ItemExplodingRecipe;
import snownee.lychee.item_inside.ItemInsideRecipe;
import snownee.lychee.item_inside.ItemInsideRecipeType;
import snownee.lychee.lightning_channeling.LightningChannelingRecipe;
import snownee.lychee.random_block_ticking.RandomBlockTickingRecipe;
import snownee.lychee.random_block_ticking.RandomBlockTickingRecipeType;

public final class RecipeTypes {

	static {
		LycheeLootContextParams.init();
		LycheeLootContextParamSets.init();
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

	public static final Set<LycheeRecipeType<?, ?>> ALL = Sets.newLinkedHashSet();
	public static final LycheeRecipeType<LycheeContext, ItemBurningRecipe> ITEM_BURNING = register(new LycheeRecipeType<>("item_burning", ItemBurningRecipe.class, null));
	public static final ItemInsideRecipeType ITEM_INSIDE = register(new ItemInsideRecipeType("item_inside", ItemInsideRecipe.class, null));
	public static final BlockKeyRecipeType<LycheeContext, BlockInteractingRecipe> BLOCK_INTERACTING = register(new BlockKeyRecipeType<>("block_interacting", BlockInteractingRecipe.class, LycheeLootContextParamSets.BLOCK_INTERACTION));
	public static final BlockKeyRecipeType<LycheeContext, BlockClickingRecipe> BLOCK_CLICKING = register(new BlockKeyRecipeType<>("block_clicking", BlockClickingRecipe.class, LycheeLootContextParamSets.BLOCK_INTERACTION));
	public static final LycheeRecipeType<AnvilContext, AnvilCraftingRecipe> ANVIL_CRAFTING = register(new LycheeRecipeType<>("anvil_crafting", AnvilCraftingRecipe.class, null));
	public static final BlockCrushingRecipeType BLOCK_CRUSHING = register(new BlockCrushingRecipeType("block_crushing", BlockCrushingRecipe.class, null));
	public static final ItemShapelessRecipeType<ItemShapelessContext, LightningChannelingRecipe> LIGHTNING_CHANNELING = register(new ItemShapelessRecipeType<>("lightning_channeling", LightningChannelingRecipe.class, null));
	public static final ItemShapelessRecipeType<ItemShapelessContext, ItemExplodingRecipe> ITEM_EXPLODING = register(new ItemShapelessRecipeType<>("item_exploding", ItemExplodingRecipe.class, null));
	public static final BlockKeyRecipeType<BlockExplodingContext, BlockExplodingRecipe> BLOCK_EXPLODING = register(new BlockKeyRecipeType<>("block_exploding", BlockExplodingRecipe.class, LycheeLootContextParamSets.BLOCK_ONLY));
	public static final RandomBlockTickingRecipeType RANDOM_BLOCK_TICKING = register(new RandomBlockTickingRecipeType("random_block_ticking", RandomBlockTickingRecipe.class, LycheeLootContextParamSets.BLOCK_ONLY));
	public static final DripstoneRecipeType DRIPSTONE_DRIPPING = register(new DripstoneRecipeType("dripstone_dripping", DripstoneRecipe.class, LycheeLootContextParamSets.BLOCK_ONLY));

	public static <T extends LycheeRecipeType<?, ?>> T register(T recipeType) {
		return Registry.register(BuiltInRegistries.RECIPE_TYPE, recipeType.id, recipeType);
	}

	public static void buildCache() {
		ALL.forEach(LycheeRecipeType::buildCache);
		ALL.forEach(LycheeRecipeType::updateEmptyState);
	}

}
