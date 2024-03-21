package snownee.lychee.compat.rei.category;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import net.minecraft.network.chat.Component;
import snownee.lychee.compat.DisplayUtils;

public interface LycheeDisplayCategory<T extends Display> extends DisplayCategory<T> {
	CategoryIdentifier<? extends T> categoryIdentifier();

	@Override
	default CategoryIdentifier<? extends T> getCategoryIdentifier() {
		return categoryIdentifier();
	}

	Renderer icon();

	@Override
	default Renderer getIcon() {
		return icon();
	}

	@Override
	default Component getTitle() {
		return DisplayUtils.makeTitle(getIdentifier());
	}
}
