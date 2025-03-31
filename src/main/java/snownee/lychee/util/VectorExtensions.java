package snownee.lychee.util;

import org.joml.Vector2f;
import org.joml.Vector2fc;

public class VectorExtensions {
	public static final Vector2fc ZERO = new Vector2f();

	public static Vector2fc offset(Vector2fc vector, float x, float y) {
		return new Vector2f(vector.x() + x, vector.y() + y);
	}

	public static Vector2fc offset(Vector2fc vector, Vector2f other) {
		return new Vector2f(vector.x() + other.x(), vector.y() + other.y());
	}

	public static Vector2fc offsetX(Vector2fc vector, float x) {
		return new Vector2f(vector.x() + x, vector.y());
	}

	public static Vector2f withX(Vector2fc vector, float x) {
		return new Vector2f(x, vector.y());
	}
}
