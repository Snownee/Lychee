package snownee.lychee.compat.recipeviewer.category;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.ui.CategoryModifier;

public abstract class RvCategoryBuilder<R extends ILycheeRecipe<LycheeContext>> {
	private final RvCategoryInstance<R> instance;
	private boolean renderDefault;
	private final Map<String, RvCategoryDecoration<R>> decorations = Maps.newHashMap();

	protected RvCategoryBuilder(RvCategoryInstance<R> instance, RecipeHolder<R> recipeHolder) {
		this.instance = instance;
		decorations.putAll(instance.decorations());
		String id = recipeHolder.id().toString();
		List<CategoryModifier> modifiers = instance.modifiers().stream()
				.map(RecipeHolder::value)
				.filter($ -> $.recipe().test(id))
				.toList();
		renderDefault = instance.renderDefault();
		for (CategoryModifier modifier : modifiers) {
			RvCategoryInstanceImpl.processDecorations(modifier.elements().orElse(null), decorations);
			renderDefault = renderDefault || modifier.renderDefault();
		}
	}

	public RvCategoryInstance<?> instance() {
		return instance;
	}

	public RvCategory<?> type() {
		return instance().type();
	}

	public RvHelper helper() {
		return instance().helper();
	}

	public int width() {
		return instance().width();
	}

	public int height() {
		return instance().height();
	}

	public Map<String, RvCategoryDecoration<R>> decorations() {
		return decorations;
	}

	@Nullable
	public Predicate<R> condition(String key) {
		return instance.conditions().get(key);
	}

	public boolean renderDefault() {
		return renderDefault;
	}
}
