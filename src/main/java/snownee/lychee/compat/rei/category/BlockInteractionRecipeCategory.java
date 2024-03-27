package snownee.lychee.compat.rei.category;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.recipes.BlockInteractingRecipe;

public class BlockInteractionRecipeCategory extends ItemAndBlockBaseCategory<BlockInteractingRecipe> {


	public BlockInteractionRecipeCategory(
			CategoryIdentifier<? extends LycheeDisplay<BlockInteractingRecipe>> id,
			Renderer icon
	) {
		super(id, icon, RecipeTypes.BLOCK_INTERACTING);
		inputBlockRect.setX(inputBlockRect.getX() + 18);
		methodRect.setX(methodRect.getX() + 18);
		infoRect.setX(infoRect.getX() + 10);
	}

	@Override
	public @Nullable Component getMethodDescription(BlockInteractingRecipe recipe) {
		return Component.translatable(Util.makeDescriptionId("tip", BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer())));
	}

	@Override
	protected void renderIngredientGroup(List<Widget> widgets, Point startPoint, BlockInteractingRecipe recipe, int y) {
		ingredientGroup(widgets, startPoint, recipe, 22, 21);
	}

	@Override
	public void drawExtra(BlockInteractingRecipe recipe, GuiGraphics graphics, double mouseX, double mouseY, int centerX) {
		KeyMapping keyMapping = getKeyMapping(recipe);
		//		if (keyMapping.getKey().getValue() == -1) { // key is unset or unknown
		//
		//		}
		//		Minecraft.getInstance().font.draw(matrixStack, keyMapping.getTranslatedKeyMessage(), 0, 0, 0);
		AllGuiTextures icon;
		if (keyMapping.matchesMouse(0)) {
			icon = AllGuiTextures.LEFT_CLICK;
		} else if (keyMapping.matchesMouse(1)) {
			icon = AllGuiTextures.RIGHT_CLICK;
		} else {
			icon = recipe.getType() == RecipeTypes.BLOCK_CLICKING ? AllGuiTextures.LEFT_CLICK : AllGuiTextures.RIGHT_CLICK;
		}
		icon.render(graphics, 51, 15);
	}

	private KeyMapping getKeyMapping(BlockInteractingRecipe recipe) {
		boolean click = recipe.getType() == RecipeTypes.BLOCK_CLICKING;
		return click ? Minecraft.getInstance().options.keyAttack : Minecraft.getInstance().options.keyUse;
	}

	public int contentWidth() {
		return super.contentWidth() + 20;
	}
}
