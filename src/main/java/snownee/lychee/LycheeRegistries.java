package snownee.lychee;

import java.util.Objects;

import com.mojang.serialization.Codec;

import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.ui.UIElementType;

@KiwiModule("registries")
public final class LycheeRegistries extends AbstractModule {
	public static final MappedRegistry<ContextualConditionType<?>> CONTEXTUAL = register("contextual");
	public static final MappedRegistry<PostActionType<?>> POST_ACTION = register("post_action");
	public static final MappedRegistry<LycheeContextKey<?>> CONTEXT = register("context");
	public static final MappedRegistry<Codec<?>> CONTEXT_SERIALIZER = register("context_serializer");
	public static final MappedRegistry<UIElementType<?>> UI_ELEMENT = register("ui_element");

	@Override
	public void addRegistries() {
		Objects.requireNonNull(CONTEXTUAL);
	}

	private static <T> MappedRegistry<T> register(String id) {
		return (MappedRegistry<T>) new RegistryBuilder<>(ResourceKey.<T>createRegistryKey(Lychee.id(id))).sync(true).create();
	}
}
