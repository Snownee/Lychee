package snownee.lychee.core.recipe;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeConfig;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.Reference;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.def.IntBoundsHelper;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.JsonPointer;
import snownee.lychee.util.LUtil;

public abstract class LycheeRecipe<C extends LycheeContext> extends ContextualHolder implements ILycheeRecipe<C>, Recipe<C> {

	private final ResourceLocation id;
	private List<PostAction> actions = Collections.EMPTY_LIST;
	protected Ints maxRepeats = Ints.ANY;
	public boolean ghost;
	public boolean hideInRecipeViewer;
	@Nullable
	public String comment;

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
	public ItemStack assemble(C inv) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

	public void addPostAction(PostAction action) {
		Objects.requireNonNull(action);
		if (actions == Collections.EMPTY_LIST) {
			actions = Lists.newArrayList();
		}
		if (!action.canRepeat()) {
			maxRepeats = IntBoundsHelper.ONE;
		}
		actions.add(action);
	}

	@Override
	public List<PostAction> getPostActions() {
		return actions;
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

	public boolean showInRecipeViewer() {
		return !hideInRecipeViewer;
	}

	// true to apply
	public boolean tickOrApply(C ctx) {
		return true;
	}

	@Override
	public IntList getItemIndexes(Reference reference) {
		int size = getIngredients().size();
		JsonPointer pointer = null;
		if (reference == Reference.DEFAULT) {
			pointer = defaultItemPointer();
		} else if (reference.isPointer()) {
			pointer = reference.getPointer();
		}
		if (pointer != null) {
			if (pointer.size() == 1 && pointer.getString(0).equals("item_in")) {
				return IntList.of(IntStream.range(0, size).toArray());
			}
			if (pointer.size() == 2 && pointer.getString(0).equals("item_in")) {
				return IntList.of(pointer.getInt(1));
			}
		}
		return IntList.of();
	}

	@Override
	public abstract LycheeRecipeType<?, ?> getType();

	@Override
	public abstract Serializer<?> getSerializer();

	public static abstract class Serializer<R extends LycheeRecipe<?>> implements RecipeSerializer<R> {

		protected final Function<ResourceLocation, R> factory;

		public Serializer(Function<ResourceLocation, R> factory) {
			this.factory = factory;
		}

		@Override
		public final R fromJson(ResourceLocation pRecipeId, JsonObject jsonObject) {
			R recipe = factory.apply(pRecipeId);
			recipe.hideInRecipeViewer = GsonHelper.getAsBoolean(jsonObject, "hide_in_viewer", false);
			recipe.ghost = GsonHelper.getAsBoolean(jsonObject, "ghost", false);
			recipe.comment = GsonHelper.getAsString(jsonObject, "comment", null);
			recipe.parseConditions(jsonObject.get("contextual"));
			PostAction.parseActions(jsonObject.get("post"), recipe::addPostAction);
			fromJson(recipe, jsonObject);
			if (jsonObject.has("max_repeats")) {
				recipe.maxRepeats = Ints.fromJson(jsonObject.get("max_repeats"));
				Integer min = recipe.maxRepeats.getMin();
				Preconditions.checkArgument(min != null && min > 0, "Min value of max_repeats should be greater than 0");
			}
			for (PostAction action : recipe.getPostActions()) {
				Preconditions.checkArgument(action.validate(recipe), "Error while validating action: %s", action);
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

				int size = buf.readVarInt();
				for (int i = 0; i < size; i++) {
					PostActionType<?> type = LUtil.readRegistryId(LycheeRegistries.POST_ACTION, buf);
					PostAction action = type.fromNetwork(buf);
					action.conditionsFromNetwork(buf);
					recipe.addPostAction(action);
				}

				recipe.comment = buf.readUtf();
				fromNetwork(recipe, buf);
				return recipe;
			} catch (Exception e) {
				Lychee.LOGGER.error("Exception while reading Lychee recipe: {}", id);
				Lychee.LOGGER.catching(e);
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
			List<PostAction> actions = recipe.getPostActions();
			buf.writeVarInt(actions.size());
			for (PostAction action : actions) {
				PostActionType type = action.getType();
				LUtil.writeRegistryId(LycheeRegistries.POST_ACTION, type, buf);
				type.toNetwork(action, buf);
				action.conditionsToNetwork(buf);
			}
			buf.writeUtf(Strings.nullToEmpty(recipe.comment));
			toNetwork0(buf, recipe);
		}

		public abstract void toNetwork0(FriendlyByteBuf pBuffer, R pRecipe);

		public ResourceLocation getRegistryName() {
			return Registry.RECIPE_SERIALIZER.getKey(this);
		}

	}

}
