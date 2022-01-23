package snownee.lychee.core.post;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class PostActionType<T extends PostAction> extends ForgeRegistryEntry<PostActionType<?>> {

	public abstract T fromJson(JsonObject o);

	public abstract T fromNetwork(FriendlyByteBuf buf);

	public abstract void toNetwork(T action, FriendlyByteBuf buf);

	public boolean canBatchRun() {
		return true;
	}

}
