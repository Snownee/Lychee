package snownee.lychee.core.def;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import snownee.lychee.mixin.StatePropertiesPredicateAccess;

public class PropertiesPredicateHelper {

	@Nullable
	public static StatePropertiesPredicate.PropertyMatcher findMatcher(StatePropertiesPredicate predicate, String name) {
		return ((StatePropertiesPredicateAccess) predicate).getProperties().stream().filter($ -> $.getName().equals(name)).findAny().orElse(null);
	}

	public static StatePropertiesPredicate fromNetwork(FriendlyByteBuf pBuffer) {
		if (pBuffer.readBoolean()) {
			return StatePropertiesPredicate.ANY;
		}
		CompoundTag compoundtag = pBuffer.readAnySizeNbt();
		JsonElement json = NbtOps.INSTANCE.convertTo(JsonOps.COMPRESSED, compoundtag);
		return StatePropertiesPredicate.fromJson(json);
	}

	public static void toNetwork(StatePropertiesPredicate predicate, FriendlyByteBuf pBuffer) {
		if (predicate == StatePropertiesPredicate.ANY) {
			pBuffer.writeBoolean(true);
			return;
		}
		pBuffer.writeBoolean(false);
		Tag tag = JsonOps.COMPRESSED.convertTo(NbtOps.INSTANCE, predicate.serializeToJson());
		pBuffer.writeNbt((CompoundTag) tag);

	}

}
