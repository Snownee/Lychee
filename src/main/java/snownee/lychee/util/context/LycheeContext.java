package snownee.lychee.util.context;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import snownee.kiwi.recipe.EmptyRecipeInput;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.codec.KeyDispatchedMapMapCodec;
import snownee.lychee.util.recipe.ILycheeRecipe;

@SuppressWarnings("unchecked")
public class LycheeContext extends EmptyRecipeInput {
	public static final Codec<LycheeContext> CODEC =
			new KeyDispatchedMapMapCodec<>(
					LycheeRegistries.CONTEXT.byNameCodec(),
					it -> {
						var serializer = it.codec();
						if (serializer == null) {
							return DataResult.error(() -> it + " isn't serializable");
						}
						return DataResult.success((Codec<Object>) serializer);
					},
					LycheeRegistries.CONTEXT_SERIALIZER,
					() -> new Reference2ReferenceOpenHashMap<>(LycheeRegistries.CONTEXT.size())
			).codec().xmap(LycheeContext::new, LycheeContext::serializableContext);

	private final Map<LycheeContextKey<?>, Object> context;
	private @Nullable Level level;

	public LycheeContext() {
		this(new Reference2ReferenceOpenHashMap<>(LycheeRegistries.CONTEXT.size()));
	}

	public LycheeContext(Map<LycheeContextKey<?>, Object> context) {
		this.context = context;
	}

	public boolean has(LycheeContextKey<?> key, boolean createIfAbsent) {
		if (createIfAbsent && key.factory != null) {
			context.computeIfAbsent(key, it -> it.factory.apply(this));
		}
		return context.get(key) != null;
	}

	@Nullable
	public <T> T getOrNull(LycheeContextKey.Optional<T> key) {
		return (T) context.get(key);
	}

	public <T> T get(LycheeContextKey<T> key) {
		if (key.factory == null) {
			return (T) Objects.requireNonNull(context.get(key));
		} else {
			return (T) Objects.requireNonNull(context.computeIfAbsent(key, it -> it.factory.apply(this)));
		}
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

	public void removeAllExcept(Collection<LycheeContextKey<?>> keys) {
		context.keySet().removeIf(key -> !keys.contains(key));
	}

	public Map<LycheeContextKey<?>, Object> serializableContext() {
		return Map.ofEntries(context.entrySet()
				.stream()
				.filter(entry -> entry.getValue() != null && entry.getKey().codec() != null)
				.toArray(Map.Entry[]::new));
	}

	public Level level() {
		if (level == null) {
			level = get(LycheeContextKey.LEVEL);
		}
		return level;
	}

	@Override
	public int size() {
		return get(LycheeContextKey.ITEM).size();
	}

	@Override
	public ItemStack getItem(int index) {
		return get(LycheeContextKey.ITEM).get(index).get();
	}

	public void setItem(int index, ItemStack stack) {
		get(LycheeContextKey.ITEM).replace(index, stack);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("context", context)
				.append("level", level)
				.toString();
	}
}
