package snownee.lychee;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public final class LycheeLootContextParams {

	public static void init() {
	}

	public static final List<LootContextParam<?>> ALL = Lists.newArrayList();
	public static final LootContextParam<BlockPos> BLOCK_POS = create("block_pos");
	public static final LootContextParam<Direction> DIRECTION = create("direction");

	private static <T> LootContextParam<T> create(String pId) {
		LootContextParam<T> param = new LootContextParam<>(new ResourceLocation(Lychee.ID, pId));
		ALL.add(param);
		return param;
	}

}
