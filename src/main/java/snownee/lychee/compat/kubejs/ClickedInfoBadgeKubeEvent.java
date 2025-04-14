package snownee.lychee.compat.kubejs;

import org.jetbrains.annotations.Nullable;

import dev.latvian.mods.kubejs.client.ClientKubeEvent;
import net.minecraft.world.item.crafting.Recipe;

public class ClickedInfoBadgeKubeEvent implements ClientKubeEvent {

	public final Recipe<?> recipe;
	@Nullable
	public final String recipeId;
	public final int button;

	public ClickedInfoBadgeKubeEvent(Recipe<?> recipe, @Nullable String recipeId, int button) {
		this.recipe = recipe;
		this.recipeId = recipeId;
		this.button = button;
	}

}
