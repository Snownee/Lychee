package snownee.lychee.compat.recipeviewer.category;

import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.recipes.ItemExplodingRecipe;

public class ItemExplodingRecipeCategory extends ItemShapelessRecipeCategory<ItemExplodingRecipe> {
	public static final TntRenderState tnt = new TntRenderState();

	public ItemExplodingRecipeCategory() {
		super(RecipeTypes.ITEM_EXPLODING);
		tnt.entityType = EntityType.TNT;
		tnt.blockState = Blocks.TNT.defaultBlockState();
	}

	@Override
	public void setupDecorations(DecorationMapBuilder<ItemExplodingRecipe> mapBuilder) {
		super.setupDecorations(mapBuilder);
		mapBuilder.condition("icon", _ -> false);

		mapBuilder.put(
				"tnt", (builder, recipeHolder) -> {
					builder.addElement(RenderElement.create(
							GuiGameElement.of(tnt)
									.scale(12)
									.atLocal(0, 1.5, 1)
									.withSize(width, height),
							$ -> {
								int fuse = 80 - (int) Objects.requireNonNull(Minecraft.getInstance().level).getGameTime() % 80;
								$.visible = fuse < 40;
								tnt.fuseRemainingInTicks = fuse + Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
							}));
				});
	}
}
