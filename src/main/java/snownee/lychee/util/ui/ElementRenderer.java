package snownee.lychee.util.ui;

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Maps;

import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.ui.SpriteElementRenderer;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public interface ElementRenderer {
	Map<UIElementType<?>, Function<? extends UIElement, ? extends RenderElement>> FACTORIES = Maps.newIdentityHashMap();

	static void init() {
		ElementRenderer.register(UIElementType.SPRITE, SpriteElementRenderer::create);
		ElementRenderer.register(UIElementType.ITEM, it -> GuiGameElement.of(it.itemStack()));
		ElementRenderer.register(UIElementType.BLOCK, it -> GuiGameElement.of(BlockPredicateExtensions.anyBlockState(it.block())));
	}

	static RenderElement of(UIElement element) {
		RenderElement renderElement;
		Function<? extends UIElement, ? extends RenderElement> function = FACTORIES.get(element.type());
		if (function == null) {
			renderElement = RenderElement.empty();
		} else {
			//noinspection unchecked
			renderElement = ((Function<UIElement, RenderElement>) function).apply(element);
		}
		UIElementCommonProperties properties = element.commonProperties();
		renderElement.at(properties.x(), properties.y(), properties.z());
		renderElement.withSize(properties.width(), properties.height());
		return renderElement;
	}

	static <T extends UIElement> void register(UIElementType<T> type, Function<T, ? extends RenderElement> renderer) {
		FACTORIES.put(type, renderer);
	}
}
