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

package net.sandius.rembulan;

/**
 * Base class for runtime exceptions that carry arbitrary <i>error objects</i>
 * attached to them.
 *
 * <p>To retrieve the error object, use {@link #getErrorObject()}.</p>
 *
 * <p>As in PUC-Lua, a string error object has the position of the error in front of it
 * ({@code "chunk:line: message"}), like a message of {@code luaL_error}: for an error of an
 * operation in a Lua function, the position in that function; for an error raised by a function
 * written in Java, the position in the Lua function that called it (see {@code ErrorPosition}).
 * The position is taken from the stack trace when the error object is first retrieved. An error
 * object that must stay as it is, like {@code lua_error}'s, is made with
 * {@link #ofErrorObject(Object)}.</p>
 */
public class LuaRuntimeException extends RuntimeException {

	private final Object errorObject;
	private final boolean withPosition;

	private Object errorObjectWithPosition;
	private boolean positionAdded;

	private LuaRuntimeException(Throwable cause, Object errorObject, boolean withPosition) {
		super(cause);
		this.errorObject = errorObject;
		this.withPosition = withPosition;
	}

	/**
	 * Constructs a new {@code LuaRuntimeException} with {@code errorObject} as its
	 * error object. {@code errorObject} may be {@code null}. If it is a string, the position
	 * of the error is put in front of it, if known.
	 *
	 * @param errorObject  the error object, may be {@code null}
	 */
	public LuaRuntimeException(Object errorObject) {
		this(null, errorObject, true);
	}

	/**
	 * Constructs a new {@code LuaRuntimeException} with {@code cause} as its cause.
	 *
	 * <p>When queried for the error object, invokes {@link Conversions#toErrorObject(Throwable)}
	 * on {@code cause}; when {@code cause} is {@code null}, then the error object
	 * is {@code null}.</p>
	 *
	 * @param cause  the cause of this error, may be {@code null}
	 */
	public LuaRuntimeException(Throwable cause) {
		this(cause, null, false);
	}

	/**
	 * Returns a new {@code LuaRuntimeException} with {@code errorObject} as its error object,
	 * exactly as it is: no position is put in front of a string (e.g. for {@code error(msg, 0)},
	 * or an error object raised again).
	 *
	 * @param errorObject  the error object, may be {@code null}
	 * @return  a new {@code LuaRuntimeException} with this error object
	 */
	public static LuaRuntimeException ofErrorObject(Object errorObject) {
		return new LuaRuntimeException(null, errorObject, false);
	}

	/**
	 * Returns the error object attached to this exception converted to a string.
	 *
	 * @return  error object converted to a string
	 */
	@Override
	public String getMessage() {
		return Conversions.toHumanReadableString(getErrorObject()).toString();
	}

	/**
	 * Returns the error object attached to this exception without the position of the error in
	 * front of a string, e.g. to use its message in the message of another error.
	 *
	 * @return  the error object attached to this exception (possibly {@code null}), without
	 *          position
	 */
	public Object getErrorObjectWithoutPosition() {
		Throwable cause = getCause();
		if (cause instanceof LuaRuntimeException) {
			return ((LuaRuntimeException) cause).getErrorObjectWithoutPosition();
		}
		else if (cause != null) {
			return cause.getMessage();
		}
		else {
			return errorObject;
		}
	}

	/**
	 * Returns the error object attached to this exception. The error object may be {@code null}.
	 *
	 * @return  the error object attached to this exception (possibly {@code null})
	 */
	public Object getErrorObject() {
		Throwable cause = getCause();
		if (cause != null) {
			return Conversions.toErrorObject(cause);
		}
		else if (!withPosition) {
			return errorObject;
		}
		else {
			if (!positionAdded) {
				errorObjectWithPosition = ErrorPosition.addTo(errorObject, getStackTrace());
				positionAdded = true;
			}
			return errorObjectWithPosition;
		}
	}

}
