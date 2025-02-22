package snownee.lychee.compat.jei.category;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.recipes.BlockInteractingRecipe;

@NotNullByDefault
public class BlockInteractionRecipeCategory extends ItemAndBlockBaseCategory<BlockInteractingRecipe> {

	public BlockInteractionRecipeCategory(RecipeType<RecipeHolder<BlockInteractingRecipe>> recipeType, IDrawable icon) {
		super(recipeType, icon, RecipeTypes.BLOCK_INTERACTING);
		inputBlockRect.setX(inputBlockRect.getX() + 18);
		methodRect.setX(methodRect.getX() + 18);
		infoRect.setX(infoRect.getX() + 10);
	}

	@Override
	public @Nullable Component getMethodDescription(BlockInteractingRecipe recipe) {
		return Component.translatable(Util.makeDescriptionId("tip", BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer())));
	}

	@Override
	protected void renderIngredientGroup(IRecipeLayoutBuilder builder, BlockInteractingRecipe recipe, int y) {
		ingredientGroup(builder, recipe, 22, 21);
	}

	@Override
	public void drawExtra(
			RecipeHolder<BlockInteractingRecipe> recipeHolder,
			GuiGraphics graphics,
			double mouseX,
			double mouseY,
			int centerX) {
		BlockInteractingRecipe recipe = recipeHolder.value();
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
