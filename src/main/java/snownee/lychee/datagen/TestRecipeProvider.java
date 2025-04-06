package snownee.lychee.datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.lychee.Lychee;
import snownee.lychee.contextual.SkyDarken;


public class TestRecipeProvider extends FabricRecipeProvider implements LycheeBuilder {
	public TestRecipeProvider(
			FabricDataOutput output,
			CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public CompletableFuture<?> run(CachedOutput writer, HolderLookup.Provider wrapperLookup) {
		setup(wrapperLookup);
		return super.run(writer, wrapperLookup);
	}

	@Override
	public void buildRecipes(RecipeOutput exporter) {
		itemBurningRecipe(SizedIngredient.of(ItemTags.BEDS, 2))
				.comment("Datagen test 1")
				.chance(0.5F)
				.post(delay(0.5F))
				.post(place("stone", new BlockPos(0, 2, 0))
						.condition(new SkyDarken(MinMaxBounds.Ints.ANY, true)))
				.export(Lychee.id("datagen_1"), exporter);
	}
}
