package net.sandius.rembulan.lib.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

public abstract class AbstractTokenizer<R> {

  protected static final char LINE_FEED = '\n';

  protected final ByteBuffer byteBuffer;
  protected final CharsetDecoder decoder;
  protected final CharBuffer charBuffer;

  protected final StringBuilder builder = new StringBuilder();
  protected long skip;

  public AbstractTokenizer(ByteBuffer byteBuffer, CharsetDecoder decoder, CharBuffer charBuffer) {
    super();
    this.byteBuffer = byteBuffer;
    this.decoder = decoder;
    this.charBuffer = charBuffer;
  }

  public final R nextToken(SeekableByteChannel channel) throws IOException {
    reset();
    builder.setLength(0);
    decoder.reset();
    charBuffer.clear();
    byteBuffer.limit(byteBuffer.capacity());
    byteBuffer.rewind();

    long mark = channel.position();
    skip = 0;

    int remainingBytes = 0;
    int remainingChars = 0;

    read_loop: while (true) {
      if (remainingBytes == 0) {
        byteBuffer.rewind();
        int bytesReadFromChannel = channel.read(byteBuffer);
        if (bytesReadFromChannel == -1) {
          break;
        }
        byteBuffer.flip();
      }

      if (remainingChars == 0) {
        charBuffer.rewind();
        CoderResult result = decoder.decode(byteBuffer, charBuffer, false);
        if (result.isError()) {
          throw new CharacterCodingException();
        }
        charBuffer.flip();
        remainingChars = charBuffer.remaining();
      }
      remainingBytes = byteBuffer.remaining();
      while (remainingChars > 0) {
        char ch = charBuffer.get();

        // handle character (start)
        boolean doContinue = handleChar(ch);
        if (!doContinue) {
          break read_loop;
        }
        // handle character (end)

        remainingChars = charBuffer.remaining();
      }
    }

    // TODO How to cleanup the charBuffer using decoder.decode(byteBuffer, charBuffer, true) and
    // decoder.flush(charBuffer);?

    channel.position(mark + skip + builder.length());

    if (builder.length() == 0 && skip == 0) {
      return null;
    } else {
      String text = builder.toString();
      return toResult(text);
    }
  }

  protected abstract void reset();

  protected abstract boolean handleChar(char ch);

  protected abstract R toResult(String text);

}
