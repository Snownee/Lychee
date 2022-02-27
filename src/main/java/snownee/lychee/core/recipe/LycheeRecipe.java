package snownee.lychee.core.recipe;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.jetbrains.annotations.MustBeInvokedByOverriders;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.LUtil;

public abstract class LycheeRecipe<C extends LycheeContext> extends ContextualHolder implements Recipe<C> {

	private final ResourceLocation id;
	private List<PostAction> actions = Collections.EMPTY_LIST;
	protected boolean repeatable = true;

	public LycheeRecipe(ResourceLocation id) {
		this.id = id;
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
			repeatable = false;
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
	 * @return false if prevent default behavior
	 */
	public boolean applyPostActions(LycheeContext ctx, int times) {
		boolean doDefault = true;
		if (!ctx.getLevel().isClientSide) {
			for (PostAction action : actions) {
				int t = action.checkConditions(this, ctx, times);
				if (t > 0) {
					doDefault &= action.doApply(this, ctx, t);
				}
			}
		}
		return doDefault;
	}

	public boolean isRepeatable() {
		return repeatable;
	}

	// true to apply
	public boolean tickOrApply(C ctx) {
		return true;
	}

	@Override
	public abstract LycheeRecipeType<?, ?> getType();

	@Override
	public abstract Serializer<?> getSerializer();

	public static abstract class Serializer<R extends LycheeRecipe<?>> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<R> {

		protected final Function<ResourceLocation, R> factory;

		public Serializer(Function<ResourceLocation, R> factory) {
			this.factory = factory;
		}

		@Override
		public final R fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
			R recipe = factory.apply(pRecipeId);
			recipe.parseConditions(pSerializedRecipe.get("contextual"));
			PostAction.parseActions(pSerializedRecipe.get("post"), recipe::addPostAction);
			fromJson(recipe, pSerializedRecipe);
			return recipe;
		}

		public abstract void fromJson(R pRecipe, JsonObject pSerializedRecipe);

		@Override
		public final R fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
			try {
				R recipe = factory.apply(pRecipeId);
				recipe.conditionsFromNetwork(pBuffer);

				int size = pBuffer.readVarInt();
				for (int i = 0; i < size; i++) {
					PostActionType<?> type = LUtil.readRegistryId(LycheeRegistries.POST_ACTION, pBuffer);
					PostAction action = type.fromNetwork(pBuffer);
					action.conditionsFromNetwork(pBuffer);
					recipe.addPostAction(action);
				}

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
		@MustBeInvokedByOverriders
		public void toNetwork(FriendlyByteBuf pBuffer, R pRecipe) {
			pRecipe.conditionsToNetwork(pBuffer);
			List<PostAction> actions = pRecipe.getPostActions();
			pBuffer.writeVarInt(actions.size());
			for (PostAction action : actions) {
				PostActionType type = action.getType();
				LUtil.writeRegistryId(LycheeRegistries.POST_ACTION, type, pBuffer);
				type.toNetwork(action, pBuffer);
				action.conditionsToNetwork(pBuffer);
			}
		}

	}

}
