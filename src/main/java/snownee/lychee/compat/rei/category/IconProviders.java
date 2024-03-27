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
import net.minecraft.world.level.block.Blocks;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.rei.ScreenElementWrapper;
import snownee.lychee.compat.rei.SideBlockIcon;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface IconProviders {
	Map<ResourceLocation, IconProvider> ALL = Maps.newHashMap();

	IconProvider BLOCK_CRUSHING = register(RecipeTypes.BLOCK_CRUSHING, (recipes) -> EntryStacks.of(Items.ANVIL));
	IconProvider BLOCK_EXPLODING = register(RecipeTypes.BLOCK_EXPLODING, (recipes) -> new ScreenElementWrapper(new SideBlockIcon(
			GuiGameElement.of(Items.TNT),
			Suppliers.memoize(() -> ItemAndBlockBaseCategory.getIconBlock((Collection) recipes)))));
	IconProvider BLOCK_INTERACTING = register(RecipeTypes.BLOCK_INTERACTING, (recipes) -> {
		var mainIcon = recipes.stream()
				.map(it -> it.value().getType())
				.anyMatch(it -> it == RecipeTypes.BLOCK_INTERACTING) ? AllGuiTextures.RIGHT_CLICK : AllGuiTextures.LEFT_CLICK;
		return new ScreenElementWrapper(new SideBlockIcon(
				mainIcon,
				Suppliers.memoize(() -> ItemAndBlockBaseCategory.getIconBlock((Collection) recipes))));
	});

	IconProvider DRIPSTONE = register(RecipeTypes.DRIPSTONE_DRIPPING, (recipes) -> EntryStacks.of(Items.POINTED_DRIPSTONE));

	IconProvider LIGHTNING_CHANNELING = register(RecipeTypes.LIGHTNING_CHANNELING, (recipes) -> EntryStacks.of(Items.LIGHTNING_ROD));
	IconProvider ITEM_EXPLODING = register(RecipeTypes.ITEM_EXPLODING, (recipes) -> EntryStacks.of(Items.TNT));

	IconProvider ITEM_BURNING = register(
			RecipeTypes.ITEM_BURNING,
			(recipes) -> new ScreenElementWrapper(new SideBlockIcon(
					AllGuiTextures.JEI_DOWN_ARROW,
					Suppliers.memoize(Blocks.FIRE::defaultBlockState))));

	IconProvider ITEM_INSIDE = register(RecipeTypes.ITEM_INSIDE, (recipes) -> new ScreenElementWrapper(new SideBlockIcon(
			AllGuiTextures.JEI_DOWN_ARROW,
			Suppliers.memoize(() -> ItemAndBlockBaseCategory.getIconBlock((Collection) recipes)))));

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
