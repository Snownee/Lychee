package snownee.lychee.recipes;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.util.recipe.LycheeRecipeType;

@NotNullByDefault
public class EntityTickingRecipeType extends LycheeRecipeType<EntityTickingRecipe> {
	public EntityTickingRecipeType(
			String name,
			Class<EntityTickingRecipe> clazz,
			@Nullable LootContextParamSet contextParamSet) {
		super(name, clazz, contextParamSet);
	}

	@Override
	public void refreshCache() {
		super.refreshCache();
	}
}
