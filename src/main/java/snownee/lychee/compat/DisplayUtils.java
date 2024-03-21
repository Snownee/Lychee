package snownee.lychee.compat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public final class DisplayUtils {
	public static MutableComponent makeTitle(ResourceLocation id) {
		String key = id.toLanguageKey("recipeType");
		int i = key.indexOf('/');
		if ("/minecraft/default".equals(key.substring(i))) {
			key = key.substring(0, i);
		}
		return Component.translatable(key);
	}
}
