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
package org.talend.designer.rowgenerator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.genhtml.HTMLDocUtils;
import org.talend.core.model.genhtml.HTMLHandler;
import org.talend.core.model.genhtml.IHTMLDocConstants;
import org.talend.core.model.genhtml.XMLHandler;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IComponentDocumentation;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.designer.rowgenerator.i18n.Messages;
import org.talend.designer.rowgenerator.ui.editor.MetadataColumnExt;

/**
 * This class is used for generating HTML file for Component 'tRowGenerator'. <br/>
 * <br/>
 *
 */
public class RowGeneratorComponentDocumentation implements IComponentDocumentation {

    private String componentName;

    private String tempFolderPath;

    private List<IMetadataTable> metadataListOut;

    private Document document;

    private String previewPicPath;

    private INode externalNode;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IComponentDocumentation#getHTMLFile()
     */
    @Override
    public URL getHTMLFile() {
        String xmlFilepath = this.tempFolderPath + File.separatorChar + this.componentName + IHTMLDocConstants.XML_FILE_SUFFIX;

        String htmlFilePath = this.tempFolderPath + File.separatorChar + this.componentName + IHTMLDocConstants.HTML_FILE_SUFFIX;

        final Bundle b = Platform.getBundle(PluginUtils.PLUGIN_ID);

        URL xslFileUrl = null;
        try {
            xslFileUrl = FileLocator
                    .toFileURL(FileLocator.find(b, new Path(IHTMLDocConstants.TROWGENERATOR_XSL_FILE_PATH), null));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }

        String xslFilePath = xslFileUrl.getPath();

        generateXMLInfo(getExternalNode());

        XMLHandler.generateXMLFile(tempFolderPath, xmlFilepath, document);
        HTMLHandler.generateHTMLFile(this.tempFolderPath, xslFilePath, xmlFilepath, htmlFilePath);

        File htmlFile = new File(htmlFilePath);
        if (htmlFile.exists()) {
            try {
                return htmlFile.toURL();
            } catch (MalformedURLException e) {
                ExceptionHandler.process(e);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IComponentDocumentation#setComponentName(java.lang.String)
     */
    @Override
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IComponentDocumentation#setTempFolderPath(java.lang.String)
     */
    @Override
    public void setTempFolderPath(String tempFolderpath) {
        this.tempFolderPath = tempFolderpath;
    }

    /**
     * Sets an instanceof <code>List<IMetadataTable></code>
     *
     * @param metadataListOut
     */
    public void setMetadataListOut(List<IMetadataTable> metadataListOut) {
        this.metadataListOut = metadataListOut;
    }

    /**
     * Generates all information which for XML file.
     */
    private void generateXMLInfo(INode externalNode) {
        document = DocumentHelper.createDocument();
        Element externalNodeElement = document.addElement("externalNode"); //$NON-NLS-1$
        generateMessages(externalNodeElement);
        externalNodeElement.addAttribute("name", HTMLDocUtils.checkString(this.componentName)); //$NON-NLS-1$
        externalNodeElement.addAttribute("preview", HTMLDocUtils.checkString(this.previewPicPath)); //$NON-NLS-1$

        Element parametersElement = externalNodeElement.addElement("parameters"); //$NON-NLS-1$
        List elementParameterList = externalNode.getElementParameters();
        generateParameters(parametersElement, elementParameterList);

        generateColumnInfo(externalNodeElement);
    }

    private void generateMessages(Element element) {
        // tRowgenerator.xsl
        element.addAttribute("i18n.job.component.parameters", Messages.getString("HTMLDocGenerator.component_parameters")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.properties", Messages.getString("HTMLDocGenerator.properties")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.values", Messages.getString("HTMLDocGenerator.values")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.rowgenerator.info.for", Messages.getString("HTMLDocGenerator.row_generator_info") + " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        element.addAttribute("i18n.job.column", Messages.getString("HTMLDocGenerator.column")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.key", Messages.getString("HTMLDocGenerator.key")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.type", Messages.getString("HTMLDocGenerator.type")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.nullable", Messages.getString("HTMLDocGenerator.nullable")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.length", Messages.getString("HTMLDocGenerator.length")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.precision", Messages.getString("HTMLDocGenerator.precision")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.rowgenerator.default", Messages.getString("HTMLDocGenerator.row_default")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.comment", Messages.getString("HTMLDocGenerator.comment")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.rowgenerator.functions", Messages.getString("HTMLDocGenerator.row_functions")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.rowgenerator.parameters", Messages.getString("HTMLDocGenerator.row_parameters")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void generateParameters(Element parametersElement, List elementParameterList) {
        List<IElementParameter> copyElementParameterList = new ArrayList(elementParameterList);
        if (elementParameterList != null && elementParameterList.size() != 0) {
            for (int j = 0; j < elementParameterList.size(); j++) {
                IElementParameter elemparameter = (IElementParameter) elementParameterList.get(j);
                if ((!elemparameter.isShow(copyElementParameterList) && (!elemparameter.getName().equals(
                        EParameterFieldType.SCHEMA_TYPE.getName())))
                        || elemparameter.getCategory().equals(EComponentCategory.VIEW)
                        || "ACTIVATE".equals(elemparameter.getName())//$NON-NLS-1$
                        || "MAP".equals(elemparameter.getName()) //$NON-NLS-1$
                        || "SCHEMA".equals(elemparameter.getName())) {//$NON-NLS-1$
                    continue;
                }

                Element columnElement = parametersElement.addElement("column"); //$NON-NLS-1$
                columnElement.addAttribute("name", HTMLDocUtils.checkString(elemparameter.getDisplayName())); //$NON-NLS-1$
                Object eleObj = elemparameter.getValue();
                String value = ""; //$NON-NLS-1$
                if (eleObj != null) {
                    value = eleObj.toString();
                    if (elemparameter.getName().equals("COMMENT")) {//$NON-NLS-1$
                        columnElement.addCDATA(value);
                    } else {
                        columnElement.setText(value);
                    }
                }

            }
        }
    }

    /**
     * Administrator Comment method "generateColumnInfo".
     *
     * @param externalNodeElement
     */
    private void generateColumnInfo(Element externalNodeElement) {

        for (IMetadataTable metadataTable : this.metadataListOut) {

            // List<IMetadataColumn> newMetadataTable =
            // rowGeneratorMain.getGeneratorUI().convert(metadataTable);
            for (IMetadataColumn tempColumn : metadataTable.getListColumns()) {
                MetadataColumnExt column = (MetadataColumnExt) tempColumn;
                Element columnElement = externalNodeElement.addElement("column"); //$NON-NLS-1$
                columnElement.addAttribute("name", HTMLDocUtils.checkString(column.getLabel())); //$NON-NLS-1$
                columnElement.addAttribute("key", String.valueOf(column.isKey())); //$NON-NLS-1$
                String type = HTMLDocUtils.checkString(column.getTalendType());
                if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
                    type = JavaTypesManager.getTypeToGenerate(column.getTalendType(), column.isNullable());
                }
                columnElement.addAttribute("type", type); //$NON-NLS-1$
                columnElement.addAttribute("nullable", String.valueOf(column.isNullable())); //$NON-NLS-1$
                String length;
                if ((column.getLength() == null) || (column.getLength() == 0)) {
                    length = ""; //$NON-NLS-1$
                } else {
                    length = String.valueOf(column.getLength());
                }

                columnElement.addAttribute("length", length); //$NON-NLS-1$
                String precision;
                if ((column.getPrecision() == null) || (column.getPrecision() == 0)) {
                    precision = ""; //$NON-NLS-1$
                } else {
                    precision = String.valueOf(column.getPrecision());
                }
                columnElement.addAttribute("precision", precision); //$NON-NLS-1$
                columnElement.addAttribute("default", HTMLDocUtils.checkString(column.getDefault())); //$NON-NLS-1$
                columnElement.addAttribute("comment", HTMLDocUtils.checkString(column.getComment())); //$NON-NLS-1$

                String functionName = ""; //$NON-NLS-1$
                if (column.getFunction() != null) {
                    functionName = column.getFunction().getName();
                }

                columnElement.addAttribute("functions", HTMLDocUtils.checkString(functionName)); //$NON-NLS-1$
                columnElement.addAttribute("parameters", HTMLDocUtils.checkString(column.getParameter())); //$NON-NLS-1$
                // columnElement.addAttribute("preview",
                // HTMLDocUtils.checkString(column.getPreview()));
            }
        }
    }

    /**
     * Sets the preview picture path of component.
     *
     * @param previewPicPath
     */
    public void setPreviewPicPath(String previewPicPath) {
        this.previewPicPath = previewPicPath;

    }

    public INode getExternalNode() {
        return this.externalNode;
    }

    public void setExternalNode(INode externalNode) {
        this.externalNode = externalNode;
    }
}
