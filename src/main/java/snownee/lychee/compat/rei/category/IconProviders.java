package snownee.lychee.compat.rei.category;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.rei.ScreenElementWrapper;
import snownee.lychee.compat.rei.SideBlockIcon;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface IconProviders {
	Map<ResourceLocation, IconProvider> ALL = Maps.newHashMap();

	IconProvider BLOCK_CRUSHING = register(RecipeTypes.BLOCK_CRUSHING, (recipes) -> EntryStacks.of(Items.ANVIL));
	IconProvider BLOCK_EXPLODING = register(RecipeTypes.BLOCK_EXPLODING, (recipes) -> new ScreenElementWrapper(new SideBlockIcon(
			GuiGameElement.of(Items.TNT),
			Suppliers.memoize(() -> ItemAndBlockBaseCategory.getIconBlock((Collection<RecipeHolder<? extends BlockKeyableRecipe<?>>>) (Collection) recipes)))));
	IconProvider LIGHTNING_CHANNELING = register(RecipeTypes.LIGHTNING_CHANNELING, (recipes) -> EntryStacks.of(Items.LIGHTNING_ROD));
	IconProvider ITEM_EXPLODING = register(RecipeTypes.ITEM_EXPLODING, (recipes) -> EntryStacks.of(Items.TNT));

	static <R extends ILycheeRecipe<LycheeContext>> IconProvider register(
			LycheeRecipeType<LycheeContext, R> recipeType,
			IconProvider renderer) {
		ALL.put(recipeType.categoryId, renderer);
		return renderer;
	}

	static <R extends ILycheeRecipe<LycheeContext>> IconProvider get(LycheeRecipeType<LycheeContext, R> recipeType) {
		return ALL.get(recipeType.categoryId);
	}

	@FunctionalInterface
	interface IconProvider {
		Renderer get(Collection<RecipeHolder<? extends ILycheeRecipe<LycheeContext>>> recipes);
	}
}
