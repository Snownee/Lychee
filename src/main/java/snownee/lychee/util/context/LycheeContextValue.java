package snownee.lychee.util.context;

import com.mojang.serialization.Codec;

public interface LycheeContextValue<T extends LycheeContextValue<T>> {

	Codec<LycheeContextValue<?>> CODEC = ;

	LycheeContextType<T> type();
}
