package snownee.lychee.core;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import net.minecraft.network.FriendlyByteBuf;

public record ContextTarget(Object value) {

	public static final Map<Object, ContextTarget> ALL = Maps.newConcurrentMap();
	public static final ContextTarget DEFAULT = of(1);

	public static ContextTarget of(Object value) {
		return ALL.computeIfAbsent(value, ContextTarget::new);
	}

	public static ContextTarget fromJson(@Nullable JsonElement element) {
		if (element == null || element.isJsonNull()) {
			return DEFAULT;
		}
		if (element.isJsonPrimitive()) {
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			if (primitive.isString()) {
				return of(primitive.getAsString());
			} else if (primitive.isNumber()) {
				return of(primitive.getAsInt());
			}
		}
		throw new JsonSyntaxException(element.toString());
	}

	public static ContextTarget fromNetwork(FriendlyByteBuf buf) {
		if (buf.readBoolean()) {
			return of(buf.readUtf());
		} else {
			return of(buf.readVarInt());
		}
	}

	public void toNetwork(FriendlyByteBuf buf) {
		if (value instanceof String) {
			buf.writeBoolean(true);
			buf.writeUtf((String) value);
		} else {
			buf.writeBoolean(false);
			buf.writeVarInt((Integer) value);
		}
	}

}
