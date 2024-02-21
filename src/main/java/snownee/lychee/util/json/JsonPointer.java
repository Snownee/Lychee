package snownee.lychee.util.json;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonPointer {

	public final List<String> tokens;

	public JsonPointer(Collection<String> tokens) {
		this.tokens = List.copyOf(tokens);
	}

	public JsonPointer(String path) {
		if (path.isEmpty()) {
			tokens = List.of();
		} else if (path.codePointAt(0) == '/') {
			tokens = List.of(path.substring(1).split("/"));
		} else {
			throw new IllegalArgumentException(path);
		}
	}

	@Override
	public String toString() {
		if (isRoot()) {
			return "";
		}
		return "/" + Joiner.on('/').join(tokens);
	}

	@Nullable
	public JsonElement find(JsonElement doc) {
		if (isRoot()) {
			return doc;
		}
		JsonElement element = doc;
		try {
			for (String token : tokens) {
				if (element.isJsonArray()) {
					element = element.getAsJsonArray().get(Integer.parseInt(token));
				} else if (element.isJsonObject()) {
					element = element.getAsJsonObject().get(token);
				} else {
					throw new IllegalArgumentException();
				}
			}
		} catch (Exception e) {
			return null;
		}
		return element;
	}

	public int size() {
		return tokens.size();
	}

	public String getString(int index) {
		if (index < 0) {
			index = tokens.size() + index;
		}
		return tokens.get(index);
	}

	public int getInt(int index) {
		return Integer.parseInt(getString(index));
	}

	public boolean isRoot() {
		return tokens.isEmpty();
	}

	public JsonPointer parent() {
		List<String> list = Lists.newArrayList(tokens);
		list.remove(list.size() - 1);
		return new JsonPointer(list);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof JsonPointer) {
			return tokens.equals(((JsonPointer) obj).tokens);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(tokens);
	}

	public JsonPointer append(String token) {
		return new JsonPointer(this + "/" + token);
	}

	public static class Serializer implements JsonDeserializer<JsonPointer>, JsonSerializer<JsonPointer> {

		@Override
		public JsonElement serialize(JsonPointer src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.toString());
		}

		@Override
		public JsonPointer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
				JsonParseException {
			return new JsonPointer(json.getAsString());
		}

	}

	public boolean isSelfOrParentOf(List<String> tokens1) {
		if (tokens.size() < tokens1.size()) {
			return false;
		}
		for (int i = 0; i < tokens1.size(); i++) {
			if (!Objects.equals(tokens.get(i), tokens1.get(i))) {
				return false;
			}
		}
		return true;
	}

}
