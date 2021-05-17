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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.commands.Command;
import org.skife.csv.CSVReader;
import org.skife.csv.SimpleReader;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.data.list.UniqueStringGenerator;
import org.talend.core.CorePlugin;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.types.JavaDataTypeHelper;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.metadata.types.PerlDataTypeHelper;
import org.talend.core.model.metadata.types.PerlTypesManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.preference.metadata.MetadataTypeLengthConstants;
import org.talend.core.ui.process.IGEFProcess;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.core.utils.CsvArray;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.filemultischemas.MultiSchemasComponent;
import org.talend.designer.filemultischemas.data.CSVArrayAndSeparator;
import org.talend.designer.filemultischemas.data.ExternalMultiSchemasUIProperties;
import org.talend.designer.filemultischemas.data.IMultiSchemaConstant;
import org.talend.designer.filemultischemas.data.MultiMetadataColumn;
import org.talend.designer.filemultischemas.data.MultiSchemaRowData;
import org.talend.designer.filemultischemas.data.MultiSchemasMetadataColumn;
import org.talend.designer.filemultischemas.data.SchemasKeyData;
import org.talend.fileprocess.FileInputDelimited;
import org.talend.metadata.managment.ui.preview.ProcessDescription;

/**
 * cLi class global comment. Detailled comment
 */
public class MultiSchemasManager {

    private final ECodeLanguage language = LanguageManager.getCurrentLanguage();

    private MultiSchemasComponent connector;

    private UIManager uiManager;

    private DelimitedFileConnection recordConnection;

    private static final String ENCODING = "ISO-8859-15"; //$NON-NLS-1$

    private String keyValues;

    private static int DEFAULT_INDEX = 0;

    protected int maximumRowsToPreview = CorePlugin.getDefault().getPreferenceStore()
            .getInt(ITalendCorePrefConstants.PREVIEW_LIMIT);

    // hywang add for featture7373
    private int selectedColumnIndex = DEFAULT_INDEX;

    public int getSelectedColumnIndex() {
        return this.selectedColumnIndex;
    }

    public void setSelectedColumnIndex(int selectedColumnIndex) {
        this.selectedColumnIndex = selectedColumnIndex;
    }

    public MultiSchemasManager(MultiSchemasComponent connector, int selectedColumnIndex) {
        super();
        this.connector = connector;
        recordConnection = ConnectionFactory.eINSTANCE.createDelimitedFileConnection();
        this.selectedColumnIndex = selectedColumnIndex;
    }

    public DelimitedFileConnection getRecordConnection() {
        return this.recordConnection;
    }

    public UIManager getUIManager() {
        if (this.uiManager == null) {
            uiManager = new UIManager(this);
        }
        return this.uiManager;
    }

    public MultiSchemasComponent getMultiSchemasComponent() {
        return this.connector;
    }

    public ECodeLanguage getLanguage() {
        return this.language;
    }

    public boolean isReadOnly() {
        if (getMultiSchemasComponent().getOriginalNode().getJobletNode() != null) {
            return getMultiSchemasComponent().getOriginalNode().isReadOnly();
        }
        return getMultiSchemasComponent().getProcess().isReadOnly();
    }

    public void executeCommand(Command cmd) {
        IProcess process = this.getMultiSchemasComponent().getProcess();

        boolean executed = false;
        if (process != null && process instanceof IGEFProcess) {
            IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
            if (designerCoreUIService != null) {
                executed = designerCoreUIService.executeCommand((IGEFProcess) process, cmd);
            }
        }
        if (!executed) {
            cmd.execute();
        }
    }

    public void setValues(EParameterName paramName, Object value) {
        IElementParameter param = getMultiSchemasComponent().getElementParameter(paramName.getName());
        if (param != null) {
            param.setValue(value);
        }
    }

    public String getParameterValue(EParameterName paramName) {
        IElementParameter elementParameter = getMultiSchemasComponent().getElementParameter(paramName.getName());
        if (elementParameter != null) {
            Object value = elementParameter.getValue();
            if (value != null && value instanceof String) {
                return (String) value;
            }
        }
        return TalendTextUtils.addQuotes(""); //$NON-NLS-1$
    }

    public Object getParameterObjectValue(EParameterName paramName) {
        IElementParameter elementParameter = getMultiSchemasComponent().getElementParameter(paramName.getName());
        if (elementParameter != null) {
            Object value = elementParameter.getValue();
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     *
     * cLi Comment method "createSchemasTree".
     *
     * @param csvArray
     * @return tree of data.
     */
    public SchemasKeyData createSchemasTree(CSVArrayAndSeparator csvArrayBean, int selectColumnIndex) {
        if (csvArrayBean == null) {
            return null;
        }
        // final int columnIndex = 2;
        final SchemasKeyData rootSchemasKeyData = new SchemasKeyData(""); //$NON-NLS-1$
        List<String[]> rows = csvArrayBean.getCsvArray().getRows();
        for (int i = 0; i < rows.size(); i++) {
            // if (line.length < columnIndex) { // must contain 2 columns
            // continue;
            // }
            String[] line = rows.get(i);
            final String key = precessString(line[selectColumnIndex]);
            if (key == null) {
                continue;
            }
            String newKey = key + csvArrayBean.getSeparators().get(i);
            //

            SchemasKeyData schemaData = findSchemasKeyData(rootSchemasKeyData, newKey);
            if (schemaData == null) {
                schemaData = new SchemasKeyData(key);
                // TDI-6803
                String separator = csvArrayBean.getSeparators().get(i);
                if (separator != null && separator.equals("\\")) {
                    schemaData.setSeparator("\\\\");
                } else {
                    schemaData.setSeparator(separator);
                }
                schemaData.setUniqueRecord(generateUniqueRecordName(null, rootSchemasKeyData));

                rootSchemasKeyData.addChild(schemaData);
            }
            //
            MultiSchemaRowData rowData = createRowData(line, selectColumnIndex);
            if (rowData != null) {
                schemaData.addRowsData(rowData);
            }

        }
        for (SchemasKeyData schemaData : rootSchemasKeyData.getChildren()) {
            createPropertiesColumns(schemaData);
        }
        return rootSchemasKeyData;
    }

    /**
     *
     * cli Comment method "generateUniqueRecordName".
     *
     */
    private String generateUniqueRecordName(String baseName, SchemasKeyData root) {
        if (baseName == null) {
            baseName = ""; //$NON-NLS-1$
        }

        char firstChar = 'A';
        final String firstName = baseName + String.valueOf(firstChar);
        String uniqueName = firstName;

        while (existRecord(root, uniqueName)) {
            firstChar++;
            if (firstChar > 'Z') {
                return generateUniqueRecordName(firstName, root);
            }
            uniqueName = baseName + String.valueOf(firstChar);
        }
        return uniqueName;
    }

    private boolean existRecord(SchemasKeyData root, String name) {
        for (SchemasKeyData data : root.getChildren()) {
            if (data.getUniqueRecord().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private String precessString(String str) {
        if (str != null) {
            str = str.trim();
            if (!"".equals(str)) { //$NON-NLS-1$
                return str;
            }
        }
        return null;
    }

    private SchemasKeyData findSchemasKeyData(SchemasKeyData schemaKeyData, String key) {
        key = key.trim();
        if (schemaKeyData != null || !"".equals(key)) { //$NON-NLS-1$
            if (schemaKeyData.getRecordType().equals(key)) {
                return schemaKeyData;
            }
            for (SchemasKeyData child : schemaKeyData.getChildren()) {
                String dataKey = child.getRecordType() + child.getSeparator();
                if (dataKey.equals(key)) {
                    return child;
                }
                SchemasKeyData foundData = findSchemasKeyData(child, key);
                if (foundData != null) {
                    return foundData;
                }
            }
        }

        return null;
    }

    /**
     *
     * cLi Comment method "createRowData".
     *
     * @param line
     * @return
     */
    public MultiSchemaRowData createRowData(String[] line, int selectColumnIndex) {
        if (line == null || line.length < 1) {
            return null;
        }

        boolean isEndEmpty = true;

        List<String> newDatas = new ArrayList<String>();

        for (int i = line.length - 1; i > -1; i--) {
            String data = line[i];
            // find the last un-empty data.
            if (!"".equals(data) && isEndEmpty) { //$NON-NLS-1$
                isEndEmpty = false;
            }
            if (!isEndEmpty) {
                newDatas.add(data);
            }
        }

        MultiSchemaRowData rowData = new MultiSchemaRowData(precessString(line[selectColumnIndex]));

        for (int i = newDatas.size() - 1; i > -1; i--) {
            final String data = newDatas.get(i);
            MultiSchemasMetadataColumn metadataColumn = guessDataProperties(data);
            metadataColumn.setData(data);

            UniqueStringGenerator<MultiSchemasMetadataColumn> generator = getUniqueStringGenerator(rowData.getColumnsData());
            metadataColumn.setLabel(generator.getUniqueString());

            rowData.addColumnData(metadataColumn);
        }
        return rowData;
    }

    private UniqueStringGenerator<MultiSchemasMetadataColumn> getUniqueStringGenerator(List<MultiSchemasMetadataColumn> columns) {
        UniqueStringGenerator<MultiSchemasMetadataColumn> generator = new UniqueStringGenerator<MultiSchemasMetadataColumn>(
                ExternalMultiSchemasUIProperties.DEFAULT_COLUMN_NAME, columns) {

            @Override
            protected String getBeanString(MultiSchemasMetadataColumn bean) {
                return bean.getLabel();
            }

        };
        return generator;
    }

    private MultiSchemasMetadataColumn guessDataProperties(String data) {
        if (data == null) {
            data = ""; //$NON-NLS-1$
        }
        String talendType = null;
        int lengthValue = 0;
        int precisionValue = 0;
        // type
        if (getLanguage() == ECodeLanguage.JAVA) {
            talendType = JavaDataTypeHelper.getTalendTypeOfValue(data);
        } else { // perl
            talendType = PerlDataTypeHelper.getNewTalendTypeOfValue(data);
        }
        // length
        if (lengthValue < data.length()) {
            lengthValue = data.length();
        }
        // precision
        if (!data.equals("")) { //$NON-NLS-1$
            int positionDecimal = 0;
            if (data.indexOf(',') > -1) {
                positionDecimal = data.lastIndexOf(',');
                precisionValue = lengthValue - positionDecimal;
            } else if (data.indexOf('.') > -1) {
                positionDecimal = data.lastIndexOf('.');
                precisionValue = lengthValue - positionDecimal;
            }
        } else {
            talendType = getValueDefaultType();
            lengthValue = getValueDefaultLength();
        }

        //
        MultiSchemasMetadataColumn metadataColumn = new MultiSchemasMetadataColumn();

        if (getLanguage() == ECodeLanguage.JAVA) {
            if (talendType.equals(JavaTypesManager.FLOAT.getId()) || talendType.equals(JavaTypesManager.DOUBLE.getId())) {
                metadataColumn.setPrecision(precisionValue);
            } else {
                metadataColumn.setPrecision(0);
            }
        } else {
            if (talendType.equals(PerlTypesManager.DECIMAL)) {
                metadataColumn.setPrecision(precisionValue);
            } else {
                metadataColumn.setPrecision(0);
            }
        }
        metadataColumn.setTalendType(talendType);
        metadataColumn.setLength(lengthValue);

        return metadataColumn;
    }

    private String getValueDefaultType() {
        String valueDefaultType = MetadataTypeLengthConstants.VALUE_DEFAULT_TYPE;
        String defaultType = DesignerPlugin.getDefault().getPreferenceStore().getString(valueDefaultType);
        if (defaultType != null && !"".equals(defaultType.trim())) { //$NON-NLS-1$
            return defaultType;
        }
        return null;
    }

    private int getValueDefaultLength() {
        String valueDefaultLength = MetadataTypeLengthConstants.VALUE_DEFAULT_LENGTH;
        String defaultLength = DesignerPlugin.getDefault().getPreferenceStore().getString(valueDefaultLength);
        if (defaultLength != null && !"".equals(defaultLength.trim())) { //$NON-NLS-1$
            return Integer.parseInt(defaultLength);
        }
        return 0;
    }

    public List<MultiMetadataColumn> createPropertiesColumns(SchemasKeyData keyData) {
        List<MultiMetadataColumn> columns = new ArrayList<MultiMetadataColumn>();
        if (keyData != null) {
            final int tagLevel = keyData.getTagLevel();
            //
            final List<MultiSchemaRowData> rowsData = keyData.getRowsData();

            for (MultiSchemaRowData row : rowsData) {
                final List<MultiSchemasMetadataColumn> columnsData = row.getColumnsData();
                for (int i = 0; i < columnsData.size(); i++) {
                    final MultiSchemasMetadataColumn metadataColumn = columnsData.get(i);

                    MultiMetadataColumn column = null;
                    if (columns.size() - 1 < i) { // not contain
                        column = new MultiMetadataColumn(row.getKeyName());
                        column.setLabel(metadataColumn.getLabel());

                        column.setContainer(keyData);
                        columns.add(column);
                    } else {
                        column = columns.get(i);
                    }
                    column.addDataColumns(metadataColumn);
                }
            }
            keyData.setMetadataColumns(columns);
        }
        return columns;
    }

    /**
     *
     * cLi Comment method "saveProperties".
     *
     */
    public void savePropertiesToComponent(SchemasKeyData data, DelimitedFileConnection connection, int selectedIndex) {
        if (data != null) {
            ChangeMultiSchemasCommand cmd = new ChangeMultiSchemasCommand(getMultiSchemasComponent(), data, connection,
                    selectedIndex);
            executeCommand(cmd);
        }
    }

    /**
     *
     * wchen Comment method "saveProperties".
     *
     */
    public void savePropertiesToComponent(SchemasKeyData data, Map<EParameterName, String> params, int selectedIndex) {
        if (data != null) {
            ChangeMultiSchemasCommand cmd = new ChangeMultiSchemasCommand(getMultiSchemasComponent(), data, params, selectedIndex);
            executeCommand(cmd);
        }
    }

    /**
     *
     * cLi Comment method "retrievePropertiesFromNode".
     *
     */
    @SuppressWarnings("unchecked")
    public SchemasKeyData retrievePropertiesFromNode() {
        SchemasKeyData rootData = new SchemasKeyData(""); //$NON-NLS-1$
        IElementParameter elementParameter = getMultiSchemasComponent().getElementParameter(EParameterName.SCHEMAS.getName());
        final Object value = elementParameter.getValue();
        if (value == null) {
            return null;
        }
        List<Map<String, String>> schemasValues = (List<Map<String, String>>) value;
        createSimpleDatas(rootData, schemasValues);
        retrieveRelativeParent(rootData, schemasValues);
        return rootData;
    }

    private void retrieveRelativeParent(SchemasKeyData rootData, List<Map<String, String>> schemasValues) {
        for (Map<String, String> line : schemasValues) {
            String code = line.get(IMultiSchemaConstant.RECORD);
            if (code != null) {
                code = TalendTextUtils.removeQuotes(code);
                SchemasKeyData currentData = findKeyData(rootData, code);
                if (currentData != null) {
                    String codeParent = line.get(IMultiSchemaConstant.PARENT_RECORD);
                    if (codeParent != null) {
                        codeParent = TalendTextUtils.removeQuotes(codeParent);
                    }
                    if (codeParent == null || "".equals(codeParent.trim())) { // root //$NON-NLS-1$
                        rootData.addChild(currentData);
                    } else {
                        SchemasKeyData parentData = findKeyData(rootData, codeParent.trim());
                        if (parentData != null) {
                            parentData.addChild(currentData);
                            // set the added column, last column.
                            List<MultiMetadataColumn> metadataColumns = currentData.getMetadataColumns();
                            if (!metadataColumns.isEmpty()) {
                                MultiMetadataColumn multiMetadataColumn = metadataColumns.get(metadataColumns.size() - 1);
                                metadataColumns.remove(multiMetadataColumn);
                                currentData.setAddedColumn(multiMetadataColumn);
                            }
                        }
                    }
                }
            }
        }
    }

    private SchemasKeyData findKeyData(SchemasKeyData parent, String key) {
        if (parent == null || key == null) {
            return null;
        }
        for (SchemasKeyData child : parent.getChildren()) {
            if (key.equals(child.getRecordType())) {
                return child;
            }
            SchemasKeyData keydata = findKeyData(child, key);
            if (keydata != null) {
                return keydata;
            }
        }
        return null;
    }

    public void createSimpleDatas(SchemasKeyData rootData, List<Map<String, String>> schemasValues) {

        for (Map<String, String> line : schemasValues) {
            String code = line.get(IMultiSchemaConstant.RECORD);
            if (code != null) {
                // code = MultiSchemasManager.removeQuotes(code);
                code = TalendTextUtils.trimParameter(code);
                SchemasKeyData data = new SchemasKeyData(code);
                final String schemaName = line.get(IMultiSchemaConstant.SCHEMA);
                if (schemaName != null) {
                    final IMetadataTable metadataTable = MetadataToolHelper.getMetadataTableFromNode(getMultiSchemasComponent(),
                            schemaName);
                    if (metadataTable != null) {
                        data.setUniqueRecord(schemaName);
                        createMultiSchemasColumns(data, metadataTable.clone(true));
                    }
                    rootData.addChild(data);
                }
                String text = line.get(IMultiSchemaConstant.KEY_COLUMN_INDEX);
                if (text == null) {
                    text = ""; //$NON-NLS-1$
                }
                final String keyColumnIndex = TalendTextUtils.removeQuotes(text);
                String[] keys = keyColumnIndex.split(","); //$NON-NLS-1$
                for (String name : keys) {
                    try {
                        List<MultiMetadataColumn> metadataColumns = data.getMetadataColumns();
                        int index = Integer.parseInt(name);
                        for (int i = 0; i < metadataColumns.size(); i++) {
                            MultiMetadataColumn multiMetadataColumn = metadataColumns.get(i);
                            if (index == i) {
                                multiMetadataColumn.setKey(true);
                            } else {
                                multiMetadataColumn.setKey(false);
                            }
                        }
                    } catch (NumberFormatException e) {
                        //
                    }
                }
                String card = line.get(IMultiSchemaConstant.CARDINALITY);
                if (card == null) {
                    card = TalendTextUtils.addQuotes(""); //$NON-NLS-1$
                }
                data.setCard(card);
                String keyIndex = line.get(IMultiSchemaConstant.FIELDDELIMITED);
                if (keyIndex == null) {
                    keyIndex = ""; //$NON-NLS-1$
                } else {
                    keyIndex = TalendTextUtils.removeQuotes(keyIndex);
                }
                data.setSeparator(keyIndex);
            }
        }
    }

    public void createMultiSchemasColumns(SchemasKeyData data, final IMetadataTable metadataTable) {
        //
        for (IMetadataColumn column : metadataTable.getListColumns()) {

            MultiMetadataColumn multiColumn = new MultiMetadataColumn(data.getRecordType());
            multiColumn.setContainer(data);
            data.getMetadataColumns().add(multiColumn);

            multiColumn.setComment(column.getComment());
            multiColumn.setCustom(column.isCustom());
            multiColumn.setCustomId(column.getCustomId());
            multiColumn.setDefault(column.getDefault());
            multiColumn.setId(column.getId());
            multiColumn.setKey(column.isKey());
            multiColumn.setLabel(column.getLabel());
            multiColumn.setLength(column.getLength());
            multiColumn.setNullable(column.isNullable());
            multiColumn.setOriginalDbColumnName(column.getOriginalDbColumnName());
            multiColumn.setPattern(column.getPattern());
            multiColumn.setPrecision(column.getPrecision());
            multiColumn.setReadOnly(column.isReadOnly());
            multiColumn.setTalendType(column.getTalendType());
            multiColumn.setType(column.getType());

        }

    }

    /**
     *
     * cLi Comment method "getOriginalValue".
     *
     * check the context or not.
     */
    public String getOriginalValue(String value) {
        if (value != null) {
            IProcess process = this.getMultiSchemasComponent().getProcess();
            // TDI-21049:if not context mode,no need to deal with it use below code.
            if (process != null && ContextParameterUtils.containContextVariables(value)) {
                // add for bug9559
                String newValue = null;
                if (value.contains("+")) {//$NON-NLS-1$
                    // not only use context variable .
                    String[] split = value.split("\\+");//$NON-NLS-1$
                    for (int i = 0; i < split.length; i++) {
                        split[i] = split[i].trim();
                        if (split[i].startsWith(TalendTextUtils.getQuoteChar())
                                && split[i].endsWith(TalendTextUtils.getQuoteChar()) && split[i].length() - 1 > 0) {
                            split[i] = split[i].substring(1, split[i].length() - 1);
                        }
                        String varName = ContextParameterUtils.getVariableFromCode(split[i]);
                        if (varName != null) {
                            IContextParameter contextParameter = process.getContextManager().getDefaultContext()
                                    .getContextParameter(varName);
                            if (contextParameter != null) {
                                String value2 = contextParameter.getValue();
                                if (value2 != null) {
                                    split[i] = value2;
                                }
                            }
                        }
                        if (newValue == null) {
                            newValue = split[i];
                        } else {
                            newValue = newValue + split[i];
                        }
                    }
                    if (newValue.startsWith(TalendTextUtils.getQuoteChar()) && newValue.endsWith(TalendTextUtils.getQuoteChar())) {
                        return newValue;
                    } else {
                        return TalendTextUtils.addQuotes(newValue);
                    }
                } else {// only use context variable.
                    String varName = ContextParameterUtils.getVariableFromCode(value);
                    if (varName != null) {
                        IContextParameter contextParameter = process.getContextManager().getDefaultContext()
                                .getContextParameter(varName);
                        if (contextParameter != null) {
                            String value2 = contextParameter.getValue();
                            if (value2 != null) {
                                if (value2.startsWith(TalendTextUtils.getQuoteChar())
                                        && value2.endsWith(TalendTextUtils.getQuoteChar())) {

                                    return value2;
                                } else { // no quote
                                    return TalendTextUtils.addQuotes(value2);
                                }
                            }
                        }
                    }
                }
            }
        }
        return value;
    }

    public CSVArrayAndSeparator retrieveCsvArrayInUniqueModel(ProcessDescription processDesc, boolean checked,
            int selectColumnIndex, boolean useMultiSeparators) {
        if (processDesc == null || !checked) {
            return null;
        }

        final String filePath = TalendTextUtils.removeQuotes(processDesc.getFilepath());
        final String encoding = TalendTextUtils.removeQuotes(processDesc.getEncoding());

        final String fieldSeparator = TalendTextUtils.trimParameter(processDesc.getFieldSeparator());
        final String rowSeparator = TalendTextUtils.trimParameter(processDesc.getRowSeparator());

        final boolean needSkpipEmptyRecord = true;
        final boolean splitRecord = processDesc.isSplitRecord();
        if (useMultiSeparators) {
            try {
                return getCsvArrayForMs(filePath, fieldSeparator, encoding, selectColumnIndex);
            } catch (UnsupportedEncodingException e) {
                ExceptionHandler.process(e);
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }
        }

        if (processDesc.isCSVOption()) {
            return retrieveCsvArrayByCSVOption(filePath, encoding, fieldSeparator, rowSeparator, needSkpipEmptyRecord,
                    splitRecord, selectColumnIndex);
        } else {
            return retrieveCsvArrayByDelimited(filePath, encoding, fieldSeparator, rowSeparator, needSkpipEmptyRecord,
                    splitRecord, selectColumnIndex);
        }
    }

    private CSVArrayAndSeparator getCsvArrayForMs(String filePath, String separators, String encoding, int selectColumnIndex)
            throws UnsupportedEncodingException, IOException {
        File file = new File(TalendTextUtils.removeQuotes(filePath));
        separators = TalendTextUtils.removeQuotes(separators);
        CSVArrayAndSeparator csvArrayBean = new CSVArrayAndSeparator();
        Set<String> uniqueKey = new HashSet<String>();
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String readLine = "";
            String[] row = null;
            while ((readLine = reader.readLine()) != null) {
                int count = 0;
                boolean added = false;
                int sepIndex = 0;
                while (count < separators.length()) {
                    CSVReader csvReader = getCsvReader(new ByteArrayInputStream(readLine.getBytes()), separators.charAt(count),
                            encoding);
                    List items = csvReader.parse(readLine);
                    if (items.size() > 0) {
                        row = (String[]) items.get(0);// csvReader.getValues();
                    }
                    if (row.length > 1 && row.length > selectColumnIndex && isInKeyValues(getKeyValues(), row[selectColumnIndex])) {
                        // this kind of record will not added in the if that deal with rows that only have one column
                        if (uniqueKey.contains(row[selectColumnIndex] + String.valueOf(separators.charAt(count)))) {
                            sepIndex = count;
                        }
                        if (!uniqueKey.contains(row[selectColumnIndex] + String.valueOf(separators.charAt(count)))) {
                            uniqueKey.add(row[selectColumnIndex] + String.valueOf(separators.charAt(count)));
                            csvArrayBean.getCsvArray().add(row);
                            csvArrayBean.getSeparators().add(String.valueOf(separators.charAt(count)));
                            // csvReader.close();
                            added = true;
                            break;
                        }
                    }

                    // csvReader.close();
                    count++;
                }
                // for rows that only have one column , if this column don't have any separator use the first as it's
                // separator
                if (!added && selectColumnIndex < row.length && isInKeyValues(getKeyValues(), row[selectColumnIndex])
                        && !uniqueKey.contains(row[selectColumnIndex] + String.valueOf(separators.charAt(sepIndex)))) {

                    uniqueKey.add(row[selectColumnIndex] + String.valueOf(separators.charAt(sepIndex)));
                    csvArrayBean.getCsvArray().add(row);
                    csvArrayBean.getSeparators().add(String.valueOf(separators.charAt(sepIndex)));

                }
            }

        }
        return csvArrayBean;
    }

    public CSVArrayAndSeparator retrieveCsvArrayByCSVOption(final String filePath, final String encoding,
            final String fieldSeparator, final String rowSeparator, final boolean needSkpipEmptyRecord,
            final boolean splitRecord, int selectColumnIndex) {
        CSVArrayAndSeparator csvArrayBean = new CSVArrayAndSeparator();
        CSVReader multiSchameCsvReader = null;
        try {
            // read max columns
            multiSchameCsvReader = getCSVReader(filePath, encoding, fieldSeparator, rowSeparator, needSkpipEmptyRecord);
            List items = multiSchameCsvReader.parse(new File(filePath));
            int columsNum = -1;
            for (Object item : items) {
                String[] values = (String[]) item;
                if (columsNum < values.length) {
                    columsNum = values.length;
                }
            }

            // while (multiSchameCsvReader.readRecord()) {
            // String[] values = multiSchameCsvReader.getValues();
            // if (columsNum < values.length) {
            // columsNum = values.length;
            // }
            // }
            // multiSchameCsvReader.close();
            if (columsNum < 0) {
                return null;
            }
            // read data
            Set<String> uniqueKey = new HashSet<String>();
            String currentRowKey = ""; //$NON-NLS-1$

            multiSchameCsvReader = getCSVReader(filePath, encoding, fieldSeparator, rowSeparator, needSkpipEmptyRecord);

            items = multiSchameCsvReader.parse(new File(filePath));
            for (Object item : items) {
                String[] values = (String[]) item;
                if (values == null || values.length < 1 || values.length <= selectColumnIndex) {
                    continue;
                }
                final String first = values[selectColumnIndex];
                if ("".equals(first.trim())) { // must be contain first //$NON-NLS-1$
                    continue;
                }
                currentRowKey = first;
                if (uniqueKey.contains(currentRowKey)) { // existed.
                    continue;
                }
                uniqueKey.add(currentRowKey);
                csvArrayBean.getCsvArray().add(values);
                csvArrayBean.getSeparators().add(fieldSeparator);
            }

            // while (multiSchameCsvReader.readRecord()) {
            // String[] values = multiSchameCsvReader.getValues();
            // if (values == null || values.length < 1 || values.length <= selectColumnIndex) {
            // continue;
            // }
            // final String first = values[selectColumnIndex];
            //                if ("".equals(first.trim())) { // must be contain first //$NON-NLS-1$
            // continue;
            // }
            // currentRowKey = first;
            // if (uniqueKey.contains(currentRowKey)) { // existed.
            // continue;
            // }
            // uniqueKey.add(currentRowKey);
            // csvArrayBean.getCsvArray().add(values);
            // csvArrayBean.getSeparators().add(fieldSeparator);
            // }
        } catch (IOException e) {
            ExceptionHandler.process(e);
            return null;
        } finally {
            // if (multiSchameCsvReader != null) {
            // multiSchameCsvReader.close();
            // }
        }
        // return handlerCSVArray(csvArray, selectColumnIndex, multiSchameCsvReader);
        return csvArrayBean;
    }

    private CSVReader getCSVReader(final String filePath, final String encoding, final String fieldSeparator,
            final String rowSeparator, final boolean needSkpipEmptyRecord) throws IOException {
        CSVReader csvReadertFileInputDelimited = new SimpleReader();
        csvReadertFileInputDelimited.setSeperator(fieldSeparator.charAt(0));
        // csvReadertFileInputDelimited = new CsvReader(new BufferedReader(new InputStreamReader(new
        // FileInputStream(filePath),
        // encoding)), fieldSeparator.charAt(0));
        //
        // csvReadertFileInputDelimited.setRecordDelimiter(rowSeparator.charAt(0));
        // csvReadertFileInputDelimited.setSkipEmptyRecords(needSkpipEmptyRecord);
        // csvReadertFileInputDelimited.setTextQualifier('"');// PTODO
        // csvReadertFileInputDelimited.setEscapeMode(CsvReader.ESCAPE_MODE_DOUBLED);// PTODO

        return csvReadertFileInputDelimited;
    }

    /**
     * cli Comment method "retrieveCsvArrayByDelimited".
     */
    private CSVArrayAndSeparator retrieveCsvArrayByDelimited(final String filePath, final String encoding,
            final String fieldSeparator, final String rowSeparator, final boolean needSkpipEmptyRecord,
            final boolean splitRecord, int selectColumnIndex) {
        CSVArrayAndSeparator csvArrayBean = new CSVArrayAndSeparator();
        FileInputDelimited fileInputDelimited = null;
        try {
            fileInputDelimited = new FileInputDelimited(filePath, encoding, fieldSeparator, rowSeparator, needSkpipEmptyRecord,
                    0, 0, -1, -1, splitRecord);
            final int maxColumnCount = FileInputDelimited.getMaxColumnCount(filePath, encoding, fieldSeparator, rowSeparator,
                    needSkpipEmptyRecord, splitRecord, 0, Integer.MAX_VALUE);
            if (maxColumnCount < 1) {
                return null;
            }
            Set<String> uniqueKey = new HashSet<String>();
            String currentRowKey = ""; //$NON-NLS-1$

            while (fileInputDelimited.nextRecord()) {
                final String first = fileInputDelimited.get(selectColumnIndex);
                if ("".equals(first.trim())) { // must be contain first //$NON-NLS-1$
                    continue;
                }
                currentRowKey = first;
                if (uniqueKey.contains(currentRowKey)) { // existed.
                    continue;
                }
                uniqueKey.add(currentRowKey);
                handlerDelimitedArray(csvArrayBean, maxColumnCount, first, fileInputDelimited, fieldSeparator);
            }
        } catch (IOException e) {
            ExceptionHandler.process(e);
            return null;
        } finally {
            if (fileInputDelimited != null) {
                fileInputDelimited.close();
            }
        }

        return csvArrayBean;
    }

    public CsvArray getCsvArrayForMultiSchemaDelimited(String filePath, String separators, String encoding, String keyValues,
            int keyIndex) throws UnsupportedEncodingException, IOException {

        File file = new File(TalendTextUtils.removeQuotes(filePath));
        separators = TalendTextUtils.removeQuotes(separators);
        CsvArray csvArray = new CsvArray();
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String readLine = "";
            String[] row = null;
            int i = 0;
            while ((readLine = reader.readLine()) != null && i < maximumRowsToPreview) {
                boolean added = false;
                int count = 0;
                while (count < separators.length()) {
                    CSVReader csvReader = getCsvReader(new ByteArrayInputStream(readLine.getBytes()), separators.charAt(count),
                            encoding);

                    List items = csvReader.parse(readLine);

                    // csvReader.readRecord();
                    if (items.size() > 0) {
                        row = (String[]) items.get(0);// csvReader.getValues();
                    }

                    if (row.length > 1 && row.length > keyIndex) {
                        if (isInKeyValues(keyValues, row[keyIndex])) {
                            added = true;
                            csvArray.add(row);
                            // csvReader.close();
                            i++;
                            break;
                        }
                    }
                    // csvReader.close();
                    count++;
                }
                if (!added && row != null && row.length > keyIndex) {
                    if (isInKeyValues(keyValues, row[keyIndex])) {
                        csvArray.add(row);
                        i++;
                    }
                }
            }

        }

        return csvArray;
    }

    private boolean isInKeyValues(String keyValues, String str) {
        if (keyValues == null || "".equals(keyValues)) {
            return false;
        }
        keyValues = TalendTextUtils.removeQuotes(keyValues);

        for (String value : keyValues.split(",")) {
            if (value.trim().equals(str)) {
                return true;
            }
        }

        return false;
    }

    private CSVReader getCsvReader(ByteArrayInputStream inputStream, char separator, String encoding)
            throws FileNotFoundException, UnsupportedEncodingException {
        encoding = TalendTextUtils.removeQuotes(encoding);
        // CSVReader csvReader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream, encoding == null ?
        // ENCODING
        // : encoding)), separator);
        // csvReader.setRecordDelimiter('\n');
        // csvReader.setSkipEmptyRecords(true);
        // csvReader.setTextQualifier('"');
        //
        // csvReader.setEscapeMode(com.csvreader.CsvReader.ESCAPE_MODE_DOUBLED);
        CSVReader csvReader = new SimpleReader();
        csvReader.setSeperator(separator);
        return csvReader;
    }

    /**
     * hywang Comment method "handlerDelimitedArray" for feature 7373.
     */
    private void handlerDelimitedArray(CSVArrayAndSeparator csvArrayBean, int maxColumnCount, String first,
            FileInputDelimited fileInputDelimited, final String fieldSeparator) throws IOException {

        String[] lineDatas = new String[maxColumnCount];
        lineDatas[0] = first;

        for (int i = 1; i < lineDatas.length; i++) {
            String value = fileInputDelimited.get(i);
            lineDatas[i] = value;
        }
        csvArrayBean.getCsvArray().add(lineDatas);
        csvArrayBean.getSeparators().add(fieldSeparator);
    }

    public String getKeyValues() {
        return this.keyValues;
    }

    public void setKeyValues(String keyValues) {
        this.keyValues = keyValues;
    }

    // /**
    // * hywang Comment method "handlerCSVArray" for feature 7373.
    // */
    // private CsvArray handlerCSVArray(CsvArray csvArray, int selectColumnIndex, CsvReader multiSchameCsvReader) {
    // CsvArray newArray = new CsvArray();
    // int maxRowCount = csvArray.getRows().size();
    // int maxColumnCount = csvArray.getRows().get(0).length;
    // int lineDataSize = maxColumnCount - selectColumnIndex;
    //
    // String first = null;
    //
    // for (int i = 0; i < maxRowCount; i++) {
    // String[] lineDatas = new String[lineDataSize];
    // first = csvArray.getRows().get(i)[selectColumnIndex];
    // lineDatas[0] = first;
    // for (int j = 1; j < lineDataSize; j++) {
    // String value = csvArray.getRows().get(i)[selectColumnIndex + j];
    // lineDatas[j] = value;
    // }
    // newArray.add(lineDatas);
    // }
    // multiSchameCsvReader.close();
    // return newArray;
    // }

}
