package snownee.lychee.compat.rv.category;

import java.util.List;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.rv.RVHelper;
import snownee.lychee.compat.rv.RVs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public interface RvCategory<R extends ILycheeRecipe<LycheeContext>> {
	ResourceLocation id();

	RvCategoryType<R> type();

	RVHelper rvHelper();

	default int width() {
		return type().width;
	}

	default int height() {
		return type().height;
	}

	default Rect2i infoRect() {
		return type().infoRect;
	}

	default Component title() {
		return RVs.makeTitle(id());
	}

	default RenderElement icon() {
		return type().iconProvider.get(this);
	}

	default List<List<ItemStack>> workstations() {
		return type().workstationProvider.get(this);
	}
}
