package snownee.lychee.mixin;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.block_exploding.BlockExplodingRecipe;
import snownee.lychee.item_exploding.ItemExplodingRecipe;

@Mixin(value = Explosion.class, priority = 700)
public class ExplosionMixin {

	@Shadow
	public float radius;
	@Final
	@Shadow
	private Level level;
	// the field "position" is added by forge
	@Final
	@Shadow
	private double x;
	@Final
	@Shadow
	private double y;
	@Final
	@Shadow
	private double z;

	@Inject(
			at = @At(
					"TAIL"
			), method = "explode()V", locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void lychee_explode(CallbackInfo ci, Set<BlockPos> set, float q, int k, int l, int r, int s, int t, int u, List<Entity> list, Vec3 vec3, int v) {
		ItemExplodingRecipe.on(level, x, y, z, list, radius);
	}

	@SuppressWarnings("deprecation")
	@Redirect(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDrops(Lnet/minecraft/world/level/storage/loot/LootParams$Builder;)Ljava/util/List;"
			), method = "finalizeExplosion(Z)V"
	)
	public List<ItemStack> lychee_getDrops(BlockState state, LootParams.Builder builder) {
		Supplier<List<ItemStack>> drops = () -> state.getBlock().getDrops(state, builder);
		return BlockExplodingRecipe.on(level, state, builder, drops);
	}

}
