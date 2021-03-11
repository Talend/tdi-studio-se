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
package org.talend.designer.core.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.commons.ui.utils.workbench.preferences.ComboFieldEditor;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.ui.preference.metadata.MetadataTypeLengthConstants;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;

/**
 * DOC qwei class global comment. Detailled comment
 */
public class MetadataTypeLengthPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private StringFieldEditor valueLengthField, fieldLengthField;

    private Composite parent;

    private ComboFieldEditor comboValueTypeField, comboFieldTypeField;

    private Group valueGroup, fieldGroup, dbTypeGroup;

    private StringFieldEditor booleanField, bigDecimalField, byteField, integerField, bytearrayField, longField, characterField;

    private StringFieldEditor objectField, dateField, shortField, doubleField, stringField, floatField, listField, datetimeField;

    public MetadataTypeLengthPreferencePage() {
        super(GRID);
        setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        // TODO Auto-generated method stub
        parent = new Composite(getFieldEditorParent(), SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        parent.setLayoutData(gridData);
        parent.setLayout(gridLayout);

        /**
         * set value Group
         */
        valueGroup = new Group(parent, SWT.NONE);
        valueGroup.setText(Messages.getString("MetadataTypeLengthPreferencePage.VALUE")); //$NON-NLS-1$
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        valueGroup.setLayoutData(gd);
        Composite com = new Composite(valueGroup, SWT.NONE);
        createJavaFieldEditors(com);
    }

    /**
     * DOC qwei Comment method "createJavaFieldEditors".
     */
    private void createJavaFieldEditors(Composite com) {
        JavaType[] strTypeValue = JavaTypesManager.getJavaTypes();
        String[] strDisplay = new String[strTypeValue.length];
        String[] strValue = new String[strTypeValue.length];
        for (int i = 0; i < strDisplay.length; i++) {
            strDisplay[i] = strTypeValue[i].getNullableClass().getSimpleName();
            strValue[i] = strTypeValue[i].getId();
        }
        String[][] strvalueType = new String[strDisplay.length][2];
        for (int i = 0; i < strDisplay.length; i++) {
            strvalueType[i][0] = strDisplay[i];
            strvalueType[i][1] = strValue[i];

        }
        comboValueTypeField = new ComboFieldEditor(MetadataTypeLengthConstants.VALUE_DEFAULT_TYPE,
                Messages.getString("MetadataTypeLengthConstants.VALUE_DEFAULT_TYPE"), strvalueType, com); //$NON-NLS-1$
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 4;
        com.setLayoutData(gridData);
        GridLayout gridLayout = (GridLayout) com.getLayout();
        gridLayout.numColumns = 6;
        com.setLayout(gridLayout);
        valueLengthField = new StringFieldEditor(MetadataTypeLengthConstants.VALUE_DEFAULT_LENGTH,
                Messages.getString("MetadataTypeLengthConstants.VALUE_DEFAULT_LENGTH"), valueGroup); //$NON-NLS-1$
        addField(comboValueTypeField);
        addField(valueLengthField);

        /**
         * set Fields
         *
         */
        fieldGroup = new Group(parent, SWT.NONE);
        fieldGroup.setText(Messages.getString("MetadataTypeLengthPreferencePage.FIELD")); //$NON-NLS-1$
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        fieldGroup.setLayoutData(gd);
        com = new Composite(fieldGroup, SWT.NONE);
        comboFieldTypeField = new ComboFieldEditor(MetadataTypeLengthConstants.FIELD_DEFAULT_TYPE,
                Messages.getString("MetadataTypeLengthConstants.FIELD_DEFAULT_TYPE"), strvalueType, com); //$NON-NLS-1$
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 4;
        com.setLayoutData(gridData);
        gridLayout = (GridLayout) com.getLayout();
        gridLayout.numColumns = 6;
        com.setLayout(gridLayout);
        fieldLengthField = new StringFieldEditor(MetadataTypeLengthConstants.FIELD_DEFAULT_LENGTH,
                Messages.getString("MetadataTypeLengthConstants.FIELD_DEFAULT_LENGTH"), fieldGroup); //$NON-NLS-1$
        /**
         * set data Type
         */
        dbTypeGroup = new Group(parent, SWT.NONE);
        dbTypeGroup.setText(Messages.getString("MetadataTypeLengthPreferencePage.DBTYPE")); //$NON-NLS-1$
        gd = new GridData(GridData.FILL_HORIZONTAL);
        dbTypeGroup.setLayoutData(gd);
        booleanField = new StringFieldEditor(MetadataTypeLengthConstants.ID_BOOLEAN,
                Messages.getString("MetadataTypeLengthConstants.BOOLEAN_LENGTH"), dbTypeGroup); //$NON-NLS-1$
        bigDecimalField = new StringFieldEditor(MetadataTypeLengthConstants.ID_BIGDECIMAL,
                Messages.getString("MetadataTypeLengthConstants.BIGDECIMAL_LENGTH"), dbTypeGroup); //$NON-NLS-1$
        byteField = new StringFieldEditor(MetadataTypeLengthConstants.ID_BYTE,
                Messages.getString("MetadataTypeLengthConstants.BYTE_LENGTH"), dbTypeGroup); //$NON-NLS-1$
        integerField = new StringFieldEditor(MetadataTypeLengthConstants.ID_INTEGER,
                Messages.getString("MetadataTypeLengthConstants.INTEGER_LENGTH"), dbTypeGroup); //$NON-NLS-1$
        bytearrayField = new StringFieldEditor(MetadataTypeLengthConstants.ID_BYTEARRAY,
                Messages.getString("MetadataTypeLengthConstants.BYTEARRAY_LENGTH"), dbTypeGroup); //$NON-NLS-1$
        longField = new StringFieldEditor(MetadataTypeLengthConstants.ID_LONG,
                Messages.getString("MetadataTypeLengthConstants.LONG_LENGTH"), dbTypeGroup); //$NON-NLS-1$
        characterField = new StringFieldEditor(MetadataTypeLengthConstants.ID_CHARACTER,
                Messages.getString("MetadataTypeLengthConstants.CHARACTER_LENGTH"), dbTypeGroup); //$NON-NLS-1$
        objectField = new StringFieldEditor(MetadataTypeLengthConstants.ID_OBJECT,
                Messages.getString("MetadataTypeLengthConstants.OBJECT_LENGTH"), dbTypeGroup); //$NON-NLS-1$
        dateField = new StringFieldEditor(MetadataTypeLengthConstants.ID_DATE,
                Messages.getString("MetadataTypeLengthConstants.DATE_LENGHT"), dbTypeGroup); //$NON-NLS-1$
        shortField = new StringFieldEditor(MetadataTypeLengthConstants.ID_SHORT,
                Messages.getString("MetadataTypeLengthConstants.SHORT_LENGTH"), dbTypeGroup); //$NON-NLS-1$
        doubleField = new StringFieldEditor(MetadataTypeLengthConstants.ID_DOUBLE,
                Messages.getString("MetadataTypeLengthConstants.DOUBLE_LENGTH"), dbTypeGroup); //$NON-NLS-1$
        stringField = new StringFieldEditor(MetadataTypeLengthConstants.ID_STRING,
                Messages.getString("MetadataTypeLengthConstants.STRING_LENGTH"), dbTypeGroup); //$NON-NLS-1$
        floatField = new StringFieldEditor(MetadataTypeLengthConstants.ID_FLOAT,
                Messages.getString("MetadataTypeLengthConstants.FLOAT_LENGTH"), dbTypeGroup); //$NON-NLS-1$
        listField = new StringFieldEditor(MetadataTypeLengthConstants.ID_LIST,
                Messages.getString("MetadataTypeLengthConstants.LIST_LENGTH"), dbTypeGroup); //$NON-NLS-1$
        addField(comboFieldTypeField);
        addField(fieldLengthField);
        gridLayout = (GridLayout) dbTypeGroup.getLayout();
        gridLayout.numColumns = 4;
        dbTypeGroup.setLayout(gridLayout);
        addField(bigDecimalField);
        addField(booleanField);
        addField(byteField);
        addField(integerField);
        addField(bytearrayField);
        addField(longField);
        addField(characterField);
        addField(objectField);
        addField(dateField);
        addField(shortField);
        addField(doubleField);
        addField(stringField);
        addField(floatField);
        addField(listField);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        getPreferenceStore().setValue(MetadataTypeLengthConstants.VALUE_DEFAULT_TYPE, comboValueTypeField.getFieldValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.VALUE_DEFAULT_LENGTH, valueLengthField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.FIELD_DEFAULT_TYPE, comboFieldTypeField.getFieldValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.FIELD_DEFAULT_LENGTH, fieldLengthField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.ID_BOOLEAN, booleanField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.ID_BIGDECIMAL, bigDecimalField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.ID_BYTE, byteField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.ID_INTEGER, integerField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.ID_BYTEARRAY, bytearrayField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.ID_LONG, longField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.ID_CHARACTER, characterField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.ID_OBJECT, objectField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.ID_DATE, dateField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.ID_SHORT, shortField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.ID_DOUBLE, doubleField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.ID_STRING, stringField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.ID_FLOAT, floatField.getStringValue());
        getPreferenceStore().setValue(MetadataTypeLengthConstants.ID_LIST, listField.getStringValue());
        return super.performOk();
    }
}
