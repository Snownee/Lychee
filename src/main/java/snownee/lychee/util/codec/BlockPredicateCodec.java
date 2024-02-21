package snownee.lychee.util.codec;

import java.util.Optional;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class BlockPredicateCodec implements Codec<BlockPredicate> {

	private static final Cache<String, BlockPredicate> CACHE = CacheBuilder.newBuilder().build();

	public static BlockPredicate fromString(String s) {
		return CACHE.get(s, () -> {
			if (s.equals("*")) {
				//TODO how to handle ANY?
				return BlockPredicate.ANY;
			}
			if (s.startsWith("#")) {
				return BlockPredicate.Builder.block().of(TagKey.create(Registries.BLOCK, new ResourceLocation(s.substring(1)))).build();
			}
			ResourceLocation id = new ResourceLocation(s);
			if (!BuiltInRegistries.BLOCK.containsKey(id)) {
				throw new IllegalArgumentException("Unknown block: " + s);
			}
			return BlockPredicate.Builder.block().of(BuiltInRegistries.BLOCK.get(id)).build();
		});
	}

	@Override
	public <T> DataResult<Pair<BlockPredicate, T>> decode(DynamicOps<T> ops, T input) {
		Optional<String> stringValue = ops.getStringValue(input).result();
		return stringValue.map(s -> DataResult.success(Pair.of(fromString(s), ops.empty())))
				.orElseGet(() -> BlockPredicate.CODEC.decode(ops, input));
	}

	@Override
	public <T> DataResult<T> encode(BlockPredicate input, DynamicOps<T> ops, T prefix) {
		if (input.nbt().isEmpty() && input.properties().isEmpty()) {
			if (input.blocks().isEmpty() && input.tag().isEmpty()) {
				return DataResult.success(ops.createString("*"));
			}
			if (input.tag().isEmpty() && input.blocks().get().size() == 1) {
				return DataResult.success(ops.createString(input.blocks().get().get(0).unwrapKey().orElseThrow().location().toString()));
			}
			if (input.blocks().isEmpty() && input.tag().isPresent()) {
				return DataResult.success(ops.createString("#" + input.tag().get().location()));
			}
		}
		return BlockPredicate.CODEC.encode(input, ops, prefix);
	}
}
