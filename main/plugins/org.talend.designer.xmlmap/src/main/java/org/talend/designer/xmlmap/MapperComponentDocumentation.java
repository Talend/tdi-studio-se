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
package org.talend.designer.xmlmap;

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
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IComponentDocumentation;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.designer.xmlmap.i18n.Messages;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.GlobalMapNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;

/**
 *
 * created by wchen on Aug 17, 2018 Detailled comment
 *
 */
public class MapperComponentDocumentation implements IComponentDocumentation {

    private String componentName;

    private String tempFolderPath;

    private Document document;

    private XmlMapData externalData;


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

        final Bundle b = Platform.getBundle(XmlMapPlugin.PLUGIN_ID);

        URL xslFileUrl = null;
        try {
            xslFileUrl = FileLocator.toFileURL(FileLocator.find(b, new Path(IHTMLDocConstants.TXMLMAP_XSL_FILE_PATH), null));
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
     * @see org.talend.core.model.process.IComponentDocumentation#setComponentLabel(java.lang.String)
     */
    @Override
    public void setComponentName(String componentLabel) {
        this.componentName = componentLabel;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IComponentDocumentation#setPath(java.lang.String)
     */
    @Override
    public void setTempFolderPath(String tempFolderPath) {
        this.tempFolderPath = tempFolderPath;
    }


    /**
     * Generates all information which for XML file.
     */
    private void generateXMLInfo(INode externalNode) {
        document = DocumentHelper.createDocument();
        Element externalNodeElement = document.addElement("externalNode"); //$NON-NLS-1$
        generateMessages(externalNodeElement);
        externalNodeElement.addAttribute("name", HTMLDocUtils.checkString(this.componentName)); //$NON-NLS-1$

        Element parametersElement = externalNodeElement.addElement("parameters"); //$NON-NLS-1$
        List elementParameterList = externalNode.getElementParameters();
        generateParameters(parametersElement, elementParameterList);

        List<InputXmlTree> inputTables = externalData.getInputTrees();
        List<OutputXmlTree> outputTables = externalData.getOutputTrees();
        List<VarTable> varTables = externalData.getVarTables();

        generateInputTablesInfo(externalNodeElement, inputTables, IHTMLDocConstants.MAPPER_TABLE_INPUT);
        generateOutputTablesInfo(externalNodeElement, outputTables, IHTMLDocConstants.MAPPER_TABLE_OUPUT);
        generateVarTablesInfo(externalNodeElement, varTables, IHTMLDocConstants.MAPPER_TABLE_VAR);
    }

    private void generateMessages(Element element) {
        // tXMLMap.xsl
        element.addAttribute("i18n.job.component.parameters", Messages.getString("HTMLDocGenerator.component_parameters")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.properties", Messages.getString("HTMLDocGenerator.properties")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.values", Messages.getString("HTMLDocGenerator.values")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.mapper.table.for", Messages.getString("HTMLDocGenerator.mapper.table.for") + " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        element.addAttribute("i18n.mapper.table.properties", Messages.getString("HTMLDocGenerator.mapper.table.properties")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.name", Messages.getString("HTMLDocGenerator.name")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.tmap.matching.mode", Messages.getString("HTMLDocGenerator.tmap.matching.mode")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.mapper.table.isminimized", Messages.getString("HTMLDocGenerator.mapper.iaminimized")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.tmap.isreject", Messages.getString("HTMLDocGenerator.tmap.isreject")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.tmap.isrejectinnnerjoin", Messages.getString("HTMLDocGenerator.tmap.isrejectinner")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.tmap.isinnerjoin", Messages.getString("HTMLDocGenerator.tmap.isinnerjoin")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.tmap.expressionfilter", Messages.getString("HTMLDocGenerator.tmap.expressionfilter")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute(
                "i18n.mapper.table.metadata.entries", Messages.getString("HTMLDocGenerator.mapper.metadatatable.entries")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.type", Messages.getString("HTMLDocGenerator.type")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.mapper.table.expression", Messages.getString("HTMLDocGenerator.mapper.expression")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.tmap.isnullable", Messages.getString("HTMLDocGenerator.tmap.isnullable")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.tmap.constraint.entries", Messages.getString("HTMLDocGenerator.tmap.constraint.entries")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void generateParameters(Element parametersElement, List elementParameterList) {
        List<IElementParameter> copyElementParameterList = new ArrayList(elementParameterList);
        if (elementParameterList != null && elementParameterList.size() != 0) {
            for (int j = 0; j < elementParameterList.size(); j++) {
                IElementParameter elemparameter = (IElementParameter) elementParameterList.get(j);
                if ((!elemparameter.isShow(copyElementParameterList) && (!elemparameter.getName().equals(
                        EParameterFieldType.SCHEMA_TYPE.getName())))
                        || elemparameter.getCategory().equals(EComponentCategory.VIEW)
                        || "ACTIVATE".equals(elemparameter.getName()) //$NON-NLS-1$
                        || "MAP".equals(elemparameter.getName()) //$NON-NLS-1$
                        || "PREVIEW".equals(elemparameter.getName())) {//$NON-NLS-1$
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



    private void generateInputTablesInfo(Element externalNodeElement, List<InputXmlTree> tables, String mapperTableType) {
        Element mapperTableElement = externalNodeElement.addElement("mapperTable"); //$NON-NLS-1$
        mapperTableElement.addAttribute("type", HTMLDocUtils.checkString(mapperTableType)); //$NON-NLS-1$
        Element tableElement = null;
        for (InputXmlTree table : tables) {
            tableElement = mapperTableElement.addElement("table"); //$NON-NLS-1$
            generateInputTableSummaryInfo(mapperTableElement, tableElement, table);

            List<GlobalMapNode> globalMapKeysValuesEntries = table.getGlobalMapKeysValues();
            if (HTMLDocUtils.checkList(globalMapKeysValuesEntries)) {

                Element globalMapKeysValuesElement = tableElement.addElement("globalMapKeysValues"); //$NON-NLS-1$
                for (TreeNode entry : globalMapKeysValuesEntries) {
                    generateTablesEntriesInfo(globalMapKeysValuesElement, entry);
                }
            }
            List<TreeNode> metadataTableEntries = table.getNodes();
            if (HTMLDocUtils.checkList(metadataTableEntries)) {
                Element metadataTableEntriesElement = tableElement.addElement("metadataTableEntries"); //$NON-NLS-1$
                for (TreeNode entry : metadataTableEntries) {
                    generateTablesEntriesInfo(metadataTableEntriesElement, entry);
                }
            }
        }
    }

    private void generateOutputTablesInfo(Element externalNodeElement, List<OutputXmlTree> tables, String mapperTableType) {
        Element mapperTableElement = externalNodeElement.addElement("mapperTable"); //$NON-NLS-1$
        mapperTableElement.addAttribute("type", HTMLDocUtils.checkString(mapperTableType)); //$NON-NLS-1$
        Element tableElement = null;
        for (OutputXmlTree table : tables) {
            tableElement = mapperTableElement.addElement("table"); //$NON-NLS-1$
            generateOutputTableSummaryInfo(mapperTableElement, tableElement, table);

            List<OutputTreeNode> metadataTableEntries = table.getNodes();
            if (HTMLDocUtils.checkList(metadataTableEntries)) {
                Element metadataTableEntriesElement = tableElement.addElement("metadataTableEntries"); //$NON-NLS-1$
                for (TreeNode entry : metadataTableEntries) {
                    generateTablesEntriesInfo(metadataTableEntriesElement, entry);
                }
            }
        }
    }

    private void generateVarTablesInfo(Element externalNodeElement, List<VarTable> tables, String mapperTableType) {
        Element mapperTableElement = externalNodeElement.addElement("mapperTable"); //$NON-NLS-1$
        mapperTableElement.addAttribute("type", HTMLDocUtils.checkString(mapperTableType)); //$NON-NLS-1$
        Element tableElement = null;
        for (VarTable table : tables) {
            tableElement = mapperTableElement.addElement("table"); //$NON-NLS-1$
            generateVarTableSummaryInfo(mapperTableElement, tableElement, table);

            List<VarNode> metadataTableEntries = table.getNodes();
            if (HTMLDocUtils.checkList(metadataTableEntries)) {
                Element metadataTableEntriesElement = tableElement.addElement("metadataTableEntries"); //$NON-NLS-1$
                for (VarNode entry : metadataTableEntries) {
                    generateTablesEntriesInfo(metadataTableEntriesElement, entry);
                }
            }
        }
    }

    /**
     * Generates metadata tables entries information.
     *
     * @param metadataTableEntriesElement
     * @param entry
     */
    private void generateTablesEntriesInfo(Element metadataTableEntriesElement, AbstractNode entry) {
        Element entryElement = metadataTableEntriesElement.addElement("entry"); //$NON-NLS-1$
        entryElement.addAttribute("name", HTMLDocUtils.checkString(entry.getName())); //$NON-NLS-1$
        String type = HTMLDocUtils.checkString(entry.getType());

        String expression = HTMLDocUtils.checkString(entry.getExpression());
        entryElement.addAttribute("expression", handleLongPathForPDF(expression)); //$NON-NLS-1$
        boolean isNullable = false;
        if (entry instanceof TreeNode) {
            isNullable = ((TreeNode) entry).isNullable();
        } else if (entry instanceof VarNode) {
            isNullable = ((VarNode) entry).isNullable();
        }
        entryElement.addAttribute("isNullable", String.valueOf(isNullable)); //$NON-NLS-1$

        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA) && type != "") { //$NON-NLS-1$
            type = JavaTypesManager.getTypeToGenerate(type, isNullable);
        }
        entryElement.addAttribute("type", type); //$NON-NLS-1$

        if (entry instanceof TreeNode) {
            TreeNode treeNode = (TreeNode) entry;
            String xpath = HTMLDocUtils.checkString(treeNode.getXpath());
            entryElement.addAttribute("name", handleLongPathForPDF(xpath)); //$NON-NLS-1$
            if (!treeNode.getChildren().isEmpty()) {
                for (TreeNode node : treeNode.getChildren()) {
                    generateTablesEntriesInfo(metadataTableEntriesElement, node);
                }
            }
        }
    }

    private String handleLongPathForPDF(String text) {
        String[] split = text.split("/");
        String newText = "";
        if (split.length > 1) {
            for (int i = 0; i < split.length - 1; i++) {
                newText = newText + split[i] + "/" + "\n";
            }
            newText = newText + split[split.length - 1];
        } else {
            return text;
        }
        return newText;
    }

    /**
     * Generates the summary information for table.
     *
     * @param mapperTableElement
     * @param tableElement
     * @param table
     */
    private void generateInputTableSummaryInfo(Element mapperTableElement, Element tableElement, InputXmlTree table) {
        tableElement.addAttribute("name", table.getName()); //$NON-NLS-1$
        tableElement.addAttribute("matching-mode", table.getMatchingMode()); //$NON-NLS-1$
        tableElement.addAttribute("lookup-mode", table.getLookupMode()); //$NON-NLS-1$
        tableElement.addAttribute("isMinimized", String.valueOf(table.isMinimized())); //$NON-NLS-1$
        tableElement.addAttribute("isInnerJoin", String.valueOf(table.isInnerJoin())); //$NON-NLS-1$
        tableElement.addAttribute("isPersistent", String.valueOf(table.isPersistent())); //$NON-NLS-1$
        tableElement.addAttribute("expressionFilter", String.valueOf(table.getExpressionFilter())); //$NON-NLS-1$
        tableElement.addAttribute("activateExpressionFilter", String.valueOf(table.isActivateExpressionFilter())); //$NON-NLS-1$
        tableElement.addAttribute("activateCondensedTool", String.valueOf(table.isActivateCondensedTool()));

        // set default value for no used properties for input
        tableElement.addAttribute("isReject", String.valueOf(false)); //$NON-NLS-1$
        tableElement.addAttribute("isRejectInnerJoin", String.valueOf(false)); //$NON-NLS-1$
    }

    private void generateOutputTableSummaryInfo(Element mapperTableElement, Element tableElement, OutputXmlTree table) {
        tableElement.addAttribute("name", table.getName()); //$NON-NLS-1$
        tableElement.addAttribute("isMinimized", String.valueOf(table.isMinimized())); //$NON-NLS-1$
        tableElement.addAttribute("isReject", String.valueOf(table.isReject())); //$NON-NLS-1$
        tableElement.addAttribute("isRejectInnerJoin", String.valueOf(table.isRejectInnerJoin())); //$NON-NLS-1$
        tableElement.addAttribute("expressionFilter", String.valueOf(table.getExpressionFilter())); //$NON-NLS-1$
        tableElement.addAttribute("activateExpressionFilter", String.valueOf(table.isActivateExpressionFilter())); //$NON-NLS-1$
        tableElement.addAttribute("activateCondensedTool", String.valueOf(table.isActivateCondensedTool()));

        // set default value for no used properties for output
        tableElement.addAttribute("isInnerJoin", String.valueOf(false)); //$NON-NLS-1$
        tableElement.addAttribute("isPersistent", String.valueOf(false)); //$NON-NLS-1$
    }

    private void generateVarTableSummaryInfo(Element mapperTableElement, Element tableElement, VarTable table) {
        tableElement.addAttribute("name", table.getName()); //$NON-NLS-1$
        tableElement.addAttribute("isMinimized", String.valueOf(table.isMinimized())); //$NON-NLS-1$

        // set default value for no used properties for var table
        tableElement.addAttribute("isReject", String.valueOf(false)); //$NON-NLS-1$
        tableElement.addAttribute("isRejectInnerJoin", String.valueOf(false)); //$NON-NLS-1$
        tableElement.addAttribute("isInnerJoin", String.valueOf(false)); //$NON-NLS-1$
        tableElement.addAttribute("isPersistent", String.valueOf(false)); //$NON-NLS-1$
    }


    public INode getExternalNode() {
        return this.externalNode;
    }

    public void setExternalNode(INode externalNode) {
        this.externalNode = externalNode;
    }

    public void setExternalData(XmlMapData emfMapData) {
        this.externalData = emfMapData;
    }
}
