// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.sbi.engines.client.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ProcessItem;
import org.talend.utils.files.FileUtils;
import org.talend.utils.xml.XmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 */
public class GenerateSpagoBIXML {

    private File file;

    private ProcessItem item;

    private String contextName;

    public static final String SPAGOBI_FILE = "spagobi.xml"; //$NON-NLS-1$

    public static final String SPAGOBI_FOLDER = "_spagobi"; //$NON-NLS-1$

    /**
     * qzhang GenerateSpagoBIXML constructor comment.
     */
    public GenerateSpagoBIXML(File tmpFold, ProcessItem item, String contextName) {
        File spagobiFolder = new File(tmpFold, item.getProperty().getLabel() + SPAGOBI_FOLDER);
        if (!spagobiFolder.exists()) {
            spagobiFolder.mkdir();
        }
        this.file = new File(spagobiFolder, SPAGOBI_FILE); //$NON-NLS-1$
        this.item = item;
        this.contextName = contextName;
        createSpagoBIXML();
    }

    public URL getResult() throws Exception {
        if (file == null) {
            createSpagoBIXML();
        }
        return file.toURL();
    }

    private void createSpagoBIXML() {
        if (file != null) {
            Writer writer = null;
            try {
                Project project = ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                        .getProject();
                final DocumentBuilderFactory fabrique = XmlUtils.getSecureDocumentBuilderFactory();
                fabrique.setValidating(true);
                final DocumentBuilder analyseur = fabrique.newDocumentBuilder();
                analyseur.setErrorHandler(new ErrorHandler() {

                    public void error(final SAXParseException exception) throws SAXException {
                        throw exception;
                    }

                    public void fatalError(final SAXParseException exception) throws SAXException {
                        throw exception;
                    }

                    public void warning(final SAXParseException exception) throws SAXException {
                        throw exception;
                    }

                });

                Document document = analyseur.newDocument();
                Element spagobi = document.createElement("etl"); //$NON-NLS-1$
                document.appendChild(spagobi);
                // ///////////////////add job element.
                Element projectElement = document.createElement("job"); //$NON-NLS-1$
                spagobi.appendChild(projectElement);

                Attr attr = document.createAttribute("project"); //$NON-NLS-1$
                attr.setNodeValue(project.getEmfProject().getLabel());
                projectElement.setAttributeNode(attr);

                attr = document.createAttribute("jobName"); //$NON-NLS-1$
                attr.setNodeValue(item.getProperty().getLabel());
                projectElement.setAttributeNode(attr);

                attr = document.createAttribute("context"); //$NON-NLS-1$
                attr.setNodeValue(contextName);
                projectElement.setAttributeNode(attr);

                attr = document.createAttribute("language"); //$NON-NLS-1$
                attr.setNodeValue(project.getLanguage().getName());
                projectElement.setAttributeNode(attr);

                writer = new java.io.FileWriter(file);
                FileUtils.writeXMLFile(document, writer);
            } catch (Exception e) {
                ExceptionHandler.process(e);
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ex) {
                        ExceptionHandler.process(ex);
                    }
                }
            }
        }

    }

}
