package snownee.lychee.util;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class RecipeMatcher<T> {

	public List<T> inputs;
	public List<? extends Predicate<T>> tests;
	public int[] inputCapacity;
	public int[] inputUsed;
	public int[][] use;
	// input to tests multimap. map to the indexes of the first N inputs according to the `inputUsed` array
	private final BitSet data;
	private final BitSet mask;

	public RecipeMatcher(List<T> inputs, List<? extends Predicate<T>> tests, int[] inputCapacity) {
		this.inputs = inputs;
		this.tests = tests;
		this.inputCapacity = inputCapacity;
		inputUsed = new int[inputs.size()];
		use = new int[inputs.size()][tests.size()];
		data = new BitSet(inputs.size() * tests.size());
		mask = new BitSet(inputs.size());
		for (int i = 0; i < tests.size(); i++) {
			Predicate<T> test = tests.get(i);
			int offset = i * inputs.size();
			for (int j = 0; j < inputs.size(); j++) {
				if (test.test(inputs.get(j))) {
					data.set(offset + j);
				}
			}
		}
		for (int i = 0; i < tests.size(); i++) {
			mask.clear();
			if (!match(i)) {
				inputUsed = null; // failed
				return;
			}
		}
	}

	private boolean match(int test) {
		int offset = test * inputs.size();
		for (int i = 0; i < inputs.size(); i++) {
			if (data.get(offset + i) && !mask.get(i)) {
				mask.set(i);
				if (inputUsed[i] < inputCapacity[i]) {
					use[i][inputUsed[i]] = test;
					++inputUsed[i];
					return true;
				}
				for (int j = 0; j < inputUsed[i]; j++) {
					if (match(use[i][j])) {
						use[i][j] = test;
						return true;
					}
				}
			}
		}
		return false;
	}

	public static <T> Optional<RecipeMatcher<T>> findMatches(
			List<T> inputs,
			List<? extends Predicate<T>> tests,
			int[] amount
	) {
		int sum = 0;
		for (int i = 0; i < amount.length; i++) {
			sum += amount[i];
		}
		int testSize = tests.size();
		if (sum < testSize) {
			return Optional.empty();
		}

		RecipeMatcher<T> matcher = new RecipeMatcher<>(inputs, tests, amount);
		return matcher.inputUsed == null ? Optional.empty() : Optional.of(matcher);
	}

}
