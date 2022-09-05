package snownee.lychee.mixin;

import com.google.gson.JsonDeserializationContext;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;

import net.minecraft.world.level.storage.loot.predicates.LocationCheck;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import snownee.lychee.core.def.LocationPredicateHelper;

@Mixin(LocationCheck.Serializer.class)
public class LocationCheckMixin {

	@Inject(at = @At(
			value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/LocationPredicate;fromJson(Lcom/google/gson/JsonElement;)Lnet/minecraft/advancements/critereon/LocationPredicate;", shift = At.Shift.BY, by = 2
	), method = "deserialize", locals = LocalCapture.CAPTURE_FAILHARD)
	private void lychee_deserialize(JsonObject object, JsonDeserializationContext context, CallbackInfoReturnable<LocationCheck> ci, LocationPredicate predicate) {
		if (predicate == null || predicate == LocationPredicate.ANY)
			return;
		object = object.getAsJsonObject("predicate");
		if (object.has("lychee:biome_tag")) {
			ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(object, "lychee:biome_tag"));
			((LocationPredicateHelper) predicate).setBiomeTag(TagKey.create(Registry.BIOME_REGISTRY, id));
		}
	}

}
