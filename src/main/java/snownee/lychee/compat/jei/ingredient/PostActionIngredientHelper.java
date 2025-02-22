package snownee.lychee.compat.jei.ingredient;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.compat.jei.LycheeJEIPlugin;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;

@NotNullByDefault
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
	public String getErrorInfo(@Nullable PostAction postAction) {
		return Objects.toString(postAction);
	}

	@Override
	public IIngredientType<PostAction> getIngredientType() {
		return LycheeJEIPlugin.POST_ACTION;
	}

	@Override
	public String getDisplayModId(PostAction postAction) {
		var modid = getResourceLocation(postAction).getNamespace();
		return CommonProxy.wrapNamespace(modid);
	}

	@Override
	public ResourceLocation getResourceLocation(PostAction postAction) {
		return Objects.requireNonNull(LycheeRegistries.POST_ACTION.getKey(postAction.type()));
	}

	@SuppressWarnings("removal")
	@Override
	public String getUniqueId(PostAction ingredient, UidContext context) {
		return "";
	}

	@Override
	public String getUid(PostAction postAction, UidContext context) {
		return getResourceLocation(postAction).toString() + postAction;
	}
}