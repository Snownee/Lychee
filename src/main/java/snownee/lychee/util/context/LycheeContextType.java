package snownee.lychee.util.context;

public interface LycheeContextType<T extends LycheeContextValue<T>> {
	T construct(LycheeContext context);
}
