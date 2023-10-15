package snownee.lychee.compat.rei.category;

import java.util.List;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.Items;
import snownee.lychee.client.gui.ILightingSettings;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.rei.display.BaseREIDisplay;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.item_exploding.ItemExplodingRecipe;

public class ItemExplodingRecipeCategory extends ItemShapelessRecipeCategory<ItemShapelessContext, ItemExplodingRecipe, BaseREIDisplay<ItemExplodingRecipe>> {

	private PrimedTnt tnt;

	public ItemExplodingRecipeCategory(LycheeRecipeType<ItemShapelessContext, ItemExplodingRecipe> recipeType) {
		super(recipeType);
	}

	@Override
	public void drawExtra(List<Widget> widgets, BaseREIDisplay<ItemExplodingRecipe> display, Rectangle bounds) {
		Widget widget = Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
			Minecraft mc = Minecraft.getInstance();
			if (tnt == null) {
				tnt = EntityType.TNT.create(mc.level);
			}
			tnt.tickCount = mc.player.tickCount;
			int fuse = 80 - tnt.tickCount % 80;
			if (fuse >= 40) {
				return;
			}
			tnt.setFuse(fuse);

			PoseStack matrixStack = graphics.pose();
			matrixStack.pushPose();
			matrixStack.translate(bounds.x + 89, bounds.y + 38, 20);
			matrixStack.scale(15, 15, 15);

			float toRad = 0.01745329251F;
			Quaternionf quaternion = new Quaternionf().rotateXYZ(200 * toRad, -20 * toRad, 0);
			matrixStack.mulPose(quaternion);

			JEIREI.FUSED_TNT_LIGHTING.applyLighting();
			EntityRenderDispatcher entityrenderermanager = mc.getEntityRenderDispatcher();
			quaternion.conjugate();
			entityrenderermanager.overrideCameraOrientation(quaternion);
			entityrenderermanager.setRenderShadow(false);
			BufferSource irendertypebuffer$impl = mc.renderBuffers().bufferSource();

			entityrenderermanager.render(tnt, 0.0D, 0.0D, 0.0D, mc.getFrameTime(), 1, matrixStack, irendertypebuffer$impl, 15728880);

			irendertypebuffer$impl.endBatch();
			entityrenderermanager.setRenderShadow(true);
			ILightingSettings.DEFAULT_3D.applyLighting();
			matrixStack.popPose();
		});
		widgets.add(widget);
	}

	@Override
	public Renderer createIcon(List<ItemExplodingRecipe> recipes) {
		return EntryStacks.of(Items.TNT);
	}

}
