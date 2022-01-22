package snownee.lychee.core.recipe;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.contextual.ContextualCondition;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;

public abstract class LycheeRecipe<C extends LycheeContext> extends ContextualHolder implements Recipe<C> {

	private final ResourceLocation id;
	private List<PostAction> actions = Collections.EMPTY_LIST;
	protected boolean willBatchRun = true;

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
		if (actions == Collections.EMPTY_LIST) {
			actions = Lists.newArrayList();
		}
		if (!action.getType().canBatchRun()) {
			willBatchRun = false;
		}
		actions.add(action);
	}

	public List<PostAction> getPostActions() {
		return actions;
	}

	public List<PostAction> getShowingPostActions() {
		return getPostActions().stream().filter($ -> !$.isHidden()).toList();
	}

	public void applyPostActions(LycheeContext ctx, int times) {
		if (!ctx.getLevel().isClientSide) {
			actions.forEach($ -> $.doApply(this, ctx, times));
		}
	}

	public boolean willBatchRun() {
		return willBatchRun;
	}

	public static abstract class Serializer<R extends LycheeRecipe<?>> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<R> {

		protected final Function<ResourceLocation, R> factory;

		public Serializer(Function<ResourceLocation, R> factory) {
			this.factory = factory;
		}

		@Override
		public final R fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
			R recipe = factory.apply(pRecipeId);
			ContextualCondition.parseConditions(pSerializedRecipe.get("contextual"), recipe::addCondition);
			PostAction.parseActions(pSerializedRecipe.get("post"), recipe::addPostAction);
			fromJson(recipe, pSerializedRecipe);
			return recipe;
		}

		public abstract void fromJson(R pRecipe, JsonObject pSerializedRecipe);

		@Override
		public final R fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
			R recipe = factory.apply(pRecipeId);
			recipe.conditionsFromNetwork(pBuffer);

			int size = pBuffer.readVarInt();
			for (int i = 0; i < size; i++) {
				PostActionType<?> type = pBuffer.readRegistryIdUnsafe(LycheeRegistries.POST_ACTION);
				recipe.addPostAction(type.fromNetwork(pBuffer));
			}

			fromNetwork(recipe, pBuffer);
			return recipe;
		}

		public abstract void fromNetwork(R pRecipe, FriendlyByteBuf pBuffer);

		@SuppressWarnings("rawtypes")
		@Override
		@OverridingMethodsMustInvokeSuper
		public void toNetwork(FriendlyByteBuf pBuffer, R pRecipe) {
			pRecipe.conditionsToNetwork(pBuffer);

			pBuffer.writeVarInt(pRecipe.getPostActions().size());
			for (PostAction action : pRecipe.getPostActions()) {
				if (action.isHidden()) {
					continue;
				}
				PostActionType type = action.getType();
				pBuffer.writeRegistryIdUnsafe(LycheeRegistries.POST_ACTION, type);
				type.toNetwork(action, pBuffer);
			}
		}

	}

}
