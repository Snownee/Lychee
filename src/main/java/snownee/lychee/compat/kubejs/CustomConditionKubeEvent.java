package snownee.lychee.compat.kubejs;

import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;

import com.google.gson.JsonObject;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.Tristate;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.contextual.CustomCondition;
import snownee.lychee.util.contextual.ContextualPredicate;

public class CustomConditionKubeEvent implements KubeEvent {

	public final String id;
	public final CustomCondition condition;
	public final JsonObject data;

	public CustomConditionKubeEvent(String id, CustomCondition condition) {
		this.id = id;
		this.condition = condition;
		this.data = condition.data;
	}

	public void setTestFunc(ContextualPredicate func) {
		condition.testFunc = func;
	}

	public void setTestInTooltipsFunc(BiFunction<Level, @Nullable Player, Object> func) {
		condition.testInTooltipsFunc = (level, player) -> switch (Tristate.wrap(func.apply(level, player))) {
			case TRUE -> TriState.TRUE;
			case FALSE -> TriState.FALSE;
			case DEFAULT -> TriState.DEFAULT;
		};
	}

}
