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
package org.talend.repository.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.metadata.types.AutoConversionType;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.RepositoryConstants;
import org.talend.utils.files.FileUtils;
import org.talend.utils.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 *
 * created by hcyi on Aug 22, 2016 Detailled comment
 *
 */
public class AutoConvertTypesUtils {

    private static Logger log = Logger.getLogger(AutoConvertTypesUtils.class);

    public static final String AUTO_CONVERSION_TYPES_RESOURCES = "AutoConversionTypes";//$NON-NLS-1$

    public static final String ENABLE_AUTO_CONVERSION = "ENABLE_AUTO_CONVERSION";//$NON-NLS-1$

    public static final String ENABLE_AUTO_CONVERT_TYPE = "ENABLE_AUTO_CONVERT_TYPE"; //$NON-NLS-1$

    private static List<AutoConversionType> beanList = null;

    public static List<AutoConversionType> getAllAutoConversionTypes() {
        if (beanList == null) {
            load(AutoConvertTypesUtils.getTypeFile());
        }
        return beanList;
    }

    public static List<AutoConversionType> load(File file) {
        beanList = new ArrayList<>();
        try {
            DocumentBuilderFactory documentBuilderFactory = XmlUtils.getSecureDocumentBuilderFactory();
            DocumentBuilder analyseur = documentBuilderFactory.newDocumentBuilder();
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
            Document document = analyseur.parse(file);
            NodeList typeNodes = document.getElementsByTagName("conversionType"); //$NON-NLS-1$
            for (int i = 0; i < typeNodes.getLength(); i++) {
                Node typeNode = typeNodes.item(i);
                NamedNodeMap typeAttributes = typeNode.getAttributes();
                AutoConversionType typeObj = new AutoConversionType();
                typeObj.setSourceDataType(typeAttributes.getNamedItem("source").getNodeValue()); //$NON-NLS-1$
                typeObj.setTargetDataType(typeAttributes.getNamedItem("target").getNodeValue()); //$NON-NLS-1$
                typeObj.setConversionFunction(typeAttributes.getNamedItem("function").getNodeValue()); //$NON-NLS-1$
                beanList.add(typeObj);
            }
        } catch (Exception e) {
            return beanList;
        }
        return beanList;
    }

    public static boolean save(List<AutoConversionType> beans, File file) throws Exception {
        DocumentBuilderFactory factory = XmlUtils.getSecureDocumentBuilderFactory();
        OutputStreamWriter output = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {

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
            Document document = builder.newDocument();
            //document.setXmlVersion("1.0");//$NON-NLS-1$
            Element root = document.createElement("mapping");//$NON-NLS-1$
            document.appendChild(root);
            for (int i = 0; i < beans.size(); i++) {
                AutoConversionType bean = beans.get(i);
                Element typeNode = document.createElement("conversionType");//$NON-NLS-1$
                typeNode.setAttribute("source", bean.getSourceDataType());//$NON-NLS-1$
                typeNode.setAttribute("target", bean.getTargetDataType());//$NON-NLS-1$
                typeNode.setAttribute("function", bean.getConversionFunction());//$NON-NLS-1$
                root.appendChild(typeNode);
            }

            // save into file
            if (document != null) {
                output = new OutputStreamWriter(new FileOutputStream(file));
                FileUtils.writeXMLFile(document, output);
                // update
                beanList = new ArrayList<>();
                beanList.addAll(beans);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                output.close();
            }
        }
        return true;
    }

    public static boolean hasTypesFile() {
        try {
            IProject project = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
            IFolder prefsSettingFolder = ResourceUtils.getFolder(project, RepositoryConstants.SETTING_DIRECTORY, false);
            IFile typesFile = prefsSettingFolder.getFile(AUTO_CONVERSION_TYPES_RESOURCES + ".xml"); //$NON-NLS-1$
            if (typesFile.exists()) {
                return true;
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static File getTypeFile() {
        try {
            IProject project = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
            IPath settingPath = new ProjectScope(project).getLocation();
            return new File(settingPath + "\\" + AUTO_CONVERSION_TYPES_RESOURCES + ".xml"); //$NON-NLS-1$//$NON-NLS-2$;
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return null;
    }
}
