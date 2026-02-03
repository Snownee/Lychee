package snownee.lychee.util.codec;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.NullOps;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.contextual.Chance;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;

public interface LycheeParser<T> {
	Map<String, LycheeParser<? extends PostAction>> ACTION_PARSERS = Util.make(
			Maps.newHashMap(), map -> {
				map.put("drop", new ActionParsers.Drop());
				map.put("place", new ActionParsers.Place());
				ActionParsers.Run run = new ActionParsers.Run();
				map.put("run", run);
				map.put("execute", run);
				map.put("delay", new ActionParsers.DelayParser());
				map.put("set_block", new ActionParsers.SetBlockParser());
				map.put("move", new ActionParsers.MoveParser());
				map.put("add_item_cooldown", new ActionParsers.ItemCooldown());
				map.put("copy_component", new ActionParsers.CopyComponentParser());
				map.put("remove_component", new ActionParsers.RemoveComponentParser());
			});

	static DataResult<PostAction> action(Context context, String s) {
		try {
			return action(context, new StringReader(s));
		} catch (Exception e) {
			return DataResult.error(() -> "Failed to parse action %s: %s".formatted(s, e.getMessage()));
		}
	}

	static DataResult<PostAction> action(Context context, StringReader reader) throws Exception {
		Identifier id = Identifier.read(reader);
		LycheeParser<? extends PostAction> parser = ACTION_PARSERS.get(id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) ?
				id.getPath() :
				id.toString());
		DataResult<PostAction> result = null;
		if (parser != null) {
			//noinspection unchecked
			result = (DataResult<PostAction>) parser.parse(context, reader);
		}
		if (result == null || result.isError()) {
			PostActionType<?> actionType = LycheeRegistries.POST_ACTION.getValue(id);
			if (actionType == null && parser != null) {
				return result;
			} else if (actionType == null) {
				return DataResult.error(() -> "Unknown action type or parser: " + id);
			}
			//noinspection unchecked
			DataResult<PostAction> secondResult = (DataResult<PostAction>) actionType.codec().decode(
					NullOps.INSTANCE,
					NullOps.INSTANCE.getMap(Unit.INSTANCE).getOrThrow());
			if (secondResult.isError()) {
				if (result != null) {
					String firstError = result.error().orElseThrow().message();
					String secondError = secondResult.error().orElseThrow().message();
					return DataResult.error(() -> "Failed to parse action %s: <First: %s; Second: %s>".formatted(
							id,
							firstError,
							secondError));
				}
				return secondResult.mapError(err -> "Failed to parse action of type " + id + ": " + err);
			}
			result = secondResult;
		}
		if (!reader.canRead()) {
			return result;
		}
		reader.expect(' ');
		PostAction action = result.getOrThrow();
		PostActionCommonProperties.Builder properties = action.commonProperties().builder();
		boolean chance = false;
		while (reader.canRead()) {
			reader.expect('/');
			String word = reader.readUnquotedString().toLowerCase();
			if (word.equals("hide")) {
				properties.hide();
			} else {
				try {
					float f = Float.parseFloat(word);
					Preconditions.checkArgument(f > 0 && f < 1, "Chance must be between 0 and 1");
					Preconditions.checkState(!chance, "Chance already set");
					chance = true;
					properties.conditions().add(new Chance(f));
				} catch (NumberFormatException e) {
					throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
				}
			}
		}
		return DataResult.success(replaceProperties(action, properties.build()));
	}

	private static PostAction replaceProperties(PostAction action, PostActionCommonProperties properties) throws Exception {
		Class<? extends PostAction> clazz = action.getClass();
		Preconditions.checkArgument(clazz.isRecord(), "Not a record: %s", action);
		RecordComponent[] components = clazz.getRecordComponents();
		Object[] args = new Object[components.length];
		for (int i = 0; i < components.length; i++) {
			if (i == 0) {
				args[i] = properties;
			} else {
				args[i] = components[i].getAccessor().invoke(action);
			}
		}
		Class<?>[] paramTypes = Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
		return clazz.getDeclaredConstructor(paramTypes).newInstance(args);
	}

	DataResult<T> parse(Context context, StringReader reader);

	record Context(RegistryOps<?> registryOps) implements HolderGetter.Provider {
		@Override
		public <T> Optional<? extends HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> key) {
			return registryOps.getter(key);
		}
	}
}
