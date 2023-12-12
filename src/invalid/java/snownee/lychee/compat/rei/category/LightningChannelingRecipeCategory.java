package snownee.lychee.compat.rei.category;

import java.util.List;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.Items;
import snownee.lychee.compat.rei.display.BaseREIDisplay;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.lightning_channeling.LightningChannelingRecipe;

public class LightningChannelingRecipeCategory extends ItemShapelessRecipeCategory<ItemShapelessContext, LightningChannelingRecipe, BaseREIDisplay<LightningChannelingRecipe>> {

	public LightningChannelingRecipeCategory(LycheeRecipeType<ItemShapelessContext, LightningChannelingRecipe> recipeType) {
		super(recipeType);
	}

	@Override
	public Renderer createIcon(List<LightningChannelingRecipe> recipes) {
		return EntryStacks.of(Items.LIGHTNING_ROD);
	}

}
