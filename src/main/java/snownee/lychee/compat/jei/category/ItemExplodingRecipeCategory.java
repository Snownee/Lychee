package snownee.lychee.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.Items;
import snownee.lychee.client.gui.CustomLightingSettings;
import snownee.lychee.client.gui.ILightingSettings;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.item_exploding.ItemExplodingRecipe;

public class ItemExplodingRecipeCategory extends ItemShapelessRecipeCategory<ItemExplodingRecipe> {

	private static final ILightingSettings LIGHT = CustomLightingSettings.builder().firstLightRotation(-120, 20).secondLightRotation(200, 45).build();
	private PrimedTnt tnt;

	public ItemExplodingRecipeCategory(LycheeRecipeType<ItemShapelessContext, ItemExplodingRecipe> recipeType) {
		super(recipeType);
	}

	@Override
	public IDrawable createIcon(IGuiHelper guiHelper) {
		return guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.TNT.getDefaultInstance());
	}

	@Override
	public void draw(ItemExplodingRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
		drawInfoBadge(recipe, matrixStack, mouseX, mouseY);

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
		matrixStack.translate(85, 34, 20);
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
		matrixStack.popPose();
	}

}
