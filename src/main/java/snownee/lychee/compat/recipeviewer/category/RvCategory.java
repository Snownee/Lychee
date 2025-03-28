package snownee.lychee.compat.recipeviewer.category;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2ic;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVHelper;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public interface RvCategory<R extends ILycheeRecipe<LycheeContext>> {
	RvCategoryType<R> type();

	RVHelper rvHelper();

	ResourceLocation id();

	List<RecipeHolder<R>> recipes();

	default void addRecipe(RecipeHolder<R> recipeHolder) {
		recipes().add(recipeHolder);
	}

	default int width() {
		return type().width;
	}

	default int height() {
		return type().height;
	}

	default Vector2ic infoPosition() {
		return type().infoPosition;
	}

	default @NotNull Component title() {
		return RVs.makeTitle(id());
	}

	default RenderElement icon() {
		return type().iconProvider.get(this);
	}

	default List<List<ItemStack>> workstations() {
		return type().workstationProvider.get(this);
	}

	void configureLayout(RvCategoryLayoutBuilder builder, RecipeHolder<R> recipeHolder, Vector2ic position);

	void configureDecorations(RvCategoryWidgetBuilder builder, RecipeHolder<R> recipeHolder, Vector2ic position);
}
