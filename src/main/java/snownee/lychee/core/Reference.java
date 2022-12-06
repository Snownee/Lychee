package snownee.lychee.core;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import snownee.lychee.util.JsonPointer;

public abstract class Reference {

	public static Reference fromJson(JsonObject parent, String key) {
		String value = GsonHelper.getAsString(parent, key, "default");
		if ("default".equals(value)) {
			return DEFAULT;
		}
		if (value.codePointAt(0) == '/') {
			return new Pointer(new JsonPointer(value));
		}
		return new Constant(value);
	}

	public static void toJson(Reference reference, JsonObject parent, String key) {
		if (reference == DEFAULT) {
			return;
		}
		parent.addProperty(key, reference.toString());
	}

	public static Reference fromNetwork(FriendlyByteBuf buf) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void toNetwork(Reference reference, FriendlyByteBuf buf) {

	}

	public boolean isPointer() {
		return getClass() == Pointer.class;
	}

	public JsonPointer getPointer() {
		return ((Pointer) this).pointer;
	}

	public static final Reference DEFAULT = new Constant("default");

	public static class Pointer extends Reference {
		private final JsonPointer pointer;

		public Pointer(JsonPointer pointer) {
			this.pointer = pointer;
		}
	}

	public static class Constant extends Reference {
		public final String name;

		public Constant(String name) {
			this.name = name;
		}
	}

}
