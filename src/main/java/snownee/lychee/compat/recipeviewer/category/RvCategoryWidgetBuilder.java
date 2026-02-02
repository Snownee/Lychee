package snownee.lychee.compat.recipeviewer.category;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class RvCategoryWidgetBuilder<R extends ILycheeRecipe<LycheeContext>> extends RvCategoryBuilder<R> {
	public static Comparator<RenderElement> ELEMENT_COMPARATOR = Comparator.<RenderElement>comparingInt(RenderElement::sortOrder)
			.thenComparingDouble(RenderElement::y)
			.thenComparingDouble(RenderElement::x)
			.reversed();
	private final List<RenderElement> elements = Lists.newArrayList();

	public RvCategoryWidgetBuilder(RvCategoryInstance<R> instance, RecipeHolder<R> recipeHolder) {
		super(instance, recipeHolder);
	}

	public void addElement(RenderElement element) {
		elements.add(element);
	}

	public Stream<RenderElement> sortElements() {
		return elements.stream().sorted(ELEMENT_COMPARATOR);
	}
}
