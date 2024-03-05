package snownee.lychee.util.action;

import java.util.stream.Stream;

public interface CompoundAction {
	Stream<PostAction> getChildActions();
}
