package snownee.lychee.compat.recipeviewer.rei;

import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import snownee.lychee.Lychee;
import snownee.lychee.compat.recipeviewer.rei.ingredient.PostActionIngredientHelper;
import snownee.lychee.util.action.PostAction;

public class LycheeREIPlugin implements REICommonPlugin {
	public static final EntryType<PostAction> POST_ACTION = EntryType.deferred(Lychee.id("post_action"));

	@Override
	public void registerEntryTypes(EntryTypeRegistry registry) {
		registry.register(POST_ACTION, new PostActionIngredientHelper());
	}
}
