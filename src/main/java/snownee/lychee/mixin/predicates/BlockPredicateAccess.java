package snownee.lychee.mixin.predicates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.serialization.Codec;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.block.Block;

@Mixin(BlockPredicate.class)
public interface BlockPredicateAccess {
	@Accessor("BLOCKS_CODEC")
	static Codec<HolderSet<Block>> blocksCodec() {
		throw new IllegalStateException();
	}
}
