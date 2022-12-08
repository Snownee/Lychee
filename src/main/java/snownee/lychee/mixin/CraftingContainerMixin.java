package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.inventory.CraftingContainer;
import snownee.lychee.crafting.CraftingContext;
import snownee.lychee.crafting.LycheeCraftingContainer;

@Mixin(CraftingContainer.class)
public class CraftingContainerMixin implements LycheeCraftingContainer {

	private CraftingContext lychee$ctx;

	@Override
	public void lychee$setContext(CraftingContext ctx) {
		lychee$ctx = ctx;
	}

	@Override
	public CraftingContext lychee$getContext() {
		return lychee$ctx;
	}

}
