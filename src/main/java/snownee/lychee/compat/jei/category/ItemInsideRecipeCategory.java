package snownee.lychee.compat.jei.category;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.LycheeRecipeType;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.item_inside.ItemInsideRecipe;

public class ItemInsideRecipeCategory extends ThrowItemRecipeCategory<LycheeContext, ItemInsideRecipe> {

	private BlockState iconBlock;

	public ItemInsideRecipeCategory(LycheeRecipeType<LycheeContext, ItemInsideRecipe> recipeType, IGuiHelper guiHelper) {
		super(recipeType, guiHelper);
	}

	@Override
	public BlockState getIconBlock() {
		if (iconBlock == null) {
			ClientPacketListener con = Minecraft.getInstance().getConnection();
			if (con == null) {
				return Blocks.AIR.defaultBlockState();
			}
			Collection<ItemInsideRecipe> recipes = recipeType.recipes(con.getRecipeManager());
			Object2IntMap<BlockState> map = new Object2IntOpenHashMap<>();
			for (ItemInsideRecipe recipe : recipes) {
				for (Block block : BlockPredicateHelper.getMatchedBlocks(recipe.getBlock())) {
					map.computeInt(block.defaultBlockState(), (state, i) -> i == null ? 1 : ++i);
				}
			}
			if (map.isEmpty()) {
				return Blocks.AIR.defaultBlockState();
			}
			iconBlock = map.object2IntEntrySet().stream().max((a, b) -> a.getIntValue() - b.getIntValue()).get().getKey();
		}
		return iconBlock;
	}

	@Override
	public void setInputs(ItemInsideRecipe recipe, IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, List.of(List.of(recipe.getInput().getItems()), BlockPredicateHelper.getMatchedItemStacks(recipe.getBlock())));
	}

	@Override
	@Nullable
	public BlockPredicate getInputBlock(ItemInsideRecipe recipe) {
		return recipe.getBlock();
	}

}
