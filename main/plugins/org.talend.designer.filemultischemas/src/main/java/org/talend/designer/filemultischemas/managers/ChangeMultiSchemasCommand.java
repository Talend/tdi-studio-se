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
package org.talend.designer.filemultischemas.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.MultiSchemasUtil;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.filemultischemas.data.IMultiSchemaConstant;
import org.talend.designer.filemultischemas.data.MultiMetadataColumn;
import org.talend.designer.filemultischemas.data.SchemasKeyData;

/**
 * cLi class global comment. Detailled comment
 */
public class ChangeMultiSchemasCommand extends Command {

    private INode node;

    private SchemasKeyData rootSchemaData;

    private Object oldFilePath, newFilePath;

    private Object oldRowSeperator, newRowSeperator;

    private Object oldFieldSeperator, newFieldSeperator;

    private Object oldEncoding, newEncoding;

    private Object oldEncodingType, newEncodingType;

    private Object oldTextEnclosure, newTextEnclosure;

    private Object oldEscapeChar, newEscapeChar;

    // hywang add for feature 7373
    private Object oldSelectedColumnIndex, newSelectedColumnIndex;

    private boolean oldCsvOption, newCsvOption;

    private boolean oldUseMultiSaparator, newUseMultiSaparator;

    private String oldSaparators, newSaparators;

    private String oldKeyValues, newKeyValues;

    private List<Map<String, String>> oldSchemasListMap, newSchemasListMap;

    private List<IMetadataTable> oldMetadataTable, newMetadataTable;

    private DelimitedFileConnection fakeConnection;

    private Map<EParameterName, String> params;

    public ChangeMultiSchemasCommand(INode node, SchemasKeyData rootSchemaData, DelimitedFileConnection fakeConnection,
            Object index) {
        super();
        this.node = node;
        this.rootSchemaData = rootSchemaData;
        this.fakeConnection = fakeConnection;
        initNew(index);
        initOld();
    }

    public ChangeMultiSchemasCommand(INode node, SchemasKeyData rootSchemaData, Map<EParameterName, String> params, Object index) {
        super();
        this.node = node;
        this.rootSchemaData = rootSchemaData;
        this.params = params;
        initNew(params);
        initOld();
    }

    private void initNew(Object object) {
        if (fakeConnection != null) {
            this.newFieldSeperator = fakeConnection.getFieldSeparatorValue();
            this.newRowSeperator = fakeConnection.getRowSeparatorValue();
            this.newEncoding = TalendTextUtils.addQuotes(fakeConnection.getEncoding());

            IElementParameter encodingType = node.getElementParameter(EParameterName.ENCODING_TYPE.getName());
            if (encodingType != null) {
                String[] codes = encodingType.getListItemsDisplayCodeName();
                if (codes != null) {
                    List<String> list = Arrays.asList(codes);
                    if (list.indexOf(fakeConnection.getEncoding()) > -1) {
                        this.newEncodingType = fakeConnection.getEncoding();
                    } else {
                        this.newEncodingType = list.get(list.size() - 1); // custom
                    }
                }
            }

            this.newFilePath = fakeConnection.getFilePath();
            this.newTextEnclosure = fakeConnection.getTextEnclosure();
            this.newEscapeChar = fakeConnection.getEscapeChar();
            this.newCsvOption = fakeConnection.isCsvOption();
            this.newSelectedColumnIndex = TalendTextUtils.QUOTATION_MARK + String.valueOf(object)
                    + TalendTextUtils.QUOTATION_MARK;
        } else if (params != null) {
            this.newFieldSeperator = params.get(EParameterName.FIELDSEPARATOR);
            this.newRowSeperator = params.get(EParameterName.ROWSEPARATOR);
            this.newEncoding = params.get(EParameterName.ENCODING);
            this.newEncodingType = params.get(EParameterName.ENCODING_TYPE);
            this.newFilePath = params.get(EParameterName.FILENAME);
            this.newTextEnclosure = params.get(EParameterName.TEXT_ENCLOSURE);
            this.newEscapeChar = params.get(EParameterName.ESCAPE_CHAR);
            this.newCsvOption = Boolean.valueOf(params.get(EParameterName.CSV_OPTION));
            this.newUseMultiSaparator = Boolean.valueOf(params.get(EParameterName.USE_MULTISEPARATORS));
            this.newSaparators = params.get(EParameterName.MULTI_SEPARATORS);
            this.newSelectedColumnIndex = params.get(EParameterName.COLUMNINDEX);
            this.newKeyValues = params.get(EParameterName.MULTI_KEYVALUES);
        }
        // map
        this.newSchemasListMap = new ArrayList<Map<String, String>>();
        // metadata table
        this.newMetadataTable = new ArrayList<IMetadataTable>();

        // hywang add for feature7373

        addSchemasMap(newSchemasListMap, newMetadataTable, this.rootSchemaData);

    }

    private void addSchemasMap(List<Map<String, String>> newValueList, List<IMetadataTable> newTables, SchemasKeyData keyData) {
        if (keyData == null) {
            return;
        }
        if (keyData.getParent() != null) { // not root
            //
            Map<String, String> map = new HashMap<String, String>();
            final String uniqueRecord = keyData.getUniqueRecord();
            final String recordType = keyData.getRecordType();

            map.put(IMultiSchemaConstant.SCHEMA, uniqueRecord);
            map.put(IMultiSchemaConstant.RECORD, TalendTextUtils.addQuotes(recordType));
            map.put(IMultiSchemaConstant.PARENT_RECORD, TalendTextUtils.addQuotes(keyData.getParent().getRecordType()));
            map.put(IMultiSchemaConstant.CARDINALITY, keyData.getCard());
            map.put(IMultiSchemaConstant.FIELDDELIMITED, TalendTextUtils.addQuotes(keyData.getSeparator()));
            newValueList.add(map);
            //
            String connectionBaseName = MetadataToolHelper.validateColumnName(
                    MultiSchemasUtil.getConnectionBaseName(uniqueRecord), 0);
            String uniqueConnName = node.getProcess().generateUniqueConnectionName(connectionBaseName);
            MetadataTable table = new MetadataTable();
            table.setLabel(uniqueRecord);
            table.setTableName(uniqueConnName);

            String keyIndex = ""; //$NON-NLS-1$
            boolean first = true;
            List<MultiMetadataColumn> metadataColumnsInModel = keyData.getMetadataColumnsInModel();

            for (int i = 0; i < metadataColumnsInModel.size(); i++) {
                MultiMetadataColumn column = metadataColumnsInModel.get(i);
                table.getListColumns().add(column.clone(true));
                if (column.isKey()) {
                    if (first) {
                        keyIndex += i;
                    } else {
                        keyIndex += "," + i; //$NON-NLS-1$
                    }
                    first = false;
                }
            }
            map.put(IMultiSchemaConstant.KEY_COLUMN_INDEX, TalendTextUtils.addQuotes(keyIndex));
            newTables.add(table);

        }
        for (SchemasKeyData child : keyData.getChildren()) {
            addSchemasMap(newValueList, newTables, child);
        }
    }

    private void initOld() {
        if (node == null) {
            return;
        }
        IElementParameter elementParameter = this.node.getElementParameter(EParameterName.FILENAME.getName());
        if (elementParameter != null) {
            this.oldFilePath = elementParameter.getValue();
        }
        elementParameter = this.node.getElementParameter(EParameterName.ENCODING_TYPE.getName());
        if (elementParameter != null) {
            this.oldEncodingType = elementParameter.getValue();
        }
        elementParameter = this.node.getElementParameter(EParameterName.ENCODING.getName());
        if (elementParameter != null) {
            this.oldEncoding = elementParameter.getValue();
        }
        elementParameter = this.node.getElementParameter(EParameterName.ROWSEPARATOR.getName());
        if (elementParameter != null) {
            this.oldRowSeperator = elementParameter.getValue();
        }
        elementParameter = this.node.getElementParameter(EParameterName.FIELDSEPARATOR.getName());
        if (elementParameter != null) {
            this.oldFieldSeperator = elementParameter.getValue();
        }
        elementParameter = this.node.getElementParameter(EParameterName.TEXT_ENCLOSURE.getName());
        if (elementParameter != null) {
            this.oldTextEnclosure = elementParameter.getValue();
        }
        elementParameter = this.node.getElementParameter(EParameterName.ESCAPE_CHAR.getName());
        if (elementParameter != null) {
            this.oldEscapeChar = elementParameter.getValue();
        }
        elementParameter = this.node.getElementParameter(EParameterName.CSV_OPTION.getName());
        if (elementParameter != null) {
            this.oldCsvOption = (Boolean) elementParameter.getValue();
        }

        // hywang add for feature7373
        elementParameter = this.node.getElementParameter(EParameterName.COLUMNINDEX.getName());
        if (elementParameter != null) {
            this.oldSelectedColumnIndex = String.valueOf(elementParameter.getValue());
        }

        elementParameter = this.node.getElementParameter(EParameterName.MULTI_SEPARATORS.getName());
        if (elementParameter != null) {
            this.oldSaparators = String.valueOf(elementParameter.getValue());
        }

        elementParameter = this.node.getElementParameter(EParameterName.USE_MULTISEPARATORS.getName());
        if (elementParameter != null) {
            this.oldUseMultiSaparator = (Boolean) elementParameter.getValue();
        }

        elementParameter = this.node.getElementParameter(EParameterName.MULTI_KEYVALUES.getName());
        if (elementParameter != null) {
            this.oldKeyValues = String.valueOf(elementParameter.getValue());
        }

        // schema table
        elementParameter = this.node.getElementParameter(EParameterName.SCHEMAS.getName());
        if (elementParameter != null) {
            Object value = elementParameter.getValue();
            List<Map<String, String>> oldvalueList = new ArrayList<Map<String, String>>();

            if (value != null) {
                List<Map<String, String>> valueList = (List<Map<String, String>>) value;
                for (Map<String, String> map : valueList) {
                    Map<String, String> valueMap = new HashMap<String, String>();
                    for (String key : map.keySet()) {
                        valueMap.put(key, map.get(key));
                    }
                    oldvalueList.add(valueMap);
                }
            }
            this.oldSchemasListMap = oldvalueList;
        }
        // metadata
        List<IMetadataTable> oldMetadataTable = new ArrayList<IMetadataTable>();
        for (IMetadataTable table : this.node.getMetadataList()) {
            oldMetadataTable.add(table.clone(true));
        }
        this.oldMetadataTable = oldMetadataTable;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        if (node == null || rootSchemaData == null) {
            return;
        }
        //
        setParameterValues(false);

    }

    private void setParameterValues(EParameterName paramName, Object oldValue, Object newValue, boolean undo) {
        IElementParameter elementParameter = null;

        elementParameter = this.node.getElementParameter(paramName.getName());
        if (elementParameter != null) {
            Object value = null;
            if (undo) {
                value = oldValue;
            } else {
                value = newValue;
            }
            elementParameter.setValue(value);
        }
    }

    private void setParameterValues(boolean undo) {
        IElementParameter elementParameter = null;

        setParameterValues(EParameterName.FILENAME, this.oldFilePath, this.newFilePath, undo);
        setParameterValues(EParameterName.ENCODING_TYPE, this.oldEncodingType, this.newEncodingType, undo);
        setParameterValues(EParameterName.ENCODING, this.oldEncoding, this.newEncoding, undo);
        setParameterValues(EParameterName.ROWSEPARATOR, this.oldRowSeperator, this.newRowSeperator, undo);
        setParameterValues(EParameterName.FIELDSEPARATOR, this.oldFieldSeperator, this.newFieldSeperator, undo);
        setParameterValues(EParameterName.TEXT_ENCLOSURE, this.oldTextEnclosure, this.newTextEnclosure, undo);
        setParameterValues(EParameterName.ESCAPE_CHAR, this.oldEscapeChar, this.newEscapeChar, undo);
        setParameterValues(EParameterName.CSV_OPTION, this.oldCsvOption, this.newCsvOption, undo);
        setParameterValues(EParameterName.COLUMNINDEX, this.oldSelectedColumnIndex, this.newSelectedColumnIndex, undo);
        setParameterValues(EParameterName.USE_MULTISEPARATORS, this.oldUseMultiSaparator, this.newUseMultiSaparator, undo);
        setParameterValues(EParameterName.MULTI_SEPARATORS, this.oldSaparators, this.newSaparators, undo);
        setParameterValues(EParameterName.MULTI_KEYVALUES, this.oldKeyValues, this.newKeyValues, undo);
        // PTODO need check again.
        elementParameter = this.node.getElementParameter(EParameterName.SCHEMAS.getName());

        if (elementParameter != null) {
            List<Map<String, String>> value = null;
            if (undo) {
                value = this.oldSchemasListMap;
            } else {
                value = this.newSchemasListMap;
            }
            elementParameter.setValue(value);
        }

        // PTODO need check again.

        if (undo) {
            if (this.oldMetadataTable != null) {
                addMetadataTable(this.oldMetadataTable);
            }
        } else {
            if (this.newMetadataTable != null) {
                addMetadataTable(this.newMetadataTable);
            }
        }
    }

    private void addMetadataTable(List<IMetadataTable> newTables) {
        // remove
        IProcess process = this.node.getProcess();

        List<IMetadataTable> needAddedTables = new ArrayList<IMetadataTable>();
        List<String> schemaNames = new ArrayList<String>();

        // record added table, and update the existed table.
        for (IMetadataTable table : newTables) {
            final String tableLabel = table.getLabel();
            schemaNames.add(tableLabel);

            IMetadataTable mappingTable = MetadataToolHelper.getMetadataTableFromNode(node, tableLabel);
            if (mappingTable == null) { // added
                needAddedTables.add(table);
                process.addUniqueConnectionName(table.getTableName());
            } else {
                MetadataToolHelper.copyTable(table, mappingTable);
            }
        }
        // record removed table
        List<IMetadataTable> needRemovedTables = new ArrayList<IMetadataTable>();
        for (IMetadataTable table : node.getMetadataList()) {
            if (!schemaNames.contains(table.getLabel())) {
                needRemovedTables.add(table);
                process.removeUniqueConnectionName(table.getTableName());
            }
        }
        node.getMetadataList().removeAll(needRemovedTables);
        node.getMetadataList().addAll(needAddedTables);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        if (node == null || rootSchemaData == null) {
            return;
        }
        setParameterValues(true);

    }

}
