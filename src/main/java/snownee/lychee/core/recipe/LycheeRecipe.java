package snownee.lychee.core.recipe;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeConfig;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.def.IntBoundsHelper;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.fragment.Fragments;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.json.JsonPointer;

public abstract class LycheeRecipe<C extends LycheeContext> extends ContextualHolder implements ILycheeRecipe<C>, Recipe<C> {

	private final ResourceLocation id;
	public boolean ghost;
	public boolean hideInRecipeViewer;
	@Nullable
	public String comment;
	public String group = "default";
	protected Ints maxRepeats = Ints.ANY;
	protected List<PostAction> actions = List.of();

	public LycheeRecipe(ResourceLocation id) {
		this.id = id;
		if (LycheeConfig.debug)
			Lychee.LOGGER.debug("Construct recipe: {}", id);
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	@Override
	public ItemStack assemble(C inv, RegistryAccess registryAccess) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		return ItemStack.EMPTY;
	}

	public void addPostAction(PostAction action) {
		Objects.requireNonNull(action);
		if (actions.isEmpty()) {
			actions = Lists.newArrayList();
		}
		if (!action.canRepeat()) {
			maxRepeats = IntBoundsHelper.ONE;
		}
		actions.add(action);
	}

	@Override
	public Stream<PostAction> getPostActions() {
		return actions.stream();
	}

	@Override
	public ContextualHolder getContextualHolder() {
		return this;
	}

	@Override
	public @Nullable String getComment() {
		return comment;
	}

	public Ints getMaxRepeats() {
		return maxRepeats;
	}

	public int getRandomRepeats(int max, C ctx) {
		int times = Integer.MAX_VALUE;
		if (!maxRepeats.isAny()) {
			times = IntBoundsHelper.random(maxRepeats, ctx.getRandom());
		}
		return Math.min(max, times);
	}

	@Override
	public boolean showInRecipeViewer() {
		return !hideInRecipeViewer;
	}

	// true to apply
	public boolean tickOrApply(C ctx) {
		return true;
	}

	@Override
	public IntList getItemIndexes(JsonPointer pointer) {
		int size = getIngredients().size();
		if (pointer.size() == 1 && pointer.getString(0).equals("item_in")) {
			return IntList.of(IntStream.range(0, size).toArray());
		}
		if (pointer.size() == 2 && pointer.getString(0).equals("item_in")) {
			try {
				return IntList.of(pointer.getInt(1));
			} catch (NumberFormatException e) {
			}
		}
		return IntList.of();
	}

	@Override
	public Map<JsonPointer, List<PostAction>> getActionGroups() {
		return Map.of(POST, actions);
	}

	@Override
	public abstract LycheeRecipeType<?, ?> getType();

	@Override
	public abstract Serializer<?> getSerializer();

	public static abstract class Serializer<R extends LycheeRecipe<?>> implements RecipeSerializer<R> {

		public static final Ingredient
				AIR_INGREDIENT =
				Ingredient.fromValues(Stream.of(new Ingredient.ItemValue(Items.AIR.getDefaultInstance())));
		protected final Function<ResourceLocation, R> factory;

		public Serializer(Function<ResourceLocation, R> factory) {
			this.factory = factory;
		}

		public static Ingredient parseIngredientOrAir(JsonElement element) {
			if (element instanceof JsonObject object && !object.has("type") && object.has("item") && "minecraft:air".equals(Objects.toString(ResourceLocation.tryParse(object.get("item").getAsString())))) {
				return AIR_INGREDIENT;
			}
			return Ingredient.fromJson(element);
		}

		public static void actionsToNetwork(FriendlyByteBuf buf, List<PostAction> actions) {
			actions = actions.stream().filter(Predicate.not(PostAction::preventSync)).toList();
			buf.writeVarInt(actions.size());
			for (PostAction action : actions) {
				PostActionType type = action.getType();
				CommonProxy.writeRegistryId(LycheeRegistries.POST_ACTION, type, buf);
				type.toNetwork(action, buf);
				action.conditionsToNetwork(buf);
			}
		}

		public static void actionsFromNetwork(FriendlyByteBuf buf, Consumer<PostAction> consumer) {
			int size = buf.readVarInt();
			for (int i = 0; i < size; i++) {
				PostActionType<?> type = CommonProxy.readRegistryId(LycheeRegistries.POST_ACTION, buf);
				PostAction action = type.fromNetwork(buf);
				action.conditionsFromNetwork(buf);
				consumer.accept(action);
			}
		}

		@Override
		public final R fromJson(ResourceLocation pRecipeId, JsonObject jsonObject) {
			Fragments.INSTANCE.process(jsonObject);
			R recipe = factory.apply(pRecipeId);
			recipe.hideInRecipeViewer = GsonHelper.getAsBoolean(jsonObject, "hide_in_viewer", false);
			recipe.ghost = GsonHelper.getAsBoolean(jsonObject, "ghost", false);
			recipe.comment = GsonHelper.getAsString(jsonObject, "comment", null);
			recipe.group = GsonHelper.getAsString(jsonObject, "group", recipe.group);
			Preconditions.checkArgument(ResourceLocation.isValidResourceLocation(recipe.group), "%s is not a valid ResourceLocation", recipe.group);
			recipe.parseConditions(jsonObject.get("contextual"));
			PostAction.parseActions(jsonObject.get("post"), recipe::addPostAction);
			fromJson(recipe, jsonObject);
			ILycheeRecipe.processActions(recipe, jsonObject);
			if (jsonObject.has("max_repeats")) {
				recipe.maxRepeats = Ints.fromJson(jsonObject.get("max_repeats"));
				Integer min = recipe.maxRepeats.getMin();
				Preconditions.checkArgument(min != null && min > 0, "Min value of max_repeats should be greater than 0");
			}
			return recipe;
		}

		public abstract void fromJson(R pRecipe, JsonObject pSerializedRecipe);

		@Override
		public final R fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
			try {
				R recipe = factory.apply(id);
				if (LycheeConfig.debug)
					Lychee.LOGGER.debug("Read recipe: {}", id);
				recipe.hideInRecipeViewer = buf.readBoolean();
				if (recipe.hideInRecipeViewer && !recipe.getType().requiresClient) {
					return recipe;
				}
				recipe.conditionsFromNetwork(buf);
				actionsFromNetwork(buf, recipe::addPostAction);
				recipe.comment = buf.readUtf();
				recipe.group = buf.readUtf();
				fromNetwork(recipe, buf);
				return recipe;
			} catch (Exception e) {
				Lychee.LOGGER.error("Exception while reading Lychee recipe: " + id, e);
				return null;
			}
		}

		public abstract void fromNetwork(R pRecipe, FriendlyByteBuf pBuffer);

		@SuppressWarnings("rawtypes")
		@Override
		public final void toNetwork(FriendlyByteBuf buf, R recipe) {
			if (LycheeConfig.debug)
				Lychee.LOGGER.debug("Write recipe: {}", recipe.getId());
			buf.writeBoolean(recipe.hideInRecipeViewer);
			if (recipe.hideInRecipeViewer && !recipe.getType().requiresClient) {
				return;
			}
			recipe.conditionsToNetwork(buf);
			actionsToNetwork(buf, recipe.actions);
			buf.writeUtf(Strings.nullToEmpty(recipe.comment));
			buf.writeUtf(recipe.group);
			toNetwork0(buf, recipe);
		}

		public abstract void toNetwork0(FriendlyByteBuf pBuffer, R pRecipe);

		public ResourceLocation getRegistryName() {
			return BuiltInRegistries.RECIPE_SERIALIZER.getKey(this);
		}

	}

}
