package snownee.lychee.compat.rv.category;

import java.util.function.Function;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import com.google.common.base.Suppliers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.rv.RVs;
import snownee.lychee.compat.rv.element.InfoElementHelper;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;

public class ItemAndBlockCategory<R extends BlockKeyableRecipe> extends AbstractRvCategory<R> {
	public static final Rect2i INFO_RECT = InfoElementHelper.getInfoRect(8, 32);

	protected static final Vector2ic INPUT_BLOCK_POSITION = new Vector2i(30, 35);
	protected static final Vector2ic METHOD_POSITION = new Vector2i(30, 12);
	protected static final Vector2ic INGREDIENT_POSITION = new Vector2i(12, 21);

	protected static final Vector2ic INPUT_BLOCK_SIZE = new Vector2i(20, 20);
	protected static final Vector2ic METHOD_SIZE = new Vector2i(20, 20);

	protected final Vector2ic inputBlockPosition;
	protected final Vector2ic methodPosition;
	protected final Vector2ic ingredientPosition;

	protected ItemAndBlockCategory(
			RvCategoryType<R> type,
			ResourceLocation id,
			RVCategoryHandler rvHandler,
			Vector2ic inputBlockPosition,
			Vector2ic methodPosition,
			Vector2ic ingredientPosition
	) {
		super(type, id, rvHandler);
		this.inputBlockPosition = inputBlockPosition;
		this.methodPosition = methodPosition;
		this.ingredientPosition = ingredientPosition;
	}

	public ItemAndBlockCategory(RvCategoryType<R> type, ResourceLocation id, RVCategoryHandler rvHandler) {
		this(type, id, rvHandler, INPUT_BLOCK_POSITION, METHOD_POSITION, INGREDIENT_POSITION);
	}

	protected static <R extends BlockKeyableRecipe> BlockState getRenderingBlock(R recipe) {
		return CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(recipe.blockPredicate()),
				Blocks.AIR.defaultBlockState(),
				1000);
	}

	protected boolean shouldRenderInputBlock(R recipe) {
		return !BlockPredicateExtensions.isAny(recipe.blockPredicate());
	}

	@Override
	public void configureLayout(RvCategoryLayoutBuilder builder, RecipeHolder<R> recipeHolder, Vector2i position) {
		var recipe = recipeHolder.value();
		var needSecondLine = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9;
		var y = needSecondLine ? 26 : 28;
		builder.ingredientGroup(recipe, ingredientPosition);
		builder.actionGroup(recipe, new Vector2i(width() - 29, y));
	}

	@Override
	public void configureDecorations(RvCategoryWidgetBuilder builder, RecipeHolder<R> recipeHolder, Vector2i position) {
		var recipe = recipeHolder.value();

		if (shouldRenderInputBlock(recipe)) {
			builder.addElement(getInputBlockElement(recipe));
		}

		var methodElement = geMethodElement();
		if (methodElement != RenderElement.EMPTY) {
			builder.addElement(methodElement);
		}

		if (AbstractRvCategory.needRemoveInputIcon(recipe)) {
			var removeActionPosition = new Vector2i();
			AbstractRvCategory.addRemoveInputIcon(builder, removeActionPosition);
		}
	}

	protected RenderElement geMethodElement() {
		return RenderElement.create(AllGuiTextures.DOWN_ARROW).at(methodPosition).withSize(METHOD_SIZE);
	}

	protected RenderElement getInputBlockElement(R recipe) {
		var questionMarkElement = Suppliers.<RenderElement>memoize(() ->
				RenderElement.create(AllGuiTextures.QUESTION_MARK)
						.at(inputBlockPosition.x() + 4, inputBlockPosition.y() + 2));
		var shadowElement = Suppliers.<RenderElement>memoize(() ->
				RenderElement.create(AllGuiTextures.SHADOW)
						.at(inputBlockPosition.x() + 11 - 26, inputBlockPosition.y() + 16 - 5));
		Function<BlockState, RenderElement> blockElement = (BlockState state) -> GuiGameElement.of(state)
				.rotateBlock(12.5, 202.5, 0)
				.scale(15)
				.lighting(RVs.BLOCK_LIGHTING)
				.atLocal(0, 0.2, 0)
				.at(inputBlockPosition)
				.withSize(INPUT_BLOCK_SIZE);
		return new InteractiveRenderElement((element) -> {
			var state = getRenderingBlock(recipe);
			if (state.isAir()) {
				return questionMarkElement.get();
			}

			return (GuiGraphics graphics, int x, int y) -> {
				if (state.getLightEmission() < 5) {
					var matrixStack = graphics.pose();
					matrixStack.pushPose();
					matrixStack.scale(.7F, .7F, .7F);
					shadowElement.get().render(graphics);
					matrixStack.popPose();
				}
				blockElement.apply(state).render(graphics);
			};
		}).onTooltip(() -> BlockPredicateExtensions.getTooltips(getRenderingBlock(recipe), recipe.blockPredicate()))
				.onClick(button ->
						rvHandler().buttonToUsageOrRecipe(button)
								.ifPresent(usageOrRecipe -> rvHandler().openPage(getRenderingBlock(recipe), usageOrRecipe)))
				.at(inputBlockPosition)
				.withSize(INPUT_BLOCK_SIZE);
	}
}
