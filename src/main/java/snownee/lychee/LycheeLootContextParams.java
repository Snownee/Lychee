package snownee.lychee;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public final class LycheeLootContextParams {

	public static final LootContextParam<BlockPos> BLOCK_POS = create("block_pos");

	private static <T> LootContextParam<T> create(String pId) {
		return new LootContextParam<>(new ResourceLocation(Lychee.ID, pId));
	}

}
