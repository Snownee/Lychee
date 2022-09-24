package snownee.lychee.compat.jei.ingredient;

import java.util.Objects;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
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
	public String getUniqueId(PostAction postAction, UidContext arg1) {
		return postAction.getType().getRegistryName().toString() + postAction.toString();
	}

	@Override
	public String getDisplayModId(PostAction ingredient) {
		String modid = ingredient.getType().getRegistryName().getNamespace();
		return LUtil.wrapNamespace(modid);
	}

	@Override
	public ResourceLocation getResourceLocation(PostAction ingredient) {
		return ingredient.getType().getRegistryName();
	}

}
