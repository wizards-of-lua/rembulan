/*
 * Copyright 2026 Michael Karneim
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package net.sandius.rembulan.lib.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;
import net.sandius.rembulan.ByteString;

/**
 * Reads the formats of {@code file:read} from a channel. Each read leaves the channel positioned
 * right after what it read, and returns {@code null} if it failed at the end of the input.
 */
class ChannelReader {

  private final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
  private final NumberTokenizer numberTokenizer = new NumberTokenizer(byteBuffer);
  private final LineTokenizer lineTokenizer = new LineTokenizer(byteBuffer);
  private final LineTokenizer lineWithEoLTokenizer = new LineTokenizer(byteBuffer, true);
  private final RestOfFileTokenizer restOfFileTokenizer = new RestOfFileTokenizer(byteBuffer);
  private final ChunkTokenizer chunkTokenizer = new ChunkTokenizer(byteBuffer);

  private final SeekableByteChannel channel;

  ChannelReader(SeekableByteChannel channel) {
    this.channel = Objects.requireNonNull(channel);
  }

  ByteString readLine(boolean returnEol) throws IOException {
    return returnEol ? lineWithEoLTokenizer.nextToken(channel) : lineTokenizer.nextToken(channel);
  }

  Number readNumber() throws IOException {
    return numberTokenizer.nextToken(channel);
  }

  ByteString readRestOfFile() throws IOException {
    return restOfFileTokenizer.nextToken(channel);
  }

  ByteString readChunk(long len) throws IOException {
    chunkTokenizer.setLength(len);
    return chunkTokenizer.nextToken(channel);
  }

}
