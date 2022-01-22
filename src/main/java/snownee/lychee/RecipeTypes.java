package snownee.lychee;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ItemAndBlockRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.BlockKeyRecipeType;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.interaction.BlockClickingRecipe;
import snownee.lychee.interaction.BlockInteractingRecipe;
import snownee.lychee.item_burning.ItemBurningRecipe;
import snownee.lychee.item_inside.ItemInsideRecipe;

public final class RecipeTypes {

	public static void init() {
	}

	public static final List<LycheeRecipeType<?, ?>> ALL = Lists.newLinkedList();
	public static final LycheeRecipeType<LycheeContext, ItemBurningRecipe> ITEM_BURNING = type("item_burning", ItemBurningRecipe.class);
	public static final BlockKeyRecipeType<LycheeContext, ItemInsideRecipe> ITEM_INSIDE = blockKey("item_inside", ItemInsideRecipe.class, null);
	public static final BlockKeyRecipeType<LycheeContext, BlockInteractingRecipe> BLOCK_INTERACTING = blockKey("block_interacting", BlockInteractingRecipe.class, LycheeLootContextParamSets.BLOCK_INTERACTION);
	public static final BlockKeyRecipeType<LycheeContext, BlockClickingRecipe> BLOCK_CLICKING = blockKey("block_clicking", BlockClickingRecipe.class, LycheeLootContextParamSets.BLOCK_INTERACTION);

	public static <C extends LycheeContext, T extends LycheeRecipe<C>> LycheeRecipeType<C, T> type(String name, Class<T> clazz) {
		LycheeRecipeType<C, T> recipeType = new LycheeRecipeType<>(name, clazz, null);
		ALL.add(recipeType);
		return Registry.register(Registry.RECIPE_TYPE, recipeType.id, recipeType);
	}

	public static <C extends LycheeContext, T extends ItemAndBlockRecipe<C>> BlockKeyRecipeType<C, T> blockKey(String name, Class<T> clazz, @Nullable LootContextParamSet paramSet) {
		BlockKeyRecipeType<C, T> recipeType = new BlockKeyRecipeType<>(name, clazz, paramSet);
		ALL.add(recipeType);
		return Registry.register(Registry.RECIPE_TYPE, recipeType.id, recipeType);
	}

}
