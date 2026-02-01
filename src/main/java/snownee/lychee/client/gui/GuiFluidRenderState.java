package snownee.lychee.client.gui;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.world.level.material.FluidState;

public record GuiFluidRenderState(
		FluidState fluidState,
		Vector3f translation,
		Quaternionf rotation,
		@Nullable Quaternionf overrideCameraAngle,
		int x0,
		int y0,
		int x1,
		int y1,
		float scale,
		@Nullable ScreenRectangle scissorArea,
		@Nullable ScreenRectangle bounds) implements PictureInPictureRenderState {
	public GuiFluidRenderState(
			FluidState fluidState,
			Vector3f translation,
			Quaternionf rotation,
			@Nullable Quaternionf overrideCameraAngle,
			int x0,
			int y0,
			int x1,
			int y1,
			float scale,
			@Nullable ScreenRectangle scissorArea) {
		this(
				fluidState,
				translation,
				rotation,
				overrideCameraAngle,
				x0,
				y0,
				x1,
				y1,
				scale,
				scissorArea,
				PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
	}
}
