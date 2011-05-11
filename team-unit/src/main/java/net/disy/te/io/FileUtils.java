package net.disy.te.io;

import java.io.File;
import java.io.IOException;

public class FileUtils {

	private FileUtils() {
	}

	public static File createTemporaryDirectory(String name) throws IOException {
		name = createPrefix(name);
		do {
			final File file = File.createTempFile(name, null);
			file.delete();
			if (file.mkdir()) {
				return file;
			}
		} while (true);
	}

	private static String createPrefix(final String name) {
		if (name.length() < 3) {
			return createPrefix(name + "_"); //$NON-NLS-1$
		}
		return name;
	}

}
