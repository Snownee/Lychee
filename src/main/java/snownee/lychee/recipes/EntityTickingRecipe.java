package snownee.lychee.recipes;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.EntityPredicateExtensions;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;


public class EntityTickingRecipe extends LycheeRecipe<LycheeContext> {
	private final EntityPredicate predicate;
	private final EntityPredicate withoutTypePredicate;
	private final int interval;

	public EntityTickingRecipe(LycheeRecipeCommonProperties commonProperties, EntityPredicate predicate, int interval) {
		super(commonProperties);
		this.predicate = predicate;
		this.interval = interval;
		withoutTypePredicate = EntityPredicateExtensions.withoutType(predicate);
		onConstructed();
	}

	public EntityPredicate predicate() {
		return predicate;
	}

	public EntityPredicate withoutTypePredicate() {
		return withoutTypePredicate;
	}

	public int interval() {
		return interval;
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		return true;
	}

	@Override
	public LycheeRecipeSerializer<EntityTickingRecipe> getSerializer() {
		return RecipeSerializers.ENTITY_TICKING;
	}

	@Override
	public EntityTickingRecipeType getType() {
		return RecipeTypes.ENTITY_TICKING;
	}

	public static class Serializer implements LycheeRecipeSerializer<EntityTickingRecipe> {
		public static final MapCodec<EntityTickingRecipe> CODEC =
				RecordCodecBuilder.mapCodec(instance -> instance.group(
						LycheeRecipeCommonProperties.SIMPLE_MAP_CODEC.forGetter(EntityTickingRecipe::commonProperties),
						EntityPredicate.CODEC.fieldOf("entity").validate(it -> {
							if (it.entityType().isEmpty() || it.entityType().get().types().size() == 0) {
								return DataResult.error(() -> "EntityPredicate must have at least one entity type");
							}
							return DataResult.success(it);
						}).forGetter(EntityTickingRecipe::predicate),
						ExtraCodecs.POSITIVE_INT.optionalFieldOf("interval", 1).forGetter(EntityTickingRecipe::interval)
				).apply(instance, EntityTickingRecipe::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, EntityTickingRecipe> STREAM_CODEC =
				StreamCodec.composite(
						LycheeRecipeCommonProperties.STREAM_CODEC,
						EntityTickingRecipe::commonProperties,
						ByteBufCodecs.fromCodecWithRegistries(EntityPredicate.CODEC),
						EntityTickingRecipe::predicate,
						ByteBufCodecs.VAR_INT,
						EntityTickingRecipe::interval,
						EntityTickingRecipe::new);

		@Override
		public MapCodec<EntityTickingRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, EntityTickingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
