package snownee.lychee.compat.recipeviewer.element;

import java.util.function.Function;
import java.util.function.Supplier;

import org.joml.Vector2f;
import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.client.SmartKey;
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
		Identifier id = light ? AllGuiTextures.LIGHT_SHADOW.id : AllGuiTextures.SHADOW.id;
		shadow = new SpriteElementRenderer(id).withSize(shadowWidth, shadowHeight);
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
		return RenderElement.create(graphics -> {
			var blockState = blockStateSupplier.get();
			if (blockState.isAir()) {
				RenderElement.create(AllGuiTextures.QUESTION_MARK).at(2, 2).render(graphics);
				return;
			}
			RenderElement element = blockElement.apply(blockState);
			float x = element.position.x + blockSize * 0.5F - shadowWidth * 0.5F;
			float y = element.position.y + blockSize - shadowHeight * 0.4F;
			var shadowPosition = new Vector2f(x, y);
			if (!Platform.isProduction() && SmartKey.hasControlDown()) {
				graphics.outline((int) element.x(), (int) element.y(), blockSize, blockSize, 0x88FF0000);
			}
			int lightEmission = blockState.getLightEmission();
			if (lightEmission < 5) {
				get(false).at(shadowPosition).debugOutline(graphics, 0x00FF00).render(graphics);
			} else if (lightEmission > 7) {
				get(true).at(shadowPosition).debugOutline(graphics, 0x00FF00).render(graphics);
			}
			element.render(graphics);
		});
	}
}
