package snownee.lychee.compat.rv;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.rv.element.InfoElementHelper;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class RvCategoryType<T extends ILycheeRecipe<?>> {
	public static final int WIDTH = 150;
	public static final int WIDER_WIDTH = WIDTH + 20;
	public static final int HEIGHT = 60;

	public final ResourceLocation id;
	public int width = WIDTH;
	public int height = HEIGHT;
	public Rect2i infoRect = InfoElementHelper.getInfoRect(InfoElementHelper.INFO_RECT.getX(), InfoElementHelper.INFO_RECT.getY());
	public Function<RvCategory<T>, Either<RenderElement, ItemStack>> iconProvider;
	public Function<RvCategory<T>, List<List<ItemStack>>> workstationProvider;

	public RvCategoryType(ResourceLocation id) {
		this.id = id;
	}

	public void setSimpleWorkstationProvider(Function<RvCategory<T>, List<ItemStack>> workstationProvider) {
		this.workstationProvider = workstationProvider.andThen($ -> Lists.transform($, List::of));
	}
}
