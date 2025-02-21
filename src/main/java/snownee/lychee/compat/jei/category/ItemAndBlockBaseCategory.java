package snownee.lychee.compat.jei.category;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

@NotNullByDefault
public class ItemAndBlockBaseCategory<T extends ILycheeRecipe<LycheeContext>> extends AbstractLycheeCategory<T> {

	private final LycheeRecipeType<T> recipeType;
	public Rect2i inputBlockRect = new Rect2i(30, 35, 20, 20);
	public Rect2i methodRect = new Rect2i(30, 12, 20, 20);

	public ItemAndBlockBaseCategory(RecipeType<RecipeHolder<T>> recipeType, IDrawable icon, LycheeRecipeType<T> vanillaRecipeType) {
		super(recipeType, icon);
		this.recipeType = vanillaRecipeType;
		infoRect.setPosition(8, 32);
	}

	public static BlockState getIconBlock(Collection<RecipeHolder<? extends BlockKeyableRecipe<?>>> recipes) {
		var con = Minecraft.getInstance().getConnection();
		if (con == null) {
			return Blocks.AIR.defaultBlockState();
		}
		return JEIREI.getMostUsedBlock(recipes).getFirst();
	}

	public BlockPredicate getInputBlock(T recipe) {
		return ((BlockKeyableRecipe<?>) recipe).blockPredicate();
	}

	public BlockState getRenderingBlock(T recipe) {
		return CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(getInputBlock(recipe)),
				Blocks.AIR.defaultBlockState(),
				1000);
	}

	@Nullable
	public Component getMethodDescription(T recipe) {
		return null;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<T> recipeHolder, IFocusGroup focuses) {
		var recipe = recipeHolder.value();
		var y = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9 ? 26 : 28;
		renderIngredientGroup(builder, recipe, y);
		actionGroup(builder, recipe, getWidth() - 29, y);
		LycheeCategory.addBlockIngredients(builder, recipe);
	}

	public void drawExtra(RecipeHolder<T> recipeHolder, GuiGraphics graphics, double mouseX, double mouseY, int centerX) {
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, methodRect.getX(), methodRect.getY());
	}

	@Override
	public void draw(
			RecipeHolder<T> recipeHolder,
			IRecipeSlotsView recipeSlotsView,
			GuiGraphics graphics,
			double mouseX,
			double mouseY
	) {
		var recipe = recipeHolder.value();
		drawInfoBadgeIfNeeded(graphics, recipe, mouseX, mouseY);
		var centerX = getWidth() / 2;
		drawExtra(recipeHolder, graphics, mouseX, mouseY, centerX);

		var state = getRenderingBlock(recipe);
		if (state.isAir()) {
			AllGuiTextures.JEI_QUESTION_MARK.render(graphics, inputBlockRect.getX() + 4, inputBlockRect.getY() + 2);
			return;
		}
		var matrixStack = graphics.pose();
		if (state.getLightEmission() < 5) {
			matrixStack.pushPose();
			matrixStack.translate(inputBlockRect.getX() + 11, inputBlockRect.getY() + 16, 0);
			matrixStack.scale(.7F, .7F, .7F);
			AllGuiTextures.JEI_SHADOW.render(graphics, -26, -5);
			matrixStack.popPose();
		}

		GuiGameElement.of(state)
				.rotateBlock(12.5, 202.5, 0)
				.scale(15)
				.lighting(JEIREI.BLOCK_LIGHTING)
				.atLocal(0, 0.2, 0)
				.at(inputBlockRect.getX(), inputBlockRect.getY())
				.render(graphics);
	}

	@Override
	public void getTooltip(
			ITooltipBuilder tooltip,
			RecipeHolder<T> recipeHolder,
			IRecipeSlotsView recipeSlotsView,
			double mouseX,
			double mouseY) {
		var recipe = recipeHolder.value();
		if (needRenderInputBlock(recipe) && inputBlockRect.contains((int) mouseX, (int) mouseY)) {
			tooltip.addAll(BlockPredicateExtensions.getTooltips(getRenderingBlock(recipe), getInputBlock(recipe)));
		} else if (methodRect.contains((int) mouseX, (int) mouseY)) {
			Component description = getMethodDescription(recipe);
			if (description != null) {
				tooltip.add(description);
			}
		}
	}

	protected boolean needRenderInputBlock(T recipe) {
		return !BlockPredicateExtensions.isAny(getInputBlock(recipe));
	}

	protected void renderIngredientGroup(IRecipeLayoutBuilder builder, T recipe, int y) {
		ingredientGroup(builder, recipe, 12, 21);
	}

	@Override
	public LycheeRecipeType<? extends T> recipeType() {
		return recipeType;
	}
}
