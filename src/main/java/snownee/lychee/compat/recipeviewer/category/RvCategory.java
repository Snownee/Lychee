package snownee.lychee.compat.recipeviewer.category;

import java.util.List;

import org.joml.Vector2fc;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.ui.CategoryMetadata;

@NotNullByDefault
public interface RvCategory<R extends ILycheeRecipe<LycheeContext>> {
	RvCategoryType<R> type();

	RvHelper rvHelper();

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

	Vector2fc infoPosition();

	default Component title() {
		return RVs.makeTitle(id());
	}

	default RenderElement icon() {
		return type().iconProvider.get(this);
	}

	default List<List<ItemStack>> workstations() {
		return type().workstationProvider.get(this);
	}

	void configureLayout(RvCategoryLayoutBuilder builder, RecipeHolder<R> recipeHolder, Vector2fc position);

	void configureDecorations(RvCategoryWidgetBuilder builder, RecipeHolder<R> recipeHolder, Vector2fc position);

	void setMetadata(CategoryMetadata metadata);
}
