package snownee.lychee.util.action;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.entity.Marker;

public interface ActionMarker {
	void lychee$setData(ActionData data);

	@Nullable
	ActionData lychee$getData();

	default Marker lychee$self() {
		return (Marker) this;
	}
}
