package snownee.lychee;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;

public final class LootContextKeys {
	public static final Map<Identifier, ContextKey<?>> ALL = Maps.newConcurrentMap();
	public static final ContextKey<BlockPos> BLOCK_POS = create("block_pos");
	public static final ContextKey<Direction> DIRECTION = create("direction");

	private static <T> ContextKey<T> create(String id) {
		return new ContextKey<>(Lychee.id(id));
	}
}
