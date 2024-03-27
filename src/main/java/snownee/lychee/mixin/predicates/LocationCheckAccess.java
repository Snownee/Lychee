package snownee.lychee.mixin.predicates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;

@Mixin(LocationCheck.class)
public interface LocationCheckAccess {
	@Accessor("OFFSET_CODEC")
	static MapCodec<BlockPos> getOffsetCodec() {
		throw new IllegalStateException();
	}
}
