package com.occamlab.te.xml.transform;

import java.io.IOException;

import javax.xml.transform.Source;

public interface SourceClosure<V, T extends Throwable> {

	public V execute(Source source) throws T, IOException;

}
