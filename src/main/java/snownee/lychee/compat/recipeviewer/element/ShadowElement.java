package snownee.lychee.compat.recipeviewer.element;

import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.loader.Platform;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.ui.SpriteElementRenderer;

public class ShadowElement {
	private final int blockSize;
	private final int shadowWidth;
	private final int shadowHeight;
	private @Nullable RenderElement darkShadow;
	private @Nullable RenderElement lightShadow;

	public ShadowElement(int blockSize, int shadowWidth, int shadowHeight) {
		this.blockSize = blockSize;
		this.shadowWidth = shadowWidth;
		this.shadowHeight = shadowHeight;
	}

	public RenderElement get(boolean light) {
		var shadow = light ? lightShadow : darkShadow;
		if (shadow != null) {
			return shadow;
		}
		var shadowOffset = new Vector2f((blockSize - shadowWidth) / 2F, blockSize - shadowHeight / 2F);
		ResourceLocation id = light ? AllGuiTextures.LIGHT_SHADOW.id : AllGuiTextures.SHADOW.id;
		shadow = new SpriteElementRenderer(id, 1F).withSize(shadowWidth, shadowHeight).at(shadowOffset);
		if (light) {
			lightShadow = shadow;
		} else {
			darkShadow = shadow;
		}
		return shadow;
	}

	public InteractiveRenderElement blockWithShadow(
			Supplier<BlockState> blockStateSupplier,
			Function<BlockState, RenderElement> blockElement) {
		return new InteractiveRenderElement(graphics -> {
			if (!Platform.isProduction() && Screen.hasControlDown()) {
				new SpriteElementRenderer(AllGuiTextures.INFO.id, 0.25F).atZ(1000).render(graphics);
			}
			var blockState = blockStateSupplier.get();
			if (blockState.isAir()) {
				RenderElement.create(AllGuiTextures.QUESTION_MARK).at(2, 2).render(graphics);
				return;
			}
			int lightEmission = blockState.getLightEmission();
			if (lightEmission < 5) {
				get(false).render(graphics);
			} else if (lightEmission > 7) {
				get(true).render(graphics);
			}
			blockElement.apply(blockState).render(graphics);
		});
	}
}
