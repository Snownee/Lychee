package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

@Mixin(LootContextParamSets.class)
public class LootContextParamSetsMixin {
	@WrapOperation(
			method = "register",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resources/Identifier;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/Identifier;"))
	private static Identifier lychee$allowOtherNamespace(final String path, final Operation<Identifier> original) {
		if (path.indexOf(Identifier.NAMESPACE_SEPARATOR) == -1) {
			// Avoid hiding the other mixins
			return original.call(path);
		}
		return Identifier.parse(path);
	}
}
