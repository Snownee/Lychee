package snownee.lychee.util.contextual;

import net.minecraft.core.Registry;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.contextual.And;
import snownee.lychee.contextual.Chance;
import snownee.lychee.contextual.CheckParam;
import snownee.lychee.contextual.CustomCondition;
import snownee.lychee.contextual.DirectionCheck;
import snownee.lychee.contextual.EntityHealth;
import snownee.lychee.contextual.Execute;
import snownee.lychee.contextual.FallDistance;
import snownee.lychee.contextual.IsDifficulty;
import snownee.lychee.contextual.IsSneaking;
import snownee.lychee.contextual.IsWeather;
import snownee.lychee.contextual.Location;
import snownee.lychee.contextual.Not;
import snownee.lychee.contextual.Or;
import snownee.lychee.contextual.Time;

public final class ContextualConditionTypes {
	public static final ContextualConditionType<And> AND = register("and", new And.Type());
	public static final ContextualConditionType<Or> OR = register("or", new Or.Type());
	public static final ContextualConditionType<Not> NOT = register("not", new Not.Type());

	public static final ContextualConditionType<Chance> CHANCE = register("chance", new Chance.Type());

	public static final ContextualConditionType<Location> LOCATION = register("location", new Location.Type());
	public static final ContextualConditionType<IsDifficulty> DIFFICULTY =
			register("difficulty", new IsDifficulty.Type());
	public static final ContextualConditionType<IsWeather> WEATHER = register("weather", new IsWeather.Type());

	public static final ContextualConditionType<Time> TIME = register("time", new Time.Type());
	public static final ContextualConditionType<Execute> EXECUTE = register("execute", new Execute.Type());
	public static final ContextualConditionType<FallDistance> FALL_DISTANCE =
			register("fall_distance", new FallDistance.Type());
	public static final ContextualConditionType<EntityHealth> ENTITY_HEALTH =
			register("entity_health", new EntityHealth.Type());
	public static final ContextualConditionType<IsSneaking> IS_SNEAKING =
			register("is_sneaking", new IsSneaking.Type());
	public static final ContextualConditionType<DirectionCheck> DIRECTION =
			register("direction", new DirectionCheck.Type());
	public static final ContextualConditionType<CheckParam> CHECK_PARAM =
			register("check_param", new CheckParam.Type());
	public static final ContextualConditionType<CustomCondition> CUSTOM =
			register("custom", new CustomCondition.Type());

	public static <T extends ContextualConditionType<?>> T register(String name, T object) {
		Registry.register(LycheeRegistries.CONTEXTUAL, name, object);
		return object;
	}
}
