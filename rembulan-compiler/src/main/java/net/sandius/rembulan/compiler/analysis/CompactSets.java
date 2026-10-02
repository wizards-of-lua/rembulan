package net.sandius.rembulan.compiler.analysis;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Immutable sets that take little memory when small, for analyses that keep a set per IR node.
 */
final class CompactSets {

	private static final int MAX_ARRAY_SIZE = 8;

	private CompactSets() {
		// not to be instantiated
	}

	/**
	 * Returns an immutable copy of {@code set}.
	 *
	 * @param set  the set to copy, must not be {@code null}
	 * @param <T>  the type of the elements
	 * @return  an immutable set with the elements of {@code set}
	 */
	static <T> Set<T> copyOf(Set<T> set) {
		if (set.isEmpty()) {
			return Collections.emptySet();
		}
		else if (set.size() <= MAX_ARRAY_SIZE) {
			return new ArraySet<>(set.toArray());
		}
		else {
			return Collections.unmodifiableSet(new HashSet<>(set));
		}
	}

	/**
	 * Returns {@code candidate} if it has the same elements as {@code set}, and an immutable copy
	 * of {@code set} otherwise.
	 *
	 * @param set  the set to copy, must not be {@code null}
	 * @param candidate  an immutable set to reuse, must not be {@code null}
	 * @param <T>  the type of the elements
	 * @return  an immutable set with the elements of {@code set}
	 */
	static <T> Set<T> copyOf(Set<T> set, Set<T> candidate) {
		if (set.size() == candidate.size() && candidate.containsAll(set)) {
			return candidate;
		}
		else {
			return copyOf(set);
		}
	}

	private static final class ArraySet<T> extends AbstractSet<T> {

		private final Object[] elements;

		ArraySet(Object[] elements) {
			this.elements = elements;
		}

		@Override
		public int size() {
			return elements.length;
		}

		@Override
		public boolean contains(Object o) {
			for (Object e : elements) {
				if (e.equals(o)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Iterator<T> iterator() {
			return new Iterator<T>() {

				private int index = 0;

				@Override
				public boolean hasNext() {
					return index < elements.length;
				}

				@Override
				@SuppressWarnings("unchecked")
				public T next() {
					if (index >= elements.length) {
						throw new NoSuchElementException();
					}
					return (T) elements[index++];
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

			};
		}

	}

}
