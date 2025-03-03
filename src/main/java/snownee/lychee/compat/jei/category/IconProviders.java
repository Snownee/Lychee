package snownee.lychee.compat.jei.category;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.jei.elements.ScreenElementWidget;
import snownee.lychee.compat.rv.SideBlockIcon;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface IconProviders {
	Map<ResourceLocation, IconProvider> ALL = Maps.newHashMap();

	IconProvider BLOCK_CRUSHING = register(
			RecipeTypes.BLOCK_CRUSHING,
			(guiHelper, recipes) -> guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.ANVIL.getDefaultInstance()));
	IconProvider BLOCK_EXPLODING = register(
			RecipeTypes.BLOCK_EXPLODING,
			(guiHelper, recipes) -> new ScreenElementWidget(new SideBlockIcon(
					GuiGameElement.of(Items.TNT),
					Suppliers.memoize(() -> ItemAndBlockBaseCategory.getIconBlock((Collection) recipes)))));
	IconProvider BLOCK_INTERACTING = register(RecipeTypes.BLOCK_INTERACTING, (guiHelper, recipes) -> {
		var mainIcon = recipes.stream()
				.map(it -> it.value().getType())
				.anyMatch(it -> it == RecipeTypes.BLOCK_INTERACTING) ? AllGuiTextures.RIGHT_CLICK : AllGuiTextures.LEFT_CLICK;
		return new ScreenElementWidget(new SideBlockIcon(
				mainIcon,
				Suppliers.memoize(() -> ItemAndBlockBaseCategory.getIconBlock((Collection) recipes))));
	});

	IconProvider DRIPSTONE = register(
			RecipeTypes.DRIPSTONE_DRIPPING,
			(guiHelper, recipes) -> guiHelper.createDrawableIngredient(
					VanillaTypes.ITEM_STACK,
					Items.POINTED_DRIPSTONE.getDefaultInstance()));

	IconProvider LIGHTNING_CHANNELING = register(
			RecipeTypes.LIGHTNING_CHANNELING,
			(guiHelper, recipes) -> guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.LIGHTNING_ROD.getDefaultInstance()));
	IconProvider ITEM_EXPLODING = register(
			RecipeTypes.ITEM_EXPLODING,
			(guiHelper, recipes) -> guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.TNT.getDefaultInstance()));

	IconProvider ITEM_BURNING = register(
			RecipeTypes.ITEM_BURNING,
			(guiHelper, recipes) -> new ScreenElementWidget(new SideBlockIcon(
					AllGuiTextures.JEI_DOWN_ARROW,
					Suppliers.memoize(Blocks.FIRE::defaultBlockState))));

	IconProvider ITEM_INSIDE = register(RecipeTypes.ITEM_INSIDE, (guiHelper, recipes) -> new ScreenElementWidget(new SideBlockIcon(
			AllGuiTextures.JEI_DOWN_ARROW,
			Suppliers.memoize(() -> ItemAndBlockBaseCategory.getIconBlock((Collection) recipes)))));

	static <R extends ILycheeRecipe<LycheeContext>> IconProvider register(
			LycheeRecipeType<R> recipeType,
			IconProvider renderer) {
		ALL.put(recipeType.categoryId, renderer);
		return renderer;
	}

	static <R extends ILycheeRecipe<LycheeContext>> IconProvider get(LycheeRecipeType<R> recipeType) {
		return ALL.get(recipeType.categoryId);
	}

	@FunctionalInterface
	interface IconProvider {
		IDrawable get(IGuiHelper guiHelper, List<RecipeHolder<? extends ILycheeRecipe<LycheeContext>>> recipes);
	}
}
