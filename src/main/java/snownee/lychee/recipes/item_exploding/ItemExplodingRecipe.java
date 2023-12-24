package snownee.lychee.recipes.item_exploding;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.recipe.ItemShapelessRecipe;
import snownee.lychee.util.recipe.OldLycheeRecipe;
import snownee.lychee.util.recipe.type.LycheeRecipeType;

public class ItemExplodingRecipe extends ItemShapelessRecipe<ItemExplodingRecipe> {

	public ItemExplodingRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public OldLycheeRecipe.@NotNull Serializer<?> getSerializer() {
		return RecipeSerializers.ITEM_EXPLODING;
	}

	@Override
	public @NotNull LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.ITEM_EXPLODING;
	}

	public static void on(Level level, double x, double y, double z, List<Entity> list1, float radius) {
		if (level.isClientSide) {
			return;
		}
		Stream<ItemEntity> itemEntities = list1.stream().filter($ -> {
			return $ instanceof ItemEntity;
		}).map(ItemEntity.class::cast);
		RecipeTypes.ITEM_EXPLODING.process(level, itemEntities, $ -> {
			$.withParameter(LootContextParams.ORIGIN, new Vec3(x, y, z));
			$.withParameter(LootContextParams.EXPLOSION_RADIUS, radius);
		});
	}

}
