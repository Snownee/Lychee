package snownee.lychee.recipes;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.context.LootParamsContext;
import snownee.lychee.util.LycheeEntity;
import snownee.lychee.util.LycheeEntityType;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.LycheeRecipeType;

@NotNullByDefault
public class EntityTickingRecipeType extends LycheeRecipeType<EntityTickingRecipe> {
	private static final Set<LycheeContextKey<?>> PRESERVED_KEYS = Set.of(LycheeContextKey.LEVEL, LycheeContextKey.LOOT_PARAMS);

	public EntityTickingRecipeType(String name, Class<EntityTickingRecipe> clazz, @Nullable LootContextParamSet contextParamSet) {
		super(name, clazz, contextParamSet);
	}

	@Override
	public void refreshCache() {
		super.refreshCache();
		ListMultimap<EntityType<?>, RecipeHolder<EntityTickingRecipe>> map = ArrayListMultimap.create();
		for (RecipeHolder<EntityTickingRecipe> recipe : recipes) {
			recipe.value()
					.predicate()
					.entityType()
					.map($ -> $.types().stream())
					.orElseGet(Stream::empty)
					.forEach(type -> map.put(type.value(), recipe));
		}
		for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
			((LycheeEntityType) type).lychee$setTickingRecipes(ImmutableList.copyOf(map.get(type)));
		}
	}

	public void process(Entity entity, List<RecipeHolder<EntityTickingRecipe>> recipes) {
		if (recipes.isEmpty() || entity.level().isClientSide) {
			return;
		}
		ServerLevel level = (ServerLevel) entity.level();
		LycheeEntity lycheeEntity = (LycheeEntity) entity;
		LycheeContext context = lycheeEntity.lychee$getContext();
		if (context == null) {
			context = new LycheeContext();
			context.put(LycheeContextKey.LEVEL, level);
			lycheeEntity.lychee$setContext(context);
		}
		LootParamsContext lootParams = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParams.setParam(LootContextParams.THIS_ENTITY, entity);
		lootParams.setParam(LootContextParams.ORIGIN, entity.position());
		for (RecipeHolder<EntityTickingRecipe> recipeHolder : recipes) {
			EntityTickingRecipe recipe = recipeHolder.value();
			if (recipe.interval() > 1 && (entity.tickCount + 1) % recipe.interval() != 0) {
				continue;
			}
			if (recipe.withoutTypePredicate().matches(level, entity.position(), entity) && recipe.test(recipe, context, 1) > 0 &&
					recipe.matches(context, level)) {
				context.put(recipeHolder);
				recipe.applyPostActions(context, 1);
				boolean avoidDefault = context.get(LycheeContextKey.ACTION).avoidDefault;
				context.removeAllExcept(PRESERVED_KEYS);
				if (avoidDefault) {
					break;
				}
			}
		}
	}
}
