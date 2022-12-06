package snownee.lychee.util;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonPointer {

	public final List<String> tokens;

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
		if (tokens.isEmpty()) {
			return "";
		}
		return "/" + Joiner.on('/').join(tokens);
	}

	@Nullable
	public JsonElement find(JsonObject object) {
		if (tokens.isEmpty()) {
			return object;
		}
		JsonElement element = object;
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
		return tokens.get(index);
	}

	public int getInt(int index) {
		return Integer.parseInt(getString(index));
	}

}
