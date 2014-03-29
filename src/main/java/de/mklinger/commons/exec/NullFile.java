/* Copyright 2013, 2014 mklinger GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mklinger.commons.exec;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access to a null file. On Unixoid systems, the native null device
 * will be used. On other systems, a temporary file will be created
 * that can be written to. In this case, the file will be deleted when
 * {@link #cleanup()} is called.
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class NullFile {
	private static final Logger LOG = LoggerFactory.getLogger(NullFile.class);
	private File file;
	private boolean temporaryFile;

	public NullFile() throws IOException {
		if (CommandLineUtil.isWindows()) {
			LOG.debug("Using native NUL device");
			this.file = new File("NUL");
			this.temporaryFile = false;
		} else {
			final File devNull = new File("/dev/null");
			if (devNull.exists() && devNull.canWrite()) {
				LOG.debug("Using native null device: {}", devNull);
				this.file = devNull;
				this.temporaryFile = false;
			} else {
				LOG.warn("Using temporary file as simulated null device");
				this.file = File.createTempFile("null", ".null");
				this.temporaryFile = true;
			}
		}
	}

	public File getFile() {
		return file;
	}

	public void cleanup() {
		if (temporaryFile) {
			file.delete();
		}
	}
}