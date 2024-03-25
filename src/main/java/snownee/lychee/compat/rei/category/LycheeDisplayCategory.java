package snownee.lychee.compat.rei.category;

import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import net.minecraft.network.chat.Component;
import snownee.lychee.compat.DisplayUtils;
import snownee.lychee.compat.rei.display.LycheeDisplay;

public interface LycheeDisplayCategory<T extends LycheeDisplay<?>> extends DisplayCategory<T> {

	@Override
	default Component getTitle() {
		return DisplayUtils.makeTitle(getIdentifier());
	}
}
