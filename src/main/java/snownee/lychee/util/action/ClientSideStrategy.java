package snownee.lychee.util.action;

import java.util.List;

public enum ClientSideStrategy {
	DEFAULT,
	PREVENT_SYNC,
	ALLOW_CLIENT_RUN;

	public static ClientSideStrategy ofEntries(List<PostAction> list) {
		if (list.isEmpty()) {
			return DEFAULT;
		}
		if (list.stream().allMatch(it -> it.clientSideStrategy() == PREVENT_SYNC)) {
			return PREVENT_SYNC;
		}
		if (list.stream().allMatch(it -> it.clientSideStrategy() == ALLOW_CLIENT_RUN)) {
			return ALLOW_CLIENT_RUN;
		}
		return DEFAULT;
	}
}
