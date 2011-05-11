package com.occamlab.te.xml.transform;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.Validate;

public class URLSourceTemplate implements SourceTemplate {

	private final URL url;

	public URLSourceTemplate(URL url) {
		Validate.notNull(url);
		this.url = url;
	}

	public <V, T extends Throwable> V execute(SourceClosure<V, T> closure)
			throws T, IOException {
		InputStream in = null;
		try {
			in = url.openStream();
			Source source = new StreamSource(in, url.toExternalForm());

			return closure.execute(source);
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(in);
		}
	}

	@Override
	public String toString() {
		return this.url.toExternalForm();
	}

}
