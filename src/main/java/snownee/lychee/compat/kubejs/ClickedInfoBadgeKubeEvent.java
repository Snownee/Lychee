package snownee.lychee.compat.kubejs;

import org.jetbrains.annotations.Nullable;

import dev.latvian.mods.kubejs.client.ClientKubeEvent;
import net.minecraft.world.item.crafting.Recipe;
import snownee.lychee.util.ui.InputAction;

public class ClickedInfoBadgeKubeEvent implements ClientKubeEvent {

	public final Recipe<?> recipe;
	@Nullable
	public final String recipeId;
	public final InputAction action;

	public ClickedInfoBadgeKubeEvent(Recipe<?> recipe, @Nullable String recipeId, InputAction action) {
		this.recipe = recipe;
		this.recipeId = recipeId;
		this.action = action;
	}

}
