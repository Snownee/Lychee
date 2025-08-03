package snownee.lychee.compat.recipeviewer.emi.recipe;

import java.util.List;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.compat.recipeviewer.emi.LycheeEMIPlugin;
import snownee.lychee.recipes.AnvilCraftingRecipe;

public class LycheeEmiAnvilRecipe implements EmiRecipe {
	private final RecipeHolder<AnvilCraftingRecipe> recipe;
	private List<EmiIngredient> inputs;
	private List<EmiStack> outputs;

	public LycheeEmiAnvilRecipe(RecipeHolder<AnvilCraftingRecipe> recipe) {
		this.recipe = recipe;
		this.inputs = recipe.value().getIngredients().stream()
				.map(EmiIngredient::of)
				.toList();
		this.outputs = List.of(EmiStack.of(recipe.value().output()));
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.ANVIL_REPAIRING;
	}

	@Override
	public ResourceLocation getId() {
		return recipe.id();
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return inputs;
	}

	@Override
	public List<EmiStack> getOutputs() {
		return outputs;
	}

	@Override
	public int getDisplayWidth() {
		return 125;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.PLUS, 27, 3);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 1);
		widgets.addSlot(inputs.getFirst(), 0, 0);
		widgets.addSlot(inputs.size() == 2 ? inputs.getLast() : EmiStack.EMPTY, 49, 0);
		widgets.addSlot(outputs.getFirst(), 107, 0).recipeContext(this);

		LycheeEMIPlugin.addInfoIcon(widgets, recipe, 75, 1);
	}
}
