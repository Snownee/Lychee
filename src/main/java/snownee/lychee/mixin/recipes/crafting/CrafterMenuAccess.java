package snownee.lychee.mixin.recipes.crafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CrafterMenu;

@Mixin(CrafterMenu.class)
public interface CrafterMenuAccess {
	@Accessor
	Player getPlayer();
}
