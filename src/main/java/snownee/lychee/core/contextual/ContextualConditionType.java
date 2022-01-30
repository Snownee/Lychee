package snownee.lychee.core.contextual;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.LycheeRegistries;

public abstract class ContextualConditionType<T extends ContextualCondition> {

	public abstract T fromJson(JsonObject o);

	public abstract T fromNetwork(FriendlyByteBuf buf);

	public abstract void toNetwork(T condition, FriendlyByteBuf buf);

	public ResourceLocation getRegistryName() {
		return LycheeRegistries.CONTEXTUAL.getKey(this);
	}

}
