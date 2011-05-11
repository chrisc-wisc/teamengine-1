package com.occamlab.te.xml.transform;

import java.io.IOException;

public interface SourceTemplate {
	
	public <V, T extends Throwable> V execute(SourceClosure<V, T> closure) throws T, IOException;

}
