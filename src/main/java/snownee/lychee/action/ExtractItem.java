package snownee.lychee.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.util.ExtraCodecs;
import snownee.lychee.LootContextKeys;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;

public record ExtractItem(PostActionCommonProperties commonProperties, ItemPredicate item, int count) implements PostAction {

	@Override
	public PostActionType<ExtractItem> type() {
		return PostActionTypes.EXTRACT_ITEM;
	}

	@Override
	public void apply(LycheeContext context, ActionContext actionContext, int times) {
		var pos = actionContext.get(LootContextKeys.BLOCK_POS);
		var level = context.level();
		var direction = actionContext.getOrNull(LootContextKeys.DIRECTION);
		CommonProxy.extractItem(level, pos, direction, item, (long) count * times);
	}

	@Override
	public boolean repeatable() {
		return false;
	}

	@Override
	public boolean hidden() {
		return true;
	}

	public static class Type implements PostActionType<ExtractItem> {
		public static final MapCodec<ExtractItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(ExtractItem::commonProperties),
				ItemPredicate.CODEC.fieldOf("item").forGetter(ExtractItem::item),
				ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(ExtractItem::count)
		).apply(instance, ExtractItem::new));

		@Override
		public MapCodec<ExtractItem> codec() {
			return CODEC;
		}
	}
}
