package snownee.lychee.core.recipe;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

public interface LycheeCounter {

	void lychee$setRecipeId(@Nullable ResourceLocation id);

	@Nullable
	ResourceLocation lychee$getRecipeId();

	void lychee$setCount(int count);

	int lychee$getCount();

}
