/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0 which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.talend.designer.runtime.visualization.core.dump;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.designer.runtime.visualization.internal.core.cpu.CpuDumpSaxEventHandler;
import org.talend.designer.runtime.visualization.internal.core.cpu.CpuModel;
import org.talend.designer.runtime.visualization.internal.core.cpu.ICpuModel;
import org.xml.sax.SAXException;

/**
 * The CPU dump parser.
 */
public class CpuDumpParser extends AbstractDumpParser {

    /** The input stream. */
    private InputStream inputStream;

    /** The CPU model. */
    private CpuModel cpuModel;

    /** The input type. */
    private InputType inputType;

    private final String DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl"; //$NON-NLS-1$
    /**
     * The constructor.
     * 
     * @param file The dump file
     * @param cpuModel The CPU model
     * @param monitor The progress monitor
     */
    public CpuDumpParser(File file, ICpuModel cpuModel, IProgressMonitor monitor) {
        this(cpuModel, monitor);
        Assert.isNotNull(file);

        this.file = file;
        inputType = InputType.FILE;
    }

    /**
     * The constructor.
     * 
     * @param inputStream The input stream
     * @param cpuModel the CPU model
     * @param monitor The progress monitor
     */
    public CpuDumpParser(InputStream inputStream, ICpuModel cpuModel, IProgressMonitor monitor) {
        this(cpuModel, monitor);
        Assert.isNotNull(inputStream);

        this.inputStream = inputStream;
        inputType = InputType.STREAM;
    }

    /**
     * The constructor.
     * 
     * @param cpuModel The CPU model
     * @param monitor The progress monitor
     */
    private CpuDumpParser(ICpuModel cpuModel, IProgressMonitor monitor) {
        super(monitor);
        Assert.isNotNull(cpuModel);

        this.cpuModel = (CpuModel) cpuModel;
    }

    /**
     * Parses the CPU dump.
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
        CpuDumpSaxEventHandler handler = new CpuDumpSaxEventHandler(cpuModel, monitor);

        if (inputType == InputType.FILE) {
            if (file.exists() && file.canRead()) {
                parser.parse(file, handler);
            }
        } else if (inputType == InputType.STREAM) {
            parser.parse(inputStream, handler);
        }

        info = handler.getProfileInfo();
    }

    /**
     * The input type.
     */
    private enum InputType {

        /** The stream. */
        STREAM,

        /** The file. */
        FILE
    }
}