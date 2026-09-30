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
package net.sandius.rembulan.env;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 * Creates the temporary files of {@code io.tmpfile}.
 *
 * @see RuntimeEnvironment#tempFileFactory()
 */
@FunctionalInterface
public interface TempFileFactory {

	/**
	 * Creates a new, empty temporary file and opens it for reading and writing. The file must be
	 * removed when the returned channel is closed.
	 *
	 * @return  a channel to the new file
	 *
	 * @throws IOException  if the file could not be created
	 */
	SeekableByteChannel newTempFile() throws IOException;

}
