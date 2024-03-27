package snownee.lychee.util.contextual;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
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
import snownee.lychee.util.SerializableType;

public interface ContextualConditionType<T extends ContextualCondition> extends SerializableType<T> {
	ContextualConditionType<And> AND = register("and", new And.Type());
	ContextualConditionType<Or> OR = register("or", new Or.Type());
	ContextualConditionType<Not> NOT = register("not", new Not.Type());

	ContextualConditionType<Chance> CHANCE = register("chance", new Chance.Type());

	ContextualConditionType<Location> LOCATION = register("location", new Location.Type());
	ContextualConditionType<IsDifficulty> DIFFICULTY =
			register("difficulty", new IsDifficulty.Type());
	ContextualConditionType<IsWeather> WEATHER = register("weather", new IsWeather.Type());

	ContextualConditionType<Time> TIME = register("time", new Time.Type());
	ContextualConditionType<Execute> EXECUTE = register("execute", new Execute.Type());
	ContextualConditionType<FallDistance> FALL_DISTANCE =
			register("fall_distance", new FallDistance.Type());
	ContextualConditionType<EntityHealth> ENTITY_HEALTH =
			register("entity_health", new EntityHealth.Type());
	ContextualConditionType<IsSneaking> IS_SNEAKING =
			register("is_sneaking", new IsSneaking.Type());
	ContextualConditionType<DirectionCheck> DIRECTION =
			register("direction", new DirectionCheck.Type());
	ContextualConditionType<CheckParam> CHECK_PARAM =
			register("check_param", new CheckParam.Type());
	ContextualConditionType<CustomCondition> CUSTOM =
			register("custom", new CustomCondition.Type());


	static <T extends ContextualConditionType<?>> T register(String name, T object) {
		return register(new ResourceLocation(name), object);
	}

	static <T extends ContextualConditionType<?>> T register(ResourceLocation location, T object) {
		Registry.register(LycheeRegistries.CONTEXTUAL, location, object);
		return object;
	}
}
