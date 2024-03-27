package snownee.lychee.compat.jei.category;

import java.util.List;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.Items;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.item_exploding.ItemExplodingRecipe;

public class ItemExplodingRecipeCategory extends ItemShapelessRecipeCategory<ItemExplodingRecipe> {

	private PrimedTnt tnt;

	public ItemExplodingRecipeCategory(LycheeRecipeType<ItemShapelessContext, ItemExplodingRecipe> recipeType) {
		super(recipeType);
	}

	@Override
	public IDrawable createIcon(IGuiHelper guiHelper, List<ItemExplodingRecipe> recipes) {
		return guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.TNT.getDefaultInstance());
	}

	@Override
	public void draw(ItemExplodingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		drawInfoBadgeIfNeeded(recipe, graphics, mouseX, mouseY);

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
		matrixStack.translate(85, 34, 20);
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
		matrixStack.popPose();
	}

}
