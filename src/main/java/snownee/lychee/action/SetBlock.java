package snownee.lychee.action;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.context.ActionContext;
import snownee.lychee.network.SUpdateFallingBlockPacket;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.Displays;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public record SetBlock(PostActionCommonProperties commonProperties, BlockPredicate block) implements PostAction {
	@Override
	public PostActionType<?> type() {
		return PostActionTypes.SET_BLOCK;
	}

	@Override
	public void apply(LycheeContext context, ActionContext actionContext, int times) {
		Entity entity = actionContext.getOrNull(LootContextParams.THIS_ENTITY);
		if (entity instanceof FallingBlockEntity fbe) {
			fbe.blockState = BlockPredicateExtensions.anyBlockState(block);
			if (!fbe.blockState.isAir()) {
				if (fbe.blockData != null || block.requiresNbt()) {
					fbe.blockData = block.nbt().map(NbtPredicate::tag).orElse(null);
				}
				new SUpdateFallingBlockPacket(fbe).send(fbe);
			}
		}
	}

	@Override
	public Component getDisplayName() {
		var blockState = BlockPredicateExtensions.anyBlockState(block);
		var key = CommonProxy.makeDescriptionId("postAction", LycheeRegistries.POST_ACTION.getKey(type()));
		return Component.translatable(key, blockState.getBlock().getName());
	}

	@Override
	public List<SlotDisplay> getOutputItems() {
		return BlockPredicateExtensions.matchedItemStacks(block).stream().map(Displays::slot).toList();
	}

	@Override
	public List<BlockPredicate> getOutputBlocks() {
		return BlockPredicateExtensions.isAny(block) ? List.of() : List.of(block);
	}

	@Override
	public boolean repeatable() {
		return false;
	}

	public static class Type implements PostActionType<SetBlock> {
		public static final MapCodec<SetBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(SetBlock::commonProperties),
				BlockPredicateExtensions.CODEC.optionalFieldOf("block", BlockPredicateExtensions.ANY).forGetter(SetBlock::block)
		).apply(instance, SetBlock::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, SetBlock> STREAM_CODEC = StreamCodec.composite(
				PostActionCommonProperties.STREAM_CODEC,
				SetBlock::commonProperties,
				BlockPredicate.STREAM_CODEC,
				SetBlock::block,
				SetBlock::new);

		@Override
		public MapCodec<SetBlock> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SetBlock> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
