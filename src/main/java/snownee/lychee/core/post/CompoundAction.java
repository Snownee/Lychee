package snownee.lychee.core.post;

import java.util.stream.Stream;

public interface CompoundAction {
	Stream<PostAction> getChildActions();
}
