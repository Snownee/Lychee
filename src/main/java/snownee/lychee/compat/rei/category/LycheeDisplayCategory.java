package snownee.lychee.compat.rei.category;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.network.chat.Component;
import snownee.lychee.compat.DisplayUtils;
import snownee.lychee.compat.rei.display.LycheeDisplay;

public abstract class LycheeDisplayCategory<T extends LycheeDisplay<?>> implements DisplayCategory<T> {
	public static final int WIDTH = 150;
	public static final int HEIGHT = 59;

	private final CategoryIdentifier<? extends T> categoryIdentifier;
	public Renderer icon;

	public LycheeDisplayCategory(CategoryIdentifier<? extends T> categoryIdentifier, Renderer icon) {
		this.icon = icon;
		this.categoryIdentifier = categoryIdentifier;
	}

	@Override
	public Renderer getIcon() {
		return icon;
	}

	@Override
	public Component getTitle() {
		return DisplayUtils.makeTitle(getIdentifier());
	}

	@Override
	public CategoryIdentifier<? extends T> getCategoryIdentifier() {
		return categoryIdentifier;
	}

	@Override
	public int getDisplayHeight() {
		return HEIGHT + 8;
	}
}
