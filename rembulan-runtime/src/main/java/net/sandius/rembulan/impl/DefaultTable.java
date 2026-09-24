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

package net.sandius.rembulan.impl;

import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.TableFactory;
import net.sandius.rembulan.util.TraversableHashMap;

import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * Default implementation of the Lua table storing all key-value pairs in a hashmap.
 * The table implementation does not support weak keys or values.
 */
public class DefaultTable extends Table {

	private final TraversableHashMap<Object, Object> values;

	/**
	 * Keys removed since the last insertion of a new key, each mapped to the key that followed it
	 * in the traversal order at the time of its removal ({@code null} if it was the last one).
	 *
	 * <p>Lua allows clearing existing fields during a traversal, so {@code next} must accept a
	 * key that has just been removed. The reference implementation keeps such a key in the table
	 * as a dead key until the next rehash, which only an insertion can trigger; this map does the
	 * same. Assigning to a non-existent field during a traversal is undefined behaviour in Lua,
	 * so an insertion may forget all of them.</p>
	 */
	private final HashMap<Object, Object> removedKeys;

	/**
	 * Constructs a new empty table.
	 */
	public DefaultTable() {
		this.values = new TraversableHashMap<>();
		this.removedKeys = new HashMap<>();
	}

	static class Factory implements TableFactory {
		@Override
		public Table newTable() {
			return newTable(0, 0);
		}

		@Override
		public Table newTable(int array, int hash) {
			return new DefaultTable();
		}
	}

	private static final TableFactory FACTORY_INSTANCE = new Factory();

	/**
	 * Returns the table factory for constructing instances of {@code DefaultTable}.
	 *
	 * @return  the table factory for {@code DefaultTable}s
	 */
	public static TableFactory factory() {
		return FACTORY_INSTANCE;
	}

	@Override
	public Object rawget(Object key) {
		key = Conversions.normaliseKey(key);
		return key != null ? values.get(key) : null;
	}

	@Override
	public void rawset(Object key, Object value) {
		key = Conversions.normaliseKey(key);

		if (key == null) {
			throw new IllegalArgumentException("table index is nil");
		}
		if (key instanceof Double && Double.isNaN(((Double) key).doubleValue())) {
			throw new IllegalArgumentException("table index is NaN");
		}

		value = Conversions.canonicalRepresentationOf(value);

		if (value == null) {
			if (values.containsKey(key)) {
				removedKeys.put(key, values.getSuccessorOf(key));
				values.remove(key);
			}
		}
		else {
			if (!removedKeys.isEmpty() && !values.containsKey(key)) {
				removedKeys.clear();
			}
			values.put(key, value);
		}

		updateBasetableModes(key, value);
	}

	@Override
	public Object initialKey() {
		return values.getFirstKey();
	}

	@Override
	public Object successorKeyOf(Object key) {
		if (key != null && !values.containsKey(key) && removedKeys.containsKey(key)) {
			// a removed key: continue with the first key after it that is still present
			Object next = removedKeys.get(key);
			while (next != null && !values.containsKey(next)) {
				next = removedKeys.get(next);
			}
			return next;
		}
		try {
			return values.getSuccessorOf(key);
		}
		catch (NoSuchElementException | NullPointerException ex) {
			throw new IllegalArgumentException("invalid key to 'next'", ex);
		}
	}

	@Override
	protected void setMode(boolean weakKeys, boolean weakValues) {
		// TODO
	}

}
