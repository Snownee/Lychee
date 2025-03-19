package snownee.lychee.compat.jei.category;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.rv.RVs;
import snownee.lychee.compat.rv.RvCategory;
import snownee.lychee.recipes.ItemExplodingRecipe;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

@NotNullByDefault
public final class CategoryProviders {
	public static final Map<ResourceLocation, CategoryProvider<?>> ALL = Maps.newHashMap();

	static {
		register(RecipeTypes.BLOCK_CRUSHING, BlockCrushingRecipeCategory::new);

		register(
				RecipeTypes.BLOCK_EXPLODING,
				(category) -> new ItemAndBlockBaseCategory<>(category, false) {
					{
						inputBlockRect = new Rect2i(15, 30, 20, 20);
					}
				});

		register(
				RecipeTypes.BLOCK_INTERACTING,
				BlockInteractionRecipeCategory::new);

		register(
				RecipeTypes.DRIPSTONE_DRIPPING,
				DripstoneRecipeCategory::new);

		register(
				RecipeTypes.LIGHTNING_CHANNELING,
				ItemShapelessRecipeCategory::new);

		register(
				RecipeTypes.ITEM_EXPLODING,
				(category) -> new ItemShapelessRecipeCategory<>(category) {
					@Override
					public void draw(
							RecipeHolder<ItemExplodingRecipe> recipe,
							IRecipeSlotsView recipeSlotsView,
							GuiGraphics graphics,
							double mouseX,
							double mouseY) {
						RVs.renderTnt(graphics, 85, 34);
					}
				});

		register(RecipeTypes.ITEM_INSIDE, ItemInsideRecipeCategory::new);

		register(RecipeTypes.ITEM_BURNING, ItemBurningRecipeCategory::new);
	}

	@Nullable
	public static <R extends ILycheeRecipe<LycheeContext>> CategoryProvider<R> get(ResourceLocation id) {
		//noinspection unchecked
		return (CategoryProvider<R>) ALL.get(id);
	}

	public static <R extends ILycheeRecipe<LycheeContext>> CategoryProvider<R> register(
			LycheeRecipeType<R> type,
			CategoryProvider<R> provider) {
		ALL.put(type.categoryId, provider);
		return provider;
	}

	public static void clear() {
		ALL.clear();
	}

	@FunctionalInterface
	public interface CategoryProvider<R extends ILycheeRecipe<LycheeContext>> {
		AbstractLycheeCategory<R> get(RvCategory<R> category);
	}
}
