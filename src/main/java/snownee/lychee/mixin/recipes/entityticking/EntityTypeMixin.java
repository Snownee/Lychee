package snownee.lychee.mixin.recipes.entityticking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.entity.EntityType;
import snownee.lychee.recipes.EntityTickingRecipe;
import snownee.lychee.util.LycheeEntityType;

@Mixin(EntityType.class)
public class EntityTypeMixin implements LycheeEntityType {
	@Unique
	private ImmutableList<EntityTickingRecipe> lychee$tickingRecipes = ImmutableList.of();

	@Override
	public ImmutableList<EntityTickingRecipe> lychee$tickingRecipes() {
		return lychee$tickingRecipes;
	}

	@Override
	public void lychee$setTickingRecipes(ImmutableList<EntityTickingRecipe> recipes) {
		lychee$tickingRecipes = recipes;
	}
}
