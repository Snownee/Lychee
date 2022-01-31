package snownee.lychee.compat.jei.category;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.type.BlockKeyRecipeType;
import snownee.lychee.interaction.BlockInteractingRecipe;

public class BlockInteractionRecipeCategory extends ItemInsideRecipeCategory<BlockInteractingRecipe> {

	public BlockInteractionRecipeCategory(List<BlockKeyRecipeType<LycheeContext, BlockInteractingRecipe>> recipeTypes, IGuiHelper guiHelper, ScreenElement mainIcon) {
		super(recipeTypes, guiHelper, mainIcon);
	}

	@Override
	public @Nullable Component getMethodDescription(BlockInteractingRecipe recipe) {
		return new TranslatableComponent(Util.makeDescriptionId("tip", recipe.getSerializer().getRegistryName()));
	}

	@Override
	public void drawExtra(BlockInteractingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
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
		icon.render(matrixStack, 33, 15);
	}

	private KeyMapping getKeyMapping(BlockInteractingRecipe recipe) {
		boolean click = recipe.getType() == RecipeTypes.BLOCK_CLICKING;
		return click ? Minecraft.getInstance().options.keyAttack : Minecraft.getInstance().options.keyUse;
	}

}
