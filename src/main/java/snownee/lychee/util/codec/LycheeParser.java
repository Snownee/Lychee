package snownee.lychee.util.codec;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.MapLike;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
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
			});

	static DataResult<PostAction> action(String s) {
		try {
			return action(new StringReader(s));
		} catch (Exception e) {
			return DataResult.error(() -> "Failed to parse action %s: %s".formatted(s, e.getMessage()));
		}
	}

	static DataResult<PostAction> action(StringReader reader) throws Exception {
		ResourceLocation id = ResourceLocation.read(reader);
		LycheeParser<? extends PostAction> parser = ACTION_PARSERS.get(id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) ?
				id.getPath() :
				id.toString());
		DataResult<PostAction> result = null;
		if (parser != null) {
			//noinspection unchecked
			result = (DataResult<PostAction>) parser.parse(reader);
		}
		if (result == null || result.isError()) {
			PostActionType<?> actionType = LycheeRegistries.POST_ACTION.get(id);
			if (actionType == null && parser != null) {
				return result;
			} else if (actionType == null) {
				return DataResult.error(() -> "Unknown action type or parser: " + id);
			}
			//noinspection unchecked
			DataResult<PostAction> secondResult = (DataResult<PostAction>) actionType.codec().decode(
					JavaOps.INSTANCE,
					EmptyMapLike.INSTANCE);
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
		Class<?>[] paramTypes = Arrays.stream(components)
				.map(RecordComponent::getType)
				.toArray(Class<?>[]::new);
		return clazz.getDeclaredConstructor(paramTypes).newInstance(args);
	}

	DataResult<T> parse(StringReader reader) throws CommandSyntaxException;

	enum EmptyMapLike implements MapLike<Object> {
		INSTANCE;

		@Override
		@Nullable
		public Object get(Object key) {
			return null;
		}

		@Override
		@Nullable
		public Object get(String key) {
			return null;
		}

		@Override
		public Stream<Pair<Object, Object>> entries() {
			return Stream.empty();
		}

		@Override
		public String toString() {
			return "EmptyMapLike";
		}
	}
}
