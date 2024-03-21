package snownee.lychee.compat.rei.category;

import java.util.List;

import com.google.common.collect.ImmutableList;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.recipes.BlockCrushingRecipeType;

public record BlockCrushingRecipeCategory(
		CategoryIdentifier<? extends LycheeDisplay<BlockCrushingRecipe>> categoryIdentifier,
		Renderer icon,
		Rect2i fallingBlockRect,
		Rect2i landingBlockRect
) implements LycheeCategory<BlockCrushingRecipe>, LycheeDisplayCategory<LycheeDisplay<BlockCrushingRecipe>> {

	public static final Rect2i FALLING_BLOCK_RECT = new Rect2i(0, -35, 20, 35);
	public static final Rect2i LANDING_BLOCK_RECT = new Rect2i(0, 0, 20, 20);

	public BlockCrushingRecipeCategory(
			CategoryIdentifier<? extends LycheeDisplay<BlockCrushingRecipe>> categoryIdentifier,
			Renderer icon
	) {
		this(categoryIdentifier, icon, FALLING_BLOCK_RECT, LANDING_BLOCK_RECT);
	}

	@Override
	public List<Widget> setupDisplay(LycheeDisplay<BlockCrushingRecipe> display, Rectangle bounds) {
		var builder = ImmutableList.<Widget>builder();

		builder.add(Widgets.createRecipeBase(bounds));

		return builder.build();
	}

	@Override
	public BlockCrushingRecipeType recipeType() {
		return RecipeTypes.BLOCK_CRUSHING;
	}
}
