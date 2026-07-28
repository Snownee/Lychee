package snownee.lychee.client.gui;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Transformation;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.state.BlockDisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
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
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.util.VecHelper;

public class GuiGameElement {
	public static final RenderStateDataKey<Lighting.Entry> CUSTOM_LIGHTING = RenderStateDataKey.create();
	public static final RenderStateDataKey<Unit> DRAW_FLUID_STATE = RenderStateDataKey.create();
	public static final ThreadLocal<@Nullable Unit> DRAW_FLUID_STATE_FLAG = new ThreadLocal<>();

	public static GuiRenderBuilder of(ItemStackTemplate stack) {
		return new GuiItemRenderBuilder(stack);
	}

	public static GuiRenderBuilder of(ItemLike itemProvider) {
		return new GuiItemRenderBuilder(itemProvider);
	}

	public static GuiRenderBuilder of(BlockState blockState) {
		Block block = blockState.getBlock();
		if (block == Blocks.AIR) {
			return new GuiBlockRenderBuilder(blockState);
		}
		if (blockState.getRenderShape() != RenderShape.MODEL && blockState.getFluidState().isEmpty()) {
			return new GuiBlockRenderBuilder(blockState, GuiGameElement.of(block));
		}
//		if (block instanceof StairBlock) {
//			blockState = blockState.setValue(StairBlock.FACING, blockState.getValue(StairBlock.FACING).getOpposite());
//		}
		return new GuiBlockRenderBuilder(blockState);
	}

	public static GuiRenderBuilder of(Fluid fluid) {
		return new GuiBlockRenderBuilder(fluid.defaultFluidState().createLegacyBlock().setValue(LiquidBlock.LEVEL, 0));
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
	}

	private static class GuiBlockRenderBuilder extends GuiRenderBuilder {

		protected BlockDisplayEntityRenderState renderState;
		protected BlockState blockState;
		private final @Nullable GuiRenderBuilder override;

		public GuiBlockRenderBuilder(@Nullable BlockState blockState) {
			this(blockState, null);
		}

		public GuiBlockRenderBuilder(@Nullable BlockState blockState, @Nullable GuiRenderBuilder override) {
			this.blockState = blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
			this.override = override;
			if (blockState != null) {
				withRotationOffset(VecHelper.getCenterOf(BlockPos.ZERO));
				renderState = new BlockDisplayEntityRenderState();
				renderState.entityType = EntityType.BLOCK_DISPLAY;
				renderState.renderState = createFreshRenderState();
				((FabricRenderState) renderState).setData(CUSTOM_LIGHTING, Lighting.Entry.ITEMS_FLAT);

				if (!blockState.getFluidState().isEmpty()) {
					((FabricRenderState) renderState).setData(DRAW_FLUID_STATE, Unit.INSTANCE);
				}

				this.blockState = this.blockState.rotate(Rotation.CLOCKWISE_180);
				Minecraft.getInstance().blockModelResolver.update(
						renderState.blockModel,
						this.blockState,
						BlockDisplayContext.create());
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
					-1);
		}

		@Override
		public void render(GuiGraphicsExtractor graphics) {
			if (override != null) {
				override.atLocal(xLocal, yLocal, zLocal).at(position).offset(-3, -3).render(graphics);
			}
			float toRad = 0.01745329251F;
			Quaternionf rotation = new Quaternionf().rotateXYZ(200 * toRad, -20 * toRad, 0);
			float x0 = x() - width();
			float y0 = y() - height();
			float x1 = x() + width() + width();
			float y1 = y() + height() + height();
			var pos0 = graphics.pose().transformPosition(new Vector2f(x0, y0));
			var pos1 = graphics.pose().transformPosition(new Vector2f(x1, y1));
			graphics.entity(
					renderState,
					scale,
					new Vector3f((float) xLocal, (float) yLocal, (float) zLocal),
					rotation,
					null,
					(int) pos0.x,
					(int) pos0.y,
					(int) pos1.x,
					(int) pos1.y);
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
		public void render(GuiGraphicsExtractor graphics) {
//			PoseStack matrixStack = graphics.pose();
//			prepareMatrix(matrixStack);
//			transformMatrix(matrixStack);
//			renderItemIntoGUI(matrixStack, stack, customLighting == null);
//			cleanUpMatrix(matrixStack);
			graphics.pose().pushMatrix();
			graphics.item(stack, (int) x(), (int) y());
			graphics.pose().popMatrix();
		}
	}

	public static class GuiEntityRenderBuilder extends GuiRenderBuilder {
		private final EntityRenderState state;

		public GuiEntityRenderBuilder(EntityRenderState state) {
			this.state = state;
		}

		@Override
		public void render(GuiGraphicsExtractor graphics) {
			float toRad = 0.01745329251F;
			Vector3f translation = new Vector3f((float) xLocal, (float) yLocal, (float) zLocal);
			Quaternionf rotation = new Quaternionf().rotateXYZ(200 * toRad, -20 * toRad, 0);
			float x0 = 0;
			float y0 = 0;
			float x1 = width();
			float y1 = height();
			var pos0 = graphics.pose().transformPosition(new Vector2f(x0, y0));
			var pos1 = graphics.pose().transformPosition(new Vector2f(x1, y1));
			graphics.entity(
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
