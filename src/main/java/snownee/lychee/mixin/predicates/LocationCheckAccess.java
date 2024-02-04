package snownee.lychee.mixin.predicates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import snownee.lychee.util.predicates.LocationCheck;

@Mixin(LocationCheck.class)
public interface LocationCheckAccess {
	@Accessor
	static MapCodec<BlockPos> getOffsetCodec() {
		throw new IllegalStateException();
	}
}
