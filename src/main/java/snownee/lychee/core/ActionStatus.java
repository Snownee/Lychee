package snownee.lychee.core;

public class ActionStatus {

	public boolean doDefault;
	public State state;

	public enum State {
		RUNNING, PAUSED, STOPPED
	}

	public ActionStatus() {
		reset();
	}

	public void reset() {
		doDefault = true;
		state = State.RUNNING;
	}

}
