package snownee.lychee.context;

public class ActionManager {
	private final ActionContext prototype;

	public ActionManager(ActionContext prototype) {
		this.prototype = prototype;
	}

	public ActionContext prototype() {
		return prototype;
	}

	public ActionContext newContext() {
		return new ActionContext(prototype.lootParams.copy());
	}

	public ActionContext singletonContext() {
		return prototype;
	}
}
