package snownee.lychee;

import snownee.kiwi.config.KiwiConfig;

@KiwiConfig
public final class LycheeConfig {

	@KiwiConfig.Path("debug.enable")
	public static boolean debug;
	@KiwiConfig.Path("fragment.enable")
	public static boolean enableFragment = true;

}
