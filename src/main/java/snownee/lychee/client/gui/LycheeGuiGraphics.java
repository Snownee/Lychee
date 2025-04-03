package snownee.lychee.client.gui;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public interface LycheeGuiGraphics {
	//TODO Remove in 1.22
	Function<ResourceLocation, RenderType> GUI_TEXTURED = Util.memoize(
			texture -> RenderType.create(
					"gui_textured",
					DefaultVertexFormat.POSITION_TEX,
					VertexFormat.Mode.QUADS,
					786432,
					false,
					false,
					RenderType.CompositeState.builder()
							.setShaderState(RenderStateShard.POSITION_TEX_SHADER)
							.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
							.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
							.createCompositeState(false)
			)
	);

	static RenderType guiTextured(ResourceLocation texture) {
		return GUI_TEXTURED.apply(texture);
	}

	void lychee$setRenderType(@Nullable Function<ResourceLocation, RenderType> renderType);
}
