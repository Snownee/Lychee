package snownee.lychee.compat.rei.category;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import snownee.lychee.client.gui.CustomLightingSettings;
import snownee.lychee.client.gui.ILightingSettings;
import snownee.lychee.compat.rei.display.ItemShapelessDisplay;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.item_exploding.ItemExplodingRecipe;

public class ItemExplodingRecipeCategory extends ItemShapelessRecipeCategory<ItemShapelessContext, ItemExplodingRecipe, ItemShapelessDisplay<ItemExplodingRecipe>> {

	private static final ILightingSettings LIGHT = CustomLightingSettings.builder().firstLightRotation(-120, 20).secondLightRotation(200, 45).build();
	private PrimedTnt tnt;

	public ItemExplodingRecipeCategory(LycheeRecipeType<ItemShapelessContext, ItemExplodingRecipe> recipeType, Renderer icon) {
		super(recipeType, icon);
	}

	@Override
	public void drawExtra(List<Widget> widgets, ItemShapelessDisplay<ItemExplodingRecipe> display, Rectangle bounds) {
		Widget widget = Widgets.createDrawableWidget((GuiComponent helper, PoseStack matrixStack, int mouseX, int mouseY, float delta) -> {
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

			matrixStack.pushPose();
			matrixStack.translate(bounds.x + 89, bounds.y + 38, 20);
			matrixStack.scale(15, 15, 15);

			Quaternion quaternion = Quaternion.fromXYZDegrees(new Vector3f(200, -20, 0));
			matrixStack.mulPose(quaternion);

			LIGHT.applyLighting();
			EntityRenderDispatcher entityrenderermanager = mc.getEntityRenderDispatcher();
			quaternion.conj();
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

}
