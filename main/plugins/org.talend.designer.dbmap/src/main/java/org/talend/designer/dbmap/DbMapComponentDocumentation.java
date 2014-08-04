package org.talend.designer.dbmap;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.genhtml.HTMLDocUtils;
import org.talend.core.model.genhtml.HTMLHandler;
import org.talend.core.model.genhtml.IHTMLDocConstants;
import org.talend.core.model.genhtml.XMLHandler;
import org.talend.core.model.process.IComponentDocumentation;
import org.talend.core.model.process.IExternalData;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.i18n.Messages;

public class DbMapComponentDocumentation implements IComponentDocumentation {

    private String componentName;

    private String tempFolderPath;

    private Document document;

    private ExternalDbMapData externalData;

    private String previewPicPath;

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
            xslFileUrl = FileLocator.toFileURL(FileLocator.find(b, new Path(IHTMLDocConstants.MAPPERCOMPONENT_XSL_FILE_PATH),
                    null));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }

        String xslFilePath = xslFileUrl.getPath();

        generateXMLInfo();

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
     * Sets the <code>externalData</code>.
     * 
     * @param data
     */
    public void setExternalData(IExternalData data) {
        this.externalData = (ExternalDbMapData) data;
    }

    /**
     * Generates all information which for XML file.
     */
    private void generateXMLInfo() {
        document = DocumentHelper.createDocument();
        Element externalNodeElement = document.addElement("externalNode"); //$NON-NLS-1$
        generateMessages(externalNodeElement);
        externalNodeElement.addAttribute("name", HTMLDocUtils.checkString(this.componentName)); //$NON-NLS-1$

        externalNodeElement.addAttribute("preview", HTMLDocUtils.checkString(this.previewPicPath)); //$NON-NLS-1$

        List<ExternalDbMapTable> inputTables = externalData.getInputTables();
        List<ExternalDbMapTable> outputTables = externalData.getOutputTables();
        List<ExternalDbMapTable> varTables = externalData.getVarsTables();

        handleMapperTablesInfo(inputTables, externalNodeElement, IHTMLDocConstants.MAPPER_TABLE_INPUT);
        handleMapperTablesInfo(outputTables, externalNodeElement, IHTMLDocConstants.MAPPER_TABLE_OUPUT);
        handleMapperTablesInfo(varTables, externalNodeElement, IHTMLDocConstants.MAPPER_TABLE_VAR);
    }

    private void generateMessages(Element element) {
        // mapperComponent.xsl
        element.addAttribute("i18n.mapper.table.for", Messages.getString("HTMLDocGenerator.mapper.table.for") + " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        element.addAttribute("i18n.mapper.table.properties", Messages.getString("HTMLDocGenerator.mapper.table.properties")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.properties", Messages.getString("HTMLDocGenerator.properties")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.values", Messages.getString("HTMLDocGenerator.values")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.job.name", Messages.getString("HTMLDocGenerator.name")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.mapper.table.name", Messages.getString("HTMLDocGenerator.mapper.table.name")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.mapper.table.alias", Messages.getString("HTMLDocGenerator.mapper.alias")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.mapper.table.join.type", Messages.getString("HTMLDocGenerator.mapper.join.type")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.mapper.table.isminimized", Messages.getString("HTMLDocGenerator.mapper.iaminimized")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute(
                "i18n.mapper.table.metadata.entries", Messages.getString("HTMLDocGenerator.mapper.metadatatable.entries")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.mapper.table.expression", Messages.getString("HTMLDocGenerator.mapper.expression")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.mapper.table.operator", Messages.getString("HTMLDocGenerator.mapper.operator")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.mapper.table.isJoin", Messages.getString("HTMLDocGenerator.mapper.isjoin")); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("i18n.mapper.table.condition.entries", Messages.getString("HTMLDocGenerator.mapper.entries")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Generates input tables information.
     * 
     * @param mapperTableType
     */
    private void handleMapperTablesInfo(List<ExternalDbMapTable> inputTables, Element externalNodeElement, String mapperTableType) {
        List<ExternalDbMapTable> tables = inputTables;
        if (!HTMLDocUtils.checkList(tables)) {
            return;
        }
        generateMapperTablesInfo(externalNodeElement, tables, mapperTableType);
    }

    /**
     * This method used for generating all mapper tables information into xml file.
     * 
     * @param externalNodeElement
     * @param tables
     * @param mapperTableType
     */
    private void generateMapperTablesInfo(Element externalNodeElement, List<ExternalDbMapTable> tables, String mapperTableType) {
        Element mapperTableElement = externalNodeElement.addElement("mapperTable"); //$NON-NLS-1$
        mapperTableElement.addAttribute("type", HTMLDocUtils.checkString(mapperTableType)); //$NON-NLS-1$
        Element tableElement = null;
        for (ExternalDbMapTable table : tables) {
            tableElement = mapperTableElement.addElement("table"); //$NON-NLS-1$
            generateTableSummaryInfo(mapperTableElement, tableElement, table);

            List<ExternalDbMapEntry> metadataTableEntries = table.getMetadataTableEntries();
            if (!HTMLDocUtils.checkList(metadataTableEntries)) {
                continue;
            }

            Element metadataTableEntriesElement = tableElement.addElement("metadataTableEntries"); //$NON-NLS-1$
            for (ExternalDbMapEntry entry : metadataTableEntries) {
                generateTablesEntriesInfo(metadataTableEntriesElement, entry);
            }

            /**
             * generate custom Where clauses conditions entries
             */
            List<ExternalDbMapEntry> customWhereConditionsEntries = table.getCustomWhereConditionsEntries();
            if (!HTMLDocUtils.checkList(customWhereConditionsEntries)) {
                continue;
            }
            Element customWhereConditionsEntriesElement = tableElement.addElement("customWhereConditionsEntries"); //$NON-NLS-1$
            for (ExternalDbMapEntry entry : customWhereConditionsEntries) {
                generateTablesEntriesInfo(customWhereConditionsEntriesElement, entry);
            }

            /**
             * generate custom Other clauses conditions entries
             */
            List<ExternalDbMapEntry> customOtherConditionsEntries = table.getCustomOtherConditionsEntries();
            if (!HTMLDocUtils.checkList(customOtherConditionsEntries)) {
                continue;
            }
            Element customOtherConditionsEntriesElement = tableElement.addElement("customOtherConditionsEntries"); //$NON-NLS-1$
            for (ExternalDbMapEntry entry : customOtherConditionsEntries) {
                generateTablesEntriesInfo(customOtherConditionsEntriesElement, entry);
            }
        }
    }

    /**
     * Generates metadata tables entries information.
     * 
     * @param metadataTableEntriesElement
     * @param entry
     */
    private void generateTablesEntriesInfo(Element metadataTableEntriesElement, ExternalDbMapEntry entry) {
        Element entryElement = metadataTableEntriesElement.addElement("entry"); //$NON-NLS-1$
        entryElement.addAttribute("name", HTMLDocUtils.checkString(entry.getName())); //$NON-NLS-1$
        entryElement.addAttribute("expression", HTMLDocUtils.checkString(entry.getExpression())); //$NON-NLS-1$
        entryElement.addAttribute("operator", HTMLDocUtils.checkString(entry.getOperator())); //$NON-NLS-1$
        entryElement.addAttribute("isJoin", String.valueOf(entry.isJoin())); //$NON-NLS-1$
    }

    /**
     * Generates the summary information for table.
     * 
     * @param mapperTableElement
     * @param tableElement
     * @param table
     */
    private void generateTableSummaryInfo(Element mapperTableElement, Element tableElement, ExternalDbMapTable table) {

        tableElement.addAttribute("name", HTMLDocUtils.checkString(table.getName())); //$NON-NLS-1$
        tableElement.addAttribute("tableName", HTMLDocUtils.checkString(table.getTableName())); //$NON-NLS-1$
        tableElement.addAttribute("alias", HTMLDocUtils.checkString(table.getAlias())); //$NON-NLS-1$
        tableElement.addAttribute("joinType", HTMLDocUtils.checkString(table.getJoinType())); //$NON-NLS-1$
        tableElement.addAttribute("alias", HTMLDocUtils.checkString(table.getAlias())); //$NON-NLS-1$
        tableElement.addAttribute("isMinimized", String.valueOf(table.isMinimized())); //$NON-NLS-1$
    }

    /**
     * Sets the path of preview picture.
     * 
     * @param previewPicPath
     */
    public void setPreviewPicPath(String previewPicPath) {
        this.previewPicPath = previewPicPath;

    }
}
