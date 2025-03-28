package snownee.lychee.util;

import org.joml.Vector2i;
import org.joml.Vector2ic;

public class VectorExtensions {
	public static final Vector2ic ZERO = new Vector2i(0, 0);

	public static Vector2ic offset(Vector2ic vector, int x, int y) {
		return new Vector2i(vector.x() + x, vector.y() + y);
	}
	public static Vector2ic offset(Vector2ic vector, Vector2ic other) {
		return new Vector2i(vector.x() + other.x(), vector.y() + other.y());
	}

	public static Vector2ic withX(Vector2ic vector, int x) {
		return new Vector2i(x, vector.y());
	}
}
