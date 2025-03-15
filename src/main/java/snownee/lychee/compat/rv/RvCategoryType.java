package snownee.lychee.compat.rv;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class RvCategoryType<T extends ILycheeRecipe<?>> {
	public final ResourceLocation id;
	public Function<RvCategory<T>, Either<RenderElement, ItemStack>> iconProvider;
	public Function<RvCategory<T>, List<List<ItemStack>>> workstationProvider;

	public RvCategoryType(ResourceLocation id) {
		this.id = id;
	}

	public void setSimpleWorkstationProvider(Function<RvCategory<T>, List<ItemStack>> workstationProvider) {
		this.workstationProvider = workstationProvider.andThen($ -> Lists.transform($, List::of));
	}
}
