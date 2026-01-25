package snownee.lychee.compat.recipeviewer.category;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.element.ShadowElement;
import snownee.lychee.recipes.LightningChannelingRecipe;

public class LightningChannelingRecipeCategory extends ItemShapelessRecipeCategory<LightningChannelingRecipe> {
	public static final int BLOCK_SIZE = 15;
	private final ShadowElement shadowElement = new ShadowElement(BLOCK_SIZE, 11, 4);

	public LightningChannelingRecipeCategory() {
		super(RecipeTypes.LIGHTNING_CHANNELING);
	}

	@Override
	public void setupDecorations(DecorationMapBuilder<LightningChannelingRecipe> mapBuilder) {
		super.setupDecorations(mapBuilder);
		mapBuilder.condition("icon", $ -> false);

		mapBuilder.put(
				"lightning_bolt", (builder, recipeHolder) -> {
					int width = builder.width();
					int height = builder.height();
					builder.addElement(RenderElement.create(graphics -> {
						RVs.renderLightning(graphics, (float) width / 2, 38);
					}).withScissors(true).withSize(width, height));
				});

		mapBuilder.put(
				"lightning_rod", (builder, recipeHolder) -> {
					Supplier<BlockState> blockStateSupplier = () -> {
						int time = RVs.LIGHTNING_BOLT.getEntity().tickCount % 80;
						BlockState blockState = Blocks.LIGHTNING_ROD.defaultBlockState();
						if (time <= 7) {
							blockState = blockState.setValue(LightningRodBlock.POWERED, true);
						}
						return blockState;
					};
					Function<BlockState, RenderElement> blockElement = blockState -> GuiGameElement.of(blockState)
							.scale(BLOCK_SIZE)
							.rotateBlock(20, 225, 0)
							.lighting(RVs.BLOCK_LIGHTING);
					builder.addElement(shadowElement
							.blockWithShadow(blockStateSupplier, blockElement)
							.at((float) builder.width() / 2 - 7.5F, 37));
				});
	}
}
