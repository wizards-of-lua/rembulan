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

package net.sandius.rembulan.parser.analysis;

/**
 * A compile error found after parsing, such as a goto without a visible label.
 */
public class NameResolutionException extends RuntimeException {

	private final int line;

	/**
	 * Constructs a new exception for an error at the line {@code line}.
	 *
	 * @param message  the error message, without source information
	 * @param line  the line of the error (non-positive if not known)
	 */
	public NameResolutionException(String message, int line) {
		super(message);
		this.line = line;
	}

	public NameResolutionException(String message) {
		this(message, 0);
	}

	/**
	 * Returns the line of the error.
	 *
	 * @return  the line of the error, or a non-positive number if it is not known
	 */
	public int line() {
		return line;
	}

}
