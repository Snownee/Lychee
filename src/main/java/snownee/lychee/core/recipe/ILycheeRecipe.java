package snownee.lychee.core.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.IntList;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.Reference;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.util.JsonPointer;

public interface ILycheeRecipe<C extends LycheeContext> {

	IntList getItemIndexes(Reference reference);

	default JsonPointer defaultItemPointer() {
		return new JsonPointer("/item_in");
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
			ctx.runtime.run(this, ctx);
		}
	}

}
