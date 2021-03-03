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
package org.talend.designer.core.ui.views.jobsettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.views.statsandlogs.StatsAndLogsViewHelper;
import org.talend.repository.UpdateRepositoryUtils;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class ImplicitContextLoadHelper {

    private static final IPreferenceStore PREFERENCE_STORE = DesignerPlugin.getDefault().getPreferenceStore();

    public static String getRepositoryTypeLabel(ConnectionItem connectionItem) {
        if (connectionItem == null) {
            return ""; //$NON-NLS-1$
        }
        ERepositoryObjectType repositoryObjectType = ERepositoryObjectType.getItemType(connectionItem);
        String aliasName = repositoryObjectType.getAlias();
        org.talend.core.model.metadata.builder.connection.Connection connection = connectionItem.getConnection();
        if (connection instanceof DatabaseConnection) {
            String currentDbType = (String) RepositoryToComponentProperty.getValue(connection, "TYPE", null); //$NON-NLS-1$
            aliasName += " (" + currentDbType + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return aliasName + ":" + connectionItem.getProperty().getLabel(); //$NON-NLS-1$
    }

    public static Object getPreferenceValue(String prefix, EParameterName param, Class type) {
        String name = prefix + getExtraParameterName(param);
        if (type == Boolean.class) {
            return PREFERENCE_STORE.getBoolean(name);
        } else if (type == String.class) {
            return PREFERENCE_STORE.getString(name);
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * DOC hcw Comment method "reloadValuesFromPreferencePage".
     *
     * @param elem
     * @param extraComposite
     */
    public static void reloadValuesFromPreferencePage(Element element, ExtraComposite extraComposite) {
        ECodeLanguage language = LanguageManager.getCurrentLanguage();
        String languagePrefix = language.toString() + "_"; //$NON-NLS-1$

        Map values = loadPreferenceValues();
        List<? extends IElementParameter> elementParameters = element.getElementParameters();

        for (IElementParameter elementParameter : elementParameters) {
            String name = elementParameter.getName();
            if (name.equals(getExtraParameterName(EParameterName.PROPERTY_TYPE))) {
                loadPropertyTypeFromPreference(element, extraComposite, languagePrefix, elementParameter);

            } else {
                Object value = values.get(name);
                if (value != null) {
                    elementParameter.setValue(value);
                }
            }
        }
    }

    /**
     * DOC hcw Comment method "reloadPropertyType".
     *
     * @param element
     * @param extraComposite
     * @param languagePrefix
     * @param elementParameter
     */
    private static void loadPropertyTypeFromPreference(Element element, ExtraComposite extraComposite, String languagePrefix,
            IElementParameter elementParameter) {
        String propertyType = (String) getPreferenceValue(languagePrefix, EParameterName.PROPERTY_TYPE, String.class);
        String id = (String) getPreferenceValue(languagePrefix, EParameterName.REPOSITORY_PROPERTY_TYPE, String.class);

        Connection repositoryConnection = null;

        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        Item item = null;
        try {
            IRepositoryViewObject repobj = factory.getLastVersion(id);
            if (repobj != null) {
                Property tmpproperty = repobj.getProperty();
                if (tmpproperty != null) {
                    item = tmpproperty.getItem();
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        if (item != null && item instanceof ConnectionItem) {
            repositoryConnection = ((ConnectionItem) item).getConnection();
        } else {
            repositoryConnection = null;
        }

        ChangeValuesFromRepository cmd1 = new ChangeValuesFromRepository(element, repositoryConnection,
                getExtraParameterName(EParameterName.PROPERTY_TYPE) + ":" + EParameterName.PROPERTY_TYPE.getName(), propertyType); //$NON-NLS-1$

        ChangeValuesFromRepository cmd2 = new ChangeValuesFromRepository(element, repositoryConnection,
                getExtraParameterName(EParameterName.PROPERTY_TYPE) + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), id); //$NON-NLS-1$

        AbstractMultiPageTalendEditor part = (AbstractMultiPageTalendEditor) ((IProcess2) element).getEditor();
        if (part instanceof AbstractMultiPageTalendEditor) {
            Object adapter = (part).getTalendEditor().getAdapter(CommandStack.class);
            if (adapter != null) {
                CommandStack commandStack = ((CommandStack) adapter);
                commandStack.execute(cmd1);
                commandStack.execute(cmd2);
            }
        }
    }

    /*
     * add quote.
     */
    private static String addQuote(String value) {
        if (value == null) {
            return TalendTextUtils.addQuotes(""); //$NON-NLS-1$
        }
        value = value.trim();
        return value;
    }

    private static String removeQuote(String value) {
        if (value == null) {
            return ""; //$NON-NLS-1$
        }
        value = value.trim();
        return value;
    }

    /**
     * DOC hcw Comment method "loadPreferenceValues".
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Map loadPreferenceValues() {
        Map values = new HashMap();
        ECodeLanguage language = LanguageManager.getCurrentLanguage();
        String languagePrefix = language.toString() + "_"; //$NON-NLS-1$

        EParameterName[] params = { EParameterName.IMPLICIT_TCONTEXTLOAD, EParameterName.FROM_FILE_FLAG,
                EParameterName.FROM_DATABASE_FLAG, EParameterName.PRINT_OPERATIONS, EParameterName.DISABLE_WARNINGS,
                EParameterName.DISABLE_ERROR, EParameterName.DISABLE_INFO };
        putValuesToMap(values, languagePrefix, params, Boolean.class);

        params = new EParameterName[] { EParameterName.IMPLICIT_TCONTEXTLOAD_FILE, EParameterName.FIELDSEPARATOR,
                EParameterName.DB_TYPE, EParameterName.LOAD_NEW_VARIABLE, EParameterName.NOT_LOAD_OLD_VARIABLE };
        putValuesToMap(values, languagePrefix, params, String.class);

        String propertyType = (String) getPreferenceValue(languagePrefix, EParameterName.PROPERTY_TYPE, String.class);
        if (!propertyType.equals(EmfComponent.BUILTIN)) {
            params = new EParameterName[] { EParameterName.HOST, EParameterName.PORT, EParameterName.DBNAME,
                    EParameterName.SCHEMA_DB, EParameterName.USER, EParameterName.PASS, EParameterName.DBFILE,
                    EParameterName.DBTABLE, EParameterName.QUERY_CONDITION };

            putValuesToMap(values, languagePrefix, params, String.class);
        }

        return values;
    }

    /**
     * DOC hcw Comment method "putValuesToMap".
     *
     * @param values
     * @param languagePrefix
     * @param params
     * @param type
     */
    private static void putValuesToMap(Map values, String languagePrefix, EParameterName[] params, Class type) {
        if (type == Boolean.class) {
            for (EParameterName param : params) {
                values.put(getExtraParameterName(param), getPreferenceValue(languagePrefix, param, Boolean.class));
            }
        } else if (type == String.class) {
            for (EParameterName param : params) {
                values.put(getExtraParameterName(param),
                        addQuote((String) getPreferenceValue(languagePrefix, param, String.class)));
            }
        }
    }

    /**
     * DOC hcw Comment method "saveValuesToPreferencePage".
     *
     * @param elem
     * @param extraComposite
     */
    public static void saveValuesToPreferencePage(Element element, ExtraComposite extraComposite) {
        ECodeLanguage language = LanguageManager.getCurrentLanguage();
        String languagePrefix = language.toString() + "_"; //$NON-NLS-1$
        EParameterName[] params = { EParameterName.IMPLICIT_TCONTEXTLOAD, EParameterName.FROM_FILE_FLAG,
                EParameterName.FROM_DATABASE_FLAG, EParameterName.IMPLICIT_TCONTEXTLOAD_FILE, EParameterName.FIELDSEPARATOR,
                EParameterName.DB_TYPE, EParameterName.HOST, EParameterName.PORT, EParameterName.DBNAME,
                EParameterName.SCHEMA_DB, EParameterName.USER, EParameterName.PASS, EParameterName.DBFILE,
                EParameterName.DBTABLE, EParameterName.QUERY_CONDITION, EParameterName.LOAD_NEW_VARIABLE,
                EParameterName.NOT_LOAD_OLD_VARIABLE, EParameterName.PRINT_OPERATIONS, EParameterName.DISABLE_WARNINGS,
                EParameterName.DISABLE_ERROR, EParameterName.DISABLE_INFO };
        Map<String, EParameterName> extraParams = new HashMap<String, EParameterName>();

        for (EParameterName param : params) {
            extraParams.put(getExtraParameterName(param), param);
        }

        List<? extends IElementParameter> elementParameters = element.getElementParameters();
        for (IElementParameter elementParameter : elementParameters) {
            String name = elementParameter.getName();
            Object value = elementParameter.getValue();
            if (name.equals(getExtraParameterName(EParameterName.PROPERTY_TYPE))) {
                savePropertyTypeToPreference(extraComposite, languagePrefix, elementParameter);
            } else if (extraParams.containsKey(name)) {
                if (value instanceof String) {
                    PREFERENCE_STORE.setValue(languagePrefix + getExtraParameterName(extraParams.get(name)),
                            removeQuote((String) value));
                } else if (value instanceof Boolean) {
                    PREFERENCE_STORE.setValue(languagePrefix + getExtraParameterName(extraParams.get(name)), (Boolean) value);
                }
            }
        }

    }

    /**
     * DOC hcw Comment method "savePropertyType".
     *
     * @param extraComposite
     * @param languagePrefix
     * @param elementParameter
     */
    private static void savePropertyTypeToPreference(ExtraComposite extraComposite, String languagePrefix,
            IElementParameter elementParameter) {
        String itemId = (String) elementParameter.getChildParameters().get("REPOSITORY_PROPERTY_TYPE").getValue(); //$NON-NLS-1$
        String propertyType = (String) elementParameter.getChildParameters().get("PROPERTY_TYPE").getValue(); //$NON-NLS-1$

        Item item = UpdateRepositoryUtils.getConnectionItemByItemId(itemId);

        PREFERENCE_STORE.setValue(languagePrefix + getExtraParameterName(EParameterName.PROPERTY_TYPE), propertyType);
        PREFERENCE_STORE.setValue(languagePrefix + getExtraParameterName(EParameterName.REPOSITORY_PROPERTY_TYPE), itemId);
        if (item != null) {
            PREFERENCE_STORE.setValue(languagePrefix + getExtraParameterName(EParameterName.REPOSITORY_PROPERTY_TYPE)
                    + StatsAndLogsViewHelper.CONNECTION_ITEM_LABEL, item.getProperty().getLabel());
        }
    }

    public static String getExtraParameterName(EParameterName param) {
        switch (param) {
        case IMPLICIT_TCONTEXTLOAD:
        case FIELDSEPARATOR:
        case IMPLICIT_TCONTEXTLOAD_FILE:
        case LOAD_NEW_VARIABLE: // java only
        case NOT_LOAD_OLD_VARIABLE: // java only
        case PRINT_OPERATIONS:
        case DISABLE_ERROR: // java only
        case DISABLE_WARNINGS:
        case DISABLE_INFO: // java only
            return param.getName();
        default:
            return JobSettingsConstants.getExtraParameterName(param.getName());
        }
    }

}
