package snownee.lychee.compat.rv.category;

import java.util.function.Function;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import com.google.common.base.Suppliers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.rv.RVs;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;

public class ItemAndBlockCategory<R extends BlockKeyableRecipe> extends AbstractRvCategory<R> {
	public static final Vector2ic INFO_POSITION = new Vector2i(8, 32);

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

	protected <R extends BlockKeyableRecipe> BlockState getRenderingBlock(R recipe) {
		return CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(recipe.blockPredicate()),
				Blocks.AIR.defaultBlockState(),
				1000);
	}

	protected boolean shouldRenderInputBlockTooltip(R recipe) {
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

		if (needInfoIcon(recipe)) {
			builder.addElement(getInfoIcon(recipeHolder).offset(position));
		}

		builder.addElement(getInputBlockElement(recipe).offset(position));

		var methodElement = geMethodElement();
		if (methodElement != RenderElement.EMPTY) {
			builder.addElement(methodElement.offset(position));
		}

		if (AbstractRvCategory.needRemoveInputIcon(recipe)) {
			var removeActionPosition = VectorExtensions.offset(
					inputBlockPosition,
					position.x() + INPUT_BLOCK_SIZE.x() - 4,
					position.y() + INPUT_BLOCK_SIZE.y() - 8);
			AbstractRvCategory.getRemoveInputIcon().at(removeActionPosition);
		}
	}

	protected RenderElement geMethodElement() {
		return RenderElement.create(AllGuiTextures.DOWN_ARROW).at(methodPosition).withSize(METHOD_SIZE);
	}

	protected RenderElement getInputBlockElement(R recipe) {
		var questionMarkElement = Suppliers.<RenderElement>memoize(() ->
				RenderElement.create(AllGuiTextures.QUESTION_MARK).at(4, 2).offset(inputBlockPosition));

		var shadowElement = Suppliers.<RenderElement>memoize(() -> {
			var shadow = RenderElement.create(AllGuiTextures.SHADOW).offset(inputBlockPosition).at(11 - 26, 16 - 5);
			return RenderElement.create((graphics, x, y) -> {
				var matrixStack = graphics.pose();
				matrixStack.pushPose();
				matrixStack.scale(0.7F, 0.7F, 1F);
				shadow.render(graphics);
				matrixStack.popPose();
			}).at(shadow.x(), shadow.y());
		});

		Function<BlockState, RenderElement> blockElement = (BlockState state) -> GuiGameElement.of(state)
				.rotateBlock(12.5, 202.5, 0)
				.scale(15)
				.lighting(RVs.BLOCK_LIGHTING)
				.atLocal(0, 0.2, 0)
				.at(inputBlockPosition)
				.withSize(INPUT_BLOCK_SIZE);

		var result = new InteractiveRenderElement((element) -> {
			var state = getRenderingBlock(recipe);
			if (state.isAir()) {
				return questionMarkElement.get();
			}

			return (GuiGraphics graphics, int x, int y) -> {
				if (state.getLightEmission() < 5) {
					shadowElement.get().render(graphics);
				}
				blockElement.apply(state).render(graphics);
			};
		});

		if (shouldRenderInputBlockTooltip(recipe)) {
			result.onTooltip(() -> BlockPredicateExtensions.getTooltips(getRenderingBlock(recipe), recipe.blockPredicate()));
		}

		return result.onClick(button ->
						rvHandler().buttonToUsageOrRecipe(button)
								.ifPresent(usageOrRecipe -> rvHandler().openPage(getRenderingBlock(recipe), usageOrRecipe)))
				.at(inputBlockPosition)
				.withSize(INPUT_BLOCK_SIZE);
	}
}
