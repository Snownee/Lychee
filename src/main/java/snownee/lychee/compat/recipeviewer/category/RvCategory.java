package snownee.lychee.compat.recipeviewer.category;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.jspecify.annotations.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.lychee.Lychee;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.element.InfoElementHelper;
import snownee.lychee.ui.SpriteElementRenderer;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;


public class RvCategory<R extends ILycheeRecipe<LycheeContext>> {
	public static final int WIDTH = 120;
	public static final int WIDER_WIDTH = WIDTH + 50;
	public static final int HEIGHT = 60;

	public final LycheeRecipeType<R> recipeType;
	public int width = WIDTH;
	public int height = HEIGHT;
	public @Nullable IconProvider<R> iconProvider;
	public WorkstationProvider<R> workstationProvider = _ -> List.of();
	public ImmutableMap<String, RvCategoryDecoration<R>> decorations = ImmutableMap.of();
	public ImmutableMap<String, Predicate<R>> conditions = ImmutableMap.of();

	public RvCategory(LycheeRecipeType<R> recipeType) {
		this.recipeType = recipeType;
	}

	public void setSimpleWorkstationProvider(Function<RvCategoryInstance<R>, List<Item>> workstationProvider) {
		this.workstationProvider = category -> Lists.transform(workstationProvider.apply(category), SlotDisplay.ItemSlotDisplay::new);
	}

	public void init() {
		ImmutableMap.Builder<String, RvCategoryDecoration<R>> decorations = ImmutableMap.builder();
		ImmutableMap.Builder<String, Predicate<R>> conditions = ImmutableMap.builder();
		Map<String, RvCategoryDecoration<R>> lowPriorityDecorations = Maps.newLinkedHashMap();
		var builder = new DecorationMapBuilder<R>() {
			@Override
			public void put(String key, RvCategoryDecoration<R> decoration) {
				if ("info".equals(key) || "consume_block_in".equals(key)) {
					lowPriorityDecorations.put(key, decoration);
				} else {
					decorations.put(key, decoration);
				}
			}

			@Override
			public void condition(String key, Predicate<R> predicate) {
				conditions.put(key, predicate);
			}
		};
		setupDecorations(builder);
		// temporary fix for sorting order
		decorations.putAll(lowPriorityDecorations);
		this.decorations = decorations.buildKeepingLast();
		this.conditions = conditions.buildKeepingLast();
	}

	public void configureLayout(RvCategoryLayoutBuilder<R> builder, RecipeHolder<R> recipeHolder) {
		var recipe = recipeHolder.value();
		builder.ingredientGroup(recipe, new Vector2f(27, 28));
		builder.actionGroup(recipe, new Vector2f(builder.width() - 29, 28));
	}

	public void setupDecorations(DecorationMapBuilder<R> mapBuilder) {
	}

	public Vector2fc infoPosition(R recipe) {
		return VectorExtensions.ZERO2F;
	}

	public Identifier id() {
		return recipeType.categoryId;
	}

	public Class<? extends R> recipeClass() {
		return recipeType.recipeClass;
	}

	@FunctionalInterface
	public interface IconProvider<R extends ILycheeRecipe<LycheeContext>> {
		RenderElement get(RvCategoryInstance<R> category);
	}

	@FunctionalInterface
	public interface WorkstationProvider<R extends ILycheeRecipe<LycheeContext>> {
		List<SlotDisplay> get(RvCategoryInstance<R> category);
	}

	public static boolean needConsumeBlockInput(ILycheeRecipe<? extends LycheeContext> recipe) {
		return recipe.postActions().stream().anyMatch(it -> it instanceof PlaceBlock placeBlock && placeBlock.fancyDisplay());
	}

	public static RenderElement consumeBlockInputIcon() {
		return new InteractiveRenderElement(
				(InteractiveRenderElement element) -> new SpriteElementRenderer(
						Lychee.id("exclamation_mark"),
						2
				).withSize(element.width(), element.height()).atZ(100), null).onTooltip(() -> List.of(Component.translatable(
				"postAction.lychee.place.consume"))).withSize(InfoElementHelper.INFO_SIZE, InfoElementHelper.INFO_SIZE);
	}

	public static boolean needInfo(ILycheeRecipe<?> recipe) {
		return recipe.conditions().hasShowingConditions() || recipe.comment().map(it -> !Strings.isNullOrEmpty(it)).orElse(false);
	}

	public static <R extends ILycheeRecipe<?>> RenderElement infoIcon(RecipeHolder<R> recipeHolder) {
		var recipe = recipeHolder.value();
		return RenderElement.create(new SpriteElementRenderer(AllGuiTextures.INFO.id).<SpriteElementRenderer>withSize(
						InfoElementHelper.INFO_SIZE))
				.onTooltip(() -> RVs.getRecipeTooltip(recipe))
				.onInput((action, element) -> ClientProxy.postWidgetInputEvent(
						recipeHolder.value(),
						recipeHolder.id().toString(),
						action,
						element))
				.withSize(InfoElementHelper.INFO_SIZE);
	}
}
