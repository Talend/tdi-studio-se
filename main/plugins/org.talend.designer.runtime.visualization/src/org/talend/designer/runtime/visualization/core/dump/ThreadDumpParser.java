/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0 which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.talend.designer.runtime.visualization.core.dump;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.designer.runtime.visualization.IThreadElement;
import org.talend.designer.runtime.visualization.internal.core.ThreadDumpSaxEventHandler;
import org.xml.sax.SAXException;

/**
 * The thread dump parser.
 */
public class ThreadDumpParser extends AbstractDumpParser {

    /** The thread list elements. */
    private List<IThreadElement> threadListElements;

    private final String DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl"; //$NON-NLS-1$

    /**
     * The constructor.
     * 
     * @param file the dump file
     * @param threadListElements The thread list elements
     * @param monitor The progress monitor
     */
    public ThreadDumpParser(File file, List<IThreadElement> threadListElements, IProgressMonitor monitor) {
        super(monitor);
        Assert.isNotNull(file);

        this.file = file;
        this.threadListElements = threadListElements;
    }

    /**
     * Parses the thread dump.
     * 
     * @throws SAXException if creating parser fails
     * @throws ParserConfigurationException if creating parser fails
     * @throws IOException if parsing input fails
     */
    public void parse() throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
        spf.setFeature(DISALLOW_DOCTYPE_DECL, true);
        parser = spf.newSAXParser();
        ThreadDumpSaxEventHandler handler = new ThreadDumpSaxEventHandler(threadListElements, monitor);

        parser.parse(file, handler);
        info = handler.getProfileInfo();
    }
}
