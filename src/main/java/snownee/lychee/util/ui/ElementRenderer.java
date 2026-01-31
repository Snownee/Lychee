package snownee.lychee.util.ui;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.ui.BlockElement;
import snownee.lychee.ui.GameElementRenderer;
import snownee.lychee.ui.ItemElement;
import snownee.lychee.ui.SpriteElementRenderer;
import snownee.lychee.ui.TextElementRenderer;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public interface ElementRenderer {
	Map<UIElementType<?>, Function<? extends UIElement, ? extends RenderElement>> FACTORIES = Maps.newIdentityHashMap();

	static void init() {
		ElementRenderer.register(UIElementType.SPRITE, SpriteElementRenderer::create);
		ElementRenderer.register(UIElementType.ITEM, (GameElementRenderer<ItemElement>) it -> GuiGameElement.of(it.itemStack()));
		ElementRenderer.register(
				UIElementType.BLOCK,
				(GameElementRenderer<BlockElement>) it -> GuiGameElement.of(BlockPredicateExtensions.anyBlockState(it.block())));
		ElementRenderer.register(UIElementType.TEXT, TextElementRenderer::create);
	}

	static RenderElement of(UIElement element) {
		return of(element, null);
	}

	static RenderElement of(UIElement element, @Nullable RecipeHolder<?> recipeHolder) {
		RenderElement renderElement;
		Function<? extends UIElement, ? extends RenderElement> function = FACTORIES.get(element.type());
		if (function == null) {
			renderElement = RenderElement.empty();
		} else {
			//noinspection unchecked
			renderElement = ((Function<UIElement, RenderElement>) function).apply(element);
		}
		UIElementCommonProperties properties = element.commonProperties();
		if (properties.tooltip().isPresent()) {
			InteractiveRenderElement interactiveElement = RenderElement.create(renderElement);
			List<Component> tooltip = properties.tooltip().get();
			renderElement = interactiveElement.onTooltip(() -> tooltip);
		}
		String id = properties.onInput().orElse(null);
		if (recipeHolder != null && id != null) {
			InteractiveRenderElement interactiveElement = RenderElement.create(renderElement);
			renderElement = interactiveElement.onInput((action, element0) -> ClientProxy.postWidgetInputEvent(
					recipeHolder.value(),
					id,
					action,
					element0));
		}
		renderElement.at(properties.pos()).withSize(properties.size()).withAlpha(properties.opacity());
		return renderElement;
	}

	static <T extends UIElement> void register(UIElementType<T> type, Function<T, ? extends RenderElement> renderer) {
		FACTORIES.put(type, renderer);
	}
}
