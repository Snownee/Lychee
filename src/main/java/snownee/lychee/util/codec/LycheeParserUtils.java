package snownee.lychee.util.codec;

import java.util.Optional;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;

import net.minecraft.CharPredicate;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import snownee.kiwi.util.codec.ThrowingFunction;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public class LycheeParserUtils {
	private static final char SYNTAX_ESCAPE = '\\';

	public static String readStringUntil(StringReader reader, CharPredicate predicate) throws CommandSyntaxException {
		final StringBuilder result = new StringBuilder();
		boolean escaped = false;
		while (reader.canRead()) {
			final char c = reader.read();
			if (escaped) {
				if (predicate.test(c) || c == SYNTAX_ESCAPE) {
					result.append(c);
					escaped = false;
				} else {
					reader.setCursor(reader.getCursor() - 1);
					throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape().createWithContext(reader, String.valueOf(c));
				}
			} else if (c == SYNTAX_ESCAPE) {
				escaped = true;
			} else if (predicate.test(c)) {
				reader.setCursor(reader.getCursor() - 1);
				return result.toString();
			} else {
				result.append(c);
			}
		}

		return result.toString();
	}

	public static DataResult<BlockPredicate> readBlock(
			HolderGetter<Block> lookup,
			StringReader reader,
			boolean forTesting) throws CommandSyntaxException {
		StringBuilder sb = new StringBuilder();
		sb.append(readStringUntil(reader, c -> c == ' ' || c == '[' || c == '{'));
		if (reader.canRead()) {
			char c = reader.peek();
			if (c == '[') {
				sb.append(reader.readStringUntil(']')).append(']');
			}
		}
		if (reader.canRead()) {
			char c = reader.peek();
			if (c == '{') {
				sb.append(reader.readStringUntil('}')).append('}');
			}
		}
		return BlockPredicateExtensions.fromString(lookup, sb.toString(), forTesting);
	}

	public static DataResult<BlockPos> readPos(StringReader reader) throws CommandSyntaxException {
		int x = reader.readInt();
		reader.expect(' ');
		int y = reader.readInt();
		reader.expect(' ');
		int z = reader.readInt();
		return DataResult.success(new BlockPos(x, y, z));
	}

	public static DataResult<Vec3> readVec3(StringReader reader) throws CommandSyntaxException {
		double x = reader.readDouble();
		reader.expect(' ');
		double y = reader.readDouble();
		reader.expect(' ');
		double z = reader.readDouble();
		return DataResult.success(new Vec3(x, y, z));
	}

	public static <T> Optional<DataResult<T>> readParam(StringReader reader, ThrowingFunction<StringReader, DataResult<T>> parser) {
		int cursor = reader.getCursor();
		try {
			reader.expect(' ');
			DataResult<T> result = parser.apply(reader);
			if (result.isError()) {
				reader.setCursor(cursor);
			}
			return Optional.of(result);
		} catch (Exception ignored) {
			reader.setCursor(cursor);
			return Optional.empty();
		}
	}
}
