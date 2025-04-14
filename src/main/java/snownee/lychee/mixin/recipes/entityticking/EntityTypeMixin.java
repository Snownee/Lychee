package snownee.lychee.mixin.recipes.entityticking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.recipes.EntityTickingRecipe;
import snownee.lychee.util.LycheeEntityType;

@Mixin(EntityType.class)
public class EntityTypeMixin implements LycheeEntityType {
	@Unique
	private ImmutableList<RecipeHolder<EntityTickingRecipe>> lychee$tickingRecipes = ImmutableList.of();

	@Override
	public ImmutableList<RecipeHolder<EntityTickingRecipe>> lychee$tickingRecipes() {
		return lychee$tickingRecipes;
	}

	@Override
	public void lychee$setTickingRecipes(ImmutableList<RecipeHolder<EntityTickingRecipe>> recipes) {
		lychee$tickingRecipes = recipes;
	}
}
