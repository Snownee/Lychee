package snownee.lychee.util;

import com.mojang.serialization.Codec;

import snownee.lychee.util.json.JsonPointer;

public abstract class Reference {
	public static final Codec<Reference> CODEC = Codec.STRING.xmap(Reference::create, Object::toString);

	public static Reference create(String value) {
		if ("default".equals(value)) {
			return DEFAULT;
		}
		if (value.codePointAt(0) == '/') {
			return new Pointer(new JsonPointer(value));
		}
		return new Constant(value);
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

		@Override
		public String toString() {
			return pointer.toString();
		}
	}

	public static class Constant extends Reference {
		public final String name;

		public Constant(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

}
