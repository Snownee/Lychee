package snownee.lychee.util;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

public class GsonContextImpl implements JsonSerializationContext, JsonDeserializationContext {

	private Gson gson;

	public GsonContextImpl(Gson gson) {
		this.gson = gson;
	}

	@Override
	public JsonElement serialize(Object src) {
		return gson.toJsonTree(src);
	}

	@Override
	public JsonElement serialize(Object src, Type typeOfSrc) {
		return gson.toJsonTree(src, typeOfSrc);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
		return (R) gson.fromJson(json, typeOfT);
	}

}
