package snownee.lychee.mixin.recipes.itemexploding;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.AABB;
import snownee.lychee.recipes.ItemExplodingRecipe;

@Mixin(value = ServerExplosion.class, priority = 700)
public abstract class ServerExplosionMixin implements Explosion {
	@WrapOperation(
			method = "hurtEntities", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
	private List<Entity> lychee_hurtEntities(ServerLevel instance, Entity entity, AABB aabb, Operation<List<Entity>> original) {
		List<Entity> list = original.call(instance, entity, aabb);
		ItemExplodingRecipe.invoke(level(), center(), list, radius());
		return list;
	}
}
