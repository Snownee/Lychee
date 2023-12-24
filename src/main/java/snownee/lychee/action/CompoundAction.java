package snownee.lychee.action;

import snownee.lychee.util.action.PostAction;

import java.util.stream.Stream;

public interface CompoundAction {
	Stream<PostAction> getChildActions();
}
