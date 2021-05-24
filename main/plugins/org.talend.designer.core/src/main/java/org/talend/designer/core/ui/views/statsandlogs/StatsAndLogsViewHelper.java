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
package org.talend.designer.core.ui.views.statsandlogs;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.process.IGEFProcess;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.repository.UpdateRepositoryUtils;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * ftang class global comment. Detailed comment. <br/>
 *
 */
public class StatsAndLogsViewHelper {

    private static final String LANGUAGE_PREFIX = LanguageManager.getCurrentLanguage().toString() + "_"; //$NON-NLS-1$

    private static final IPreferenceStore PREFERENCE_STORE = DesignerPlugin.getDefault().getPreferenceStore();

    public static final String FILE_NAME_REGEX = "[^\\>\\<\\\\\\/\\!\\:\\|\\?\\\"\\'\\s\\.]" //$NON-NLS-1$
            + "+[\\.][^\\>\\<\\\\\\/\\!\\:\\|\\?\\\"\\'\\s\\.]+"; //$NON-NLS-1$

    public static final String OTHER_FILE_NAME_REGEX = "[^\\\"\\'\\s]*"; //$NON-NLS-1$

    public static final String CONNECTION_ITEM_LABEL = "_CONNECTION_ITEM_LABEL"; //$NON-NLS-1$

    /**
     * yzhang Comment method "applySettings".
     *
     * @param element
     * @param dynamicProperty
     */
    public static void applySettings(IElement element, IElement applyTo, IDynamicProperty dynamicProperty) {
        List<? extends IElementParameter> elementParameters = applyTo.getElementParameters();

        for (IElementParameter elementParameterType : elementParameters) {

            String parameterName = elementParameterType.getName();

            if (parameterName.equals(EParameterName.ON_STATCATCHER_FLAG.getName())) {
                elementParameterType.setValue(element.getElementParameter(EParameterName.ON_STATCATCHER_FLAG.getName())
                        .getValue());
                continue;
            }

            if (parameterName.equals(EParameterName.ON_LOGCATCHER_FLAG.getName())) {
                elementParameterType
                        .setValue(element.getElementParameter(EParameterName.ON_LOGCATCHER_FLAG.getName()).getValue());
                continue;
            }

            if (parameterName.equals(EParameterName.ON_METERCATCHER_FLAG.getName())) {
                elementParameterType.setValue(element.getElementParameter(EParameterName.ON_METERCATCHER_FLAG.getName())
                        .getValue());
                continue;
            }

            if (parameterName.equals(EParameterName.ON_FILES_FLAG.getName())) {
                elementParameterType.setValue(element.getElementParameter(EParameterName.ON_FILES_FLAG.getName()).getValue());
                continue;
            }

            if (parameterName.equals(EParameterName.FILE_PATH.getName())) {
                elementParameterType.setValue(element.getElementParameter(EParameterName.FILE_PATH.getName()).getValue());
                continue;
            }

            if (parameterName.equals(EParameterName.FILENAME_STATS.getName())) {
                elementParameterType.setValue(element.getElementParameter(EParameterName.FILENAME_STATS.getName()).getValue());
                continue;
            }

            if (parameterName.equals(EParameterName.FILENAME_LOGS.getName())) {
                elementParameterType.setValue(element.getElementParameter(EParameterName.FILENAME_LOGS.getName()).getValue());
                continue;
            }

            if (parameterName.equals(EParameterName.FILENAME_METTER.getName())) {
                elementParameterType.setValue(element.getElementParameter(EParameterName.FILENAME_METTER.getName()).getValue());
                continue;
            }

            if (parameterName.equals(EParameterName.ON_DATABASE_FLAG.getName())) {
                elementParameterType.setValue(element.getElementParameter(EParameterName.ON_DATABASE_FLAG.getName()).getValue());
                continue;
            }

            if (parameterName.equals(EParameterName.PROPERTY_TYPE.getName())) {

                String id = (String) element.getElementParameter(
                        EParameterName.PROPERTY_TYPE.getName() + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName()) //$NON-NLS-1$
                        .getValue();
                String propertyType = (String) element.getElementParameter(
                        EParameterName.PROPERTY_TYPE.getName() + ":" + EParameterName.PROPERTY_TYPE.getName()).getValue(); //$NON-NLS-1$
                /* 16969 */
                Connection repositoryConnection = null;
                // Map<String, ConnectionItem> repositoryConnectionItemMap =
                // dynamicProperty.getRepositoryConnectionItemMap();
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
                // if (repositoryConnectionItemMap.containsKey(id)) {
                // repositoryConnection = repositoryConnectionItemMap.get(id).getConnection();
                // } else {
                // repositoryConnection = null;
                // }

                ChangeValuesFromRepository cmd1 = new ChangeValuesFromRepository(applyTo, repositoryConnection,
                        EParameterName.PROPERTY_TYPE.getName() + ":" + EParameterName.PROPERTY_TYPE.getName(), propertyType); //$NON-NLS-1$

                ChangeValuesFromRepository cmd2 = new ChangeValuesFromRepository(applyTo, repositoryConnection,
                        EParameterName.PROPERTY_TYPE.getName() + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), id); //$NON-NLS-1$

                CompoundCommand cc = new CompoundCommand();
                cc.add(cmd1);
                cc.add(cmd2);

                boolean executed = false;
                if (applyTo instanceof IGEFProcess) {
                    IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
                    if (designerCoreUIService != null) {
                        executed = designerCoreUIService.executeCommand((IGEFProcess) applyTo, cc);
                    }
                }

                if (!executed) {
                    cc.execute();
                }

                continue;
            }

            if (PREFERENCE_STORE.getString(LANGUAGE_PREFIX + EParameterName.PROPERTY_TYPE.getName()).equals(EmfComponent.BUILTIN)) {

                if (parameterName.equals(EParameterName.DB_TYPE.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.DB_TYPE.getName()).getValue());
                    continue;
                }

                if (parameterName.equals(EParameterName.HOST.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.HOST.getName()).getValue());
                    continue;
                }

                if (parameterName.equals(EParameterName.PORT.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.PORT.getName()).getValue());
                    continue;
                }

                if (parameterName.equals(EParameterName.DBNAME.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.DBNAME.getName()).getValue());
                    continue;
                }
                if (parameterName.equals(EParameterName.PROPERTIES.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.PROPERTIES.getName()).getValue());
                    continue;
                }

                if (parameterName.equals(EParameterName.DBFILE.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.DBFILE.getName()).getValue());
                    continue;
                }
                if (parameterName.equals(EParameterName.SCHEMA_DB.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.SCHEMA_DB.getName()).getValue());
                    continue;
                }

                if (parameterName.equals(EParameterName.USER.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.USER.getName()).getValue());
                    continue;
                }

                if (parameterName.equals(EParameterName.PASS.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.PASS.getName()).getValue());
                    continue;
                }

                if (parameterName.equals(EParameterName.TABLE_STATS.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.TABLE_STATS.getName()).getValue());
                    continue;
                }

                if (parameterName.equals(EParameterName.TABLE_LOGS.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.TABLE_LOGS.getName()).getValue());
                    continue;
                }

                if (parameterName.equals(EParameterName.TABLE_METER.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.TABLE_METER.getName()).getValue());
                    continue;
                }

                if (parameterName.equals(EParameterName.CATCH_RUNTIME_ERRORS.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.CATCH_RUNTIME_ERRORS.getName())
                            .getValue());
                    continue;
                }

                if (parameterName.equals(EParameterName.CATCH_USER_ERRORS.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.CATCH_USER_ERRORS.getName())
                            .getValue());
                    continue;
                }

                if (parameterName.equals(EParameterName.CATCH_USER_WARNING.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.CATCH_USER_WARNING.getName())
                            .getValue());
                    continue;
                }

                if (parameterName.equals(EParameterName.CATCH_REALTIME_STATS.getName())) {
                    elementParameterType.setValue(element.getElementParameter(EParameterName.CATCH_REALTIME_STATS.getName())
                            .getValue());
                    continue;
                }
            }
        }
    }

    public static void applySettings(EList<ElementParameterType> elementParameterTypes, Element elem) {

        for (ElementParameterType elementParameterType : elementParameterTypes) {

            String parameterTypeName = elementParameterType.getName();

            if (parameterTypeName.equals(EParameterName.ON_STATCATCHER_FLAG.getName())) {
                elementParameterType.setValue(String.valueOf(elem.getElementParameter(
                        EParameterName.ON_STATCATCHER_FLAG.getName()).getValue()));
                continue;
            }

            if (parameterTypeName.equals(EParameterName.ON_LOGCATCHER_FLAG.getName())) {
                elementParameterType.setValue(String.valueOf(elem
                        .getElementParameter(EParameterName.ON_LOGCATCHER_FLAG.getName()).getValue()));
                continue;
            }

            if (parameterTypeName.equals(EParameterName.ON_METERCATCHER_FLAG.getName())) {
                elementParameterType.setValue(String.valueOf(elem.getElementParameter(
                        EParameterName.ON_METERCATCHER_FLAG.getName()).getValue()));
                continue;
            }

            if (parameterTypeName.equals(EParameterName.ON_FILES_FLAG.getName())) {
                elementParameterType.setValue(String.valueOf(elem.getElementParameter(EParameterName.ON_FILES_FLAG.getName())
                        .getValue()));
                continue;
            }

            if (parameterTypeName.equals(EParameterName.FILE_PATH.getName())) {
                elementParameterType.setValue(String.valueOf(elem.getElementParameter(EParameterName.FILE_PATH.getName())
                        .getValue()));
                continue;
            }

            if (parameterTypeName.equals(EParameterName.FILENAME_STATS.getName())) {
                elementParameterType.setValue(String.valueOf(elem.getElementParameter(EParameterName.FILENAME_STATS.getName())
                        .getValue()));
                continue;
            }

            if (parameterTypeName.equals(EParameterName.FILENAME_LOGS.getName())) {
                elementParameterType.setValue(String.valueOf(elem.getElementParameter(EParameterName.FILENAME_LOGS.getName())
                        .getValue()));
                continue;
            }

            if (parameterTypeName.equals(EParameterName.FILENAME_METTER.getName())) {
                elementParameterType.setValue(String.valueOf(elem.getElementParameter(EParameterName.FILENAME_METTER.getName())
                        .getValue()));
                continue;
            }

            if (parameterTypeName.equals(EParameterName.ON_DATABASE_FLAG.getName())) {
                elementParameterType.setValue(String.valueOf(elem.getElementParameter(EParameterName.ON_DATABASE_FLAG.getName())
                        .getValue()));
                continue;
            }

            if (parameterTypeName.equals(EParameterName.PROPERTY_TYPE.getName())) {
                elementParameterType.setValue(String.valueOf(elem.getElementParameter(EParameterName.PROPERTY_TYPE.getName())
                        .getValue()));
                continue;
            }

            if (parameterTypeName.equals(EParameterName.PROPERTY_TYPE.getName() + ":" //$NON-NLS-1$
                    + EParameterName.REPOSITORY_PROPERTY_TYPE.getName())) {
                elementParameterType.setValue(String.valueOf(elem.getElementParameter(
                        EParameterName.PROPERTY_TYPE.getName() + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName()) //$NON-NLS-1$
                        .getValue()));
                continue;
            }

            if (parameterTypeName.equals(EParameterName.PROPERTY_TYPE.getName() + ":" + EParameterName.PROPERTY_TYPE.getName())) { //$NON-NLS-1$
                elementParameterType.setValue(String.valueOf(elem.getElementParameter(
                        EParameterName.PROPERTY_TYPE.getName() + ":" + EParameterName.PROPERTY_TYPE.getName()).getValue())); //$NON-NLS-1$
                continue;
            }

            if (elem.getElementParameter(EParameterName.PROPERTY_TYPE.getName() + ":" + EParameterName.PROPERTY_TYPE.getName()) //$NON-NLS-1$
                    .getValue().equals(EmfComponent.BUILTIN)) {
                if (parameterTypeName.equals(EParameterName.DB_TYPE.getName())) {
                    elementParameterType.setValue(String.valueOf(elem.getElementParameter(EParameterName.DB_TYPE.getName())
                            .getValue()));
                    continue;
                }

                if (parameterTypeName.equals(EParameterName.HOST.getName())) {
                    elementParameterType.setValue(String.valueOf(elem.getElementParameter(EParameterName.HOST.getName())
                            .getValue()));
                    continue;
                }

                if (parameterTypeName.equals(EParameterName.PORT.getName())) {
                    elementParameterType.setValue(String.valueOf(elem.getElementParameter(EParameterName.PORT.getName())
                            .getValue()));
                    continue;
                }

                if (parameterTypeName.equals(EParameterName.DBNAME.getName())) {
                    elementParameterType.setValue(checkAndAddQuote(String.valueOf(elem.getElementParameter(
                            EParameterName.DBNAME.getName()).getValue())));
                    continue;
                }
                if (parameterTypeName.equals(EParameterName.PROPERTIES.getName())) {
                    elementParameterType.setValue(checkAndAddQuote(String.valueOf(elem.getElementParameter(
                            EParameterName.PROPERTIES.getName()).getValue())));
                    continue;
                }

                if (parameterTypeName.equals(EParameterName.DBFILE.getName())) {
                    elementParameterType.setValue(checkAndAddQuote(String.valueOf(elem.getElementParameter(
                            EParameterName.DBFILE.getName()).getValue())));
                    continue;
                }
                if (parameterTypeName.equals(EParameterName.SCHEMA_DB.getName())) {
                    elementParameterType.setValue(checkAndAddQuote(String.valueOf(elem.getElementParameter(
                            EParameterName.SCHEMA_DB.getName()).getValue())));
                    continue;
                }

                if (parameterTypeName.equals(EParameterName.USER.getName())) {
                    elementParameterType.setValue(checkAndAddQuote(String.valueOf(elem.getElementParameter(
                            EParameterName.USER.getName()).getValue())));
                    continue;
                }

                if (parameterTypeName.equals(EParameterName.PASS.getName())) {
                    elementParameterType.setValue(checkAndAddQuote(String.valueOf(elem.getElementParameter(
                            EParameterName.PASS.getName()).getValue())));
                    continue;
                }

                if (parameterTypeName.equals(EParameterName.TABLE_STATS.getName())) {
                    elementParameterType.setValue(checkAndAddQuote(String.valueOf(elem.getElementParameter(
                            EParameterName.TABLE_STATS.getName()).getValue())));
                    continue;
                }

                if (parameterTypeName.equals(EParameterName.TABLE_LOGS.getName())) {
                    elementParameterType.setValue(checkAndAddQuote(String.valueOf(elem.getElementParameter(
                            EParameterName.TABLE_LOGS.getName()).getValue())));
                    continue;
                }

                if (parameterTypeName.equals(EParameterName.TABLE_METER.getName())) {
                    elementParameterType.setValue(checkAndAddQuote(String.valueOf(elem.getElementParameter(
                            EParameterName.TABLE_METER.getName()).getValue())));
                    continue;
                }

                if (parameterTypeName.equals(EParameterName.CATCH_RUNTIME_ERRORS.getName())) {
                    elementParameterType.setValue(String.valueOf(elem.getElementParameter(
                            EParameterName.CATCH_RUNTIME_ERRORS.getName()).getValue()));
                    continue;
                }

                if (parameterTypeName.equals(EParameterName.CATCH_USER_ERRORS.getName())) {
                    elementParameterType.setValue(String.valueOf(elem.getElementParameter(
                            EParameterName.CATCH_USER_ERRORS.getName()).getValue()));
                    continue;
                }

                if (parameterTypeName.equals(EParameterName.CATCH_USER_WARNING.getName())) {
                    elementParameterType.setValue(String.valueOf(elem.getElementParameter(
                            EParameterName.CATCH_USER_WARNING.getName()).getValue()));
                    continue;
                }

                if (parameterTypeName.equals(EParameterName.CATCH_REALTIME_STATS.getName())) {
                    elementParameterType.setValue(String.valueOf(elem.getElementParameter(
                            EParameterName.CATCH_REALTIME_STATS.getName()).getValue()));
                    continue;
                }
            }
        }
    }

    /**
     * ftang Comment method "reloadValuesFromPreferencePage".
     *
     * @param preferenceStore
     * @param element
     */
    public static void reloadValuesFromPreferencePage(Element element, IDynamicProperty propertyComposite) {

        List<? extends IElementParameter> elementParameters = element.getElementParameters();
        for (IElementParameter elementParameter : elementParameters) {
            String name = elementParameter.getName();

            if (name.equals(EParameterName.ON_STATCATCHER_FLAG.getName())) {
                elementParameter.setValue(PREFERENCE_STORE.getBoolean(LANGUAGE_PREFIX
                        + EParameterName.ON_STATCATCHER_FLAG.getName()));
                continue;
            }

            if (name.equals(EParameterName.ON_LOGCATCHER_FLAG.getName())) {
                elementParameter.setValue(PREFERENCE_STORE.getBoolean(LANGUAGE_PREFIX
                        + EParameterName.ON_LOGCATCHER_FLAG.getName()));
                continue;
            }

            if (name.equals(EParameterName.ON_METERCATCHER_FLAG.getName())) {
                elementParameter.setValue(PREFERENCE_STORE.getBoolean(LANGUAGE_PREFIX
                        + EParameterName.ON_METERCATCHER_FLAG.getName()));
                continue;
            }

            if (name.equals(EParameterName.ON_FILES_FLAG.getName())) {
                elementParameter.setValue(PREFERENCE_STORE.getBoolean(LANGUAGE_PREFIX + EParameterName.ON_FILES_FLAG.getName()));
                continue;
            }

            if (name.equals(EParameterName.FILE_PATH.getName())) {
                elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                        + EParameterName.FILE_PATH.getName())));
                continue;
            }

            if (name.equals(EParameterName.FILENAME_STATS.getName())) {
                elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                        + EParameterName.FILENAME_STATS.getName())));
                continue;
            }

            if (name.equals(EParameterName.FILENAME_LOGS.getName())) {
                elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                        + EParameterName.FILENAME_LOGS.getName())));
                continue;
            }

            if (name.equals(EParameterName.FILENAME_METTER.getName())) {
                elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                        + EParameterName.FILENAME_METTER.getName())));
                continue;
            }

            if (name.equals(EParameterName.ON_DATABASE_FLAG.getName())) {
                elementParameter
                        .setValue(PREFERENCE_STORE.getBoolean(LANGUAGE_PREFIX + EParameterName.ON_DATABASE_FLAG.getName()));
                continue;
            }

            if (name.equals(EParameterName.PROPERTY_TYPE.getName())) {
                String propertyType = PREFERENCE_STORE.getString(LANGUAGE_PREFIX + EParameterName.PROPERTY_TYPE.getName());
                String id = PREFERENCE_STORE.getString(LANGUAGE_PREFIX + EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                /* 16969 */
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
                // Map<String, ConnectionItem> repositoryConnectionItemMap =
                // propertyComposite.getRepositoryConnectionItemMap();
                //
                // if (repositoryConnectionItemMap.containsKey(id)) {
                // repositoryConnection = repositoryConnectionItemMap.get(id).getConnection();
                // } else {
                // repositoryConnection = null;
                // }

                ChangeValuesFromRepository cmd1 = new ChangeValuesFromRepository(element, repositoryConnection,
                        EParameterName.PROPERTY_TYPE.getName() + ":" + EParameterName.PROPERTY_TYPE.getName(), propertyType); //$NON-NLS-1$

                ChangeValuesFromRepository cmd2 = new ChangeValuesFromRepository(element, repositoryConnection,
                        EParameterName.PROPERTY_TYPE.getName() + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), id); //$NON-NLS-1$

                AbstractMultiPageTalendEditor part = (AbstractMultiPageTalendEditor) ((IProcess2) element).getEditor();
                if (part instanceof AbstractMultiPageTalendEditor) {
                    Object adapter = part.getTalendEditor().getAdapter(CommandStack.class);
                    if (adapter != null) {
                        CommandStack commandStack = ((CommandStack) adapter);
                        commandStack.execute(cmd1);
                        commandStack.execute(cmd2);
                    }
                }

                continue;
            }

            if (PREFERENCE_STORE.getString(LANGUAGE_PREFIX + EParameterName.PROPERTY_TYPE.getName()).equals(EmfComponent.BUILTIN)) {
                if (name.equals(EParameterName.DB_TYPE.getName())) {
                    elementParameter.setValue(PREFERENCE_STORE.getString(LANGUAGE_PREFIX + EParameterName.DB_TYPE.getName()));
                    continue;
                }

                if (name.equals(EParameterName.HOST.getName())) {
                    elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                            + EParameterName.HOST.getName())));
                    continue;
                }

                if (name.equals(EParameterName.PORT.getName())) {
                    elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                            + EParameterName.PORT.getName())));
                    continue;
                }

                if (name.equals(EParameterName.DBNAME.getName())) {
                    elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                            + EParameterName.DBNAME.getName())));
                    continue;
                }
                if (name.equals(EParameterName.PROPERTIES.getName())) {
                    elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                            + EParameterName.PROPERTIES.getName())));
                    continue;
                }

                if (name.equals(EParameterName.DBFILE.getName())) {
                    elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                            + EParameterName.DBFILE.getName())));
                    continue;
                }
                if (name.equals(EParameterName.SCHEMA_DB.getName())) {
                    elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                            + EParameterName.SCHEMA_DB.getName())));
                    continue;
                }

                if (name.equals(EParameterName.USER.getName())) {
                    elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                            + EParameterName.USER.getName())));
                    continue;
                }

                if (name.equals(EParameterName.PASS.getName())) {
                    elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                            + EParameterName.PASS.getName())));
                    continue;
                }
            }

            if (name.equals(EParameterName.TABLE_STATS.getName())) {
                elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                        + EParameterName.TABLE_STATS.getName())));
                continue;
            }

            if (name.equals(EParameterName.TABLE_LOGS.getName())) {
                elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                        + EParameterName.TABLE_LOGS.getName())));
                continue;
            }

            if (name.equals(EParameterName.TABLE_METER.getName())) {
                elementParameter.setValue(checkAndAddQuote(PREFERENCE_STORE.getString(LANGUAGE_PREFIX
                        + EParameterName.TABLE_METER.getName())));
                continue;
            }

            if (name.equals(EParameterName.CATCH_RUNTIME_ERRORS.getName())) {
                elementParameter.setValue(PREFERENCE_STORE.getBoolean(LANGUAGE_PREFIX
                        + EParameterName.CATCH_RUNTIME_ERRORS.getName()));
                continue;
            }

            if (name.equals(EParameterName.CATCH_USER_ERRORS.getName())) {
                elementParameter.setValue(PREFERENCE_STORE.getBoolean(LANGUAGE_PREFIX
                        + EParameterName.CATCH_USER_ERRORS.getName()));
                continue;
            }

            if (name.equals(EParameterName.CATCH_USER_WARNING.getName())) {
                elementParameter.setValue(PREFERENCE_STORE.getBoolean(LANGUAGE_PREFIX
                        + EParameterName.CATCH_USER_WARNING.getName()));
                continue;
            }

            if (name.equals(EParameterName.CATCH_REALTIME_STATS.getName())) {
                elementParameter.setValue(PREFERENCE_STORE.getBoolean(LANGUAGE_PREFIX
                        + EParameterName.CATCH_REALTIME_STATS.getName()));
                continue;
            }

        }
    }

    /**
     * ftang "saveValuesToPreferencePage".
     *
     * @param preferenceStore
     * @param element
     */
    public static void saveValuesToPreferencePage(IElement element, IDynamicProperty dynamicProperty) {

        List<? extends IElementParameter> elementParameters = element.getElementParameters();
        for (IElementParameter elementParameter : elementParameters) {
            String name = elementParameter.getName();
            Object elementValue = elementParameter.getValue();

            if (name.equals(EParameterName.ON_STATCATCHER_FLAG.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.ON_STATCATCHER_FLAG.getName(), (Boolean) elementValue);
                continue;
            }

            if (name.equals(EParameterName.ON_LOGCATCHER_FLAG.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.ON_LOGCATCHER_FLAG.getName(), (Boolean) elementValue);
                continue;

            }

            if (name.equals(EParameterName.ON_METERCATCHER_FLAG.getName())) {
                PREFERENCE_STORE
                        .setValue(LANGUAGE_PREFIX + EParameterName.ON_METERCATCHER_FLAG.getName(), (Boolean) elementValue);
                continue;
            }

            if (name.equals(EParameterName.ON_FILES_FLAG.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.ON_FILES_FLAG.getName(), (Boolean) elementValue);
                continue;
            }

            if (name.equals(EParameterName.FILE_PATH.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.FILE_PATH.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }

            if (name.equals(EParameterName.FILENAME_STATS.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.FILENAME_STATS.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }

            if (name.equals(EParameterName.FILENAME_LOGS.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.FILENAME_LOGS.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }

            if (name.equals(EParameterName.FILENAME_METTER.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.FILENAME_METTER.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }

            if (name.equals(EParameterName.ON_DATABASE_FLAG.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.ON_DATABASE_FLAG.getName(), (Boolean) elementValue);
                continue;
            }

            if (name.equals(EParameterName.DB_TYPE.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.DB_TYPE.getName(), (String) elementValue);
                continue;
            }

            if (name.equals(EParameterName.HOST.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.HOST.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }

            if (name.equals(EParameterName.PORT.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.PORT.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }

            if (name.equals(EParameterName.DBNAME.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.DBNAME.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }
            if (name.equals(EParameterName.PROPERTIES.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.PROPERTIES.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }
            if (name.equals(EParameterName.DBFILE.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.DBFILE.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }
            if (name.equals(EParameterName.SCHEMA_DB.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.SCHEMA_DB.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }

            if (name.equals(EParameterName.USER.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.USER.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }

            if (name.equals(EParameterName.PASS.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.PASS.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }

            if (name.equals(EParameterName.TABLE_STATS.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.TABLE_STATS.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }

            if (name.equals(EParameterName.TABLE_LOGS.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.TABLE_LOGS.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }

            if (name.equals(EParameterName.TABLE_METER.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.TABLE_METER.getName(),
                        checkAndRemoveQuote((String) elementValue));
                continue;
            }

            if (name.equals(EParameterName.CATCH_RUNTIME_ERRORS.getName())) {
                PREFERENCE_STORE
                        .setValue(LANGUAGE_PREFIX + EParameterName.CATCH_RUNTIME_ERRORS.getName(), (Boolean) elementValue);
                continue;
            }

            if (name.equals(EParameterName.CATCH_USER_ERRORS.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.CATCH_USER_ERRORS.getName(), (Boolean) elementValue);
                continue;
            }

            if (name.equals(EParameterName.CATCH_USER_WARNING.getName())) {
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.CATCH_USER_WARNING.getName(), (Boolean) elementValue);
                continue;
            }

            if (name.equals(EParameterName.CATCH_REALTIME_STATS.getName())) {
                PREFERENCE_STORE
                        .setValue(LANGUAGE_PREFIX + EParameterName.CATCH_REALTIME_STATS.getName(), (Boolean) elementValue);
                continue;
            }

            if (name.equals(EParameterName.PROPERTY_TYPE.getName())) {
                String itemId = (String) elementParameter.getChildParameters().get("REPOSITORY_PROPERTY_TYPE").getValue(); //$NON-NLS-1$
                String propertyType = (String) elementParameter.getChildParameters().get("PROPERTY_TYPE").getValue(); //$NON-NLS-1$

                Item item = UpdateRepositoryUtils.getConnectionItemByItemId(itemId);
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.PROPERTY_TYPE.getName(), propertyType);
                PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), itemId);
                if (item != null) {
                    PREFERENCE_STORE.setValue(LANGUAGE_PREFIX + EParameterName.REPOSITORY_PROPERTY_TYPE.getName()
                            + CONNECTION_ITEM_LABEL, item.getProperty().getLabel());
                }
            }
        }
    }

    /*
     * add quote.
     */
    private static String checkAndAddQuote(String value) {
        if (value == null) {
            return TalendTextUtils.addQuotes(""); //$NON-NLS-1$
        }
        value = value.trim();
        return value;
    }

    /*
     * remove quote.
     */
    private static String checkAndRemoveQuote(String value) {
        if (value == null) {
            return ""; //$NON-NLS-1$
        }
        value = value.trim();
        return value;
    }
}
