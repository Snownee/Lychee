package snownee.lychee.client.gui;

import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.LightCoordsUtil;

public class GuiFluidRenderer extends PictureInPictureRenderer<GuiFluidRenderState> {
	private final Model.Simple model;

	public GuiFluidRenderer(MultiBufferSource.BufferSource bufferSource) {
		super(bufferSource);
		List<ModelPart.Cube> cubes = CubeListBuilder.create()
				.addBox(0, 0, 0, 16, 16, 16)
				.getCubes()
				.stream()
				.map($ -> $.bake(32, 32))
				.toList();
		model = new Model.Simple(new ModelPart(cubes, Map.of()), RenderTypes::entityTranslucent);
	}

	@Override
	public Class<GuiFluidRenderState> getRenderStateClass() {
		return GuiFluidRenderState.class;
	}

	@Override
	protected void renderToTexture(GuiFluidRenderState renderState, PoseStack poseStack) {
		FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(renderState.fluid());
		if (handler == null) {
			return;
		}
		TextureAtlasSprite[] sprites = handler.getFluidSprites(null, null, renderState.fluid().defaultFluidState());
		model.renderToBuffer(
				poseStack,
				bufferSource.getBuffer(model.renderType(sprites[0].atlasLocation())),
				LightCoordsUtil.UI_FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY);
	}

	@Override
	protected String getTextureLabel() {
		return "lychee fluid";
	}
}
