package snownee.lychee.recipes.crafting;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeConfig;
import snownee.lychee.LycheeLootContextParamSets;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.util.input.ItemStackHolderCollection;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.action.input.SetItem;
import snownee.lychee.core.recipe.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;
import snownee.lychee.fragment.Fragments;
import snownee.lychee.mixin.CraftingMenuAccess;
import snownee.lychee.mixin.InventoryMenuAccess;
import snownee.lychee.mixin.ShapedRecipeAccess;
import snownee.lychee.mixin.TransientCraftingContainerAccess;
import snownee.lychee.util.Pair;
import snownee.lychee.util.json.JsonPointer;

public class ShapedCraftingRecipe extends ShapedRecipe implements LycheeRecipe<CraftingContext> {

	public static final Cache<Class<?>, Function<CraftingContainer, Pair<Vec3, Player>>> CONTAINER_WORLD_LOCATOR =
			CacheBuilder.newBuilder().build();
	public static final Cache<Class<?>, Function<AbstractContainerMenu, Pair<Vec3, Player>>> MENU_WORLD_LOCATOR =
			CacheBuilder.newBuilder().build();
	private static final Cache<CraftingContainer, CraftingContext> CONTEXT_CACHE =
			CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.SECONDS).build();

	static {
		CONTAINER_WORLD_LOCATOR.put(TransientCraftingContainer.class, container -> {
			TransientCraftingContainerAccess access = (TransientCraftingContainerAccess) container;
			AbstractContainerMenu menu = access.getMenu();
			try {
				return MENU_WORLD_LOCATOR.get(menu.getClass(), () -> {
					Class<?> clazz = menu.getClass().getSuperclass();
					while (clazz != AbstractContainerMenu.class) {
						var locator = MENU_WORLD_LOCATOR.getIfPresent(clazz);
						if (locator != null) {
							return locator;
						}
						clazz = clazz.getSuperclass();
					}
					return menu1 -> null;
				}).apply(menu);
			} catch (ExecutionException e) {
				return null;
			}
		});
		MENU_WORLD_LOCATOR.put(CraftingMenu.class, menu -> {
			CraftingMenuAccess access = (CraftingMenuAccess) menu;
			return Pair.of(
					access.getAccess()
						  .evaluate((level, pos) -> Vec3.atCenterOf(pos), access.getPlayer().position()),
					access.getPlayer()
			);
		});
		MENU_WORLD_LOCATOR.put(InventoryMenu.class, menu -> {
			InventoryMenuAccess access = (InventoryMenuAccess) menu;
			return Pair.of(access.getOwner().position(), access.getOwner());
		});
	}

	private final ContextualHolder conditions = new ContextualHolder();
	public boolean ghost;
	public boolean hideInRecipeViewer;
	@Nullable
	public String comment;
	@Nullable
	public String pattern;
	private List<PostAction> actions = List.of();
	private List<PostAction> assembling = List.of();

	public ShapedCraftingRecipe(
			ResourceLocation id,
			String group,
			CraftingBookCategory category,
			int width,
			int height,
			NonNullList<Ingredient> ingredients,
			ItemStack result,
			boolean showNotification
	) {
		super(id, group, category, width, height, ingredients, result, showNotification);
	}

	@Nullable
	private static Pair<Vec3, Player> getContainerContext(CraftingContainer container) {
		try {
			return CONTAINER_WORLD_LOCATOR.get(container.getClass(), () -> {
				Class<?> clazz = container.getClass().getSuperclass();
				while (clazz != CraftingContainer.class) {
					var locator = CONTAINER_WORLD_LOCATOR.getIfPresent(clazz);
					if (locator != null) {
						return locator;
					}
					clazz = clazz.getSuperclass();
				}
				return container1 -> null;
			}).apply(container);
		} catch (ExecutionException e) {
			return null;
		}
	}

	public static CraftingContext makeContext(
			CraftingContainer container,
			Level level,
			int matchX,
			int matchY,
			boolean mirror
	) {
		var pair = getContainerContext(container);
		var builder = new CraftingContext.Builder(level, matchX, matchY, mirror);
		if (pair != null) {
			builder.withOptionalParameter(LootContextParams.ORIGIN, pair.getFirst());
			builder.withOptionalParameter(LootContextParams.THIS_ENTITY, pair.getSecond());
		}
		CraftingContext ctx = builder.create(LycheeLootContextParamSets.CRAFTING);
		CONTEXT_CACHE.put(container, ctx);
		return ctx;
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		//		Lychee.LOGGER.info("matches");
		if (ghost) {
			return false;
		}
		if (level.isClientSide) {
			return super.matches(container, level);
		}
		ShapedRecipeAccess access = (ShapedRecipeAccess) this;
		int i = 0, j = 0;
		boolean mirror = false, matched = false;
		outer:
		for (i = 0; i <= container.getWidth() - getWidth(); ++i) {
			for (j = 0; j <= container.getHeight() - getHeight(); ++j) {
				if (access.callMatches(container, i, j, true)) {
					matched = true;
					break outer;
				}
				if (getWidth() > 1 && access.callMatches(container, i, j, false)) {
					matched = true;
					mirror = true;
					break outer;
				}
			}
		}
		if (!matched) {
			return false;
		}
		CraftingContext ctx = makeContext(container, level, i, j, mirror);
		matched = conditions.checkConditions(this, ctx, 1) > 0;
		if (matched) {
			ItemStack result = getResultItem(level.registryAccess()).copy();
			List<Ingredient> ingredients = getIngredients();
			ItemStack[] items = new ItemStack[ingredients.size() + 1];
			int startIndex = container.getWidth() * ctx.matchY + ctx.matchX;
			int k = 0;
			for (i = 0; i < getHeight(); i++) {
				for (j = 0; j < getWidth(); j++) {
					items[k] = container.getItem(
							startIndex + container.getWidth() * i + (ctx.mirror ? getWidth() - j : j));
					if (!items[k].isEmpty()) {
						items[k] = items[k].copy();
						items[k].setCount(1);
					}
					++k;
				}
			}
			items[ingredients.size()] = result;
			ctx.itemHolders = ItemStackHolderCollection.Inventory.of(ctx, items);
		}
		return matched;
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
		//		Lychee.LOGGER.info("assemble");
		CraftingContext ctx = CONTEXT_CACHE.getIfPresent(container);
		if (ctx == null) {
			return ItemStack.EMPTY;
		}
		ctx.enqueueActions(assembling.stream(), 1, true);
		ctx.runtime.run(this, ctx);
		return ctx.getItem(ctx.getContainerSize() - 1);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
		//		Lychee.LOGGER.info("getRemainingItems");
		NonNullList<ItemStack> items = super.getRemainingItems(container);
		CraftingContext ctx = CONTEXT_CACHE.getIfPresent(container);
		if (ctx == null) {
			return items;
		}
		applyPostActions(ctx, 1);
		int startIndex = container.getWidth() * ctx.matchY + ctx.matchX;
		int k = 0;
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				if (ctx.itemHolders.ignoreConsumptionFlags.get(k)) {
					items.set(
							startIndex + container.getWidth() * i + (ctx.mirror ? getWidth() - j : j),
							ctx.getItem(k)
					);
				}
				++k;
			}
		}
		return items;
	}

	@Override
	public JsonPointer defaultItemPointer() {
		return RESULT;
	}

	@Override
	public IntList getItemIndexes(JsonPointer pointer) {
		int size = getIngredients().size();
		if (pointer.size() == 1 && pointer.getString(0).equals("result")) {
			return IntList.of(size);
		}
		if (pointer.size() == 2 && pointer.getString(0).equals("key")) {
			String key = pointer.getString(1);
			if (key.length() != 1) {
				return IntList.of();
			}
			IntList list = IntArrayList.of();
			int cp = key.codePointAt(0);
			int l = pattern.length();
			for (int i = 0; i < l; i++) {
				if (cp == pattern.codePointAt(i)) {
					list.add(i);
				}
			}
			return list;
		}
		return IntList.of(size);
	}

	@Override
	public Stream<PostAction> getPostActions() {
		return actions.stream();
	}

	@Override
	public Stream<PostAction> getAllActions() {
		return Stream.concat(getPostActions(), assembling.stream());
	}

	@Override
	public ContextualHolder getContextualHolder() {
		return conditions;
	}

	@Override
	public String getComment() {
		return comment;
	}

	public void addPostAction(PostAction action) {
		Objects.requireNonNull(action);
		if (action instanceof SetItem setItem) {
			if (getItemIndexes(setItem.target).contains(getIngredients().size())) {
				throw new JsonSyntaxException("Can't set item to the result in \"post\", use \"assembling\".");
			}
		}
		if (actions.isEmpty()) {
			actions = Lists.newArrayList();
		}
		actions.add(action);
	}

	public void addAssemblingAction(PostAction action) {
		Objects.requireNonNull(action);
		if (action instanceof SetItem setItem) {
			IntList intList = getItemIndexes(setItem.target);
			if (!intList.isEmpty() && !intList.contains(getIngredients().size())) {
				throw new JsonSyntaxException("Can't set item to the ingredients in \"assembling\", use \"post\".");
			}
		}
		if (assembling.isEmpty()) {
			assembling = Lists.newArrayList();
		}
		assembling.add(action);
	}

	@Override
	public boolean showInRecipeViewer() {
		return !hideInRecipeViewer;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializers.CRAFTING;
	}

	@Override
	public boolean isSpecial() {
		return !conditions.getConditions().isEmpty() || !assembling.isEmpty();
	}

	@Override
	public boolean isActionPath(JsonPointer pointer) {
		if (pointer.isRoot())
			return false;
		String token = pointer.getString(0);
		return "assembling".equals(token) || "post".equals(token);
	}

	@Override
	public Map<JsonPointer, List<PostAction>> getActionGroups() {
		return Map.of(POST, actions, new JsonPointer("/assembling"), assembling);
	}

	public static class Serializer implements RecipeSerializer<ShapedCraftingRecipe> {
		private static ShapedCraftingRecipe fromNormal(ShapedRecipe recipe) {
			return new ShapedCraftingRecipe(
					recipe.getId(),
					recipe.getGroup(),
					recipe.category(),
					recipe.getWidth(),
					recipe.getHeight(),
					recipe.getIngredients(),
					((ShapedRecipeAccess) recipe).getResult(),
					recipe.showNotification()
			);
		}

		@Override
		public ShapedCraftingRecipe fromJson(ResourceLocation id, JsonObject jsonObject) {
			Fragments.INSTANCE.process(jsonObject);
			ShapedCraftingRecipe recipe = fromNormal(RecipeSerializer.SHAPED_RECIPE.fromJson(id, jsonObject));
			recipe.hideInRecipeViewer = GsonHelper.getAsBoolean(jsonObject, "hide_in_viewer", false);
			recipe.ghost = GsonHelper.getAsBoolean(jsonObject, "ghost", false);
			recipe.comment = GsonHelper.getAsString(jsonObject, "comment", null);
			StringBuilder sb = new StringBuilder();
			/* off */
			StreamSupport.stream(jsonObject.getAsJsonArray("pattern").spliterator(), false)
						 .map(JsonElement::getAsString)
						 .forEach(sb::append);
			/* on */
			recipe.pattern = sb.toString();
			recipe.conditions.parseConditions(jsonObject.get("contextual"));
			PostAction.parseActions(jsonObject.get("post"), recipe::addPostAction);
			PostAction.parseActions(jsonObject.get("assembling"), recipe::addAssemblingAction);
			LycheeRecipe.processActions(recipe, jsonObject);
			return recipe;
		}

		@Override
		public ShapedCraftingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
			if (LycheeConfig.debug)
				Lychee.LOGGER.debug("Read recipe: {}", id);
			ShapedCraftingRecipe recipe = fromNormal(RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buf));
			recipe.hideInRecipeViewer = buf.readBoolean();
			if (recipe.hideInRecipeViewer) {
				return recipe;
			}
			recipe.conditions.conditionsFromNetwork(buf);
			OldLycheeRecipe.Serializer.actionsFromNetwork(buf, recipe::addPostAction);
			recipe.comment = buf.readUtf();
			recipe.pattern = buf.readUtf();
			return recipe;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void toNetwork(FriendlyByteBuf buf, ShapedCraftingRecipe recipe) {
			if (LycheeConfig.debug)
				Lychee.LOGGER.debug("Write recipe: {}", recipe.getId());
			RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe);
			buf.writeBoolean(recipe.hideInRecipeViewer);
			if (recipe.hideInRecipeViewer) {
				return;
			}
			recipe.conditions.conditionsToNetwork(buf);
			OldLycheeRecipe.Serializer.actionsToNetwork(buf, recipe.actions);
			buf.writeUtf(Strings.nullToEmpty(recipe.comment));
			buf.writeUtf(Strings.nullToEmpty(recipe.pattern));
		}
	}

}
