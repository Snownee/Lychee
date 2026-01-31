package snownee.lychee.client.gui;

import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.state.BlockDisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.util.VecHelper;

public class GuiGameElement {
	public static final RenderStateDataKey<Lighting.Entry> CUSTOM_LIGHTING = RenderStateDataKey.create();

	public static GuiRenderBuilder of(ItemStackTemplate stack) {
		return new GuiItemRenderBuilder(stack);
	}

	public static GuiRenderBuilder of(ItemLike itemProvider) {
		return new GuiItemRenderBuilder(itemProvider);
	}

	public static GuiRenderBuilder of(BlockState blockState) {
		Block block = blockState.getBlock();
		if (block == Blocks.AIR) {
			return new GuiBlockStateRenderBuilder(blockState);
		}
		if (blockState.getRenderShape() != RenderShape.MODEL && blockState.getFluidState().isEmpty()) {
			return new GuiBlockStateRenderBuilder(blockState, GuiGameElement.of(block));
		}
		if (block instanceof StairBlock) {
			blockState = blockState.setValue(StairBlock.FACING, blockState.getValue(StairBlock.FACING).getOpposite());
		}
		return new GuiBlockStateRenderBuilder(blockState);
	}

	public static GuiRenderBuilder of(Fluid fluid) {
		return new GuiBlockStateRenderBuilder(fluid.defaultFluidState().createLegacyBlock().setValue(LiquidBlock.LEVEL, 0));
	}

	public static GuiRenderBuilder of(EntityRenderState renderState) {
		return new GuiEntityRenderBuilder(renderState);
	}

	public static abstract class GuiRenderBuilder extends RenderElement {
		protected double xLocal, yLocal, zLocal = 1;
		protected double xRot, yRot, zRot;
		protected float scale = 1;
		protected int color = 0xFFFFFF;
		protected Vec3 rotationOffset = Vec3.ZERO;
		protected ILightingSettings customLighting = null;

		public GuiRenderBuilder atLocal(double x, double y, double z) {
			this.xLocal = x;
			this.yLocal = y;
			this.zLocal = z;
			return this;
		}

		public GuiRenderBuilder rotate(double xRot, double yRot, double zRot) {
			this.xRot = xRot;
			this.yRot = yRot;
			this.zRot = zRot;
			return this;
		}

		public GuiRenderBuilder rotateBlock(double xRot, double yRot, double zRot) {
			return this.rotate(xRot, yRot, zRot).withRotationOffset(VecHelper.getCenterOf(BlockPos.ZERO));
		}

		public GuiRenderBuilder scale(float scale) {
			this.scale = scale;
			return this;
		}

		public GuiRenderBuilder color(int color) {
			this.color = color;
			return this;
		}

		public GuiRenderBuilder withRotationOffset(Vec3 offset) {
			this.rotationOffset = offset;
			return this;
		}

		public GuiRenderBuilder lighting(ILightingSettings lighting) {
			customLighting = lighting;
			return this;
		}

		protected void prepareMatrix(PoseStack matrixStack) {
//			matrixStack.pushPose();
//			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//			RenderSystem.enableDepthTest();
//			RenderSystem.enableBlend();
//			RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
//			prepareLighting(matrixStack);
		}

		protected void transformMatrix(GuiGraphics graphics) {
//			Matrix3x2fStack pose = graphics.pose();
//			float scale = this.scale;
//			pose.translate(x(), y() + scale);
//			pose.scale(scale);
//			pose.translate(xLocal, yLocal);
//			pose.translate(rotationOffset.x, rotationOffset.y);
//
////			matrixStack.mulPose(Axis.YP.rotationDegrees((float) Util.getMillis() / 20));
//
//			pose.mulPose(Axis.ZP.rotationDegrees((float) zRot));
//			pose.mulPose(Axis.XP.rotationDegrees((float) xRot));
//			pose.mulPose(Axis.YP.rotationDegrees((float) yRot));
//			pose.translate(-rotationOffset.x, -rotationOffset.y, -rotationOffset.z);
		}

		protected void cleanUpMatrix(PoseStack matrixStack) {
			matrixStack.popPose();
			cleanUpLighting(matrixStack);
		}

		protected void prepareLighting(PoseStack matrixStack) {
//			if (customLighting != null) {
//				customLighting.applyLighting();
//			} else {
//				Lighting.setupFor3DItems();
//			}
		}

		protected void cleanUpLighting(PoseStack matrixStack) {
//			if (customLighting != null) {
//				Lighting.setupFor3DItems();
//			}
		}
	}

	private static class GuiBlockModelRenderBuilder extends GuiRenderBuilder {

		protected final BlockDisplayEntityRenderState renderState;
		protected BlockStateModel blockModel;
		protected BlockState blockState;

		public GuiBlockModelRenderBuilder(BlockStateModel bakedModel, @Nullable BlockState blockState) {
			this.blockState = blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
			this.blockModel = bakedModel;
			withRotationOffset(VecHelper.getCenterOf(BlockPos.ZERO));
			renderState = new BlockDisplayEntityRenderState();
			renderState.entityType = EntityType.BLOCK_DISPLAY;
			renderState.renderState = createFreshRenderState();
			renderState.setData(CUSTOM_LIGHTING, Lighting.Entry.ITEMS_FLAT);
			if (blockState != null) {
				renderState.blockRenderState = new Display.BlockDisplay.BlockRenderState(blockState);
			}
		}

		private static Display.RenderState createFreshRenderState() {
			Vector3fc translation = new Vector3f(-0.5f, -0.5f, -0.5f);
			Transformation transformation = new Transformation(translation, null, null, null);
			return new Display.RenderState(
					Display.GenericInterpolator.constant(transformation),
					Display.BillboardConstraints.FIXED,
					-1,
					Display.FloatInterpolator.constant(0),
					Display.FloatInterpolator.constant(1),
					-1
			);
		}

		@Override
		public void render(GuiGraphics graphics) {
			float toRad = 0.01745329251F;
			Vector3f translation = new Vector3f((float) xLocal, (float) yLocal, (float) zLocal);
			Quaternionf rotation = new Quaternionf().rotateXYZ(200 * toRad, -20 * toRad, 0);
			float halfWidth = width() / 2f;
			float halfHeight = height() / 2f;
			float x0 = -halfWidth;
			float y0 = -halfHeight;
			float x1 = width() + halfWidth;
			float y1 = height() + halfHeight;
			var pos0 = graphics.pose().transformPosition(new Vector2f(x0, y0));
			var pos1 = graphics.pose().transformPosition(new Vector2f(x1, y1));
			graphics.submitEntityRenderState(
					renderState,
					scale,
					translation,
					rotation,
					null,
					(int) pos0.x,
					(int) pos0.y,
					(int) pos1.x,
					(int) pos1.y);
		}

		protected void renderModel(BlockRenderDispatcher blockRenderer, MultiBufferSource.BufferSource buffer, PoseStack ms) {
//			Minecraft mc = Minecraft.getInstance();
//			int color = mc.getBlockColors().getColor(
//					blockState,
//					mc.level,
//					mc.getCameraEntity() != null ? mc.getCameraEntity().blockPosition() : null,
//					0
//			);
//			Color rgb = new Color(color == -1 ? this.color : color);
//			blockRenderer.getModelRenderer().renderModel(
//					ms.last(),
//					buffer.getBuffer(blockState.getBlock() == Blocks.AIR ?
//							Sheets.translucentCullBlockSheet() :
//							ItemBlockRenderTypes.getRenderType(blockState, true)),
//					blockState,
//					blockModel,
//					rgb.getRedAsFloat(),
//					rgb.getGreenAsFloat(),
//					rgb.getBlueAsFloat(),
//					LightTexture.FULL_BRIGHT,
//					OverlayTexture.NO_OVERLAY
//			);
//			buffer.endBatch();
		}
	}

	public static class GuiBlockStateRenderBuilder extends GuiBlockModelRenderBuilder {
		private final @Nullable GuiRenderBuilder override;

		public GuiBlockStateRenderBuilder(BlockState blockState) {
			this(blockState, null);
		}

		public GuiBlockStateRenderBuilder(BlockState blockState, @Nullable GuiRenderBuilder override) {
			super(Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState), blockState);
			this.override = override;
		}

		@Override
		public void render(GuiGraphics graphics) {
			if (override != null) {
				override.atLocal(xLocal, yLocal, zLocal).at(position).offset(-3, -3).render(graphics);
			}
			super.render(graphics);
		}

		@Override
		protected void renderModel(BlockRenderDispatcher blockRenderer, MultiBufferSource.BufferSource buffer, PoseStack ms) {
//			if (blockState.getBlock() instanceof BaseFireBlock) {
//				Lighting.setupForFlatItems();
//				blockRenderer.renderSingleBlock(
//						blockState,
//						ms,
//						buffer,
//						LightTexture.FULL_BRIGHT,
//						OverlayTexture.NO_OVERLAY
//				);
//				buffer.endBatch();
//				Lighting.setupFor3DItems();
//				return;
//			}
//
//			super.renderModel(blockRenderer, buffer, ms);
//
//			if (blockState.getFluidState().isEmpty()) {
//				return;
//			}
//
//			float min = 0.001F, max = 0.999F;
//			// LiquidBlockRenderer.MAX_FLUID_HEIGHT
//			FluidRenderer.renderFluidBox(
//					blockState.getFluidState(),
//					min,
//					min,
//					min,
//					max,
//					max * 0.8888889F,
//					max,
//					buffer,
//					ms,
//					LightTexture.FULL_BRIGHT,
//					false
//			);
//			buffer.endBatch();
		}
	}

	public static class GuiItemRenderBuilder extends GuiRenderBuilder {

		private final ItemStack stack;

		public GuiItemRenderBuilder(ItemStackTemplate stack) {
			this.stack = stack.create();
			scale = 10;
		}

		public GuiItemRenderBuilder(ItemLike provider) {
			Item item = provider.asItem();
			Preconditions.checkArgument(item != Items.AIR, "Item provider provides air item");
			this(new ItemStackTemplate(item));
		}

		@Override
		public void render(GuiGraphics graphics) {
//			PoseStack matrixStack = graphics.pose();
//			prepareMatrix(matrixStack);
//			transformMatrix(matrixStack);
//			renderItemIntoGUI(matrixStack, stack, customLighting == null);
//			cleanUpMatrix(matrixStack);
			graphics.pose().pushMatrix();
			graphics.renderItem(stack, (int) x(), (int) y());
			graphics.pose().popMatrix();
		}

		@Override
		protected void transformMatrix(GuiGraphics graphics) {
			Matrix3x2fStack pose = graphics.pose();
			pose.translate(x(), y());
//			pose.translate(xLocal * scale, yLocal * scale);
//			UIRenderHelper.flipForGuiRender(graphics);
		}

		public static void renderItemIntoGUI(PoseStack matrixStack, ItemStack stack, boolean useDefaultLighting) {
//			ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
//			BakedModel bakedModel = renderer.getModel(stack, null, null, 0);
//
//			Minecraft.getInstance().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
//			RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
//			RenderSystem.enableBlend();
//			RenderSystem.enableCull();
//			RenderSystem.blendFunc(
//					GlStateManager.SourceFactor.SRC_ALPHA,
//					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
//			);
//			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//			matrixStack.pushPose();
//			matrixStack.translate(0, 0, 100.0F);
//			matrixStack.translate(8.0F, -8.0F, 0.0F);
//			matrixStack.scale(16.0F, 16.0F, 16.0F);
//			MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
//			boolean flatLighting = !bakedModel.usesBlockLight();
//			if (useDefaultLighting && flatLighting) {
//				Lighting.setupForFlatItems();
//			}
//
//			renderer.render(
//					stack,
//					ItemDisplayContext.GUI,
//					false,
//					matrixStack,
//					buffer,
//					LightTexture.FULL_BRIGHT,
//					OverlayTexture.NO_OVERLAY,
//					bakedModel
//			);
//			RenderSystem.disableDepthTest();
//			buffer.endBatch();
//
//			RenderSystem.enableDepthTest();
//			if (useDefaultLighting && flatLighting) {
//				Lighting.setupFor3DItems();
//			}
//
//			matrixStack.popPose();
		}

		@Override
		public GuiRenderBuilder lighting(ILightingSettings lighting) {
			return this;
		}

	}

	public static class GuiEntityRenderBuilder extends GuiRenderBuilder {
		private final EntityRenderState state;

		public GuiEntityRenderBuilder(EntityRenderState state) {
			this.state = state;
		}

		@Override
		public void render(GuiGraphics graphics) {
			float toRad = 0.01745329251F;
			Vector3f translation = new Vector3f((float) xLocal, (float) yLocal, (float) zLocal);
			Quaternionf rotation = new Quaternionf().rotateXYZ(200 * toRad, -20 * toRad, 0);
			float x0 = 0;
			float y0 = 0;
			float x1 = width();
			float y1 = height();
			var pos0 = graphics.pose().transformPosition(new Vector2f(x0, y0));
			var pos1 = graphics.pose().transformPosition(new Vector2f(x1, y1));
			graphics.submitEntityRenderState(
					state,
					scale,
					translation,
					rotation,
					null,
					(int) pos0.x,
					(int) pos0.y,
					(int) pos1.x,
					(int) pos1.y);
		}
	}

}
