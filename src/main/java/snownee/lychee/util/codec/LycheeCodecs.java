package snownee.lychee.util.codec;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.kiwi.util.codec.KCodecs;

public final class LycheeCodecs {
	public static final ThreadLocal<@Nullable Unit> skipComponentsValidation = new ThreadLocal<>();
	private static final MapCodec<Integer> ITEM_STACK_COUNT = ExtraCodecs.NON_NEGATIVE_INT.fieldOf("count").orElse(1);

	private static final MapCodec<ItemStackTemplate> ITEM_STACK_TEMPLATE_MAP_ENCODER = RecordCodecBuilder.mapCodec(instance -> instance.group(
					Item.CODEC.fieldOf("id").forGetter(ItemStackTemplate::item),
					ITEM_STACK_COUNT.forGetter(ItemStackTemplate::count),
					DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStackTemplate::components))
			.apply(instance, ItemStackTemplate::new));
	private static final MapCodec<Optional<ItemStackTemplate>> OPTIONAL_ITEM_STACK_TEMPLATE_MAP_ENCODER = Codec.mapEither(
			ITEM_STACK_TEMPLATE_MAP_ENCODER.flatXmap(
					$ -> DataResult.success(Optional.of($)),
					$ -> $.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Item must not be minecraft:air"))),
			BuiltInRegistries.ITEM.holderByNameCodec().validate($ -> $.value() == Items.AIR ?
					DataResult.success($) :
					DataResult.error(() -> "Item must be minecraft:air")).fieldOf("id").flatXmap(
					_ -> DataResult.success(Optional.<ItemStackTemplate>empty()),
					$ -> $.isEmpty() ?
							DataResult.success(BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR)) :
							DataResult.error(() -> "Item must be minecraft:air"))).xmap(
			Either::unwrap,
			$ -> $.isPresent() ? Either.left($) : Either.right($));

	public static final MapCodec<Optional<ItemStackTemplate>> OPTIONAL_ITEM_STACK_TEMPLATE_MAP_CODEC = MapCodec.of(
			OPTIONAL_ITEM_STACK_TEMPLATE_MAP_ENCODER, new MapDecoder.Implementation<>() {
				@Override
				public <T> DataResult<Optional<ItemStackTemplate>> decode(DynamicOps<T> ops, MapLike<T> input) {
					DataResult<Optional<ItemStackTemplate>> result = OPTIONAL_ITEM_STACK_TEMPLATE_MAP_ENCODER.decode(ops, input);
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
							return Stream.empty();
						}

						@Override
						public Stream<HolderLookup.RegistryLookup<?>> listRegistries() {
							return Stream.empty();
						}

						@Override
						public <R> Optional<HolderLookup.RegistryLookup<R>> lookup(ResourceKey<? extends Registry<? extends R>> resourceKey) {
							return registryOps.getter(resourceKey).map($ -> new HolderLookup.RegistryLookup<>() {
								@Override
								public ResourceKey<? extends Registry<? extends R>> key() {
									return resourceKey;
								}

								@Override
								public Lifecycle registryLifecycle() {
									return Lifecycle.stable();
								}

								@Override
								public Stream<Holder.Reference<R>> listElements() {
									return Stream.empty();
								}

								@Override
								public Stream<HolderSet.Named<R>> listTags() {
									return Stream.empty();
								}

								@Override
								public Optional<Holder.Reference<R>> get(ResourceKey<R> id) {
									return $.get(id);
								}

								@Override
								public Optional<HolderSet.Named<R>> get(TagKey<R> id) {
									return $.get(id);
								}
							});
						}

						@Override
						public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> dynamicOps) {
							return registryOps.withParent(dynamicOps);
						}
					});
					ItemInput itemInput;
					try {
						skipComponentsValidation.set(Unit.INSTANCE);
						itemInput = parser.parse(new StringReader(ops.getStringValue(id).getOrThrow()));
					} catch (Exception e) {
						return DataResult.error(e::getMessage);
					} finally {
						skipComponentsValidation.remove();
					}
					if (input.get("components") != null) {
						return DataResult.error(() -> "id with brackets cannot have the components field");
					}
					DataResult<Integer> count = ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).decode(ops, input);
					if (count.isError()) {
						return DataResult.error(() -> "Failed to decode count: " + count.error().orElseThrow().message());
					}
					return DataResult.success(Optional.of(new ItemStackTemplate(
							itemInput.item(),
							count.getOrThrow(),
							itemInput.components())));
				}

				@Override
				public <T> Stream<T> keys(DynamicOps<T> ops) {
					return ITEM_STACK_TEMPLATE_MAP_ENCODER.keys(ops);
				}
			}, () -> "MapCodec[ItemStackTemplate]");
	public static final MapCodec<ItemStackTemplate> ITEM_STACK_TEMPLATE_MAP_CODEC = OPTIONAL_ITEM_STACK_TEMPLATE_MAP_CODEC.flatXmap(
			$ -> {
				return $.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "ItemStack cannot be empty"));
			}, $ -> DataResult.success(Optional.of($)));
	public static final Codec<ItemStackTemplate> ITEM_STACK_TEMPLATE = Codec.withAlternative(
			ITEM_STACK_TEMPLATE_MAP_CODEC.codec(),
			ExtraCodecs.NON_EMPTY_STRING.flatXmap(
					s -> KCodecs.tryCatch(() -> ParsedItem.read(new StringReader(s)).template()),
					_ -> DataResult.error(() -> "Encoding shorthand ItemStack is not supported")));

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
	public static final Codec<Ingredient> INGREDIENT = Codec.withAlternative(
			Ingredient.CODEC, ExtraCodecs.NON_EMPTY_STRING.flatXmap(
					s -> KCodecs.tryCatch(() -> {
						StringReader reader = new StringReader(s);
						ParsedItem parsedItem = ParsedItem.read(reader);
						Preconditions.checkArgument(!reader.canRead(), "Cannot parse %s", s);
						return parsedItem.ingredient();
					}), _ -> DataResult.error(() -> "Encoding shorthand Ingredient is not supported")));

	public static final Codec<SizedIngredient> SIZED_INGREDIENT = Codec.withAlternative(
			SizedIngredient.CODEC, ExtraCodecs.NON_EMPTY_STRING.flatXmap(
					s -> KCodecs.tryCatch(() -> {
						StringReader reader = new StringReader(s);
						ParsedItem parsedItem = ParsedItem.read(reader);
						Preconditions.checkArgument(!reader.canRead(), "Cannot parse %s", s);
						return parsedItem.sizedIngredient();
					}), _ -> DataResult.error(() -> "Encoding shorthand SizedIngredient is not supported")));

	public static final Codec<List<DataComponentType<?>>> WILDCARD_COMPONENTS = Codec.withAlternative(
			ExtraCodecs.compactListCodec(DataComponentType.CODEC), Codec.STRING.flatXmap(
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

	public static <A> MapCodec<A> optionalInput(Codec<A> codec, String fieldName, String fallback) {
		return codec.fieldOf(fieldName).mapResult(new MapCodec.ResultFunction<>() {
			@Override
			public <T> DataResult<A> apply(DynamicOps<T> ops, MapLike<T> input, DataResult<A> a) {
				if (a.isSuccess() || input.get(fieldName) != null) {
					return a;
				}
				return codec.parse(ops, ops.createString(fallback));
			}

			@Override
			public <T> RecordBuilder<T> coApply(DynamicOps<T> ops, A input, RecordBuilder<T> t) {
				return t;
			}
		});
	}
}
