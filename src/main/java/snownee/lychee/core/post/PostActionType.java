package snownee.lychee.core.post;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;

public abstract class PostActionType<T extends PostAction> {

	public abstract T fromJson(JsonObject o);

	public abstract T fromNetwork(FriendlyByteBuf buf);

	public abstract void toNetwork(T action, FriendlyByteBuf buf);

}
