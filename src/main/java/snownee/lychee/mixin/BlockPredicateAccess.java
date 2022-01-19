package snownee.lychee.mixin;

import java.util.Set;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;

@Mixin(BlockPredicate.class)
public interface BlockPredicateAccess {

	@Accessor
	@Nullable
	Tag<Block> getTag();

	@Accessor
	@Nullable
	Set<Block> getBlocks();

	@Accessor
	StatePropertiesPredicate getProperties();

	@Accessor
	NbtPredicate getNbt();

}
