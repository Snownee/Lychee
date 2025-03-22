package snownee.lychee.compat.rv.category;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import net.minecraft.resources.ResourceLocation;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.rv.RVHelper;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public abstract class AbstractRvCategory<R extends ILycheeRecipe<LycheeContext>> implements RvCategory<R> {
	private final RvCategoryType<R> type;
	private final ResourceLocation id;
	private final RVHelper rvHelper;

	private final Supplier<RenderElement> iconSupplier;

	protected AbstractRvCategory(RvCategoryType<R> type, ResourceLocation id, RVHelper rvHelper) {
		this.type = type;
		this.id = id;
		this.iconSupplier = Suppliers.memoize(() -> type.iconProvider.get(this));
		this.rvHelper = rvHelper;
	}

	@Override
	public ResourceLocation id() {
		return id;
	}

	@Override
	public RvCategoryType<R> type() {
		return type;
	}

	@Override
	public RVHelper rvHelper() {
		return rvHelper;
	}

	@Override
	public RenderElement icon() {
		return iconSupplier.get();
	}
}
