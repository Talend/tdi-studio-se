// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MultiSchemasUtil;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.hl7.HL7InputComponent;
import org.talend.designer.hl7.ui.data.HL7TreeNode;
import org.talend.designer.hl7.ui.header.TalendHL7Reader;

/**
 * DOC hywang class global comment. Detailled comment <br/>
 * 
 */
public class HL7Manager {

    protected HL7InputComponent hl7Component;

    protected UIManager uiManager;

    private List<String> msgContentList;

    private List<String> initMsgContentList = new ArrayList<String>();

    protected Map<String, List<MetadataColumn>> schemaRelationMap;

    private boolean hasFile;

    protected String filePath;

    private String startChar;

    private String endChar;

    /**
     * constructor.
     */
    public HL7Manager(HL7InputComponent hl7Component) {
        this.hl7Component = hl7Component;
        this.uiManager = new UIManager(this);
        this.schemaRelationMap = new HashMap<String, List<MetadataColumn>>();
        if (!hl7Component.isHL7Output()) {
            readMessageContent();
        }
    }

    private void readMessageContent() {
        filePath = hl7Component.getElementParameter(EParameterName.FILENAME.getName()).getValue().toString();
        String filePathNoQuotes = TalendTextUtils.removeQuotes(filePath);
        File file = Path.fromOSString(filePathNoQuotes).toFile();
        startChar = hl7Component.getElementParameter("START_MSG").getValue().toString();
        endChar = hl7Component.getElementParameter("END_MSG").getValue().toString();

        IElementParameter messagePara = hl7Component.getElementParameter("MESSAGE"); //$NON-NLS-1$
        List<Map<String, String>> msgListMap = (List<Map<String, String>>) messagePara.getValue();
        if (msgListMap != null && msgListMap.size() > 0) {
            for (Map<String, String> msgMap : msgListMap) {
                if (msgMap.get("MSGITEM") != null && msgMap.get("MSGITEM") instanceof String) {
                    String msgItem = msgMap.get("MSGITEM");
                    initMsgContentList.add(msgItem);

                }
            }
        } else if (file.exists()) {
            hasFile = true;
            ByteArray array = PropertiesFactory.eINSTANCE.createByteArray();
            try {
                array.setInnerContentFromFile(file);
                TalendHL7Reader talendHL7Reader = new TalendHL7Reader(new java.io.FileInputStream(file), "ISO-8859-15");
                String HL7InputTem = null;
                String messageText = "";

                while ((HL7InputTem = talendHL7Reader.getMessage()) != null) {
                    initMsgContentList.add(HL7InputTem);
                }
                if (initMsgContentList == null || initMsgContentList.size() == 0) {
                    String msgText = new String(array.getInnerContent());
                    initMsgContentList.add(msgText);
                }
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void updateRelationMapping(String key, MetadataColumn modifiedColumn, boolean isAdd) {
        if (schemaRelationMap != null) {
            List<MetadataColumn> schema = schemaRelationMap.get(key);
            if (schema != null) {
                if (!isAdd) {
                    for (MetadataColumn column : schema) {
                        if (column.getLabel().equals(modifiedColumn.getLabel())) {
                            schema.remove(column);
                            // schema.add(modifiedColumn);
                            break;
                        }
                    }
                } else {
                    schema.add(modifiedColumn);
                }
            }
        } else {
            schemaRelationMap = new HashMap<String, List<MetadataColumn>>();
        }
    }

    public boolean saveDataToComponent() {
        boolean result = false;
        List<Map<String, String>> schemas = convertMetadataColumns2Propertis();
        result = hl7Component.setTableElementParameter(schemas, "SCHEMAS"); //$NON-NLS-N$
        String startNsg = this.getUiManager().getHl7UI().getHeader().getStartCharValue();
        String endNsg = this.getUiManager().getHl7UI().getHeader().getEndCharValue();
        // save message List;
        msgContentList = this.getUiManager().getHl7UI().getHeader().getMsgContentList();
        boolean isChange = this.getUiManager().getHl7UI().getHeader().isMsgIsChange();
        IElementParameter MESSAGES = hl7Component.getElementParameter("MESSAGE");
        List<Map<String, String>> messageMap = (List<Map<String, String>>) MESSAGES.getValue();
        if (isChange) {
            if (messageMap == null) {
                messageMap = new ArrayList<Map<String, String>>();
            } else {
                messageMap.clear();
            }
            for (String msgItem : msgContentList) {

                Map<String, String> msgItemMap = new HashMap<String, String>();
                if (msgItem != null && !"".equals(msgItem)) {
                    msgItemMap.put("MSGITEM", msgItem);
                }
                messageMap.add(msgItemMap);
            }
            this.getUiManager().getHl7UI().getHeader().setMsgIsChange(false);
        }
        hl7Component.setValueToParameter("START_MSG", startNsg);
        hl7Component.setValueToParameter("END_MSG", endNsg);
        String filePath = this.getUiManager().getHl7UI().getHeader().getFilePath();
        hl7Component.setValueToParameter(EParameterName.FILENAME.getName(), filePath);
        return result;
    }

    public List<Map<String, String>> convertMetadataColumns2Propertis() {
        List<Map<String, String>> schemas = new ArrayList<Map<String, String>>();
        Iterator<String> it = schemaRelationMap.keySet().iterator();
        while (it.hasNext()) {
            MetadataTable metatable = ConnectionFactory.eINSTANCE.createMetadataTable();
            String schemaKey = it.next().toString();
            List<MetadataColumn> columns = new ArrayList<MetadataColumn>();
            columns.addAll(schemaRelationMap.get(schemaKey));
            Map<String, String> oneSchema = new HashMap<String, String>();
            String displayName = ""; //$NON-NLS-N$
            for (int i = 0; i < columns.size(); i++) {
                MetadataColumn column = columns.get(i);
                String original = columns.get(i).getOriginalField();
                if (original != null && !"".equals(original)) {
                    if (original.indexOf(TalendTextUtils.LBRACKET) > 0) {
                        original = original.substring(0, original.indexOf(TalendTextUtils.LBRACKET));
                    }
                }
                if (i != columns.size() - 1) {
                    displayName = displayName + TalendTextUtils.QUOTATION_MARK + original + TalendTextUtils.QUOTATION_MARK + ",";
                } else {
                    displayName = displayName + TalendTextUtils.QUOTATION_MARK + original + TalendTextUtils.QUOTATION_MARK;
                }
                if (column.getSourceType() == null) {
                    column.setSourceType("id_String");
                }
                if (column.getTalendType() == null) {
                    column.setTalendType("id_String");
                }
                // column.setLabel(column.getOriginalField()); // display user defined column name
                metatable.getColumns().add(column);
            }
            metatable.setLabel(schemaKey);
            schemaKey = mapSchemaKeyFromNode(schemaKey);
            IMetadataTable table = ConvertionHelper.convert(metatable);
            table.setTableName(schemaKey);
            if (displayName != null && !"".equals(displayName)) {
                oneSchema.put("SCHEMA", schemaKey); //$NON-NLS-N$
                oneSchema.put("MAPPING", displayName); //$NON-NLS-N$
                schemas.add(oneSchema);
            }
            if (table != null && !table.getListColumns().isEmpty()) {// 
                boolean find = false;
                int index = 0;
                for (int i = 0; i < hl7Component.getMetadataList().size(); i++) {
                    if (table.getLabel().equals(hl7Component.getMetadataList().get(i).getLabel())) {
                        find = true;
                        index = i;
                        break;
                    }
                }
                if (find) {
                    hl7Component.getMetadataList().remove(index);
                } else {
                    schemaKey = hl7Component.getProcess().generateUniqueConnectionName(
                            MultiSchemasUtil.getConnectionBaseName(schemaKey));
                    table.setTableName(schemaKey);
                }
                hl7Component.getMetadataList().add(table);
            } else if (table != null && table.getListColumns().isEmpty()) {
                boolean find = false;
                int index = 0;
                for (int i = 0; i < hl7Component.getMetadataList().size(); i++) {
                    if (table.getLabel().equals(hl7Component.getMetadataList().get(i).getLabel())) {
                        find = true;
                        index = i;
                        break;
                    }
                }
                if (find) {
                    hl7Component.getMetadataList().remove(index);
                    hl7Component.getMetadataList().add(table);
                }
            }
        }
        return schemas;
    }

    protected String mapSchemaKeyFromNode(String schemaKey) {
        for (IMetadataTable table1 : hl7Component.getMetadataList()) {
            if (table1.getTableName() != null && !"".equals(table1.getTableName()))
                if (table1.getTableName().split("_").length > 2) { // for 12862,split table name from node to map
                    // the label when the node is from repository
                    String labelInNode = table1.getTableName().split("_")[1];
                    if (labelInNode.equals(schemaKey)) {
                        schemaKey = table1.getTableName();
                        break;
                    }
                }

        }
        return schemaKey;
    }

    public List<String> getMsgContentList() {
        return this.msgContentList;
    }

    public void setMsgContentList(List<String> msgContentList) {
        this.msgContentList = msgContentList;
    }

    public List<String> getInitMsgContentList() {
        return this.initMsgContentList;
    }

    public UIManager getUiManager() {
        return this.uiManager;
    }

    public void setUiManager(UIManager uiManager) {
        this.uiManager = uiManager;
    }

    public HL7InputComponent getHl7Component() {
        return this.hl7Component;
    }

    public Map<String, List<MetadataColumn>> getSchemaRelationMap() {
        return this.schemaRelationMap;
    }

    public boolean isHasFile() {
        return this.hasFile;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public String getStartChar() {
        return this.startChar;
    }

    public String getEndChar() {
        return this.endChar;
    }

    public String getCurrentSchema(boolean sign) {
        return "";
    }

    public List<HL7TreeNode> getTreeData(String curSchema) {
        return new ArrayList<HL7TreeNode>();
    }

    public List<IMetadataColumn> getSchemaData(String currentSchema) {
        return new ArrayList<IMetadataColumn>();
    }

}
