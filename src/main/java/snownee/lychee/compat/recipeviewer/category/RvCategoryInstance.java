package snownee.lychee.compat.recipeviewer.category;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.ui.CategoryMetadata;
import snownee.lychee.util.ui.CategoryModifier;


public interface RvCategoryInstance<R extends ILycheeRecipe<LycheeContext>> {
	RvCategory<R> type();

	RvHelper helper();

	Identifier id();

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

	default Component title() {
		return RVs.makeTitle(id());
	}

	CategoryMetadata metadata();

	List<RecipeHolder<CategoryModifier>> modifiers();

	default RenderElement icon() {
		return type().iconProvider.get(this);
	}

	default List<SlotDisplay> workstations() {
		return type().workstationProvider.get(this);
	}

	default boolean renderDefault() {
		return true;
	}

	Map<String, RvCategoryDecoration<R>> decorations();

	Map<String, Predicate<R>> conditions();

	void configureDecorations(RvCategoryWidgetBuilder<R> builder, RecipeHolder<R> recipeHolder);
}
