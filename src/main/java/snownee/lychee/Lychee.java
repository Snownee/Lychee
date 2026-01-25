package snownee.lychee;

import java.util.Objects;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.Identifier;
import snownee.kiwi.util.KUtil;

public final class Lychee {
	public static final String ID = "lychee";

	public static final Logger LOGGER = LogUtils.getLogger();

	public static Identifier id(String path) {
		return Objects.requireNonNull(KUtil.RL(path, ID));
	}
}
