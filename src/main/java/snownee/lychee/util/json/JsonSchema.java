package snownee.lychee.util.json;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonSchema {

	public static abstract class Node {
		@Nullable
		public JsonElement override;

		@Nullable
		public final JsonElement build() {
			if (override != null) {
				return override.deepCopy();
			}
			return buildInternal();
		}

		@Nullable
		protected abstract JsonElement buildInternal();

		public void walk(Deque<String> path, Consumer<Node> consumer, BooleanSupplier pathAcceptor) {
			consumer.accept(this);
		}

		public ObjectNode asObject() {
			throw new UnsupportedOperationException();
		}

		public ArrayNode asArray() {
			throw new UnsupportedOperationException();
		}
	}

	public static class Anchor extends Node {
		public final String type;
		@Nullable
		public String id;

		public Anchor(String type) {
			this(type, null);
		}

		public Anchor(String type, String id) {
			this.type = type;
			this.id = id;
		}

		@Override
		protected @Nullable JsonElement buildInternal() {
			return new JsonPrimitive(toString());
		}

		@Override
		public String toString() {
			return "anchor:" + type + "." + id;
		}
	}

	public static class ObjectNode extends Node {
		private final Map<String, Node> members = Maps.newLinkedHashMap();

		@Override
		protected @Nullable JsonElement buildInternal() {
			JsonObject jsonObject = new JsonObject();
			members.forEach((k, v) -> {
				JsonElement e = v.build();
				if (e != null) {
					jsonObject.add(k, e);
				}
			});
			return jsonObject;
		}

		@Override
		public void walk(Deque<String> path, Consumer<Node> consumer, BooleanSupplier pathAcceptor) {
			consumer.accept(this);
			members.entrySet().removeIf(entry -> {
				path.addLast(entry.getKey());
				if (pathAcceptor.getAsBoolean()) {
					entry.getValue().walk(path, consumer, pathAcceptor);
					path.removeLast();
					return false;
				} else {
					path.removeLast();
					return true;
				}
			});
		}

		@Override
		public ObjectNode asObject() {
			return (ObjectNode) this;
		}

		public <T extends Node> T add(String key, T node) {
			members.put(key, node);
			return node;
		}

		public boolean has(String key) {
			return members.containsKey(key);
		}

		public Node get(String key) {
			return members.get(key);
		}
	}

	public static class ArrayNode extends Node {
		private final List<Node> members = Lists.newArrayList();

		@Override
		protected @Nullable JsonElement buildInternal() {
			JsonArray jsonArray = new JsonArray();
			members.forEach(v -> {
				if (v == null) {
					jsonArray.add(JsonNull.INSTANCE);
				} else {
					jsonArray.add(v.build());
				}
			});
			return jsonArray;
		}

		@Override
		public void walk(Deque<String> path, Consumer<Node> consumer, BooleanSupplier pathAcceptor) {
			consumer.accept(this);
			MutableInt i = new MutableInt();
			members.replaceAll(v -> {
				if (v == null) {
					i.increment();
					return null;
				}
				path.addLast(i.toString());
				i.increment();
				if (pathAcceptor.getAsBoolean()) {
					v.walk(path, consumer, pathAcceptor);
				} else {
					v = null;
				}
				path.removeLast();
				return v;
			});
		}

		@Override
		public ArrayNode asArray() {
			return (ArrayNode) this;
		}

		public void add(Node node) {
			members.add(node);
		}

		public int maxIndex() {
			return members.size();
		}

		public @Nullable Node get(int index) {
			if (index > maxIndex())
				return null;
			return members.get(index);
		}

		public @Nullable Node set(int index, Node node) {
			while (index > maxIndex()) {
				add(null);
			}
			members.set(index, node);
			return node;
		}
	}

	public final ObjectNode root = new ObjectNode();
	private final Map<JsonPointer, Anchor> anchors = Maps.newHashMap();

	public Map<JsonPointer, Anchor> anchors() {
		return Map.copyOf(anchors);
	}

	public JsonObject buildSkeleton() {
		return root.build().getAsJsonObject();
	}

	@Override
	public String toString() {
		return buildSkeleton().toString();
	}

	public void bakeAnchors() {
		anchors.clear();
		Deque<String> path = new ArrayDeque<>();
		root.walk(path, node -> {
			if (node instanceof Anchor anchor) {
				anchors.put(new JsonPointer(path), anchor);
			}
		}, () -> true);
	}

	public void strip(Set<JsonPointer> pointers) {
		anchors.clear();
		LinkedList<String> path = Lists.newLinkedList();
		root.walk(path, node -> {
		}, () -> pointers.stream().anyMatch(pointer -> pointer.isSelfOrParentOf(path)));
	}

	public Node getOrCreateByPointer(JsonPointer pointer, JsonObject jsonObject) {
		Node node = root;
		JsonElement element = jsonObject;
		for (String token : pointer.tokens) {
			if (element.isJsonObject()) {
				element = element.getAsJsonObject().get(token);
				if (!node.asObject().has(token)) {
					node = node.asObject().add(token, element.isJsonObject() ? new ObjectNode() : new ArrayNode());
				} else {
					node = node.asObject().get(token);
				}
			} else if (element.isJsonArray()) {
				int index = Integer.parseInt(token);
				element = element.getAsJsonArray().get(index);
				if (node.asArray().get(index) == null) {
					node = node.asArray().set(index, element.isJsonObject() ? new ObjectNode() : new ArrayNode());
				} else {
					node = node.asArray().get(index);
				}
			} else {
				throw new IllegalArgumentException();
			}
		}
		return node;
	}

	/**
	 * BiFunction<Integer, Boolean, Node> index, is object, returns node
	 */
	public void buildObjectOrList(JsonObject jsonObject, JsonPointer pointer, BiFunction<Integer, Boolean, Node> nodeCreator) {
		Preconditions.checkArgument(!pointer.isRoot(), "Cannot build root");
		JsonElement e = pointer.find(jsonObject);
		if (e == null) {
			return;
		}
		JsonPointer parentPointer = pointer.parent();
		JsonElement parent = parentPointer.find(jsonObject);
		Node parentNode = getOrCreateByPointer(parentPointer, jsonObject);
		Consumer<Node> consumer;
		if (parent.isJsonObject()) {
			consumer = $ -> parentNode.asObject().add(pointer.getString(-1), $);
		} else {
			consumer = $ -> parentNode.asArray().set(pointer.getInt(-1), $);
		}
		if (e.isJsonObject()) {
			Node node = nodeCreator.apply(0, true);
			if (node != null) {
				consumer.accept(node);
			}
		} else if (e.isJsonArray()) {
			ArrayNode arrayNode = new ArrayNode();
			int size = e.getAsJsonArray().size();
			for (int i = 0; i < size; i++) {
				arrayNode.add(nodeCreator.apply(i, false));
			}
			consumer.accept(arrayNode);
		}
	}

}
