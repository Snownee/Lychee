package snownee.lychee.util.action;

import java.util.List;

public enum ClientSideStrategy {
	DEFAULT,
	PREVENT_SYNC,
	ALLOW_CLIENT_RUN;

	public static ClientSideStrategy ofEntries(List<PostAction> list) {
		return null;
	}
}
