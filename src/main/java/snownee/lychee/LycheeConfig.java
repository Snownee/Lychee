package snownee.lychee;

import snownee.kiwi.config.KiwiConfig;

@KiwiConfig
public final class LycheeConfig {

	@KiwiConfig.Path("debug.enable")
	public static boolean debug;
	public static boolean dispenserFallableBlockPlacement = true;

}
