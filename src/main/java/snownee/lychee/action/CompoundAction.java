package snownee.lychee.action;

import java.util.stream.Stream;

import snownee.lychee.util.action.PostAction;

public interface CompoundAction {
	Stream<PostAction> getChildActions();
}
