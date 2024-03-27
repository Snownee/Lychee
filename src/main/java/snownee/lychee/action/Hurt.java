package snownee.lychee.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.mixin.DamageSourcesAccess;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record Hurt(
		PostActionCommonProperties commonProperties,
		MinMaxBounds.Doubles damage,
		ResourceKey<DamageType> source) implements PostAction {
	@Override
	public PostActionType<Hurt> type() {
		return PostActionTypes.HURT;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		var entity = lootParamsContext.get(LootContextParams.THIS_ENTITY);
		entity.invulnerableTime = 0;
		try {
			entity.hurt(
					((DamageSourcesAccess) entity.damageSources()).callSource(source),
					BoundsExtensions.random(damage, context.get(LycheeContextKey.RANDOM)) * times
			);
		} catch (Exception e) {
			Lychee.LOGGER.error("Failed to invoke Hurt action for entity " + entity, e);
		}
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(
				CommonProxy.makeDescriptionId("postAction", LycheeRegistries.POST_ACTION.getKey(type())),
				BoundsExtensions.getDescription(damage)
		);
	}

	public static class Type implements PostActionType<Hurt> {
		public static final Codec<Hurt> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(Hurt::commonProperties),
				MinMaxBounds.Doubles.CODEC.fieldOf("damage").forGetter(Hurt::damage),
				ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.DAMAGE_TYPE), "source", DamageTypes.GENERIC)
						.forGetter(Hurt::source)
		).apply(instance, Hurt::new));

		@Override
		public @NotNull Codec<Hurt> codec() {
			return CODEC;
		}
	}
}
