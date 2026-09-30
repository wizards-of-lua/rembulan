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

package net.sandius.rembulan.env;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class SystemRuntimeEnvironment implements RuntimeEnvironment {

	private final InputStream in;
	private final OutputStream out;
	private final OutputStream err;

	private static final SystemRuntimeEnvironment INSTANCE = new SystemRuntimeEnvironment(
			System.in, System.out, System.err);

	SystemRuntimeEnvironment(InputStream in, OutputStream out, OutputStream err) {
		this.in = in;
		this.out = out;
		this.err = err;
	}

	public static SystemRuntimeEnvironment getInstance() {
		return INSTANCE;
	}

	@Override
	public InputStream standardInput() {
		return in;
	}

	@Override
	public OutputStream standardOutput() {
		return out;
	}

	@Override
	public OutputStream standardError() {
		return err;
	}

	@Override
	public FileSystem fileSystem() {
		return FileSystems.getDefault();
	}

	@Override
	public String getEnv(String name) {
		return System.getenv(name);
	}

	@Override
	public double getCpuTime() {
		// FIXME: this really isn't a good idea...
		long nanos = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		return nanos / 1000000000.0;
	}

	/**
	 * Creates the files in the system's temporary directory. They are removed when closed, or
	 * else when the JVM exits (on some systems only if it exits normally).
	 */
	@Override
	public TempFileFactory tempFileFactory() {
		return () -> {
			Path path = Files.createTempFile("lua", ".tmp");
			return Files.newByteChannel(path, StandardOpenOption.READ, StandardOpenOption.WRITE,
					StandardOpenOption.DELETE_ON_CLOSE);
		};
	}

}
