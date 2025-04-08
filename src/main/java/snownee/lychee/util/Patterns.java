package snownee.lychee.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.Lychee;

public class Patterns implements Predicate<String> {
	public static final Codec<Patterns> CODEC = ExtraCodecs.nonEmptyList(KCodecs.compactList(ExtraCodecs.NON_EMPTY_STRING))
			.xmap(Patterns::new, Patterns::strings);
	public static final StreamCodec<ByteBuf, Patterns> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())
			.map(Patterns::new, Patterns::strings);
	public static final Patterns EMPTY = new Patterns(List.of());

	private final List<String> strings;
	private @Nullable List<Pattern> patterns;

	public Patterns(List<String> strings) {
		this.strings = strings;
	}

	public List<String> strings() {
		return strings;
	}

	public List<Pattern> patterns() {
		if (patterns == null) {
			patterns = strings.stream().map(it -> {
				try {
					return Pattern.compile(it);
				} catch (Exception e) {
					Lychee.LOGGER.error("Failed to compile pattern: {}", it);
					return null;
				}
			}).filter(Objects::nonNull).toList();
		}
		return patterns;
	}

	@Override
	public boolean test(String s) {
		if (strings.isEmpty()) {
			return false;
		}
		for (Pattern pattern : patterns()) {
			if (pattern.matcher(s).matches()) {
				return true;
			}
		}
		return false;
	}
}
