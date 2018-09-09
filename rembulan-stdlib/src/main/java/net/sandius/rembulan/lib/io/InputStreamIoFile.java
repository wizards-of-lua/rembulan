/*
 * Copyright 2016 Miroslav Janíček
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.lib.IoFile;

public class InputStreamIoFile extends IoFile {

  private final SeekableInputStream in;
  private final BufferedReader reader;

  public InputStreamIoFile(InputStream in, Table metatable, Object userValue) {
    super(metatable, userValue);
    this.in = new SeekableInputStream(Objects.requireNonNull(in));
    this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
  }

  @Override
  public boolean isClosed() {
    return false;
  }

  @Override
  public void close() throws IOException {
    reader.close();
    in.close();
    // throw new UnsupportedOperationException("cannot close standard file");
  }

  @Override
  public void flush() throws IOException {
    // no-op
  }

  @Override
  public void write(ByteString s) throws IOException {
    throw new UnsupportedOperationException("Bad file descriptor");
  }

  @Override
  public long seek(IoFile.Whence whence, long offset) throws IOException {
    switch (whence) {
      case BEGINNING:
      case END:
        return in.setPosition(offset);

      case CURRENT_POSITION:
        return in.addPosition(offset);

      default:
        throw new IllegalArgumentException("Illegal whence: " + whence);
    }
  }

  @Override
  public String readLine() throws IOException {
    return reader.readLine();
  }

}
