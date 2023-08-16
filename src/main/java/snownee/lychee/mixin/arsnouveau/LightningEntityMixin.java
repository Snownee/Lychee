package snownee.lychee.mixin.arsnouveau;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.collect.Sets;
import com.hollingsworth.arsnouveau.common.entity.LightningEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import snownee.lychee.RecipeTypes;
import snownee.lychee.lightning_channeling.LightningChannelingRecipe;

@Mixin(LightningEntity.class)
public class LightningEntityMixin {

	@Shadow(remap = false)
	private int lightningState;
	private final Set<Entity> lychee$hitItems = Sets.newHashSet();

	@Inject(at = @At("TAIL"), method = "tick")
	private void lychee_tick(CallbackInfo ci) {
		if (!RecipeTypes.LIGHTNING_CHANNELING.isEmpty() && lightningState == 0 && !lychee$hitItems.isEmpty()) {
			LightningBolt entity = (LightningBolt) (Object) this;
			LightningChannelingRecipe.on(entity, List.copyOf(lychee$hitItems));
		}
	}

	@Inject(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;thunderHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LightningBolt;)V"
			), method = "tick", locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private void lychee_onThunderHit(CallbackInfo ci, List<Entity> list, Iterator<Entity> var2, Entity entity) {
		if (!RecipeTypes.LIGHTNING_CHANNELING.isEmpty() && entity instanceof ItemEntity) {
			lychee$hitItems.add(entity);
		}
	}

}
