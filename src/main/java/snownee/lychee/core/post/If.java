package snownee.lychee.core.post;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.input.NBTPatch;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.json.JsonPointer;

public class If extends PostAction implements CompoundAction {

	public final PostAction[] successEntries;
	public final PostAction[] failureEntries;
	public final boolean canRepeat;
	public final boolean hidden;
	public final boolean preventSync;

	public If(PostAction[] successEntries, PostAction[] failureEntries) {
		this.successEntries = successEntries;
		this.failureEntries = failureEntries;
		canRepeat = getChildActions().allMatch(PostAction::canRepeat);
		hidden = getChildActions().allMatch(PostAction::isHidden);
		preventSync = getChildActions().allMatch(PostAction::preventSync);
	}

	public void getConsequenceTooltips(List<Component> list, PostAction[] actions, String translation) {
		if (actions.length == 0) {
			return;
		}
		if (showingConditionsCount() > 0) {
			list.add(Component.translatable(translation).withStyle(ChatFormatting.GRAY));
		}
		for (PostAction child : actions) {
			if (child.isHidden()) {
				continue;
			}
			list.add(Component.literal("- ").withStyle(ChatFormatting.GRAY).append(child.getDisplayName()));
		}
	}

	@Override
	public Stream<PostAction> getChildActions() {
		return Stream.concat(Arrays.stream(successEntries), Arrays.stream(failureEntries));
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.IF;
	}

	@Override
	public void doApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		ctx.enqueueActions(Stream.of(successEntries), times, false);
	}

	@Override
	public void onFailure(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		ctx.enqueueActions(Stream.of(failureEntries), times, false);
	}

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
	}

	@Override
	public List<ItemStack> getItemOutputs() {
		return getChildActions().map(PostAction::getItemOutputs).flatMap(List::stream).toList();
	}

	@Override
	public List<BlockPredicate> getBlockOutputs() {
		return getChildActions().map(PostAction::getBlockOutputs).flatMap(List::stream).toList();
	}

	@Override
	public boolean canRepeat() {
		return canRepeat;
	}

	@Override
	public boolean isHidden() {
		return hidden;
	}

	@Override
	public boolean preventSync() {
		return preventSync;
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe, ILycheeRecipe.NBTPatchContext patchContext) {
		Preconditions.checkArgument(!getConditions().isEmpty() || failureEntries.length == 0, "Failure entries must be empty when there is no condition");
		for (PostAction action : getChildActions().toList()) {
			Preconditions.checkArgument(action.getClass() != NBTPatch.class, "NBTPatch cannot be used in RandomSelect");
			action.validate(recipe, patchContext);
		}
	}

	@Override
	public void getUsedPointers(ILycheeRecipe<?> recipe, Consumer<JsonPointer> consumer) {
		for (PostAction action : getChildActions().toList()) {
			action.getUsedPointers(recipe, consumer);
		}
	}

	@Override
	public JsonElement provideJsonInfo(ILycheeRecipe<?> recipe, JsonPointer pointer, JsonObject recipeObject) {
		JsonObject jsonObject = new JsonObject();
		int i = 0;
		JsonArray array = new JsonArray();
		for (PostAction action : successEntries) {
			array.add(action.provideJsonInfo(recipe, pointer.append("then/" + i), recipeObject));
			i++;
		}
		jsonObject.add("then", array);
		i = 0;
		array = new JsonArray();
		for (PostAction action : failureEntries) {
			array.add(action.provideJsonInfo(recipe, pointer.append("else/" + i), recipeObject));
			i++;
		}
		jsonObject.add("else", array);
		return jsonObject;
	}

	public static class Type extends PostActionType<If> {

		@Override
		public If fromJson(JsonObject o) {
			List<PostAction> successEntries = Lists.newArrayList();
			List<PostAction> failureEntries = Lists.newArrayList();
			PostAction.parseActions(o.get("then"), successEntries::add);
			PostAction.parseActions(o.get("else"), failureEntries::add);
			Preconditions.checkArgument(successEntries.size() + failureEntries.size() > 0, "entries can not be empty");
			return new If(successEntries.toArray(PostAction[]::new), failureEntries.toArray(PostAction[]::new));
		}

		@Override
		public void toJson(If action, JsonObject o) {
			JsonArray array = new JsonArray();
			for (PostAction entry : action.successEntries) {
				array.add(entry.toJson());
			}
			o.add("then", array);
			array = new JsonArray();
			for (PostAction entry : action.failureEntries) {
				array.add(entry.toJson());
			}
			o.add("else", array);
		}

		@Override
		public If fromNetwork(FriendlyByteBuf buf) {
			List<PostAction> successEntries = Lists.newArrayList();
			List<PostAction> failureEntries = Lists.newArrayList();
			LycheeRecipe.Serializer.actionsFromNetwork(buf, successEntries::add);
			LycheeRecipe.Serializer.actionsFromNetwork(buf, failureEntries::add);
			return new If(successEntries.toArray(PostAction[]::new), failureEntries.toArray(PostAction[]::new));
		}

		@Override
		public void toNetwork(If action, FriendlyByteBuf buf) {
			LycheeRecipe.Serializer.actionsToNetwork(buf, List.of(action.successEntries));
			LycheeRecipe.Serializer.actionsToNetwork(buf, List.of(action.failureEntries));
		}

	}

}
