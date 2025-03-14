package snownee.kiwi.recipe_;

import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class SizedIngredient {
	public static final SizedIngredient EMPTY = new SizedIngredient(Ingredient.EMPTY, 1);

	public static final Codec<SizedIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Ingredient.MAP_CODEC_NONEMPTY.forGetter(SizedIngredient::ingredient),
					ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(SizedIngredient::count))
			.apply(instance, SizedIngredient::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SizedIngredient> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC,
			SizedIngredient::ingredient,
			ByteBufCodecs.VAR_INT,
			SizedIngredient::count,
			SizedIngredient::new);

	public static SizedIngredient of(ItemLike item, int count) {
		return new SizedIngredient(Ingredient.of(item), count);
	}

	public static SizedIngredient of(TagKey<Item> tag, int count) {
		return new SizedIngredient(Ingredient.of(tag), count);
	}

	private static MapCodec<Ingredient> makeIngredientMapCodec() {
		// Dispatch codec for custom ingredient types, else fallback to vanilla ingredient codec.
		return NeoForgeExtraCodecs.<IngredientType<?>, ICustomIngredient, Ingredient.Value>dispatchMapOrElse(
						NeoForgeRegistries.INGREDIENT_TYPES.byNameCodec(),
						ICustomIngredient::getType,
						IngredientType::codec,
						Ingredient.Value.MAP_CODEC)
				.xmap(
						either -> either.map(ICustomIngredient::toVanilla, v -> Ingredient.fromValues(Stream.of(v))), ingredient -> {
							if (!ingredient.isCustom()) {
								var values = ingredient.getValues();
								if (values.length == 1) {
									return Either.right(values[0]);
								}
								// Convert vanilla ingredients with 2+ values to a CompoundIngredient. Empty ingredients are not allowed here.
								return Either.left(new CompoundIngredient(Stream.of(ingredient.getValues())
										.map(v -> Ingredient.fromValues(Stream.of(v)))
										.toList()));
							}
							return Either.left(ingredient.getCustomIngredient());
						})
				.validate(ingredient -> {
					if (!ingredient.isCustom() && ingredient.getValues().length == 0) {
						return DataResult.error(() -> "Cannot serialize empty ingredient using the map codec");
					}
					return DataResult.success(ingredient);
				});
	}

	private final Ingredient ingredient;
	private final int count;
	@Nullable
	private ItemStack[] cachedStacks;

	public SizedIngredient(Ingredient ingredient, int count) {
		Preconditions.checkArgument(count > 0, "Count must be positive");
		this.ingredient = ingredient;
		this.count = count;
	}

	public Ingredient ingredient() {
		return ingredient;
	}

	public int count() {
		return count;
	}

	public boolean test(ItemStack stack) {
		return ingredient.test(stack) && stack.getCount() >= count;
	}

	public ItemStack[] getItems() {
		if (cachedStacks == null) {
			cachedStacks = Stream.of(ingredient.getItems()).map(s -> s.copyWithCount(count)).toArray(ItemStack[]::new);
		}
		return cachedStacks;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SizedIngredient other)) {
			return false;
		}
		return count == other.count && ingredient.equals(other.ingredient);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ingredient, count);
	}

	@Override
	public String toString() {
		return count + "x " + ingredient;
	}
}
