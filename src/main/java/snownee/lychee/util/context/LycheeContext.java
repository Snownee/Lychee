package snownee.lychee.util.context;

import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import snownee.kiwi.recipe.EmptyRecipeInput;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.codec.KeyDispatchedMapMapCodec;

@SuppressWarnings("unchecked")
public class LycheeContext extends EmptyRecipeInput {
	private final Map<LycheeContextKey<?>, Object> context =
			new Object2ObjectOpenHashMap<>(LycheeRegistries.CONTEXTUAL.size());
	public static final Codec<LycheeContext> CODEC =
			new KeyDispatchedMapMapCodec<LycheeContextKey<?>, Object>(
					LycheeRegistries.CONTEXT.byNameCodec(),
					it -> it instanceof KeyedContextValue<?> value
							? DataResult.success(value.key())
							: DataResult.error(() -> "Can't determine key of " + it + " that isn't LycheeContextValue"),
					it -> {
						final var key = LycheeRegistries.CONTEXT.getKey(it);
						final var serializer = LycheeRegistries.CONTEXT_SERIALIZER.get(key);
						if (serializer != null) {
							return DataResult.success(serializer.codec().codec());
						}
						return DataResult.error(() -> it + " isn't serializable");
					},
					LycheeRegistries.CONTEXT_SERIALIZER
			).codec().xmap(it -> {
				final var context = new LycheeContext();
				context.putAll(it);
				return context;
			}, LycheeContext::asMap);

	@Nullable
	private Level level;

	public <T> T get(LycheeContextKey<T> type) {
		if (LycheeContextRequired.CONSTRUCTORS.containsKey(type)) {
			return (T) this.context.computeIfAbsent(type, (it) -> LycheeContextRequired.CONSTRUCTORS.get(it).apply(this));
		}
		return (T) context.get(type);
	}

	public <T> T put(LycheeContextKey<T> key, T value) {
		if (key == LycheeContextKey.LEVEL) {
			level = (Level) value;
		}
		return (T) context.put(key, value);
	}

	public void putAll(Map<? extends LycheeContextKey<?>, ?> map) {
		context.putAll(map);
	}

	public Map<LycheeContextKey<?>, Object> asMap() {
		return Map.copyOf(context);
	}

	public Level level() {
		if (level == null) {
			level = get(LycheeContextKey.LEVEL);
		}
		return Objects.requireNonNull(level);
	}

	@Override
	public int size() {
		return get(LycheeContextKey.ITEM).size();
	}

	@Override
	public @NotNull ItemStack getItem(final int index) {
		return get(LycheeContextKey.ITEM).get(index).get();
	}

	public void setItem(final int index, final ItemStack stack) {
		get(LycheeContextKey.ITEM).replace(index, stack);
	}
}
