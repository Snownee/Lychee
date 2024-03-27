package snownee.lychee;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.util.Util;

public final class Lychee {
	public static final String ID = "lychee";

	public static final Logger LOGGER = LogUtils.getLogger();

	public static ResourceLocation id(String path) {
		return Util.RL(path, ID);
	}
}
