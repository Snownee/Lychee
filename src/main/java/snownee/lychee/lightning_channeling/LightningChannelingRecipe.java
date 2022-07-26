package snownee.lychee.lightning_channeling;

import java.util.List;
import java.util.stream.Stream;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.recipe.ItemShapelessRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

public class LightningChannelingRecipe extends ItemShapelessRecipe<LightningChannelingRecipe> {

	public LightningChannelingRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public LycheeRecipe.Serializer<?> getSerializer() {
		return RecipeSerializers.LIGHTNING_CHANNELING;
	}

	@Override
	public LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.LIGHTNING_CHANNELING;
	}

	public static void on(LightningBolt lightningBolt, List<Entity> list1) {
		Stream<ItemEntity> itemEntities = list1.stream().filter($ -> {
			return $ instanceof ItemEntity;
		}).map(ItemEntity.class::cast);
		RecipeTypes.LIGHTNING_CHANNELING.process(lightningBolt.level, itemEntities, $ -> {
			$.withParameter(LootContextParams.ORIGIN, lightningBolt.position());
			$.withParameter(LootContextParams.THIS_ENTITY, lightningBolt);
		});
	}

}
