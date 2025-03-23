package snownee.lychee.util;

import org.joml.Vector2i;
import org.joml.Vector2ic;

public class VectorExtensions {
	public static Vector2ic offset(Vector2ic vector, int x, int y) {
		return new Vector2i(vector.x() + x, vector.y() + y);
	}
}
