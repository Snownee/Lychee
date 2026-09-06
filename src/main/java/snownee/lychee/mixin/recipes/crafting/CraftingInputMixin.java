package snownee.lychee.mixin.recipes.crafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.item.crafting.CraftingInput;
import snownee.lychee.util.LycheeCraftingInput;

@Mixin(CraftingInput.class)
public class CraftingInputMixin implements LycheeCraftingInput {
	@Unique
	private int lychee$hash;

	@Override
	public int lychee$hash() {
		return lychee$hash;
	}

	@Override
	public void lychee$setHash(int hash) {
		this.lychee$hash = hash;
	}
}
