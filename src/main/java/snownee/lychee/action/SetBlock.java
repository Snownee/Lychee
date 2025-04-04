package snownee.lychee.action;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.network.SUpdateFallingBlockPacket;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record SetBlock(PostActionCommonProperties commonProperties, BlockPredicate block) implements PostAction {
	@Override
	public PostActionType<?> type() {
		return PostActionTypes.SET_BLOCK;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		Entity entity = context.get(LycheeContextKey.LOOT_PARAMS).getOrNull(LootContextParams.THIS_ENTITY);
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

	public static class Type implements PostActionType<SetBlock> {
		public static final MapCodec<SetBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(SetBlock::commonProperties),
				BlockPredicateExtensions.CODEC.optionalFieldOf("block", BlockPredicateExtensions.ANY).forGetter(SetBlock::block)
		).apply(instance, SetBlock::new));

		@Override
		public MapCodec<SetBlock> codec() {
			return CODEC;
		}
	}
}
