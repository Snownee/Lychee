package snownee.lychee.util.json;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class JsonPatch {

	/* off */
	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(JsonPointer.class, new JsonPointer.Serializer())
			.create();
	/* on */

	public enum Type {
		add, remove, replace, copy, move, test, merge, deep_merge
	}

	public final Type op;
	public JsonPointer path;
	@Nullable
	public JsonPointer from;
	@Nullable
	public JsonElement value;

	public JsonPatch(Type op, JsonPointer path, @Nullable JsonPointer from, @Nullable JsonElement value) {
		this.op = op;
		this.path = path;
		this.from = from;
		this.value = value;
	}

	public static JsonPatch parse(JsonObject jsonObject) {
		try {
			JsonPatch patch = GSON.fromJson(jsonObject, JsonPatch.class);
			if (patch.op != null && patch.path != null) {
				return patch;
			}
		} catch (Exception e) {
		}
		return null;
	}

	public JsonElement apply(JsonElement doc) {
		if (op == Type.add) {
			return add(doc, path, value.deepCopy());
		} else if (op == Type.remove) {
			return remove(doc, path);
		} else if (op == Type.replace) {
			return replace(doc, path, value.deepCopy());
		} else if (op == Type.copy) {
			return copy(doc, path, from);
		} else if (op == Type.move) {
			return move(doc, path, from);
		} else if (op == Type.test) {
			return test(doc, path, value);
		} else if (op == Type.merge) {
			return merge(doc, path, value.deepCopy());
		} else if (op == Type.deep_merge) {
			return deepMerge(doc, path, value.deepCopy());
		}
		throw new IllegalArgumentException("Invalid op: " + op);
	}

	public JsonObject toJson() {
		return (JsonObject) GSON.toJsonTree(this, JsonPatch.class);
	}

	public static JsonElement add(JsonElement doc, JsonPointer path, JsonElement value) {
		if (path.isRoot()) {
			return value;
		}
		JsonElement parent = path.parent().find(doc);
		Preconditions.checkNotNull(parent, "Invalid path: " + path);
		String last = path.getString(-1);
		if (parent.isJsonObject()) {
			parent.getAsJsonObject().add(last, value);
		} else if (parent.isJsonArray()) {
			if ("-".equals(last)) {
				parent.getAsJsonArray().add(value);
			} else {
				JsonArray array = parent.getAsJsonArray();
				int size = array.size();
				int after = size - Integer.parseInt(last);
				array.add(JsonNull.INSTANCE);
				for (int i = 0; i < after; i++) {
					array.set(size - i, array.get(size - i - 1));
				}
				array.set(size - after, value);
			}
		} else {
			throw new IllegalArgumentException("Invalid path: " + path);
		}
		return doc;
	}

	public static JsonElement remove(JsonElement doc, JsonPointer path) {
		if (path.isRoot()) {
			if (doc.isJsonObject()) {
				doc = new JsonObject();
			} else if (doc.isJsonArray()) {
				doc = new JsonArray();
			}
			return doc;
		}
		JsonElement parent = path.parent().find(doc);
		Preconditions.checkNotNull(parent, "Invalid path: " + path);
		String last = path.getString(-1);
		if (parent.isJsonObject()) {
			Preconditions.checkArgument(parent.getAsJsonObject().has(last), last);
			parent.getAsJsonObject().remove(last);
		} else if (parent.isJsonArray()) {
			JsonArray array = parent.getAsJsonArray();
			if ("-".equals(last)) {
				array.remove(array.size() - 1);
			} else {
				array.remove(Integer.parseInt(last));
			}
		} else {
			throw new IllegalArgumentException("Invalid path: " + path);
		}
		return doc;
	}

	public static JsonElement replace(JsonElement doc, JsonPointer path, JsonElement value) {
		doc = remove(doc, path);
		return add(doc, path, value);
	}

	public static JsonElement move(JsonElement doc, JsonPointer path, JsonPointer from) {
		Preconditions.checkNotNull(from, "from");
		JsonElement value = from.find(doc);
		doc = remove(doc, from);
		return add(doc, path, value);
	}

	public static JsonElement copy(JsonElement doc, JsonPointer path, JsonPointer from) {
		Preconditions.checkNotNull(from, "from");
		JsonElement value = from.find(doc);
		return add(doc, path, value.deepCopy());
	}

	public static JsonElement test(JsonElement doc, JsonPointer path, JsonElement value) {
		Preconditions.checkNotNull(value, "value");
		JsonElement target = path.find(doc);
		Preconditions.checkArgument(Objects.equals(value, target), "Invalid value: " + target);
		return doc;
	}

	public static JsonElement merge(JsonElement doc, JsonPointer path, JsonElement value) {
		Preconditions.checkNotNull(value, "value");
		Preconditions.checkArgument(value.isJsonObject(), "Invalid value: " + value);
		JsonElement target = path.find(doc);
		Preconditions.checkArgument(target != null && target.isJsonObject(), "Invalid path: " + path);
		JsonObject targetObject = target.getAsJsonObject();
		JsonObject valueObject = value.getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : valueObject.entrySet()) {
			targetObject.add(entry.getKey(), entry.getValue());
		}
		return doc;
	}

	public static JsonElement deepMerge(JsonElement doc, JsonPointer path, JsonElement value) {
		Preconditions.checkNotNull(value, "value");
		JsonElement target = path.find(doc);
		Preconditions.checkNotNull(target, "Invalid path: " + path);
		ArrayDeque<String> tokens = new ArrayDeque<>(path.tokens);
		deepMergeInternal(tokens, target, value);
		return doc;
	}

	private static JsonElement deepMergeInternal(ArrayDeque<String> tokens, JsonElement target, JsonElement value) {
		if (target.getClass() != value.getClass()) {
			return value;
		}
		if (target.isJsonObject()) {
			JsonObject targetObject = target.getAsJsonObject();
			JsonObject valueObject = value.getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : valueObject.entrySet()) {
				String key = entry.getKey();
				if (targetObject.has(key)) {
					tokens.addLast(key);
					targetObject.add(key, deepMergeInternal(tokens, targetObject.get(key), entry.getValue()));
					tokens.removeLast();
				} else {
					targetObject.add(key, entry.getValue());
				}
			}
		} else if (target.isJsonArray()) {
			JsonArray targetArray = target.getAsJsonArray();
			JsonArray valueArray = value.getAsJsonArray();
			int size = targetArray.size();
			for (int i = 0; i < size; i++) {
				if (i < valueArray.size()) {
					tokens.addLast(String.valueOf(i));
					targetArray.set(i, deepMergeInternal(tokens, targetArray.get(i), valueArray.get(i)));
					tokens.removeLast();
				} else {
					valueArray.add(targetArray.get(i));
				}
			}
		}
		return value;
	}
}
