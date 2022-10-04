package snownee.lychee.core.recipe;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

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
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.def.IntBoundsHelper;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;

public abstract class LycheeRecipe<C extends LycheeContext> extends ContextualHolder implements Recipe<C> {

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

	//todo: In 1.19.2, RecipeManager#getRecipeFor no longer calls RecipeType#tryMatch. Then shall we move that call of checkConditions function here?

	//@Override
	//public boolean matches(C container, Level level) {
	//	return checkConditions(this, container, 1) > 0;
	//}

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

	public List<PostAction> getPostActions() {
		return actions;
	}

	public List<PostAction> getShowingPostActions() {
		return getPostActions().stream().filter($ -> !$.isHidden()).toList();
	}

	/**
	 * @return true if do default behavior
	 */
	public void applyPostActions(LycheeContext ctx, int times) {
		if (!ctx.getLevel().isClientSide) {
			ctx.status.reset();
			PostAction.applySequence(actions, this, ctx, times);
		}
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
	public abstract LycheeRecipeType<?, ?> getType();

	@Override
	public abstract Serializer<?> getSerializer();

	public static abstract class Serializer<R extends LycheeRecipe<?>> implements RecipeSerializer<R> {

		protected final Function<ResourceLocation, R> factory;

		public Serializer(Function<ResourceLocation, R> factory) {
			this.factory = factory;
		}

		@Override
		public final R fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
			R recipe = factory.apply(pRecipeId);
			recipe.hideInRecipeViewer = GsonHelper.getAsBoolean(pSerializedRecipe, "hide_in_viewer", false);
			recipe.ghost = GsonHelper.getAsBoolean(pSerializedRecipe, "ghost", false);
			recipe.comment = GsonHelper.getAsString(pSerializedRecipe, "comment", null);
			recipe.parseConditions(pSerializedRecipe.get("contextual"));
			PostAction.parseActions(pSerializedRecipe.get("post"), recipe::addPostAction);
			fromJson(recipe, pSerializedRecipe);
			if (pSerializedRecipe.has("max_repeats")) {
				recipe.maxRepeats = Ints.fromJson(pSerializedRecipe.get("max_repeats"));
				Integer min = recipe.maxRepeats.getMin();
				Preconditions.checkArgument(min != null && min > 0, "Min value of max_repeats should be greater than 0");
			}
			return recipe;
		}

		public abstract void fromJson(R pRecipe, JsonObject pSerializedRecipe);

		@Override
		public final R fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
			try {
				R recipe = factory.apply(pRecipeId);
				if (LycheeConfig.debug)
					Lychee.LOGGER.debug("Read recipe: {}", pRecipeId);
				recipe.hideInRecipeViewer = pBuffer.readBoolean();
				if (recipe.hideInRecipeViewer && !recipe.getType().requiresClient()) {
					return recipe;
				}
				recipe.conditionsFromNetwork(pBuffer);

				int size = pBuffer.readVarInt();
				for (int i = 0; i < size; i++) {
					PostActionType<?> type = LUtil.readRegistryId(LycheeRegistries.POST_ACTION, pBuffer);
					PostAction action = type.fromNetwork(pBuffer);
					action.conditionsFromNetwork(pBuffer);
					recipe.addPostAction(action);
				}

				recipe.comment = pBuffer.readUtf();
				fromNetwork(recipe, pBuffer);
				return recipe;
			} catch (Exception e) {
				Lychee.LOGGER.error("Exception while reading Lychee recipe: {}", pRecipeId);
				Lychee.LOGGER.catching(e);
				return null;
			}
		}

		public abstract void fromNetwork(R pRecipe, FriendlyByteBuf pBuffer);

		@SuppressWarnings("rawtypes")
		@Override
		public final void toNetwork(FriendlyByteBuf pBuffer, R pRecipe) {
			if (LycheeConfig.debug)
				Lychee.LOGGER.debug("Write recipe: {}", pRecipe.getId());
			pBuffer.writeBoolean(pRecipe.hideInRecipeViewer);
			if (pRecipe.hideInRecipeViewer && !pRecipe.getType().requiresClient()) {
				return;
			}
			pRecipe.conditionsToNetwork(pBuffer);
			List<PostAction> actions = pRecipe.getPostActions();
			pBuffer.writeVarInt(actions.size());
			for (PostAction action : actions) {
				PostActionType type = action.getType();
				LUtil.writeRegistryId(LycheeRegistries.POST_ACTION, type, pBuffer);
				type.toNetwork(action, pBuffer);
				action.conditionsToNetwork(pBuffer);
			}
			pBuffer.writeUtf(Strings.nullToEmpty(pRecipe.comment));
			toNetwork0(pBuffer, pRecipe);
		}

		public abstract void toNetwork0(FriendlyByteBuf pBuffer, R pRecipe);

		public ResourceLocation getRegistryName() {
			return Registry.RECIPE_SERIALIZER.getKey(this);
		}

	}

}
