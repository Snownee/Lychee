package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import snownee.lychee.core.def.LocationPredicateHelper;

@Mixin(LocationCheck.Serializer.class)
public class LocationCheckMixin {

	@Inject(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/LocationPredicate;fromJson(Lcom/google/gson/JsonElement;)Lnet/minecraft/advancements/critereon/LocationPredicate;", shift = At.Shift.BY, by = 2
			), method = "deserialize", locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void lychee_deserialize(JsonObject object, JsonDeserializationContext context, CallbackInfoReturnable<LocationCheck> ci, LocationPredicate predicate) {
		if (predicate == null || predicate == LocationPredicate.ANY)
			return;
		object = object.getAsJsonObject("predicate");
		if (object.has("lychee:biome_tag")) {
			ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(object, "lychee:biome_tag"));
			((LocationPredicateHelper) predicate).setBiomeTag(TagKey.create(Registry.BIOME_REGISTRY, id));
		}
	}

	@Inject(at = @At("TAIL"), method = "serialize")
	private void lychee_serialize(JsonObject object, LocationCheck locationCheck, JsonSerializationContext jsonSerializationContext, CallbackInfo ci) {
		if (!object.get("predicate").isJsonNull()) {
			LocationPredicateHelper predicate = (LocationPredicateHelper) ((LocationCheckAccess) locationCheck).getPredicate();
			if (predicate.getBiomeTag() != null) {
				object.getAsJsonObject("predicate").addProperty("lychee:biome_tag", predicate.getBiomeTag().location().toString());
			}
		}
	}

}
