package snownee.lychee.mixin;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import snownee.lychee.block_exploding.BlockExplodingRecipe;
import snownee.lychee.item_exploding.ItemExplodingRecipe;

@Mixin(value = Explosion.class, priority = 700)
public class ExplosionMixin {

	@Shadow
	private Level level;
	// the field "position" is added by forge
	@Shadow
	private double x;
	@Shadow
	private double y;
	@Shadow
	private double z;
	@Shadow
	private float radius;

	@Inject(
			at = @At(
				"TAIL"
			), method = "Lnet/minecraft/world/level/Explosion;explode()V", locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void lychee_explode(CallbackInfo ci, Set<BlockPos> set, int i, float f2, int k1, int l1, int i2, int i1, int j2, int j1, List<Entity> list) {
		ItemExplodingRecipe.on(level, x, y, z, list, radius);
	}

	@SuppressWarnings("deprecation")
	@Redirect(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDrops(Lnet/minecraft/world/level/storage/loot/LootContext$Builder;)Ljava/util/List;"
			), method = "Lnet/minecraft/world/level/Explosion;finalizeExplosion(Z)V"
	)
	public List<ItemStack> lychee_getDrops(BlockState state, LootContext.Builder pBuilder) {
		Supplier<List<ItemStack>> drops = () -> state.getBlock().getDrops(state, pBuilder);
		return BlockExplodingRecipe.on(level, state, pBuilder, drops);
	}

}