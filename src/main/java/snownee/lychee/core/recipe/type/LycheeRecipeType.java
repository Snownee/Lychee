package snownee.lychee.core.recipe.type;

import java.util.Collection;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import snownee.lychee.Lychee;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.mixin.RecipeManagerAccess;

public class LycheeRecipeType<C extends LycheeContext, T extends LycheeRecipe<C>> implements RecipeType<T> {
	public final ResourceLocation id;
	public final Class<? extends T> clazz;
	public final LootContextParamSet contextParamSet;
	private boolean empty;

	public static final Component DEFAULT_PREVENT_TIP = new TranslatableComponent("tip.lychee.prevent_default.default").withStyle(ChatFormatting.YELLOW);

	public LycheeRecipeType(String name, Class<T> clazz, @Nullable LootContextParamSet contextParamSet) {
		id = new ResourceLocation(Lychee.ID, name);
		this.clazz = clazz;
		this.contextParamSet = contextParamSet == null ? LootContextParamSets.get(id) : contextParamSet;
		Objects.requireNonNull(this.contextParamSet);
	}

	@Override
	public String toString() {
		return id.toString();
	}

	@SuppressWarnings("rawtypes")
	public Collection<T> recipes(RecipeManager recipeManager) {
		return (Collection) ((RecipeManagerAccess) recipeManager).callByType(this).values();
	}

	public void updateEmptyState(RecipeManager recipeManager) {
		empty = recipes(recipeManager).isEmpty();
	}

	public boolean isEmpty() {
		return empty;
	}

	public void buildCache(RecipeManager recipeManager) {

	}

	public Component getPreventDefaultDescription(LycheeRecipe<?> recipe) {
		return DEFAULT_PREVENT_TIP;
	}
}