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
package org.talend.repository.ui.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ComponentSetting;
import org.talend.core.model.properties.Status;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.repository.ProjectManager;
import org.talend.utils.files.FileUtils;
import org.talend.utils.xml.XmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * wchen class global comment. Detailled comment
 */
public class ExportProjectSettings {

    private String path;

    private Project pro;

    public ExportProjectSettings(String path) {
        this.path = path;
        pro = ProjectManager.getInstance().getCurrentProject();
    }

    public void saveProjectSettings() {
        if (path == null) {
            return;
        }

        File xmlFile = new File(path);
        org.talend.core.model.properties.Project project = pro.getEmfProject();

        try {
            final DocumentBuilderFactory fabrique = XmlUtils.getSecureDocumentBuilderFactory();

            DocumentBuilder analyseur = fabrique.newDocumentBuilder();
            analyseur.setErrorHandler(new ErrorHandler() {

                @Override
                public void error(final SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void fatalError(final SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void warning(final SAXParseException exception) throws SAXException {
                    throw exception;
                }

            });

            Document document = analyseur.newDocument();
            Element root = document.createElement("exportParameters"); //$NON-NLS-1$
            createVersionAttr(document, root);
            document.appendChild(root);

            // status
            List technicals = project.getTechnicalStatus();
            createStatus(technicals, document, root, "technicalStatus"); //$NON-NLS-1$
            List documentation = project.getDocumentationStatus();
            createStatus(documentation, document, root, "documentationStatus"); //$NON-NLS-1$

            // security

            Element security = document.createElement("exportParameter"); //$NON-NLS-1$
            root.appendChild(security);

            Attr typeAttr = document.createAttribute("type"); //$NON-NLS-1$
            typeAttr.setNodeValue("security"); //$NON-NLS-1$
            security.setAttributeNode(typeAttr);

            Attr name = document.createAttribute("name"); //$NON-NLS-1$
            name.setNodeValue("hidePassword"); //$NON-NLS-1$
            security.setAttributeNode(name);
            security.setTextContent(String.valueOf(project.isHidePassword()));

            // stats and logs
            if (project.getStatAndLogsSettings() != null) {
                List statAndLogs = project.getStatAndLogsSettings().getParameters().getElementParameter();
                saveParameters(document, root, statAndLogs, "statAndLogs"); //$NON-NLS-1$
            }

            // implicit context
            if (project.getImplicitContextSettings() != null) {
                List implicit = project.getImplicitContextSettings().getParameters().getElementParameter();
                saveParameters(document, root, implicit, "implicitContext"); //$NON-NLS-1$
            }

            // palette
            List componentSettings = project.getComponentsSettings();
            savePalette(document, root, componentSettings);

            saveDocumentByEncoding(document, xmlFile);
        } catch (ParserConfigurationException e) {
            ExceptionHandler.process(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

    }

    /**
     * record the version for studio.
     *
     */
    private void createVersionAttr(Document document, Element root) {
        String studioVersion = VersionUtils.getVersion();
        String talendVersion = VersionUtils.getTalendVersion();

        Attr sVersion = document.createAttribute("version"); //$NON-NLS-1$
        sVersion.setNodeValue(studioVersion);
        root.setAttributeNode(sVersion);

        boolean sameVersion = false;
        if (studioVersion.equals(talendVersion)) { // complete same
            sameVersion = true;
        } else if (VersionUtils.compareTo(studioVersion, talendVersion) == 0) { // only check the Major and Minor
            sameVersion = true;
        }
        if (!sameVersion) {
            Attr tVersion = document.createAttribute("talendVersion"); //$NON-NLS-1$
            tVersion.setNodeValue(talendVersion);
            root.setAttributeNode(tVersion);
        }
    }

    /**
     * wchen Comment method "saveParameters".
     *
     * @param document
     * @param root
     * @param list
     */
    private void savePalette(Document document, Element root, List list) {

        for (Object obj : list) {
            Element exportParameter = document.createElement("exportParameter"); //$NON-NLS-1$
            root.appendChild(exportParameter);
            Attr typeAttr = document.createAttribute("type"); //$NON-NLS-1$
            typeAttr.setNodeValue("palette"); //$NON-NLS-1$
            exportParameter.setAttributeNode(typeAttr);

            Attr name = document.createAttribute("name"); //$NON-NLS-1$
            name.setNodeValue(((ComponentSetting) obj).getName());
            exportParameter.setAttributeNode(name);

            Attr family = document.createAttribute("family"); //$NON-NLS-1$
            family.setNodeValue(((ComponentSetting) obj).getFamily());
            exportParameter.setAttributeNode(family);

            exportParameter.setTextContent(String.valueOf(((ComponentSetting) obj).isHidden()));

        }
    }

    /**
     * wchen Comment method "saveParameters".
     *
     * @param document
     * @param root
     * @param list
     */
    private void saveParameters(Document document, Element root, List list, String type) {
        for (Object obj : list) {
            Element exportParameter = document.createElement("exportParameter"); //$NON-NLS-1$
            root.appendChild(exportParameter);

            Attr typeAttr = document.createAttribute("type"); //$NON-NLS-1$
            typeAttr.setNodeValue(type);
            exportParameter.setAttributeNode(typeAttr);

            Attr name = document.createAttribute("name"); //$NON-NLS-1$
            name.setNodeValue(((ElementParameterType) obj).getName());
            exportParameter.setAttributeNode(name);
            exportParameter.setTextContent(((ElementParameterType) obj).getValue());

        }
    }

    /**
     * wchen Comment method "createStatus".
     *
     * @param technicals
     * @param document
     * @param root
     */
    private void createStatus(List technicals, Document document, Element root, String type) {
        for (Object obj : technicals) {
            Element exportParameter = document.createElement("exportParameter"); //$NON-NLS-1$
            root.appendChild(exportParameter);

            Attr name = document.createAttribute("name"); //$NON-NLS-1$
            name.setNodeValue(((Status) obj).getCode());
            exportParameter.setAttributeNode(name);

            Attr typeAttr = document.createAttribute("type"); //$NON-NLS-1$
            typeAttr.setNodeValue(type);
            exportParameter.setAttributeNode(typeAttr);

            exportParameter.setTextContent(((Status) obj).getLabel());
        }
    }

    /**
     * wchen Comment method "createStatsLogsSettings".
     *
     * @param list
     * @param document
     * @param element
     */
    private void createElementParameters(List list, Document document, Element element) {
        for (Object obj : list) {
            Element elementParameter = document.createElement("elementParameter"); //$NON-NLS-1$
            element.appendChild(elementParameter);
            ElementParameterType type = (ElementParameterType) obj;
            Attr name = document.createAttribute("name"); //$NON-NLS-1$
            name.setNodeValue(type.getName());
            elementParameter.setAttributeNode(name);
            Attr value = document.createAttribute("value"); //$NON-NLS-1$
            value.setNodeValue(type.getValue());
            elementParameter.setAttributeNode(value);
        }
    }

    private static void saveDocumentByEncoding(Document document, File file) throws IOException {
        if (document == null || file == null) {
            return;
        }
        OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(file), "UTF-8"); //$NON-NLS-1$
        FileUtils.writeXMLFile(document, output);
        output.close();
    }
}
