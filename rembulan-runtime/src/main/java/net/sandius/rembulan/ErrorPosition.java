package net.sandius.rembulan;

/**
 * The position ({@code "chunk:line: "}) that PUC-Lua puts in front of the message of an error
 * at level 1, found in the stack trace of the exception: the error of an operation in a Lua
 * function has the position of that function, the error of a function written in Java (a library
 * function, {@code error}) the position of the Lua function that called it.
 *
 * <p>No position is given when it is not known: when a Java function was called by another Java
 * function (e.g. {@code pcall(error, "x")}, as in PUC-Lua), or in a tail call, which runs after
 * the calling function has returned.</p>
 *
 * <p>Compiled Lua functions are recognised by the methods the compiler generates for their code
 * ({@code run}, {@code run_N}), which have a line number and the chunk name as their file name.</p>
 */
final class ErrorPosition {

	private static final String DISPATCH = "net.sandius.rembulan.runtime.Dispatch";

	private ErrorPosition() {
		// not to be instantiated
	}

	/**
	 * Returns the position of an error raised where {@code stackTrace} was taken, as
	 * {@code "chunk:line: "}, or {@code null} if it is not known.
	 */
	static String of(StackTraceElement[] stackTrace) {
		int i = 0;
		while (i < stackTrace.length) {
			StackTraceElement elem = stackTrace[i];
			if (isLuaFrame(elem)) {
				// an operation of this Lua function failed
				return positionOf(elem);
			}
			else if (isDispatch(elem)) {
				// the call of the function that raised the error: its caller is next
				boolean tailCall = false;
				while (i < stackTrace.length && isDispatch(stackTrace[i])) {
					tailCall |= stackTrace[i].getMethodName().equals("evaluateTailCalls");
					i++;
				}
				// the runtime's own operations (e.g. a metamethod call for a comparison)
				while (i < stackTrace.length && !isLuaFrame(stackTrace[i])
						&& isRuntime(stackTrace[i].getClassName())) {
					i++;
				}
				return !tailCall && i < stackTrace.length && isLuaFrame(stackTrace[i])
						? positionOf(stackTrace[i])
						: null;
			}
			i++;
		}
		return null;
	}

	private static String positionOf(StackTraceElement elem) {
		return elem.getFileName() + ":" + elem.getLineNumber() + ": ";
	}

	private static boolean isDispatch(StackTraceElement elem) {
		return elem.getClassName().equals(DISPATCH);
	}

	/** Classes of the runtime itself, not library functions or executors. */
	private static boolean isRuntime(String className) {
		return className.startsWith("net.sandius.rembulan.")
				&& !className.startsWith("net.sandius.rembulan.lib.")
				&& !className.startsWith("net.sandius.rembulan.exec.");
	}

	private static boolean isLuaFrame(StackTraceElement elem) {
		String method = elem.getMethodName();
		String file = elem.getFileName();
		return (method.equals("run") || method.startsWith("run_"))
				&& elem.getLineNumber() >= 0
				&& file != null
				&& !file.endsWith(".java")
				&& !file.endsWith(".kt")
				&& !file.endsWith(".scala")
				&& !file.endsWith(".groovy");
	}

	/**
	 * Returns {@code errorObject} with the position of the error in front of it if it is a string
	 * and the position is known, else {@code errorObject} itself.
	 */
	static Object addTo(Object errorObject, StackTraceElement[] stackTrace) {
		if (errorObject instanceof ByteString || errorObject instanceof String) {
			String position = of(stackTrace);
			if (position != null) {
				return errorObject instanceof ByteString
						? ByteString.of(position).concat((ByteString) errorObject)
						: position + errorObject;
			}
		}
		return errorObject;
	}

}
