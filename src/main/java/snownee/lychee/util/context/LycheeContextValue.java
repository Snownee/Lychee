package snownee.lychee.util.context;

public interface LycheeContextValue<T extends LycheeContextValue<T>> {
	LycheeContextType<T> type();
}
