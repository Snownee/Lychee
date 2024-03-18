package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.serialization.Codec;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public interface ItemStackAccess {
	@Accessor("ITEM_NON_AIR_CODEC")
	static Codec<Holder<Item>> getItemNonAirCodec() {
		throw new IllegalStateException();
	}
}
