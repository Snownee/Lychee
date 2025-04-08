package snownee.lychee.compat.recipeviewer.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.joml.Vector2ic;

import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.ui.CategoryMetadata;
import snownee.lychee.util.ui.ElementRenderer;
import snownee.lychee.util.ui.UIElement;

public class RvCategoryInstanceImpl<R extends ILycheeRecipe<LycheeContext>> implements RvCategoryInstance<R> {
	private final RvCategory<R> type;
	private final RvHelper helper;
	private final ResourceLocation id;
	protected final RecipeHolder<CategoryMetadata> metadata;
	private final List<RecipeHolder<R>> recipes = new ArrayList<>();
	private final Map<String, RvCategoryDecoration<R>> decorations = Maps.newLinkedHashMap();
	private final Map<String, Predicate<R>> conditions = Maps.newLinkedHashMap();

	public RvCategoryInstanceImpl(RvCategory<R> type, ResourceLocation id, RvHelper helper) {
		this.type = type;
		this.id = id;
		this.helper = helper;
		this.metadata = helper.getMetadata(this);
		decorations.putAll(type.decorations);
		conditions.putAll(type.conditions);
		processDecorations();
	}

	@Override
	public final RvCategory<R> type() {
		return type;
	}

	@Override
	public ResourceLocation id() {
		return id;
	}

	@Override
	public RvHelper helper() {
		return helper;
	}

	@Override
	public List<RecipeHolder<R>> recipes() {
		return recipes;
	}

	@Override
	public RenderElement icon() {
		return metadata.value().icon().map(ElementRenderer::of).orElseGet(RvCategoryInstance.super::icon);
	}

	@Override
	public List<Ingredient> workstations() {
		return metadata.value().workstation().orElseGet(RvCategoryInstance.super::workstations);
	}

	@Override
	public int width() {
		Vector2ic size = metadata.value().size().orElse(null);
		return size == null ? RvCategoryInstance.super.width() : size.x();
	}

	@Override
	public int height() {
		Vector2ic size = metadata.value().size().orElse(null);
		return size == null ? RvCategoryInstance.super.height() : size.y();
	}

	@Override
	public boolean renderDefault() {
		return metadata.value().renderDefault();
	}

	@Override
	public Map<String, RvCategoryDecoration<R>> decorations() {
		return decorations;
	}

	@Override
	public Map<String, Predicate<R>> conditions() {
		return conditions;
	}

	@Override
	public void configureDecorations(RvCategoryWidgetBuilder builder, RecipeHolder<R> recipeHolder) {
		boolean renderDefault = renderDefault();
		for (Map.Entry<String, RvCategoryDecoration<R>> entry : decorations.entrySet()) {
			String key = entry.getKey();
			if (!renderDefault && type.decorations.containsKey(key)) {
				continue;
			}
			Predicate<R> condition = conditions.get(key);
			if (condition != null && !condition.test(recipeHolder.value())) {
				continue;
			}
			entry.getValue().setup(builder, recipeHolder);
		}
	}

	private void processDecorations() {
		for (Map.Entry<String, List<UIElement>> entry : metadata.value().elements().orElse(Map.of()).entrySet()) {
			String key = entry.getKey();
			List<UIElement> elements = entry.getValue();
			RvCategoryDecoration<R> decoration = (builder, recipeHolder) -> {
				for (UIElement element : elements) {
					builder.addElement(ElementRenderer.of(element));
				}
			};
			decorations.put(key, decoration);
		}
	}
}
