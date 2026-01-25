package snownee.lychee.compat.recipeviewer.category;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.ui.CategoryMetadata;
import snownee.lychee.util.ui.CategoryModifier;
import snownee.lychee.util.ui.ElementRenderer;
import snownee.lychee.util.ui.UIElement;

public class RvCategoryInstanceImpl<R extends ILycheeRecipe<LycheeContext>> implements RvCategoryInstance<R> {
	private final RvCategory<R> type;
	private final RvHelper helper;
	private final Identifier id;
	protected final RecipeHolder<CategoryMetadata> metadata;
	protected final List<RecipeHolder<CategoryModifier>> modifiers;
	private final List<RecipeHolder<R>> recipes = Lists.newArrayList();
	private final Map<String, RvCategoryDecoration<R>> decorations = Maps.newLinkedHashMap();
	private final Map<String, Predicate<R>> conditions = Maps.newLinkedHashMap();

	public RvCategoryInstanceImpl(RvCategory<R> type, Identifier id, RvHelper helper) {
		this.type = type;
		this.id = id;
		this.helper = helper;
		metadata = helper.getMetadata(this);
		modifiers = helper.getModifiers(this);
		decorations.putAll(type.decorations);
		conditions.putAll(type.conditions);
		processDecorations(metadata().elements().orElse(null), decorations);
	}

	@Override
	public final RvCategory<R> type() {
		return type;
	}

	@Override
	public Identifier id() {
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
	public CategoryMetadata metadata() {
		return metadata.value();
	}

	@Override
	public List<RecipeHolder<CategoryModifier>> modifiers() {
		return modifiers;
	}

	@Override
	public RenderElement icon() {
		return metadata().icon().map(ElementRenderer::of).orElseGet(RvCategoryInstance.super::icon);
	}

	@Override
	public List<SlotDisplay> workstations() {
		return metadata().workstation()
				.map($ -> $.stream().map(Ingredient::display).toList())
				.orElseGet(RvCategoryInstance.super::workstations);
	}

	@Override
	public int width() {
		Vector2ic size = metadata().size().orElse(null);
		return size == null ? RvCategoryInstance.super.width() : size.x();
	}

	@Override
	public int height() {
		Vector2ic size = metadata().size().orElse(null);
		return size == null ? RvCategoryInstance.super.height() : size.y();
	}

	@Override
	public boolean renderDefault() {
		return metadata().renderDefault();
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
	public void configureDecorations(RvCategoryWidgetBuilder<R> builder, RecipeHolder<R> recipeHolder) {
		for (Map.Entry<String, RvCategoryDecoration<R>> entry : builder.decorations().entrySet()) {
			String key = entry.getKey();
			if (!builder.renderDefault() && type.decorations.containsKey(key)) {
				continue;
			}
			Predicate<R> condition = builder.condition(key);
			if (condition != null && !condition.test(recipeHolder.value())) {
				continue;
			}
			entry.getValue().setup(builder, recipeHolder);
		}
	}

	public static <R extends ILycheeRecipe<LycheeContext>> void processDecorations(
			@Nullable Map<String, List<UIElement>> uiElements,
			Map<String, RvCategoryDecoration<R>> decorations) {
		if (uiElements == null) {
			return;
		}
		for (Map.Entry<String, List<UIElement>> entry : uiElements.entrySet()) {
			String key = entry.getKey();
			List<UIElement> elements = entry.getValue();
			RvCategoryDecoration<R> decoration = (builder, recipeHolder) -> {
				for (UIElement element : elements) {
					builder.addElement(ElementRenderer.of(element, recipeHolder));
				}
			};
			decorations.put(key, decoration);
		}
	}
}
