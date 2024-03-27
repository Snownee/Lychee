package snownee.lychee.util.context;

public interface KeyedContextValue<T extends KeyedContextValue<T>> {
	LycheeContextKey<T> key();
}
