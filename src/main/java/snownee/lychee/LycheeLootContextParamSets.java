package snownee.lychee;

import java.util.function.Predicate;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.mixin.LootContextParamSetsAccess;

public final class LycheeLootContextParamSets {
	public static final LootContextParamSet ALL = LootContextParamSetsAccess.callRegister(
			"lychee:all", $ -> {
				$.required(LootContextParams.ORIGIN);
				LycheeLootContextParams.ALL.values().stream().filter(Predicate.not(LootContextParams.ORIGIN::equals)).forEach($::optional);
			});

	public static final LootContextParamSet ITEM_BURNING =
			LootContextParamSetsAccess.callRegister(
					"lychee:item_burning",
					$ -> $.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY)
			);

	public static final LootContextParamSet ITEM_INSIDE = LootContextParamSetsAccess.callRegister(
			"lychee:item_inside",
			$ -> $.required(LootContextParams.ORIGIN)
					.required(LootContextParams.THIS_ENTITY)
					.required(LootContextParams.BLOCK_STATE)
					.required(LycheeLootContextParams.BLOCK_POS)
					.optional(LootContextParams.BLOCK_ENTITY)
	);

	public static final LootContextParamSet BLOCK_INTERACTION = LootContextParamSetsAccess.callRegister(
			"lychee:block_interaction",
			$ -> $.required(LootContextParams.ORIGIN)
					.required(LootContextParams.THIS_ENTITY)
					.required(LootContextParams.BLOCK_STATE)
					.required(LycheeLootContextParams.BLOCK_POS)
					.optional(LootContextParams.BLOCK_ENTITY)
					.required(LycheeLootContextParams.DIRECTION)
	);

	public static final LootContextParamSet ANVIL_CRAFTING = LootContextParamSetsAccess.callRegister(
			"lychee:anvil_crafting",
			$ -> $.required(LootContextParams.ORIGIN)
					.required(LootContextParams.THIS_ENTITY)
					.optional(LootContextParams.BLOCK_STATE)
					.optional(LycheeLootContextParams.BLOCK_POS)
					.optional(LootContextParams.BLOCK_ENTITY)
	);

	public static final LootContextParamSet BLOCK_CRUSHING = LootContextParamSetsAccess.callRegister(
			"lychee:block_crushing",
			$ -> $.required(LootContextParams.ORIGIN)
					.required(LootContextParams.THIS_ENTITY)
					.required(LootContextParams.BLOCK_STATE)
					.required(LycheeLootContextParams.BLOCK_POS)
					.optional(LootContextParams.BLOCK_ENTITY)
	);

	public static final LootContextParamSet LIGHTNING_CHANNELING = LootContextParamSetsAccess.callRegister(
			"lychee:lightning_channeling",
			$ -> $.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY)
	);

	public static final LootContextParamSet ITEM_EXPLODING = LootContextParamSetsAccess.callRegister(
			"lychee:item_exploding",
			$ -> $.required(LootContextParams.ORIGIN).required(LootContextParams.EXPLOSION_RADIUS)
	);

	public static final LootContextParamSet BLOCK_ONLY = LootContextParamSetsAccess.callRegister(
			"lychee:block_only",
			$ -> $.required(LootContextParams.ORIGIN)
					.required(LootContextParams.BLOCK_STATE)
					.optional(LycheeLootContextParams.BLOCK_POS)
					.optional(LootContextParams.BLOCK_ENTITY)
	);

	public static final LootContextParamSet CRAFTING = LootContextParamSetsAccess.callRegister(
			"lychee:crafting",
			$ -> $.optional(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY)
	);

}
