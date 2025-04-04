package snownee.lychee.util.context;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import snownee.kiwi.recipe.EmptyRecipeInput;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.codec.KeyDispatchedMapMapCodec;
import snownee.lychee.util.recipe.ILycheeRecipe;

@NotNullByDefault
@SuppressWarnings("unchecked")
public class LycheeContext extends EmptyRecipeInput {
	private final Map<LycheeContextKey<?>, Object> context = new Object2ObjectOpenHashMap<>(10);
	public static final Codec<LycheeContext> CODEC =
			new KeyDispatchedMapMapCodec<>(
					LycheeRegistries.CONTEXT.byNameCodec(),
					it -> {
						final var key = LycheeRegistries.CONTEXT.getKey(it);
						final var serializer = LycheeRegistries.CONTEXT_SERIALIZER.get(key);
						if (serializer == null) {
							return DataResult.error(() -> it + " isn't serializable");
						}
						return DataResult.success((Codec<Object>) serializer);
					},
					LycheeRegistries.CONTEXT_SERIALIZER
			).codec().xmap(
					it -> {
						final var context = new LycheeContext();
						context.putAll(it);
						return context;
					}, LycheeContext::asMap);

	@Nullable
	private Level level;

	@Nullable
	public <T> T getOrNull(LycheeContextKey<T> key) {
		if (LycheeContextRequired.CONSTRUCTORS.containsKey(key)) {
			return (T) this.context.computeIfAbsent(key, (it) -> LycheeContextRequired.CONSTRUCTORS.get(it).apply(this));
		}
		return (T) context.get(key);
	}

	public <T> T get(LycheeContextKey<T> key) {
		return Objects.requireNonNull(getOrNull(key));
	}

	@Nullable
	public <T> T put(LycheeContextKey<T> key, T value) {
		if (key == LycheeContextKey.LEVEL) {
			level = (Level) value;
		}
		return (T) context.put(key, value);
	}

	public void put(RecipeHolder<? extends ILycheeRecipe<?>> recipeHolder) {
		put(LycheeContextKey.RECIPE, recipeHolder.value());
		put(LycheeContextKey.RECIPE_ID, recipeHolder.id());
	}

	public void putAll(Map<LycheeContextKey<?>, ?> map) {
		context.putAll(map);
	}

	public void removeAllExcept(Collection<LycheeContextKey<?>> keys) {
		context.keySet().removeIf(key -> !keys.contains(key));
	}

	public Map<LycheeContextKey<?>, Object> asMap() {
		return Map.copyOf(context);
	}

	public Level level() {
		if (level == null) {
			level = getOrNull(LycheeContextKey.LEVEL);
		}
		return Objects.requireNonNull(level);
	}

	@Override
	public int size() {
		return get(LycheeContextKey.ITEM).size();
	}

	@Override
	public ItemStack getItem(final int index) {
		return get(LycheeContextKey.ITEM).get(index).get();
	}

	public void setItem(final int index, final ItemStack stack) {
		get(LycheeContextKey.ITEM).replace(index, stack);
	}
}
