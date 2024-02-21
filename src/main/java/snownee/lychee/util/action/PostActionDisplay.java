package snownee.lychee.util.action;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.compat.IngredientInfo;
import snownee.lychee.util.recipe.ILycheeRecipe;

public interface PostActionDisplay {

	Component getDisplayName();

	default List<BlockPredicate> getOutputBlocks() {
		return List.of();
	}

	default List<ItemStack> getOutputItems() {
		return List.of();
	}

	default boolean hidden() {
		return preventSync();
	}

	default boolean preventSync() {
		return false;
	}

	default void loadCatalystsInfo(@Nullable ILycheeRecipe<?> recipe, List<IngredientInfo> ingredients) {}

	String toJsonString();
}
