package snownee.lychee;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraftforge.registries.ForgeRegistries;
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
import snownee.lychee.interaction.BlockClickingRecipe;
import snownee.lychee.interaction.BlockInteractingRecipe;
import snownee.lychee.item_burning.ItemBurningRecipe;
import snownee.lychee.item_exploding.ItemExplodingRecipe;
import snownee.lychee.item_inside.ItemInsideRecipe;
import snownee.lychee.item_inside.ItemInsideRecipeType;
import snownee.lychee.lightning_channeling.LightningChannelingRecipe;

public final class RecipeTypes {

	static {
		LycheeLootContextParamSets.init();
	}

	public static void init() {
		BLOCK_INTERACTING.setRequiresClient();
		BLOCK_CLICKING.setRequiresClient();
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
	public static final BlockKeyRecipeType<BlockExplodingContext, BlockExplodingRecipe> BLOCK_EXPLODING = register(new BlockKeyRecipeType<>("block_exploding", BlockExplodingRecipe.class, null));

	public static <T extends LycheeRecipeType<?, ?>> T register(T recipeType) {
		ALL.add(recipeType);
		ForgeRegistries.RECIPE_TYPES.register(recipeType.id, recipeType);
		return recipeType;
	}

	public static void buildCache() {
		ALL.forEach(LycheeRecipeType::buildCache);
		ALL.forEach(LycheeRecipeType::updateEmptyState);
	}

}
