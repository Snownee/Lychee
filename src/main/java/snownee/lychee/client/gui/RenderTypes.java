package snownee.lychee.client.gui;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import snownee.lychee.Lychee;

// TODO 1.17: use custom shaders instead of vanilla ones
public class RenderTypes extends RenderStateShard {

	private static final RenderType FLUID = RenderType.create(
			createLayerName("fluid"),
			DefaultVertexFormat.NEW_ENTITY,
			VertexFormat.Mode.QUADS,
			256,
			true,
			true,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
					.setTextureState(BLOCK_SHEET_MIPPED)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setLightmapState(LIGHTMAP)
					.setOverlayState(OVERLAY)
					.createCompositeState(true)
	);

	public static RenderType getFluid() {
		return FLUID;
	}

	private static String createLayerName(String name) {
		return Lychee.ID + ":" + name;
	}

	// Mmm gimme those protected fields
	private RenderTypes() {
		super(null, null, null);
	}

}
