package snownee.lychee.compat.recipeviewer.jei.ingredient;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.Identifier;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.compat.recipeviewer.jei.LycheeJEIPlugin;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;


public class PostActionIngredientHelper implements IIngredientHelper<PostAction> {

	@Override
	public PostAction copyIngredient(PostAction postAction) {
		return postAction;
	}

	@Override
	public String getDisplayName(PostAction postAction) {
		return PostAction.getDisplayName(postAction).getString();
	}

	@Override
	public String getErrorInfo(@Nullable PostAction postAction) {
		return Objects.toString(postAction);
	}

	@Override
	public IIngredientType<PostAction> getIngredientType() {
		return LycheeJEIPlugin.POST_ACTION;
	}

	@Override
	public String getDisplayModId(PostAction postAction) {
		var modid = getIdentifier(postAction).getNamespace();
		return CommonProxy.wrapNamespace(modid);
	}

	@Override
	public Identifier getIdentifier(PostAction postAction) {
		return Objects.requireNonNull(LycheeRegistries.POST_ACTION.getKey(postAction.type()));
	}

	@Override
	public String getUid(PostAction postAction, UidContext context) {
		return getIdentifier(postAction).toString() + postAction;
	}
}