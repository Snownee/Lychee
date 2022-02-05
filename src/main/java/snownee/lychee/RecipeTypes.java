package snownee.lychee;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.core.Registry;
import snownee.lychee.anvil_crafting.AnvilContext;
import snownee.lychee.anvil_crafting.AnvilCraftingRecipe;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.type.BlockKeyRecipeType;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.interaction.BlockClickingRecipe;
import snownee.lychee.interaction.BlockInteractingRecipe;
import snownee.lychee.item_burning.ItemBurningRecipe;
import snownee.lychee.item_inside.ItemInsideRecipe;

public final class RecipeTypes {

	static {
		LycheeLootContextParamSets.init();
	}

	public static void init() {
	}

	public static final Set<LycheeRecipeType<?, ?>> ALL = Sets.newLinkedHashSet();
	public static final LycheeRecipeType<LycheeContext, ItemBurningRecipe> ITEM_BURNING = register(new LycheeRecipeType<>("item_burning", ItemBurningRecipe.class, null));
	public static final BlockKeyRecipeType<LycheeContext, ItemInsideRecipe> ITEM_INSIDE = register(new BlockKeyRecipeType<>("item_inside", ItemInsideRecipe.class, null));
	public static final BlockKeyRecipeType<LycheeContext, BlockInteractingRecipe> BLOCK_INTERACTING = register(new BlockKeyRecipeType<>("block_interacting", BlockInteractingRecipe.class, LycheeLootContextParamSets.BLOCK_INTERACTION));
	public static final BlockKeyRecipeType<LycheeContext, BlockClickingRecipe> BLOCK_CLICKING = register(new BlockKeyRecipeType<>("block_clicking", BlockClickingRecipe.class, LycheeLootContextParamSets.BLOCK_INTERACTION));
	public static final LycheeRecipeType<AnvilContext, AnvilCraftingRecipe> ANVIL_CRAFTING = register(new LycheeRecipeType<>("anvil_crafting", AnvilCraftingRecipe.class, null));

	public static <T extends LycheeRecipeType<?, ?>> T register(T recipeType) {
		ALL.add(recipeType);
		return Registry.register(Registry.RECIPE_TYPE, recipeType.id, recipeType);
	}

}
