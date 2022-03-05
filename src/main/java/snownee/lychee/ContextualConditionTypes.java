package snownee.lychee;

import net.minecraft.core.Registry;
import snownee.lychee.core.contextual.And;
import snownee.lychee.core.contextual.Chance;
import snownee.lychee.core.contextual.ContextualConditionType;
import snownee.lychee.core.contextual.Execute;
import snownee.lychee.core.contextual.IsDifficulty;
import snownee.lychee.core.contextual.IsWeather;
import snownee.lychee.core.contextual.Location;
import snownee.lychee.core.contextual.Not;
import snownee.lychee.core.contextual.Or;
import snownee.lychee.core.contextual.Time;

public final class ContextualConditionTypes {

	public static void init() {
	}

	public static final ContextualConditionType<Chance> CHANCE = register("chance", new Chance.Type());
	public static final ContextualConditionType<Location> LOCATION = register("location", new Location.Type());
	public static final ContextualConditionType<IsDifficulty> DIFFICULTY = register("difficulty", new IsDifficulty.Type());
	public static final ContextualConditionType<IsWeather> WEATHER = register("weather", new IsWeather.Type());
	public static final ContextualConditionType<Not> NOT = register("not", new Not.Type());
	public static final ContextualConditionType<Or> OR = register("or", new Or.Type());
	public static final ContextualConditionType<And> AND = register("and", new And.Type());
	public static final ContextualConditionType<Time> TIME = register("time", new Time.Type());
	public static final ContextualConditionType<Execute> EXECUTE = register("execute", new Execute.Type());

	public static <T extends ContextualConditionType<?>> T register(String name, T t) {
		Registry.register(LycheeRegistries.CONTEXTUAL, name, t);
		return t;
	}

}
