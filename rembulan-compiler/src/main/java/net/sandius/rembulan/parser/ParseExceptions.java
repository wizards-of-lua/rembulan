package net.sandius.rembulan.parser;

/**
 * Parse exceptions for errors that have a line but no parser state: lexical errors, errors found
 * in a grammar action or after parsing.
 *
 * <p>Chunk loaders take the line of a {@link ParseException} from its {@code currentToken}, and
 * whether the input may be partial (an unexpected end of input) from that token's {@code next}
 * being {@link ParserConstants#EOF}. The exceptions made here carry just that.</p>
 */
public final class ParseExceptions {

	private ParseExceptions() {
		// not to be instantiated
	}

	/**
	 * Returns a parse exception with the message {@code message} for an error at the line
	 * {@code line}.
	 *
	 * @param message  the error message, without source information
	 * @param line  the line of the error (non-positive if not known)
	 * @param atEndOfInput  {@code true} if the error is the end of the input, i.e., the input may
	 *                      be partial
	 * @return  the parse exception
	 */
	public static ParseException at(String message, int line, boolean atEndOfInput) {
		Token at = new Token();
		at.beginLine = line;
		at.endLine = line;
		if (atEndOfInput) {
			Token eof = new Token(ParserConstants.EOF);
			eof.beginLine = line;
			eof.endLine = line;
			at.next = eof;
		}
		ParseException ex = new ParseException(message);
		ex.currentToken = at;
		return ex;
	}

	/**
	 * Returns a parse exception for a syntax error at the token {@code t}, with a message as in
	 * PUC-Lua: {@code message near 'token'}, or {@code message near <eof>}.
	 *
	 * @param message  the error message, without source information
	 * @param t  the token where the error is
	 * @return  the parse exception
	 */
	static ParseException near(String message, Token t) {
		boolean eof = t.kind == ParserConstants.EOF;
		return at(message + " near " + (eof ? "<eof>" : "'" + t.image + "'"), t.beginLine, eof);
	}

}
