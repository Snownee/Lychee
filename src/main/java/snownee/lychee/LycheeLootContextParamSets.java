package snownee.lychee;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.mixin.LootContextParamSetsAccess;

public final class LycheeLootContextParamSets {

	public static void init() {
	}

	public static final LootContextParamSet ALL = LootContextParamSetsAccess.callRegister("lychee:all", $ -> {
		$
				.optional(LootContextParams.THIS_ENTITY)
				.optional(LootContextParams.LAST_DAMAGE_PLAYER)
				.optional(LootContextParams.DAMAGE_SOURCE)
				.optional(LootContextParams.KILLER_ENTITY)
				.optional(LootContextParams.DIRECT_KILLER_ENTITY)
				.optional(LootContextParams.ORIGIN)
				.optional(LootContextParams.BLOCK_STATE)
				.optional(LootContextParams.BLOCK_ENTITY)
				.optional(LootContextParams.TOOL)
				.optional(LootContextParams.EXPLOSION_RADIUS)
				.optional(LycheeLootContextParams.BLOCK_POS);
	});

	public static final LootContextParamSet ITEM_BURNING = LootContextParamSetsAccess.callRegister("lychee:item_burning", $ -> {
		$.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY);
	});

	public static final LootContextParamSet ITEM_INSIDE = LootContextParamSetsAccess.callRegister("lychee:item_inside", $ -> {
		$.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY).required(LootContextParams.BLOCK_STATE).required(LycheeLootContextParams.BLOCK_POS).optional(LootContextParams.BLOCK_ENTITY);
	});

	public static final LootContextParamSet BLOCK_INTERACTION = LootContextParamSetsAccess.callRegister("lychee:block_interaction", $ -> {
		$.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY).required(LootContextParams.BLOCK_STATE).required(LycheeLootContextParams.BLOCK_POS).optional(LootContextParams.BLOCK_ENTITY).required(LootContextParams.TOOL);
	});

	public static final LootContextParamSet ANVIL_CRAFTING = LootContextParamSetsAccess.callRegister("lychee:anvil_crafting", $ -> {
		$.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY).optional(LootContextParams.BLOCK_STATE).optional(LycheeLootContextParams.BLOCK_POS).optional(LootContextParams.BLOCK_ENTITY);
	});

	public static final LootContextParamSet BLOCK_CRUSHING = LootContextParamSetsAccess.callRegister("lychee:block_crushing", $ -> {
		$.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY).required(LootContextParams.BLOCK_STATE).required(LycheeLootContextParams.BLOCK_POS).optional(LootContextParams.BLOCK_ENTITY);
	});

	public static final LootContextParamSet LIGHTNING_CHANNELING = LootContextParamSetsAccess.callRegister("lychee:lightning_channeling", $ -> {
		$.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY);
	});

	public static final LootContextParamSet ITEM_EXPLODING = LootContextParamSetsAccess.callRegister("lychee:item_exploding", $ -> {
		$.required(LootContextParams.ORIGIN);
	});

	public static final LootContextParamSet BLOCK_EXPLODING = LootContextParamSetsAccess.callRegister("lychee:block_exploding", $ -> {
		$.required(LootContextParams.ORIGIN).required(LootContextParams.BLOCK_STATE).optional(LycheeLootContextParams.BLOCK_POS).optional(LootContextParams.BLOCK_ENTITY);
	});

}
