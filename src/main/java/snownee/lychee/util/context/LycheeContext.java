package snownee.lychee.util.context;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.recipe.EmptyContainer;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.codec.KeyDispatchedMapCodec;

@SuppressWarnings("unchecked")
public class LycheeContext extends EmptyContainer {
	private final Map<LycheeContextKey<?>, Object> context = new Object2ObjectOpenHashMap<>(LycheeRegistries.CONTEXTUAL.size());
	public static final Codec<LycheeContext> CODEC =
			new KeyDispatchedMapCodec<LycheeContextKey<?>, Object>(
					LycheeRegistries.CONTEXT.byNameCodec(),
					it -> it instanceof KeyedContextValue<?> value
						  ? DataResult.success(value.key())
						  : DataResult.error(() -> "Can't determine key of " + it + "that isn't LycheeContextValue"),
					it -> {
						final var key = LycheeRegistries.CONTEXT.getKey(it);
						final var serializer = LycheeRegistries.CONTEXT_SERIALIZER.get(key);
						if (serializer != null) return DataResult.success(serializer.codec());
						return DataResult.error(() -> it + " isn't serializable");
					},
					LycheeRegistries.CONTEXT_SERIALIZER
			).codec().xmap(it -> {
				final var context = new LycheeContext();
				context.putAll(it);
				return context;
			}, LycheeContext::asMap);

	public <T> T get(LycheeContextKey<T> type) {
		return (T) context.get(type);
	}

	public <T> T put(LycheeContextKey<T> key, T value) {
		return (T) context.put(key, value);
	}

	public void putAll(Map<? extends LycheeContextKey<?>, ?> map) {
		context.putAll(map);
	}

	public Map<LycheeContextKey<?>, Object> asMap() {
		return Map.copyOf(context);
	}

	@Override
	public int getContainerSize() {
		return get(LycheeContextKey.ITEM).size();
	}

	@Override
	public @NotNull ItemStack getItem(final int index) {
		return get(LycheeContextKey.ITEM).get(index).get();
	}

	@Override
	public void setItem(final int index, final ItemStack stack) {
		get(LycheeContextKey.ITEM).replace(index, stack);
	}
}
