package snownee.lychee;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.mixin.LootContextParamSetsAccess;

public final class LycheeLootContextParamSets {

	public static void init() {
	}

	public static final LootContextParamSet ITEM_BURNING = LootContextParamSetsAccess.callRegister("inworldcrafting:item_burning", $ -> {
		$.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY);
	});

	public static final LootContextParamSet ITEM_INSIDE = LootContextParamSetsAccess.callRegister("inworldcrafting:item_inside", $ -> {
		$.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY).required(LootContextParams.BLOCK_STATE).required(LycheeLootContextParams.BLOCK_POS).optional(LootContextParams.BLOCK_ENTITY);
	});

}
