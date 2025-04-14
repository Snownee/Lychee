package snownee.lychee;

import com.mojang.serialization.Codec;

import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.ui.UIElementType;

public final class LycheeRegistries {
	public static final MappedRegistry<ContextualConditionType<?>> CONTEXTUAL = register("contextual");
	public static final MappedRegistry<PostActionType<?>> POST_ACTION = register("post_action");
	public static final MappedRegistry<LycheeContextKey<?>> CONTEXT = register("context");
	public static final MappedRegistry<Codec<?>> CONTEXT_SERIALIZER = register("context_serializer");
	public static final MappedRegistry<UIElementType<?>> UI_ELEMENT = register("ui_element");

	public static void init(NewRegistryEvent event) {
		event.register(CONTEXTUAL);
		event.register(POST_ACTION);
		event.register(CONTEXT);
		event.register(CONTEXT_SERIALIZER);
		event.register(UI_ELEMENT);
	}

	private static <T> MappedRegistry<T> register(String id) {
		return (MappedRegistry<T>) new RegistryBuilder<>(ResourceKey.<T>createRegistryKey(Lychee.id(id))).sync(true).create();
	}
}
