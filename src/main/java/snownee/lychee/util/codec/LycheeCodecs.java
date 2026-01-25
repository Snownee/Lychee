package snownee.lychee.util.codec;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.kiwi.util.codec.KCodecs;

public final class LycheeCodecs {
	private static final MapCodec<Integer> ITEM_STACK_COUNT = ExtraCodecs.NON_NEGATIVE_INT.fieldOf("count").orElse(1);

	private static final MapCodec<ItemStack> ITEM_STACK_MAP_ENCODER = RecordCodecBuilder.mapCodec(instance -> instance.group(
					BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id").forGetter(ItemStack::typeHolder),
					ITEM_STACK_COUNT.forGetter(ItemStack::getCount),
					DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch))
			.apply(instance, ItemStack::new));

	public static final MapCodec<ItemStack> ITEM_STACK_MAP_CODEC = MapCodec.of(
			ITEM_STACK_MAP_ENCODER, new MapDecoder.Implementation<>() {
				@Override
				public <T> DataResult<ItemStack> decode(DynamicOps<T> ops, MapLike<T> input) {
					DataResult<ItemStack> result = ITEM_STACK_MAP_ENCODER.decode(ops, input);
					if (result.isSuccess()) {
						return result;
					}
					T id = input.get("id");
					if (id == null) {
						return DataResult.error(() -> "Missing id");
					}
					if (!(ops instanceof RegistryOps<T> registryOps)) {
						return DataResult.error(() -> "Not a registry ops");
					}
					ItemParser parser = new ItemParser(new HolderLookup.Provider() {
						@Override
						public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
							throw new IllegalStateException();
						}

						@Override
						public Stream<HolderLookup.RegistryLookup<?>> listRegistries() {
							throw new IllegalStateException();
						}

						@Override
						public <R> Optional<HolderLookup.RegistryLookup<R>> lookup(ResourceKey<? extends Registry<? extends R>> resourceKey) {
							//noinspection unchecked
							return Optional.of((HolderLookup.RegistryLookup<R>) BuiltInRegistries.ITEM);
						}

						@Override
						public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> dynamicOps) {
							return registryOps.withParent(dynamicOps);
						}
					});
					ItemParser.ItemResult itemResult;
					try {
						itemResult = parser.parse(new StringReader(ops.getStringValue(id).getOrThrow()));
					} catch (Exception e) {
						return DataResult.error(e::getMessage);
					}
					if (input.get("components") != null) {
						return DataResult.error(() -> "id with brackets cannot have the components field");
					}
					DataResult<Integer> count = ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).decode(ops, input);
					if (count.isError()) {
						return DataResult.error(() -> "Failed to decode count: " + count.error().orElseThrow().message());
					}
					return DataResult.success(new ItemStack(itemResult.item(), count.getOrThrow(), itemResult.components()));
				}

				@Override
				public <T> Stream<T> keys(DynamicOps<T> ops) {
					return ITEM_STACK_MAP_ENCODER.keys(ops);
				}
			}, () -> "MapCodec[ItemStack]");

	public static final MapCodec<ItemStack> NONEMPTY_ITEM_STACK_MAP_CODEC = ITEM_STACK_MAP_CODEC.validate(stack -> {
		if (stack.isEmpty()) {
			return DataResult.error(() -> "ItemStack cannot be empty");
		}
		return DataResult.success(stack);
	});

	public static final Codec<ItemStack> ITEM_STACK = Codec.withAlternative(
			NONEMPTY_ITEM_STACK_MAP_CODEC.codec(), ExtraCodecs.NON_EMPTY_STRING.flatXmap(
					s -> KCodecs.tryCatch(() -> ParsedItem.read(new StringReader(s)).itemStack()),
					stack -> DataResult.error(() -> "Encoding shorthand ItemStack is not supported")
			));

	public static final Codec<ItemStackTemplate> ITEM_STACK_TEMPLATE = null;
	public static final MapCodec<ItemStackTemplate> ITEM_STACK_TEMPLATE_MAP_CODEC = null;
	public static final MapCodec<ItemStackTemplate> NONEMPTY_ITEM_STACK_TEMPLATE_MAP_CODEC = null;

	public static final MapCodec<BlockPos> OFFSET = RecordCodecBuilder.mapCodec(posInstance -> posInstance.group(
			Codec.INT.optionalFieldOf("offsetX", 0).forGetter(Vec3i::getX),
			Codec.INT.optionalFieldOf("offsetY", 0).forGetter(Vec3i::getY),
			Codec.INT.optionalFieldOf("offsetZ", 0).forGetter(Vec3i::getZ)).apply(
			posInstance, (x, y, z) -> {
				if (x == 0 && y == 0 && z == 0) {
					return BlockPos.ZERO;
				}
				return new BlockPos(x, y, z);
			}));

	public static <T, L extends List<T>> Codec<L> sizeLimit(Codec<L> listCodec, int minSize, int maxSize) {
		return listCodec.validate(list -> {
			if (list.size() < minSize) {
				return DataResult.error(() -> "List is too short: " + minSize + ", expected range [" + minSize + "-" + maxSize + "]");
			}
			if (list.size() > maxSize) {
				return DataResult.error(() -> "List is too long: " + maxSize + ", expected range [" + minSize + "-" + maxSize + "]");
			}
			return DataResult.success(list);
		});
	}

	//TODO move to Kiwi
	public static final Codec<Ingredient> NONEMPTY_INGREDIENT = Codec.withAlternative(
			Ingredient.CODEC, ExtraCodecs.NON_EMPTY_STRING.flatXmap(
					s -> KCodecs.tryCatch(() -> {
						StringReader reader = new StringReader(s);
						ParsedItem parsedItem = ParsedItem.read(reader);
						Preconditions.checkArgument(!reader.canRead(), "Cannot parse %s", s);
						return parsedItem.ingredient();
					}), ingredient -> DataResult.error(() -> "Encoding shorthand Ingredient is not supported")
			));

	//TODO move to Kiwi
	public static final Codec<SizedIngredient> SIZED_INGREDIENT = Codec.withAlternative(
			SizedIngredient.CODEC, ExtraCodecs.NON_EMPTY_STRING.flatXmap(
					s -> KCodecs.tryCatch(() -> {
						StringReader reader = new StringReader(s);
						ParsedItem parsedItem = ParsedItem.read(reader);
						Preconditions.checkArgument(!reader.canRead(), "Cannot parse %s", s);
						return parsedItem.sizedIngredient();
					}), ingredient -> DataResult.error(() -> "Encoding shorthand SizedIngredient is not supported")
			)
	);

	public static final Codec<List<DataComponentType<?>>> WILDCARD_COMPONENTS = Codec.withAlternative(
			KCodecs.compactList(DataComponentType.CODEC), Codec.STRING.flatXmap(
					s -> {
						if (s.equals("*")) {
							return DataResult.success(List.of());
						}
						return DataResult.error(() -> "Expected '*'");
					}, componentTypes -> {
						if (componentTypes.isEmpty()) {
							return DataResult.success("*");
						}
						return DataResult.error(() -> "Expected empty list");
					}));
}
