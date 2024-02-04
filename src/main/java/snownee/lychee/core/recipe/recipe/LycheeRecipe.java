package snownee.lychee.core.recipe.recipe;

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
import net.minecraft.world.item.crafting.Recipe;
import snownee.lychee.Lychee;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.core.Reference;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.json.JsonPatch;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.BlockKeyableRecipe;

public interface LycheeRecipe<C extends LycheeRecipeContext> extends Recipe<C> {
	JsonPointer ITEM_IN = new JsonPointer("/item_in");
	JsonPointer ITEM_OUT = new JsonPointer("/item_out");
	JsonPointer RESULT = new JsonPointer("/result");
	JsonPointer POST = new JsonPointer("/post");

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

	Stream<PostAction<?>> getPostActions();

	default Stream<PostAction<?>> getAllActions() {
		return getPostActions();
	}

	Map<JsonPointer, List<PostAction<?>>> getActionGroups();

	static Stream<PostAction<?>> filterHidden(Stream<PostAction<?>> stream) {
		return stream.filter(Predicate.not(PostAction::hidden));
	}

	default int showingActionsCount() {
		return (int) LycheeRecipe.filterHidden(getPostActions()).count();
	}

	ContextualHolder getContextualHolder();

	@Nullable
	String getComment();

	boolean showInRecipeViewer();

	default void applyPostActions(C ctx, int times) {
		if (!ctx.getLevel().isClientSide) {
			ctx.enqueueActions(getPostActions(), times, true);
			ctx.runtime.run(this, ctx);
		}
	}

	default List<BlockPredicate> getBlockInputs() {
		if (this instanceof BlockKeyableRecipe<?> blockPredicateRecipe) {
			return List.of(blockPredicateRecipe.blockPredicate());
		}
		return List.of();
	}

	default List<BlockPredicate> getBlockOutputs() {
		return filterHidden(getAllActions()).map(PostAction::getOutputBlocks).flatMap(List::stream).toList();
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

		public int countTargets(LycheeRecipe recipe, Reference reference) {
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

		public int countTargets(LycheeRecipe recipe, JsonPointer pointer) {
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

	static void processActions(LycheeRecipe recipe, JsonObject recipeObject) {
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
			for (Map.Entry<JsonPointer, List<PostAction<?>>> entry : recipe.getActionGroups().entrySet()) {
				JsonPointer pointer = entry.getKey();
				List<PostAction<?>> actions = entry.getValue();
				JsonElement element = processActionGroup(recipe, pointer, actions, recipeObject);
				if (element != null) {
					JsonPatch.add(jsonObject, pointer, element);
				}
			}
			patchContext.setValue(new NBTPatchContext(jsonObject, usedIndexes, splits));
			patchContexts.put(recipe.lychee$getId(), patchContext.getValue());
		} else { // here we remove data from the last reload
			patchContexts.remove(recipe.lychee$getId());
		}
		recipe.getAllActions().forEach(action -> {
			try {
				action.validate(recipe, patchContext.getValue());
			} catch (Exception e) {
				Lychee.LOGGER.error("Error while validating action " + action, e);
			}
		});
	}

	static JsonElement processActionGroup(
			LycheeRecipe<?> recipe,
			JsonPointer pointer,
			List<PostAction<?>> actions,
			JsonObject recipeObject
	) {
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
