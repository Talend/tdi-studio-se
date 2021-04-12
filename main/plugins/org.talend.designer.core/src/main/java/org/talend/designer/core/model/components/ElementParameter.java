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
package org.talend.designer.core.model.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IElementParameterDefaultValue;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;

/**
 * Each parameter of the components are read and written in this class. <br/>
 *
 * $Id$
 *
 */
public class ElementParameter implements IElementParameter {

    private static final int NB_LINES_DEFAULT = 3;

    private String name, displayName;

    private EParameterFieldType field;

    private EComponentCategory category;

    private boolean show = true, required = false, readOnly = false;

    private Object value;

    private IElement element;

    // used for CLOSED_LIST / TABLE
    private String[] itemsDisplayName;

    // used for CLOSED_LIST / TABLE
    private String[] itemsDisplayCodeName;

    // used for CLOSED_LIST / TABLE
    private String[] itemsShowIf;

    // used for CLOSED_LIST / TABLE
    private String[] itemsNotShowIf;

    // used for CLOSED_LIST
    private Object[] itemsValue;

    // used for CLOSED_LIST / TABLE
    private String[] itemsRepository;

    // used for CLOSED_LIST
    private Object defaultClosedListValue;

    // hshen 6930
    private String[] itemsNotReadOnlyIf;

    private String[] itemsReadOnlyIf;

    private boolean basedOnSchema = false;

    private boolean basedOnSubjobStarts = false;

    private boolean columnsBasedOnSchema = false;

    // achen add
    private boolean basedOnInputSchema = false;

    private int nbLines = NB_LINES_DEFAULT, numRow = 0; // Default values

    private String repositoryValue;

    private boolean repositoryValueUsed = false;

    private String showIf = null;

    private String requiredIF = null;

    private String notShowIf = null;

    // hshen 6930
    private String readonlyIf = null;

    private String notReadonlyIf = null;

    private boolean raw;

    private boolean enable = true;

    private List<IElementParameterDefaultValue> defaultValues = new ArrayList<IElementParameterDefaultValue>();

    private String filter = null;

    private boolean noCheck = false;

    private String context, groupName, groupDisplayName;

    private Map<String, IElementParameter> childParameters;

    private IElementParameter parentParameter;

    private int currentRow; // for Table only

    private Item linkedRepositoryItem;

    private boolean contextMode;

    private String labelFromRepository;

    private RGB color;

    private RGB backgroundColor;

    private boolean dynamicSettings = false;

    private boolean noContextAssist;

    private String javaClass; // for JAVA_COMMAND

    private String javaFunction; // for JAVA_COMMAND

    private String jar; // for JAVA_COMMAND

    private String[] args;

    private int maxlength;

    private String repositoryProperty;

    private Object defaultValue;

    private Map<String, Object> taggedValues = new HashMap<>();

    protected boolean isSerialized = false;

    public ElementParameter(final IElement element) {
        this.element = element;
    }

    public boolean isBasedOnInputSchema() {
        return this.basedOnInputSchema;
    }

    public void setBasedOnInputSchema(boolean basedOnInputSchema) {
        this.basedOnInputSchema = basedOnInputSchema;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#setName(java.lang.String)
     */
    @Override
    public void setName(final String s) {
        name = s;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#getVariableName()
     */
    @Override
    public String getVariableName() {
        return "__" + name + "__"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void setCategory(final EComponentCategory cat) {
        category = cat;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#getCategory()
     */
    @Override
    public EComponentCategory getCategory() {
        return this.category;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#setDisplayName(java.lang.String)
     */
    @Override
    public void setDisplayName(final String s) {
        displayName = s;
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.designer.core.model.components.IDesignerElementParameter#setField(org.talend.core.model.designer.
     * EParameterFieldType)
     */
    @Override
    public void setFieldType(final EParameterFieldType type) {
        field = type;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#setValue(java.lang.Object)
     */
    @Override
    public void setValue(final Object o) {
        value = o;
    }

    @Override
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#getField()
     */
    @Override
    public EParameterFieldType getFieldType() {
        return field;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#getValue()
     */
    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setListItemsDisplayName(final String[] list) {
        itemsDisplayName = list;
    }

    @Override
    public String[] getListItemsDisplayName() {
        return itemsDisplayName;
    }

    @Override
    public void setListItemsDisplayCodeName(final String[] list) {
        itemsDisplayCodeName = list;
    }

    @Override
    public String[] getListItemsDisplayCodeName() {
        return itemsDisplayCodeName;
    }

    @Override
    public void setListItemsValue(final Object[] list) {
        if (this.getFieldType() == EParameterFieldType.TABLE) {
            EParameterFieldType.AS400_CHECK.getClass();
        }
        itemsValue = list;
    }

    @Override
    public Object[] getListItemsValue() {
        return itemsValue;
    }

    @Override
    public void setDefaultClosedListValue(Object o) {
        defaultClosedListValue = o;
    }

    @Override
    public Object getDefaultClosedListValue() {
        return defaultClosedListValue;
    }

    @Override
    public void setListRepositoryItems(final String[] list) {
        itemsRepository = list;
    }

    @Override
    public String[] getListRepositoryItems() {
        return itemsRepository;
    }

    @Override
    public void setListItemsShowIf(String[] list) {
        itemsShowIf = list;
    }

    @Override
    public String[] getListItemsShowIf() {
        return itemsShowIf;
    }

    @Override
    public void setListItemsNotShowIf(String[] list) {
        itemsNotShowIf = list;
    }

    @Override
    public String[] getListItemsNotShowIf() {
        return itemsNotShowIf;
    }

    @Override
    public int getIndexOfItemFromList(String item) {
        int index = -1;
        boolean found = false;
        if (itemsDisplayCodeName != null) {
            for (int i = 0; i < itemsDisplayCodeName.length && !found; i++) {
                String string = itemsDisplayCodeName[i];
                if (string.equals(item)) {
                    found = true;
                    index = i;
                }
            }
        }
        for (int i = 0; i < itemsValue.length && !found; i++) {
            String string = (String) itemsValue[i];
            if (string.equals(item)) {
                found = true;
                index = i;
            }
        }
        return index;
    }

    @Override
    public int getNbLines() {
        return this.nbLines;
    }

    @Override
    public void setNbLines(final int nbLines) {
        this.nbLines = nbLines;
    }

    @Override
    public int getNumRow() {
        return this.numRow;
    }

    @Override
    public void setNumRow(final int numRow) {
        this.numRow = numRow;
    }

    @Override
    public boolean isReadOnly() {
        if (element != null) {
            return (this.readOnly || element.isReadOnly());
        }
        return this.readOnly;
    }

    public boolean getOriginalityReadOnly() {
        return this.readOnly;
    }

    @Override
    public void setReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public boolean isRequired() {
        return this.required;
    }

    @Override
    public void setRequired(final boolean required) {
        this.required = required;
    }

    @Override
    public void setShow(final boolean show) {
        this.show = show;
    }

    @Override
    public String getRepositoryValue() {
        return this.repositoryValue;
    }

    @Override
    public void setRepositoryValue(String repositoryValue) {
        this.repositoryValue = repositoryValue;
    }

    @Override
    public boolean isRepositoryValueUsed() {
        return this.repositoryValueUsed;
    }

    @Override
    public void setRepositoryValueUsed(boolean repositoryUsed) {
        this.repositoryValueUsed = repositoryUsed;
    }

    @Override
    public String getShowIf() {
        return showIf;
    }

    @Override
    public void setShowIf(String showIf) {
        this.showIf = showIf;
    }

    @Override
    public String getNotShowIf() {
        return notShowIf;
    }

    @Override
    public void setNotShowIf(String notShowIf) {
        this.notShowIf = notShowIf;
    }

    @Override
    public boolean isShow(List<? extends IElementParameter> listParam) {
        boolean showParameter = false;

        if (((showIf != null) || (notShowIf != null)) && show) {
            if (listParam.isEmpty()) {
                return true;
            }

            if (showIf != null) {
                showParameter = Expression.evaluate(showIf, listParam, this);
            } else {
                showParameter = !Expression.evaluate(notShowIf, listParam, this);
            }
        } else {
            showParameter = show;
        }
        return showParameter;
    }

    // added by dlin for feature TDI-22421
    @Override
    public boolean isRequired(List<? extends IElementParameter> listParam) {
        boolean requiredParameter = false;
        if (this.requiredIF != null && !required) {
            requiredParameter = Expression.evaluate(requiredIF, listParam, this);
        } else {
            requiredParameter = required;
        }
        return requiredParameter;
    }

    @Override
    public boolean isShow(String conditionShowIf, String conditionNotShowIf, List<? extends IElementParameter> listParam) {
        boolean showParameter = false;

        if (((conditionShowIf != null) || (conditionNotShowIf != null)) && show) {
            if (conditionShowIf != null) {
                showParameter = Expression.evaluate(conditionShowIf, listParam, this);
            } else {
                showParameter = !Expression.evaluate(conditionNotShowIf, listParam, this);
            }
        } else {
            showParameter = show;
        }
        return showParameter;
    }

    public boolean isCondition(String conditionShowIf, String conditionNotShowIf, List<? extends IElementParameter> listParam) {

        boolean showParameter = false;

        if ((conditionShowIf != null) || (conditionNotShowIf != null)) {
            if (conditionShowIf != null) {
                showParameter = Expression.evaluate(conditionShowIf, listParam, this);
            } else {
                showParameter = !Expression.evaluate(conditionNotShowIf, listParam, this);
            }
        }
        return showParameter;
    }

    @Override
    public List<IElementParameterDefaultValue> getDefaultValues() {
        return this.defaultValues;
    }

    @Override
    public void setDefaultValues(List<IElementParameterDefaultValue> defaultValues) {
        this.defaultValues = defaultValues;
    }

    @Override
    public void setValueToDefault(List<? extends IElementParameter> listParam) {
        if (defaultValues == null) {
            return;
        }
        int size = defaultValues.size();
        // a performance improvement, and keep the result like before.
        // for (IElementParameterDefaultValue defaultValue : defaultValues) {
        for (int i = size - 1; 0 <= i; i--) {
            IElementParameterDefaultValue iDefaultValue = defaultValues.get(i);
            boolean setDefaultValue = false;
            String conditionIf = iDefaultValue.getIfCondition();
            String conditionNotIf = iDefaultValue.getNotIfCondition();

            if ((conditionIf != null) || (conditionNotIf != null)) {
                if (conditionIf != null) {
                    setDefaultValue = Expression.evaluate(conditionIf, listParam);
                } else {
                    setDefaultValue = !Expression.evaluate(conditionNotIf, listParam);
                }
            }
            if (setDefaultValue) {
                if (this.field.equals(EParameterFieldType.CHECK) || this.field.equals(EParameterFieldType.RADIO)) {
                    setValue(new Boolean(iDefaultValue.getDefaultValue().toString()));
                } else {
                    setValue(iDefaultValue.getDefaultValue());
                }
                break;
            }
        }
    }

    @Override
    public IElement getElement() {
        return element;
    }

    @Override
    public void setElement(IElement element) {
        this.element = element;
    }

    @Override
    public boolean isBasedOnSchema() {
        return basedOnSchema;
    }

    @Override
    public void setBasedOnSchema(boolean basedOnSchema) {
        this.basedOnSchema = basedOnSchema;
    }

    @Override
    public String toString() {
        if (value == null) {
            return name + ": null"; //$NON-NLS-1$
        }
        return name + ": " + value.toString(); //$NON-NLS-1$
    }

    @Override
    public String getFilter() {
        return filter;
    }

    @Override
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Getter for noCheck.
     *
     * @return the noCheck
     */
    @Override
    public boolean isNoCheck() {
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();

        if (!preferenceStore.getBoolean(TalendDesignerPrefConstants.PROPERTY_CODE_CHECK)) {
            // if the check has been completely disabled then no check.
            // if not disabled in the preferences, then it will depends on the next conditions.
            return true;
        }

        if (!(element instanceof INode)) {
            return true;
        }
        return noCheck;
    }

    /**
     * Sets the noCheck.
     *
     * @param noCheck the noCheck to set
     */
    @Override
    public void setNoCheck(boolean noCheck) {
        this.noCheck = noCheck;
    }

    /**
     * Getter for context.
     *
     * @return the context
     */
    @Override
    public String getContext() {
        return context;
    }

    /**
     * Sets the context.
     *
     * @param context the context to set
     */
    @Override
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * Getter for childParameters.
     *
     * @return the childParameters
     */
    @Override
    public Map<String, IElementParameter> getChildParameters() {
        if (childParameters == null) {
            childParameters = new HashMap<String, IElementParameter>();
        }
        return childParameters;
    }

    @Override
    public IElementParameter getParentParameter() {
        return parentParameter;
    }

    @Override
    public void setParentParameter(IElementParameter parentParameter) {
        this.parentParameter = parentParameter;
        // keep the same category with parent.
        this.setCategory(parentParameter.getCategory());
        parentParameter.getChildParameters().put(this.getName(), this);
    }

    public boolean isDisplayedByDefault() {
        return this.show;
    }

    /**
     * Getter for currentRow.
     *
     * @return the currentRow
     */
    public int getCurrentRow() {
        return this.currentRow;
    }

    /**
     * Sets the currentRow.
     *
     * @param currentRow the currentRow to set
     */
    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#getGroup()
     */
    @Override
    public String getGroup() {
        return this.groupName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#setGroup(java.lang.String)
     */
    @Override
    public void setGroup(String groupName) {
        this.groupName = groupName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#getGroupDisplayName()
     */
    @Override
    public String getGroupDisplayName() {
        return this.groupDisplayName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#setGroupDisplayName(java.lang.String)
     */
    @Override
    public void setGroupDisplayName(String groupDisplayName) {
        this.groupDisplayName = groupDisplayName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#getContextMode()
     */
    @Override
    public boolean isContextMode() {
        return this.contextMode;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#setContextMode(java.lang.String)
     */
    @Override
    public void setContextMode(boolean mode) {
        this.contextMode = mode;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#getLabelFromRepository()
     */
    @Override
    public String getLabelFromRepository() {
        return this.labelFromRepository;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#setLabelFromRepository(java.lang.String)
     */
    @Override
    public void setLabelFromRepository(String label) {
        this.labelFromRepository = label;

    }

    /**
     * Getter for color.
     *
     * @return the color
     */
    @Override
    public RGB getColor() {
        return this.color;
    }

    /**
     * Sets the color.
     *
     * @param color the color to set
     */
    @Override
    public void setColor(RGB color) {
        this.color = color;
    }

    /**
     * Getter for backgroundColor.
     *
     * @return the backgroundColor
     */
    @Override
    public RGB getBackgroundColor() {
        return this.backgroundColor;
    }

    /**
     * Sets the backgroundColor.
     *
     * @param backgroundColor the backgroundColor to set
     */
    @Override
    public void setBackgroundColor(RGB backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#isBasedOnSubjobStarts()
     */
    @Override
    public boolean isBasedOnSubjobStarts() {
        return basedOnSubjobStarts;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#setBasedOnSubjobStarts(boolean)
     */
    @Override
    public void setBasedOnSubjobStarts(boolean basedOnSubjobStarts) {
        this.basedOnSubjobStarts = basedOnSubjobStarts;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#isDynamicSettings()
     */
    @Override
    public boolean isDynamicSettings() {
        return this.dynamicSettings;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#setDynamicSettings(java.lang.Boolean)
     */
    @Override
    public void setDynamicSettings(boolean dynamicSettings) {
        this.dynamicSettings = dynamicSettings;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#getListItemsNotReadOnlyIf()
     */
    @Override
    public String[] getListItemsNotReadOnlyIf() {
        return this.itemsNotReadOnlyIf;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#getListItemsReadOnlyIf()
     */
    @Override
    public String[] getListItemsReadOnlyIf() {
        return this.itemsReadOnlyIf;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#getNotReadOnlyIf()
     */
    @Override
    public String getNotReadOnlyIf() {
        // TODO Auto-generated method stub
        return this.notReadonlyIf;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#getReadOnlyIf()
     */
    @Override
    public String getReadOnlyIf() {
        // TODO Auto-generated method stub
        return this.readonlyIf;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#isReadOnly(java.util.List)
     */
    @Override
    public boolean isReadOnly(List<? extends IElementParameter> listParam) {
        boolean readonlyParameter = false;

        if (((readonlyIf != null) || (this.notReadonlyIf != null)) && !readOnly) {
            if (readonlyIf != null) {
                readonlyParameter = Expression.evaluate(readonlyIf, listParam, this);
            } else {
                readonlyParameter = !Expression.evaluate(this.notReadonlyIf, listParam, this);
            }
        } else {
            readonlyParameter = readOnly;
        }
        return readonlyParameter;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#isReadOnly(java.lang.String, java.lang.String,
     * java.util.Map)
     */
    @Override
    public boolean isReadOnly(String conditionReadOnlyIf, String conditionNotReadOnlyIf,
            List<? extends IElementParameter> listParam) {
        boolean readonlyParameter = false;

        if (((conditionReadOnlyIf != null) || (conditionNotReadOnlyIf != null)) && !readOnly) {
            if (conditionReadOnlyIf != null) {
                readonlyParameter = Expression.evaluate(conditionReadOnlyIf, listParam, this);
            } else {
                readonlyParameter = !Expression.evaluate(conditionNotReadOnlyIf, listParam, this);
            }
        } else {
            readonlyParameter = readOnly;
        }
        return readonlyParameter;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#setListItemsNotReadOnlyIf(java.lang.String[])
     */
    @Override
    public void setListItemsNotReadOnlyIf(String[] list) {
        itemsNotReadOnlyIf = list;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#setListItemsReadOnlyIf(java.lang.String[])
     */
    @Override
    public void setListItemsReadOnlyIf(String[] list) {
        itemsReadOnlyIf = list;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#setNotReadOnlyIf(java.lang.String)
     */
    @Override
    public void setNotReadOnlyIf(String notReadOnly) {
        this.notReadonlyIf = notReadOnly;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#setReadOnlyIf(java.lang.String)
     */
    @Override
    public void setReadOnlyIf(String readOnly) {
        this.readonlyIf = readOnly;
    }

    @Override
    public boolean isColumnsBasedOnSchema() {
        return this.columnsBasedOnSchema;
    }

    @Override
    public void setColumnsBasedOnSchema(boolean columnsBasedOnSchema) {
        this.columnsBasedOnSchema = columnsBasedOnSchema;
    }

    @Override
    public boolean isNoContextAssist() {
        return this.noContextAssist;
    }

    @Override
    public void setNoContextAssist(boolean enable) {
        this.noContextAssist = enable;
    }

    @Override
    public IElementParameter getClone() {
        IElementParameter clone = new ElementParameter(this.element);

        final RGB bgc = getBackgroundColor();
        if (bgc != null) {
            clone.setBackgroundColor(new RGB(bgc.red, bgc.green, bgc.blue));
        }
        clone.setBasedOnSchema(isBasedOnSchema());
        clone.setBasedOnSubjobStarts(isBasedOnSubjobStarts());
        clone.setCategory(getCategory());
        final RGB c = getColor();
        if (c != null) {
            clone.setColor(new RGB(c.red, c.green, c.blue));
        }
        clone.setContext(getContext());
        clone.setContextMode(isContextMode());
        clone.setDefaultClosedListValue(getDefaultClosedListValue()); // ?
        clone.setDefaultValues(getDefaultValues()); // ?
        clone.setDisplayName(getDisplayName());
        clone.setDynamicSettings(isDynamicSettings());
        clone.setElement(getElement()); // maybe, need reset it after.
        clone.setFieldType(getFieldType());
        clone.setFilter(getFilter());
        clone.setGroupDisplayName(getGroupDisplayName());
        clone.setLabelFromRepository(getLabelFromRepository());
        clone.setListItemsDisplayCodeName(getListItemsDisplayCodeName());
        clone.setListItemsDisplayName(getListItemsDisplayName());
        clone.setListItemsNotReadOnlyIf(getListItemsNotReadOnlyIf());
        clone.setListItemsNotShowIf(getListItemsNotShowIf());
        clone.setListItemsReadOnlyIf(getListItemsReadOnlyIf());
        clone.setListItemsShowIf(getListItemsShowIf());
        clone.setListItemsValue(getListItemsValue()); // ?
        clone.setListRepositoryItems(getListRepositoryItems());
        clone.setName(getName());
        clone.setNbLines(getNbLines());
        clone.setNoCheck(isNoCheck());
        clone.setNotReadOnlyIf(getNotReadOnlyIf());
        clone.setNotShowIf(getNotShowIf());
        clone.setNumRow(getNumRow());
        // changed by hqzhang for TDI 19754 start
        // final IElementParameter pParameter = getParentParameter();
        // if (pParameter != null) {
        // clone.setParentParameter(pParameter.getClone());
        // }
        clone.setReadOnly(isReadOnly());
        clone.setReadOnlyIf(getReadOnlyIf());
        clone.setRepositoryValue(getRepositoryValue());
        clone.setRepositoryValueUsed(isRepositoryValueUsed());
        clone.setRequired(isRequired());
        clone.setShow(isDisplayedByDefault());
        clone.setShowIf(getShowIf());
        clone.setValue(getValue()); // ?
        // clone.setValueToDefault(null)
        clone.setNoContextAssist(isNoContextAssist());
        if (this.getChildParameters().size() > 0) {
            for (String name : this.getChildParameters().keySet()) {
                IElementParameter childParamClone = this.getChildParameters().get(name).getClone();
                clone.getChildParameters().put(name, childParamClone);
                childParamClone.setParentParameter(clone);
            }
        }// TDI 19754 end
        return clone;
    }

    /**
     * Getter for javaMethod.
     *
     * @return the javaMethod
     */
    public String getJavaClass() {
        return this.javaClass;
    }

    /**
     * Sets the javaMethod.
     *
     * @param javaMethod the javaMethod to set
     */
    public void setJavaClass(String javaClass) {
        this.javaClass = javaClass;
    }

    /**
     * Getter for jar.
     *
     * @return the jar
     */
    public String getJar() {
        return this.jar;
    }

    /**
     * Sets the jar.
     *
     * @param jar the jar to set
     */
    public void setJar(String jar) {
        this.jar = jar;
    }

    /**
     * Getter for args.
     *
     * @return the args
     */
    public String[] getArgs() {
        return this.args;
    }

    /**
     * Sets the args.
     *
     * @param args the args to set
     */
    public void setArgs(String[] args) {
        this.args = args;
    }

    /**
     * Getter for javaFunction.
     *
     * @return the javaFunction
     */
    public String getJavaFunction() {
        return this.javaFunction;
    }

    /**
     * Sets the javaFunction.
     *
     * @param javaFunction the javaFunction to set
     */
    public void setJavaFunction(String javaFunction) {
        this.javaFunction = javaFunction;
    }

    /**
     * DOC Administrator Comment method "setMaxLength".
     *
     * @param maxlength
     */
    public void setMaxLength(int maxlength) {
        this.maxlength = maxlength;
    }

    /**
     * Getter for maxlength.
     *
     * @return the maxlength
     */
    @Override
    public int getMaxlength() {
        return this.maxlength;
    }

    // added by dlin for feature TDI-22421
    public String getRequiredIF() {
        return this.requiredIF;
    }

    // added by dlin for feature TDI-22421
    public void setRequiredIF(String requiredIF) {
        this.requiredIF = requiredIF;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#getRepositoryProperty()
     */
    @Override
    public String getRepositoryProperty() {
        return this.repositoryProperty;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#setRepositoryProperty(java.lang.String)
     */
    @Override
    public void setRepositoryProperty(String repositoryProperty) {
        this.repositoryProperty = repositoryProperty;
    }

    @SuppressWarnings("rawtypes")
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        // re-set the defaultValue
        if (defaultValue != null) {
            if (defaultValue instanceof ArrayList) {
                this.defaultValue = ((ArrayList) defaultValue).clone();
            } else if (defaultValue instanceof HashSet) {
                this.defaultValue = ((HashSet) defaultValue).clone();
            } else if (defaultValue instanceof HashMap) {
                this.defaultValue = ((HashMap) defaultValue).clone();
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#isDefault()
     */
    @Override
    public boolean isValueSetToDefault() {
        if (defaultValue == null) {
            if (value == null) {
                return true;
            }
            return false;
        } else {
            return defaultValue.equals(value);
        }
    }

    @Override
    public boolean isRaw() {
        return raw;
    }

    @Override
    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#isLog4JEnabled()
     */
    @Override
    public boolean isLog4JEnabled() {
        return this.enable;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#setLog4JEnabled(boolean)
     */
    @Override
    public void setLog4JEnabled(boolean enable) {
        this.enable = enable;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#isSerialized()
     */
    @Override
    public boolean isSerialized() {
        return isSerialized;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElementParameter#setSerialized(boolean)
     */
    @Override
    public void setSerialized(boolean isSerialized) {
        this.isSerialized = isSerialized;
    }

    public void setTaggedValue(String key, Object value) {
        taggedValues.put(key, value);
    }

    public Object getTaggedValue(String key) {
        return taggedValues.get(key);
    }

    public boolean isSelectedFromItemValue() {

        Object[] listItemsValue = this.getListItemsValue();
        if (listItemsValue == null) {
            return false;
        }
        Object value = this.getValue();
        if (value == null || "".equals(value)) {
            return false;
        }

        if (Arrays.asList(listItemsValue).contains(value)) {
            return true;
        }
        return false;
    }

}
