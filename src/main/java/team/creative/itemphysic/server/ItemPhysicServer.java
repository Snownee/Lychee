package team.creative.itemphysic.server;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;

public class ItemPhysicServer {

	public static boolean hurt(ItemEntity item, DamageSource source, float amount) {
		item.discard();
		return false;
	}

}
