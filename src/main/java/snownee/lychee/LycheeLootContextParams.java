package snownee.lychee;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public final class LycheeLootContextParams {

	public static void init() {
	}

	public static final Map<String, LootContextParam<?>> ALL = Maps.newConcurrentMap();
	public static final LootContextParam<BlockPos> BLOCK_POS = create("block_pos");
	public static final LootContextParam<Direction> DIRECTION = create("direction");

	private static <T> LootContextParam<T> create(String pId) {
		return new LootContextParam<>(new ResourceLocation(Lychee.ID, pId));
	}

	// TODO 1.20.4: use Kiwi's Util class instead
	public static String trimRL(String rl) {
		return trimRL(rl, ResourceLocation.DEFAULT_NAMESPACE);
	}

	public static String trimRL(String rl, String defaultNamespace) {
		return rl.startsWith(defaultNamespace + ":") ? rl.substring(defaultNamespace.length() + 1) : rl;
	}

}
