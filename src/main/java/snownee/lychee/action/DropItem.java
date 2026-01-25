package snownee.lychee.action;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeContextKeys;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeTypes;
import snownee.lychee.mixin.ItemEntityAccess;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record DropItem(PostActionCommonProperties commonProperties, ItemStackTemplate itemStack) implements PostAction {
	@Override
	public PostActionType<DropItem> type() {
		return PostActionTypes.DROP_ITEM;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParams = context.get(LycheeContextKey.LOOT_PARAMS);
		var pos = lootParams.get(LootContextParams.ORIGIN);
		var level = context.level();
		if (recipe instanceof BlockCrushingRecipe) {
			var state = lootParams.get(LootContextParams.BLOCK_STATE);
			if (state.is(LycheeTags.EXTEND_BOX)) {
				pos = Vec3.atCenterOf(lootParams.get(LycheeContextKeys.BLOCK_POS));
			}
		}
		var itemStack = this.itemStack.create();
		itemStack.setCount(itemStack.getCount() * times);
		if (recipe != null && recipe.getType() == RecipeTypes.BLOCK_EXPLODING) {
			context.get(LycheeContextKey.ITEM).stacksNeedHandle.add(itemStack);
		} else {
			CommonProxy.dropItemStack(level, pos.x, pos.y, pos.z, itemStack, $ -> ((ItemEntityAccess) $).setHealth(80));
		}
	}

	@Override
	public Component getDisplayName() {
		return itemStack.create().getHoverName();
	}

	@Override
	public List<SlotDisplay> getOutputItems() {
		return List.of(itemStack);
	}

	public static class Type implements PostActionType<DropItem> {
		public static final MapCodec<DropItem> CODEC = RecordCodecBuilder.mapCodec(instance ->
				instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(DropItem::commonProperties),
						LycheeCodecs.NONEMPTY_ITEM_STACK_TEMPLATE_MAP_CODEC.forGetter(DropItem::itemStack)
				).apply(instance, DropItem::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, DropItem> STREAM_CODEC = StreamCodec.composite(
				PostActionCommonProperties.STREAM_CODEC,
				DropItem::commonProperties,
				ItemStackTemplate.STREAM_CODEC,
				DropItem::itemStack,
				DropItem::new);

		@Override
		public MapCodec<DropItem> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, DropItem> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
