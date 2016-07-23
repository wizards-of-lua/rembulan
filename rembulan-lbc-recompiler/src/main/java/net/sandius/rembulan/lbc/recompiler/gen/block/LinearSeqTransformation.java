package net.sandius.rembulan.lbc.recompiler.gen.block;

import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.List;

public abstract class LinearSeqTransformation {

	public abstract void apply(LinearSeq seq);

	public static class Remove extends LinearSeqTransformation {

		public final LinearPredicate predicate;

		public Remove(LinearPredicate predicate) {
			this.predicate = Check.notNull(predicate);
		}

		@Override
		public void apply(LinearSeq seq) {
			List<Linear> nodes = new ArrayList<>();
			for (Linear n : seq.nodes()) {
				if (predicate.apply(n)) nodes.add(n);
			}

			for (Linear n : nodes) {
				n.remove();
			}
		}

	}

}