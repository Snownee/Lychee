package snownee.lychee.core.recipe;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.Lychee;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.Reference;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.util.json.JsonPatch;
import snownee.lychee.util.json.JsonPointer;

public interface ILycheeRecipe<C extends LycheeContext> {

	JsonPointer ITEM_IN = new JsonPointer("/item_in");
	JsonPointer OUTPUT = new JsonPointer("/output");
	JsonPointer RESULT = new JsonPointer("/result");
	JsonPointer POST = new JsonPointer("/post");

	ResourceLocation getId();

	default IntList getItemIndexes(Reference reference) {
		JsonPointer pointer = null;
		if (reference == Reference.DEFAULT) {
			pointer = defaultItemPointer();
		} else if (reference.isPointer()) {
			pointer = reference.getPointer();
		}
		if (pointer != null) {
			return getItemIndexes(pointer);
		}
		return IntList.of();
	}

	IntList getItemIndexes(JsonPointer pointer);

	default JsonPointer defaultItemPointer() {
		return ITEM_IN;
	}

	Stream<PostAction> getPostActions();

	default Stream<PostAction> getAllActions() {
		return getPostActions();
	}

	static Stream<PostAction> filterHidden(Stream<PostAction> stream) {
		return stream.filter(Predicate.not(PostAction::isHidden));
	}

	default int showingActionsCount() {
		return (int) ILycheeRecipe.filterHidden(getPostActions()).count();
	}

	ContextualHolder getContextualHolder();

	@Nullable
	String getComment();

	boolean showInRecipeViewer();

	default void applyPostActions(LycheeContext ctx, int times) {
		if (!ctx.getLevel().isClientSide) {
			ctx.enqueueActions(getPostActions(), times, true);
			ctx.runtime.run(this, ctx, times);
		}
	}

	default List<BlockPredicate> getBlockInputs() {
		if (this instanceof BlockKeyRecipe<?> blockKeyRecipe) {
			return List.of(blockKeyRecipe.getBlock());
		}
		return List.of();
	}

	default List<BlockPredicate> getBlockOutputs() {
		return filterHidden(getAllActions()).map(PostAction::getBlockOutputs).flatMap(List::stream).toList();
	}

	record NBTPatchContext(JsonObject template, IntCollection usedIndexes, Object2IntMap<JsonPointer> splits) {
		public JsonPointer convertPath(JsonPointer path, BiFunction<String, String, String> composer) {
			int index = splits.getOrDefault(path, -1);
			if (index == -1) {
				return path;
			}
			String s = path.toString();
			String first = s.substring(0, index);
			String last = s.substring(index);
			return new JsonPointer(composer.apply(first, last));
		}

		public int countTargets(ILycheeRecipe<?> recipe, Reference reference) {
			JsonPointer pointer = null;
			if (reference == Reference.DEFAULT) {
				pointer = recipe.defaultItemPointer();
			} else if (reference.isPointer()) {
				pointer = reference.getPointer();
			}
			if (pointer == null) {
				return 0;
			}
			return countTargets(recipe, pointer);
		}

		public int countTargets(ILycheeRecipe<?> recipe, JsonPointer pointer) {
			if (recipe.isActionPath(pointer)) {
				return 1;
			}
			int index = splits.getOrDefault(pointer, -1);
			if (index == -1) {
				return 0;
			}
			return recipe.getItemIndexes(new JsonPointer(pointer.toString().substring(0, index))).size();
		}
	}

	Map<ResourceLocation, NBTPatchContext> patchContexts = Maps.newHashMap();

	default boolean isActionPath(JsonPointer pointer) {
		return !pointer.isRoot() && "post".equals(pointer.getString(0));
	}

	static void processActions(ILycheeRecipe<?> recipe, Map<JsonPointer, List<PostAction>> actionGroups, JsonObject recipeObject) {
		MutableObject<NBTPatchContext> patchContext = new MutableObject<>();
		Set<JsonPointer> usedPointers = Sets.newHashSet();
		recipe.getAllActions().forEach(action -> action.getUsedPointers(recipe, usedPointers::add));
		if (!usedPointers.isEmpty()) {
			IntSet usedIndexes = new IntArraySet();
			Object2IntMap<JsonPointer> splits = new Object2IntArrayMap<>();
			usedPointers.forEach(pointer -> {
				if (recipe.isActionPath(pointer)) {
					return;
				}
				List<String> tokens = Lists.newArrayList(pointer.tokens);
				while (!tokens.isEmpty()) {
					JsonPointer current = new JsonPointer(tokens);
					if (current.find(recipeObject) != null) {
						IntList indexes = recipe.getItemIndexes(current);
						if (!indexes.isEmpty()) {
							usedIndexes.addAll(indexes);
							splits.put(pointer, current.toString().length());
							break;
						}
					}
					tokens.remove(tokens.size() - 1);
				}
			});
			JsonObject jsonObject = new JsonObject();
			for (Map.Entry<JsonPointer, List<PostAction>> entry : actionGroups.entrySet()) {
				JsonPointer pointer = entry.getKey();
				List<PostAction> actions = entry.getValue();
				JsonElement element = processActionGroup(recipe, pointer, actions, recipeObject);
				if (element != null) {
					JsonPatch.add(jsonObject, pointer, element);
				}
			}
			patchContext.setValue(new NBTPatchContext(jsonObject, usedIndexes, splits));
			patchContexts.put(recipe.getId(), patchContext.getValue());
		} else { // here we remove data from the last reload
			patchContexts.remove(recipe.getId());
		}
		recipe.getAllActions().forEach(action -> {
			try {
				action.validate(recipe, patchContext.getValue());
			} catch (Exception e) {
				Lychee.LOGGER.error("Error while validating action " + action, e);
			}
		});
	}

	static JsonElement processActionGroup(ILycheeRecipe<?> recipe, JsonPointer pointer, List<PostAction> actions, JsonObject recipeObject) {
		if (actions.isEmpty()) {
			return null;
		}
		JsonElement element = pointer.find(recipeObject);
		if (element == null) {
			return null;
		}
		if (element.isJsonObject()) {
			element = actions.get(0).provideJsonInfo(recipe, pointer, recipeObject);
			if (element.isJsonNull()) {
				return null;
			}
			return element;
		} else { // is array
			JsonArray array = new JsonArray();
			int size = element.getAsJsonArray().size();
			for (int i = 0; i < size; i++) {
				array.add(actions.get(i).provideJsonInfo(recipe, pointer.append(Integer.toString(i)), recipeObject));
			}
			return array;
		}
	}
}
