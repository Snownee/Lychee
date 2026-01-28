package snownee.lychee.datagen;

import java.util.concurrent.CompletableFuture;

import com.mojang.serialization.JavaOps;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.lychee.Lychee;
import snownee.lychee.contextual.SkyDarken;


public class TestRecipeProvider extends FabricRecipeProvider implements LycheeBuilder {
	public TestRecipeProvider(
			FabricPackOutput output,
			CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
		setup(registries);
		return new RecipeProvider(registries, output) {
			@Override
			protected void buildRecipes() {
				HolderLookup.RegistryLookup<Item> items = registries.lookupOrThrow(Registries.ITEM);
				itemBurningRecipe(SizedIngredient.of(items, ItemTags.BEDS, 2))
						.comment("Datagen test 1")
						.chance(0.5F)
						.post(delay(0.5F))
						.post(place("stone", new BlockPos(0, 2, 0))
								.condition(new SkyDarken(MinMaxBounds.Ints.ANY, true)))
						.export(Lychee.id("datagen_1"), output);
			}
		};
	}

	@Override
	public String getName() {
		return "Lychee Recipes";
	}
}
