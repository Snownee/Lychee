package snownee.lychee.compat.recipeviewer.category;

import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.world.entity.EntityType;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.recipes.ItemExplodingRecipe;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public class ItemExplodingRecipeCategory extends ItemShapelessRecipeCategory<ItemExplodingRecipe> {
	public static final TntRenderState tnt = new TntRenderState();

	public ItemExplodingRecipeCategory() {
		super(RecipeTypes.ITEM_EXPLODING);
		tnt.entityType = EntityType.TNT;
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
								tnt.blockState = BlockPredicateExtensions.anyBlockState(recipeHolder.value().displayTNT());
								int fuse = 80 - (int) Objects.requireNonNull(Minecraft.getInstance().level).getGameTime() % 80;
								$.visible = fuse < 40;
								tnt.fuseRemainingInTicks = fuse + Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
							}));
				});
	}
}
