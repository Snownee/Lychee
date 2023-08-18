package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import snownee.lychee.core.def.LocationPredicateHelper;

@Mixin(LocationPredicate.class)
public class LocationPredicateMixin implements LocationPredicateHelper {

	@Unique
	private TagKey<Biome> lychee$biomeTag;

	@Override
	public void lychee$setBiomeTag(TagKey<Biome> biomeTag) {
		lychee$biomeTag = biomeTag;
	}

	@Override
	public TagKey<Biome> lychee$getBiomeTag() {
		return lychee$biomeTag;
	}

	@Inject(method = "matches", at = @At(
			value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;isLoaded(Lnet/minecraft/core/BlockPos;)Z", shift = At.Shift.BY, by = 2
	), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void lychee_matches(ServerLevel level, double x, double y, double z, CallbackInfoReturnable<Boolean> cir, BlockPos blockpos, boolean loaded) {
		if (lychee$biomeTag != null && !level.getBiome(blockpos).is(lychee$biomeTag)) {
			cir.setReturnValue(false);
		}
	}
}
