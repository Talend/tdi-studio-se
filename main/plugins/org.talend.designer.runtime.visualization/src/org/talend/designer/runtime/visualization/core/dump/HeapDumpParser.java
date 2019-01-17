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
import org.talend.designer.runtime.visualization.IHeapElement;
import org.talend.designer.runtime.visualization.internal.core.HeapDumpSaxEventHandler;
import org.xml.sax.SAXException;

/**
 * The heap dump parser.
 */
public class HeapDumpParser extends AbstractDumpParser {

    /** The heap list elements. */
    private List<IHeapElement> heapListElements;

    /**
     * The constructor.
     * 
     * @param file the dump file
     * @param heapListElements The heap list elements
     * @param monitor The progress monitor
     */
    public HeapDumpParser(File file, List<IHeapElement> heapListElements, IProgressMonitor monitor) {
        super(monitor);
        Assert.isNotNull(file);
        Assert.isNotNull(heapListElements);

        this.file = file;
        this.heapListElements = heapListElements;
    }

    /**
     * Parses the heap dump.
     * 
     * @throws SAXException if creating parser fails
     * @throws ParserConfigurationException if creating parser fails
     * @throws IOException if parsing input fails
     */
    public void parse() throws ParserConfigurationException, SAXException, IOException {
        parser = SAXParserFactory.newInstance().newSAXParser();
        HeapDumpSaxEventHandler handler = new HeapDumpSaxEventHandler(heapListElements, monitor);

        parser.parse(file, handler);
        info = handler.getProfileInfo();
    }
}
