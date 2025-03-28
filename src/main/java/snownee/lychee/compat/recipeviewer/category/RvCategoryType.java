package snownee.lychee.compat.recipeviewer.category;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.element.InfoElementHelper;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class RvCategoryType<T extends ILycheeRecipe<LycheeContext>> {
	public static final int WIDTH = 150;
	public static final int WIDER_WIDTH = WIDTH + 20;
	public static final int HEIGHT = 60;

	public final ResourceLocation id;
	public int width = WIDTH;
	public int height = HEIGHT;
	public Vector2ic infoPosition = InfoElementHelper.INFO_POSITION;
	public @Nullable IconProvider<T> iconProvider;
	public @NotNull WorkstationProvider<T> workstationProvider = category -> List.of();

	public RvCategoryType(ResourceLocation id) {
		this.id = id;
	}

	public void setSimpleWorkstationProvider(Function<RvCategory<T>, List<ItemStack>> workstationProvider) {
		this.workstationProvider = category -> List.of(workstationProvider.apply(category));
	}

	@FunctionalInterface
	public interface IconProvider<T extends ILycheeRecipe<LycheeContext>> {
		RenderElement get(RvCategory<T> category);
	}

	@FunctionalInterface
	public interface WorkstationProvider<T extends ILycheeRecipe<LycheeContext>> {
		List<List<ItemStack>> get(RvCategory<T> category);
	}
}
