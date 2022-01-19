package snownee.lychee.compat.jei.ingredient;

import java.util.Objects;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import snownee.lychee.compat.jei.JEICompat;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.util.LUtil;

public class PostActionIngredientHelper implements IIngredientHelper<PostAction> {

	@Override
	public PostAction copyIngredient(PostAction postAction) {
		return postAction;
	}

	@Override
	public String getDisplayName(PostAction postAction) {
		return postAction.getDisplayName().getString();
	}

	@Override
	public String getErrorInfo(PostAction postAction) {
		return Objects.toString(postAction);
	}

	@Override
	public IIngredientType<PostAction> getIngredientType() {
		return JEICompat.POST_ACTION;
	}

	@Override
	public PostAction getMatch(Iterable<PostAction> arg0, PostAction arg1, UidContext arg2) {
		return null;
	}

	@Override
	public String getModId(PostAction postAction) {
		String modid = postAction.getType().getRegistryName().getNamespace();
		return LUtil.wrapNamespace(modid);
	}

	@Override
	public String getResourceId(PostAction postAction) {
		return postAction.getType().getRegistryName().getPath();
	}

	@Override
	public String getUniqueId(PostAction postAction, UidContext arg1) {
		return postAction.getType().getRegistryName().toString();
	}

}
