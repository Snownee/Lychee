package snownee.lychee.core.recipe;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.Reference;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.json.JsonSchema;
import snownee.lychee.util.json.JsonSchema.Anchor;

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

	List<PostAction> getPostActions();

	ContextualHolder getContextualHolder();

	@Nullable
	String getComment();

	boolean showInRecipeViewer();

	default List<PostAction> getShowingPostActions() {
		return getPostActions().stream().filter($ -> !$.isHidden()).toList();
	}

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
		return Streams.stream(getAllShowingActions()).map(PostAction::getBlockOutputs).flatMap(List::stream).toList();
	}

	default Iterable<PostAction> getAllShowingActions() {
		return getPostActions();
	}

	JsonSchema generateSchema(JsonObject jsonObject);

	Map<ResourceLocation, JsonSchema> recipeSchemas = Maps.newHashMap();

	static void processActions(ILycheeRecipe<?> recipe, Iterable<PostAction> actions, JsonObject jsonObject) {
		Set<JsonPointer> usedPointers = Sets.newHashSet();
		for (PostAction action : actions) {
			Preconditions.checkArgument(action.validate(recipe), "Error while validating action: %s", action);
			action.getUsedPointers(recipe, usedPointers::add);
		}
		if (!usedPointers.isEmpty()) {
			JsonSchema schema = recipe.generateSchema(jsonObject);
			schema.strip(usedPointers);
			generateActionsSchema(jsonObject, POST, recipe.getPostActions(), schema);
			schema.bakeAnchors();
			Object2IntMap<String> counter = new Object2IntArrayMap<>();
			for (Anchor anchor : schema.anchors().values()) {
				if (anchor.id == null) {
					int id = counter.getInt(anchor.type);
					anchor.id = Integer.toString(id);
					counter.put(anchor.type, id + 1);
				}
			}
			//			Lychee.LOGGER.info(schema);
			recipeSchemas.put(recipe.getId(), schema);
		} else { // here we remove data from the last reload
			recipeSchemas.remove(recipe.getId());
		}
	}

	static void generateActionsSchema(JsonObject jsonObject, JsonPointer pointer, List<PostAction> actions, JsonSchema schema) {
		schema.buildObjectOrList(jsonObject, pointer, (i, isObject) -> {
			PostAction action = actions.get(i);
			JsonSchema.Node node = action.generateSchema();
			if (node != null) {
				if (isObject) {
					action.path = pointer.toString();
				} else {
					action.path = pointer + "/" + i;
				}
			}
			return node;
		});
	}

}
