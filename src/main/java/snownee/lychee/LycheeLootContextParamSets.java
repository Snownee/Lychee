package snownee.lychee;

import java.util.function.Predicate;

import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.mixin.LootContextParamSetsAccess;

public final class LycheeLootContextParamSets {
	public static final ContextKeySet ALL = LootContextParamSetsAccess.callRegister(
			"lychee:all", $ -> {
				$.required(LootContextParams.ORIGIN);
				LootContextKeys.ALL.values().stream().filter(Predicate.not(LootContextParams.ORIGIN::equals)).forEach($::optional);
			});

	public static final ContextKeySet ITEM_BURNING =
			LootContextParamSetsAccess.callRegister(
					"lychee:item_burning",
					$ -> $.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY)
			);

	public static final ContextKeySet ITEM_INSIDE = LootContextParamSetsAccess.callRegister(
			"lychee:item_inside",
			$ -> $.required(LootContextParams.ORIGIN)
					.required(LootContextParams.THIS_ENTITY)
					.required(LootContextParams.BLOCK_STATE)
					.required(LootContextKeys.BLOCK_POS)
					.optional(LootContextParams.BLOCK_ENTITY)
	);

	public static final ContextKeySet BLOCK_INTERACTION = LootContextParamSetsAccess.callRegister(
			"lychee:block_interaction",
			$ -> $.required(LootContextParams.ORIGIN)
					.required(LootContextParams.THIS_ENTITY)
					.required(LootContextParams.BLOCK_STATE)
					.required(LootContextKeys.BLOCK_POS)
					.optional(LootContextParams.BLOCK_ENTITY)
					.required(LootContextKeys.DIRECTION)
	);

	public static final ContextKeySet ANVIL_CRAFTING = LootContextParamSetsAccess.callRegister(
			"lychee:anvil_crafting",
			$ -> $.required(LootContextParams.ORIGIN)
					.required(LootContextParams.THIS_ENTITY)
					.optional(LootContextParams.BLOCK_STATE)
					.optional(LootContextKeys.BLOCK_POS)
					.optional(LootContextParams.BLOCK_ENTITY)
	);

	public static final ContextKeySet BLOCK_CRUSHING = LootContextParamSetsAccess.callRegister(
			"lychee:block_crushing",
			$ -> $.required(LootContextParams.ORIGIN)
					.required(LootContextParams.THIS_ENTITY)
					.required(LootContextParams.BLOCK_STATE)
					.required(LootContextKeys.BLOCK_POS)
					.optional(LootContextParams.BLOCK_ENTITY)
	);

	public static final ContextKeySet LIGHTNING_CHANNELING = LootContextParamSetsAccess.callRegister(
			"lychee:lightning_channeling",
			$ -> $.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY)
	);

	public static final ContextKeySet ITEM_EXPLODING = LootContextParamSetsAccess.callRegister(
			"lychee:item_exploding",
			$ -> $.required(LootContextParams.ORIGIN).required(LootContextParams.EXPLOSION_RADIUS)
	);

	public static final ContextKeySet BLOCK_ONLY = LootContextParamSetsAccess.callRegister(
			"lychee:block_only",
			$ -> $.required(LootContextParams.ORIGIN)
					.required(LootContextParams.BLOCK_STATE)
					.optional(LootContextKeys.BLOCK_POS)
					.optional(LootContextParams.BLOCK_ENTITY)
	);

	public static final ContextKeySet CRAFTING = LootContextParamSetsAccess.callRegister(
			"lychee:crafting",
			$ -> $.optional(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY)
	);

	public static final ContextKeySet ENTITY_TICKING =
			LootContextParamSetsAccess.callRegister(
					"lychee:entity_ticking",
					$ -> $.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY)
			);
}
