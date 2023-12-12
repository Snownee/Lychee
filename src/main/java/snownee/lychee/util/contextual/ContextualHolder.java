package snownee.lychee.util.contextual;

public interface ContextualHolder<C extends Contextual<C>> {
	Contextual<C> contextual();
}
