package snownee.lychee.compat.jei.category;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.BlockKeyRecipe;
import snownee.lychee.core.recipe.ItemAndBlockRecipe;
import snownee.lychee.core.recipe.type.BlockKeyRecipeType;
import snownee.lychee.item_inside.ItemInsideRecipe;
import snownee.lychee.util.LUtil;
import snownee.lychee.util.Pair;

public class ItemInsideRecipeCategory<T extends ItemAndBlockRecipe<LycheeContext> & BlockKeyRecipe> extends ItemAndBlockBaseCategory<LycheeContext, T> {

	private BlockState iconBlock;

	public ItemInsideRecipeCategory(BlockKeyRecipeType<LycheeContext, T> recipeType, IGuiHelper guiHelper, ScreenElement mainIcon) {
		this(List.of(recipeType), guiHelper, mainIcon);
	}

	@SuppressWarnings("rawtypes")
	public ItemInsideRecipeCategory(List<BlockKeyRecipeType<LycheeContext, T>> recipeTypes, IGuiHelper guiHelper, ScreenElement mainIcon) {
		super((List) recipeTypes, guiHelper, mainIcon);
	}

	@Override
	public BlockState getIconBlock() {
		if (iconBlock == null) {
			ClientPacketListener con = Minecraft.getInstance().getConnection();
			if (con == null) {
				return Blocks.AIR.defaultBlockState();
			}
			/* off */
			iconBlock = recipeTypes.stream()
					.map($ -> ((BlockKeyRecipeType<?, ?>) $).getMostUsedBlock())
					.max((a, b) -> a.getSecond() - b.getSecond())
					.map(Pair::getFirst)
					.orElse(Blocks.AIR.defaultBlockState());
			/* on */
		}
		return iconBlock;
	}

	@Override
	public void setInputs(T recipe, IIngredients ingredients) {
		List<List<ItemStack>> items = Lists.newArrayList();
		items.add(List.of(recipe.getInput().getItems()));
		List<ItemStack> items1 = BlockPredicateHelper.getMatchedItemStacks(recipe.getBlock());
		if (!items1.isEmpty()) {
			items.add(items1);
		}
		ingredients.setInputLists(VanillaTypes.ITEM, items);
	}

	@Override
	@Nullable
	public BlockPredicate getInputBlock(T recipe) {
		return recipe.getBlock();
	}

	@Override
	public void drawExtra(T recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		super.drawExtra(recipe, matrixStack, mouseX, mouseY);
		ItemInsideRecipe insideRecipe = (ItemInsideRecipe) recipe;
		if (insideRecipe.getTime() > 0) {
			Minecraft.getInstance().font.draw(matrixStack, LUtil.format("tip.lychee.sec", insideRecipe.getTime()), 0, 0, 0);
		}
	}

}
