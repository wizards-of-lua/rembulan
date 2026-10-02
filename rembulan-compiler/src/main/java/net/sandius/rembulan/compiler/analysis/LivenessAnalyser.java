/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.compiler.util.CodeUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LivenessAnalyser {

	private final IRFunc fn;

	private Map<Label, Set<Var>> endVarLiveIn;
	private Map<Label, Set<AbstractVal>> endValLiveIn;

	private LivenessAnalyser(IRFunc fn) {
		this.fn = Objects.requireNonNull(fn);

		this.endVarLiveIn = new HashMap<>();
		this.endValLiveIn = new HashMap<>();
	}

	public static LivenessInfo computeLiveness(IRFunc fn) {
		LivenessAnalyser analyser = new LivenessAnalyser(fn);
		return analyser.analyse();
	}

	public LivenessInfo analyse() {
		Code code = fn.code();

		Map<Label, Set<Label>> in = CodeUtils.inLabels(code);

		// initialise
		for (Label l : code.labels()) {
			endVarLiveIn.put(l, new HashSet<Var>());
			endValLiveIn.put(l, new HashSet<AbstractVal>());
		}

		Deque<Label> open = new ArrayDeque<>();

		// make sure we'll visit all labels at least once
		for (Label l : CodeUtils.labelsBreadthFirst(code)) {
			open.push(l);
		}

		// iterate until the values live at the end of each block are stable;
		// the values live at each node are computed only once, from the final result
		while (!open.isEmpty()) {
			Label l = open.pop();

			LivenessVisitor visitor = new LivenessVisitor(
					endVarLiveIn.get(l),
					endValLiveIn.get(l));

			processBlock(visitor, code.block(l));

			for (Label inl : in.get(l)) {
				boolean changed = false;

				changed |= endVarLiveIn.get(inl).addAll(visitor.currentVarLiveIn());
				changed |= endValLiveIn.get(inl).addAll(visitor.currentValLiveIn());

				if (changed) {
					if (open.contains(inl)) {
						open.remove(inl);
					}
					open.push(inl);
				}
			}

		}

		return result();
	}

	// Large chunks have hundreds of thousands of nodes, so the sets are kept small: they are
	// immutable, the live-out set of a node is the live-in set of the node following it, and a node
	// that changes nothing shares its successor's set.
	private LivenessInfo result() {
		// identity: some nodes are equal to each other (e.g. CPUWithdraw of the same cost)
		Map<IRNode, LivenessInfo.Entry> entries = new IdentityHashMap<>();

		Iterator<BasicBlock> blockIterator = fn.code().blockIterator();
		while (blockIterator.hasNext()) {
			BasicBlock b = blockIterator.next();

			Set<Var> varOut = CompactSets.copyOf(endVarLiveIn.get(b.label()));
			Set<AbstractVal> valOut = CompactSets.copyOf(endValLiveIn.get(b.label()));

			LivenessVisitor visitor = new LivenessVisitor(varOut, valOut);

			// iterating backwards
			for (int i = b.body().size(); i >= 0; i--) {
				IRNode node = i < b.body().size() ? b.body().get(i) : b.end();
				node.accept(visitor);

				Set<Var> varIn = CompactSets.copyOf(visitor.currentVarLiveIn(), varOut);
				Set<AbstractVal> valIn = CompactSets.copyOf(visitor.currentValLiveIn(), valOut);

				entries.put(node, new LivenessInfo.Entry(varIn, varOut, valIn, valOut));

				varOut = varIn;
				valOut = valIn;
			}
		}

		return new LivenessInfo(entries);
	}

	private static void processBlock(LivenessVisitor visitor, BasicBlock block) {
		// iterating backwards
		block.end().accept(visitor);

		for (int i = block.body().size() - 1; i >= 0; i--) {
			block.body().get(i).accept(visitor);
		}
	}

	private class LivenessVisitor extends AbstractUseDefVisitor {

		private Set<Var> currentVarLiveIn;
		private Set<AbstractVal> currentValLiveIn;

		public LivenessVisitor(Set<Var> currentVarLiveIn, Set<AbstractVal> currentValLiveIn) {
			this.currentVarLiveIn = new HashSet<>(Objects.requireNonNull(currentVarLiveIn));
			this.currentValLiveIn = new HashSet<>(Objects.requireNonNull(currentValLiveIn));
		}

		public Set<Var> currentVarLiveIn() {
			return currentVarLiveIn;
		}

		public Set<AbstractVal> currentValLiveIn() {
			return currentValLiveIn;
		}

		@Override
		protected void def(Val v) {
			currentValLiveIn.remove(v);
		}

		@Override
		protected void use(Val v) {
			currentValLiveIn.add(v);
		}

		@Override
		protected void def(PhiVal pv) {
			currentValLiveIn.remove(pv);
		}

		@Override
		protected void use(PhiVal pv) {
			currentValLiveIn.add(pv);
		}

		@Override
		protected void def(MultiVal mv) {
			// TODO
		}

		@Override
		protected void use(MultiVal mv) {
			// TODO
		}

		@Override
		protected void def(Var v) {
			currentVarLiveIn.remove(v);
		}

		@Override
		protected void use(Var v) {
			currentVarLiveIn.add(v);
		}

		@Override
		protected void def(UpVar uv) {
			// no effect on liveness
		}

		@Override
		protected void use(UpVar uv) {
			// no effect on liveness
		}

		@Override
		public void visit(VarStore node) {
			use(node.src());
			use(node.var());  // Note that this is a use, not a def
		}

	}

}
