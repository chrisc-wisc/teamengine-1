/*
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s): No additional contributors to date
 */

package com.occamlab.te;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.sf.saxon.FeatureKeys;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.occamlab.te.index.Index;
import com.occamlab.te.util.Misc;
import com.occamlab.te.xml.transform.SourceClosure;
import com.occamlab.te.xml.transform.SourceTemplate;
import com.occamlab.te.xml.transform.URLSourceTemplate;

public class Generator {

	public static final Validator NULL_VALIDATOR = new Validator() {

		@Override
		public void validate(Source source, Result result) throws SAXException,
				IOException {
		}

		@Override
		public void setResourceResolver(LSResourceResolver resourceResolver) {
		}

		@Override
		public void setErrorHandler(ErrorHandler errorHandler) {
		}

		@Override
		public void reset() {
		}

		@Override
		public LSResourceResolver getResourceResolver() {
			return null;
		}

		@Override
		public ErrorHandler getErrorHandler() {
			return null;
		}
	};

	// Generates XSL template files from CTL sources and a master index
	// of metadata about the CTL objects
	private static Logger logger = Logger
			.getLogger("com.occamlab.te.Generator");

	public Index generateXsl(SetupOptions opts) throws Exception {

		final boolean validate = opts.isValidate();
		final File workDir = opts.getWorkDir();
		final Set<Entry<URL, File>> sourceEntries = createSourceEntries(opts,
				workDir);

		return generateIndex(workDir, sourceEntries, validate);

	}

	public Index generateIndex(final File workDir,
			final Set<Entry<URL, File>> sourceEntries, final boolean validate)
			throws SAXException, Exception, IOException {
		final Validator validator;
		if (validate) {
			// Create CTL validator
			SchemaFactory sf = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema ctl_schema = sf.newSchema(Generator.class
					.getResource("/com/occamlab/te/schemas/ctl.xsd"));
			validator = ctl_schema.newValidator();
		} else {
			validator = NULL_VALIDATOR;
		}
		final CtlErrorHandler errorHandler = new CtlErrorHandler();
		validator.setErrorHandler(errorHandler);

		// Create a transformer to generate executable scripts from CTL
		// sources
		Processor processor = new Processor(false);
		processor.setConfigurationProperty(FeatureKeys.XINCLUDE, Boolean.TRUE);
		processor.setConfigurationProperty(FeatureKeys.LINE_NUMBERING,
				Boolean.TRUE);
		final XsltCompiler generatorCompiler = processor.newXsltCompiler();

		URL generatorStylesheet = Generator.class
				.getResource("/com/occamlab/te/generate_xsl.xsl");

		return new URLSourceTemplate(generatorStylesheet).execute(

		new SourceClosure<Index, Exception>() {
			@Override
			public Index execute(Source source) throws Exception, IOException {
				final XsltExecutable generatorXsltExecutable = generatorCompiler
						.compile(source);
				final XsltTransformer generatorTransformer = generatorXsltExecutable
						.load();

				return generate(workDir, validator, errorHandler,
						generatorTransformer, sourceEntries);
			}
		});
	}

	private Set<Entry<URL, File>> createSourceEntries(SetupOptions opts,
			final File workDir) throws UnsupportedEncodingException,
			MalformedURLException {
		// Create a list of CTL sources (may be files or dirs)
		ArrayList<File> files = new ArrayList<File>();
		files.addAll(opts.getSources());

		final Map<URL, File> sources = new LinkedHashMap<URL, File>();
		// Create a list of source CTL files only (no dirs),
		// and a corresponding list containing a working dir for each file
		Iterator<File> it = files.iterator();
		while (it.hasNext()) {
			File source = it.next();
			// System.out.println("Processing source(s) at: " +
			// source.getAbsolutePath());
			// appLogger.log(Level.INFO, "Processing source(s) at: " +
			// source.getAbsolutePath());

			String encodedName = URLEncoder.encode(source.getAbsolutePath(),
					"UTF-8");
			// encodedName = encodedName.replace('%', '~'); // In Java 5,
			// the Document.parse function has trouble
			// with the URL % encoding
			File workingDir = new File(workDir, encodedName);
			workingDir.mkdir();

			if (source.isDirectory()) {
				String[] children = source.list();
				for (int i = 0; i < children.length; i++) {
					// Finds all .ctl and .xml files in the directory to use
					String lowerName = children[i].toLowerCase();
					if (lowerName.endsWith(".ctl")
							|| lowerName.endsWith(".xml")) {
						File file = new File(source, children[i]);
						if (file.isFile()) {
							String basename = children[i].substring(0,
									children[i].length() - 4);
							File subdir = new File(workingDir, basename);
							subdir.mkdir();

							sources.put(file.toURI().toURL(), subdir);
						}
					}
				}
			} else {
				sources.put(source.toURI().toURL(), workingDir);
			}
		}

		final Set<Entry<URL, File>> sourceEntries = sources.entrySet();
		return sourceEntries;
	}

	private Index generate(final File workDir, Validator validator,
			CtlErrorHandler errorHandler, XsltTransformer generatorTransformer,

			Collection<Entry<URL, File>> sources)
			throws UnsupportedEncodingException, Exception,
			MalformedURLException {
		Index masterIndex = new Index();
		// formerly used to be in a resources classpath directory, but now
		// are included in the .jar
		{
			URL url = Generator.class
					.getResource("/com/occamlab/te/scripts/parsers.ctl");
			File workingDir = new File(workDir, URLEncoder.encode(
					"/com/occamlab/te/scripts/parsers.ctl", "UTF-8"));
			workingDir.mkdir();
			File indexFile = new File(workingDir, "index.xml");
			Index tmp = validateSourceFileAndGenerateIndex(url, validator,
					errorHandler, generatorTransformer, workingDir, indexFile);
			if (tmp != null) {
				masterIndex.add(tmp);
			}
		}
		{
			URL url = Generator.class
					.getResource("/com/occamlab/te/scripts/functions.ctl");
			File workingDir = new File(workDir, URLEncoder.encode(
					"/com/occamlab/te/scripts/functions.ctl", "UTF-8"));
			workingDir.mkdir();
			File indexFile = new File(workingDir, "index.xml");
			Index tmp = validateSourceFileAndGenerateIndex(url, validator,
					errorHandler, generatorTransformer, workingDir, indexFile);
			if (tmp != null) {
				masterIndex.add(tmp);
			}
		}

		// Process each CTL source file
		for (Entry<URL, File> source : sources) {
			final URL url = source.getKey();
			final File workingDir = source.getValue();

			// Read previous index for this file (if any), and determine
			// whether the
			// index and xsl need to be regenerated
			final File indexFile = new File(workingDir, "index.xml");
			Index index = null;
			boolean regenerate = true;
			if (indexFile.isFile()) {
				try {
					index = new Index(indexFile);
					regenerate = index.outOfDate();
				} catch (Exception e) {
					// If there was an exception reading the index file, it
					// is likely corrupt. Regenerate it.
					regenerate = true;
				}
			}

			if (regenerate) {
				final Index tmp = validateSourceFileAndGenerateIndex(url,
						validator, errorHandler, generatorTransformer,
						workingDir, indexFile);
				if (tmp != null) {
					index = tmp;
				}
			}

			// Add new index entries to the master index
			masterIndex.add(index);
		}

		// If there were any validation errors, display them and throw an
		// exception
		int error_count = errorHandler.getErrorCount();
		if (error_count > 0) {
			String msg = error_count + " validation error"
					+ (error_count == 1 ? "" : "s");
			int warning_count = errorHandler.getWarningCount();
			if (warning_count > 0) {
				msg += " and " + warning_count + " warning"
						+ (warning_count == 1 ? "" : "s");
			}
			msg += " detected.";
			// appLogger.severe(msg);
			throw new Exception(msg);
		}
		return masterIndex;
	}

	private Index validateSourceFileAndGenerateIndex(URL sourceFile,
			final Validator validator, final CtlErrorHandler errorHandler,
			final XsltTransformer generatorTransformer, final File workingDir,
			final File indexFile) throws Exception {

		final SourceTemplate sourceTemplate = new URLSourceTemplate(sourceFile);

		return validateSourceFileAndGenerateIndex(validator, errorHandler,
				generatorTransformer, workingDir, indexFile, sourceTemplate);
	}

	private Index validateSourceFileAndGenerateIndex(final Validator validator,
			final CtlErrorHandler errorHandler,
			final XsltTransformer generatorTransformer, final File workingDir,
			final File indexFile, final SourceTemplate sourceTemplate)
			throws SAXException, IOException, Exception {
		if (!validateSourceFile(validator, errorHandler, sourceTemplate)) {

			return generateIndex(generatorTransformer, workingDir, indexFile,
					sourceTemplate);

		}
		return null;
	}

	private boolean validateSourceFile(final Validator validator,
			final CtlErrorHandler errorHandler,
			final SourceTemplate sourceTemplate) throws SAXException,
			IOException {
		logger.log(Level.INFO, "Validating [" + sourceTemplate + "].");
		return sourceTemplate
				.execute(new SourceClosure<Boolean, SAXException>() {
					@Override
					public Boolean execute(Source source) throws SAXException,
							IOException {
						int old_count = errorHandler.getErrorCount();
						validator.validate(source);
						return (errorHandler.getErrorCount() > old_count);
					}
				});
	}

	private Index generateIndex(final XsltTransformer generatorTransformer,
			final File workingDir, final File indexFile,
			final SourceTemplate sourceTemplate) throws Exception, IOException {
		return sourceTemplate.execute(new SourceClosure<Index, Exception>() {
			@Override
			public Index execute(Source source) throws Exception, IOException {
				// Clean up the working directory
				Misc.deleteDirContents(workingDir);

				// Run the generator transformation. Output is an index file
				// and
				// is saved to disk.
				// The generator also creates XSL template files in the
				// working
				// dir.
				generatorTransformer.setSource(source);
				Serializer generatorSerializer = new Serializer();
				generatorSerializer.setOutputFile(indexFile);
				generatorTransformer.setDestination(generatorSerializer);
				XdmAtomicValue av = new XdmAtomicValue(workingDir
						.getAbsolutePath());
				generatorTransformer.setParameter(new QName("outdir"), av);
				generatorTransformer.transform();

				// Read the generated index
				return new Index(indexFile);
			}
		});
	}

}
