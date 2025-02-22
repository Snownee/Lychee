package snownee.lychee;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public final class LycheeLootContextParams {
	public static final Map<String, LootContextParam<?>> ALL = Maps.newConcurrentMap();
	public static final LootContextParam<BlockPos> BLOCK_POS = create("block_pos");
	public static final LootContextParam<Direction> DIRECTION = create("direction");

	private static <T> LootContextParam<T> create(String id) {
		return new LootContextParam<>(Lychee.id(id));
	}
}
