package snownee.lychee.recipes;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.EntityPredicateExtensions;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeType;

@NotNullByDefault
public class EntityTickingRecipe extends LycheeRecipe<LycheeContext> {
	public final EntityPredicate predicate;
	public final EntityPredicate withoutTypePredicate;

	public EntityTickingRecipe(LycheeRecipeCommonProperties commonProperties, EntityPredicate predicate) {
		super(commonProperties);
		this.predicate = predicate;
		withoutTypePredicate = EntityPredicateExtensions.withoutType(predicate);
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return null;
	}

	@Override
	public LycheeRecipeType<EntityTickingRecipe> getType() {
		return null;
	}
}
