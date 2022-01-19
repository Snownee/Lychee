package snownee.lychee.core.contextual;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class ContextualConditionType<T extends ContextualCondition> extends ForgeRegistryEntry<ContextualConditionType<?>> {

	public abstract T fromJson(JsonObject o);

	public abstract T fromNetwork(FriendlyByteBuf buf);

	public abstract void toNetwork(T condition, FriendlyByteBuf buf);

}
