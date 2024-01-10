package snownee.lychee;

import java.util.Iterator;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import snownee.kiwi.Kiwi;
import snownee.lychee.core.contextual.ContextualConditionType;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.util.CommonProxy;

public final class LycheeRegistries {

	public static MappedRegistry<ContextualConditionType<?>> CONTEXTUAL;

	public static MappedRegistry<PostActionType<?>> POST_ACTION;

	public static void init(NewRegistryEvent event) {
		LycheeRegistries.<ContextualConditionType<?>>register("contextual", ContextualConditionType.class, event, v -> CONTEXTUAL = v);
		LycheeRegistries.<PostActionType<?>>register("post_action", PostActionType.class, event, v -> POST_ACTION = v);
	}

	private static <T> void register(String name, Class<?> clazz, NewRegistryEvent event, Consumer<MappedRegistry<T>> consumer) {
		RegistryBuilder<T> builder = new RegistryBuilder<T>().setName(new ResourceLocation(Lychee.ID, name));
		event.create(builder, v -> {
			consumer.accept(new MappedRegistry<>(v));
			if (CommonProxy.hasKiwi) {
				Kiwi.registerRegistry(v, clazz);
			}
		});
	}

	public record MappedRegistry<T>(IForgeRegistry<T> registry) implements Iterable<T> { //TODO 1.20 move to Kiwi

		public ResourceKey<Registry<T>> key() {
			return registry.getRegistryKey();
		}

		public void register(ResourceLocation id, T t) {
			registry.register(id, t);
		}

		public T get(ResourceLocation key) {
			return registry.getValue(key);
		}

		public ResourceLocation getKey(T t) {
			return registry.getKey(t);
		}

		@NotNull
		@Override
		public Iterator<T> iterator() {
			return registry.iterator();
		}
	}
}
