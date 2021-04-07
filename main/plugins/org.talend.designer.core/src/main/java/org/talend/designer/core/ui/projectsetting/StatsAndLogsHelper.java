// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.projectsetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.param.EConnectionParameterName;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.StatAndLogsSettings;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.preferences.StatsAndLogsConstants;
import org.talend.designer.core.ui.views.jobsettings.ImplicitContextLoadHelper;
import org.talend.designer.core.ui.views.statsandlogs.StatsAndLogsComposite;
import org.talend.librariesmanager.model.ModulesNeededProvider;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * Helper class for Load StatsAndLogs Preferences to EMF project in Project setting
 */
public class StatsAndLogsHelper extends Utils {

    public static final String ENCODING_TYPE_UTF_8 = "UTF-8"; //$NON-NLS-1$

    public static final String ENCODING_TYPE_ISO_8859_15 = "ISO-8859-15"; //$NON-NLS-1$

    public static final String ENCODING_TYPE_CUSTOM = "CUSTOM"; //$NON-NLS-1$

    /**
     *
     * Load StatsAndLogs Preference setting to Project Only load Once
     *
     * @param pro
     */
    static void loadPreferenceToProject(Project pro) {
        TalendFileFactory talendF = TalendFileFactory.eINSTANCE;

        StatAndLogsSettings stats = PropertiesFactory.eINSTANCE.createStatAndLogsSettings();
        pro.getEmfProject().setStatAndLogsSettings(stats);
        stats.setParameters(talendF.createParametersType());

        ParametersType pType = stats.getParameters();
        StatsAndLogsElement elem = new StatsAndLogsElement();
        pro.setStatsAndLog(elem);
        StatsAndLogsHelper.createStatsAndLogsParameters(elem);
        ElementParameter2ParameterType.saveElementParameters(elem, pType);
    }

    static void createStatsAndLogsParameters(Element elem) {
        statsAndLogsParametersTitlePart(elem);
        statsAndLogsParametersFilePart(elem);
        statsAndLogsParametersDBPart(elem);
        statsAndLogsParametersFinalPart(elem);

    }

    private static void statsAndLogsParametersTitlePart(Element elem) {
        ElementParameter param;
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();

        List<IElementParameter> paramList = (List<IElementParameter>) elem.getElementParameters();

        String languagePrefix = LanguageManager.getCurrentLanguage().toString() + "_"; //$NON-NLS-1$

        param = new ElementParameter(elem);
        param.setName(EParameterName.UPDATE_COMPONENTS.getName());
        param.setValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(1);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        paramList.add(param);

        param = new ElementParameter(elem);
        param.setName(EParameterName.STATS_DEFAULT_PROJECTSETTING.getName());
        param.setValue(Boolean.TRUE);
        param.setGroupDisplayName(EParameterName.STATS_DEFAULT_PROJECTSETTING.getDisplayName());
        param.setDisplayName(EParameterName.STATS_DEFAULT_PROJECTSETTING.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(3);
        param.setShow(false);
        paramList.add(param);

        param = new ElementParameter(elem);
        param.setName(EParameterName.ON_STATCATCHER_FLAG.getName());
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.ON_STATCATCHER_FLAG.getName()));
        param.setDisplayName(EParameterName.ON_STATCATCHER_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(1);
        paramList.add(param);

        param = new ElementParameter(elem);
        param.setName(EParameterName.ON_LOGCATCHER_FLAG.getName());
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.ON_LOGCATCHER_FLAG.getName()));
        param.setDisplayName(EParameterName.ON_LOGCATCHER_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(1);
        paramList.add(param);

        param = new ElementParameter(elem);
        param.setName(EParameterName.ON_METERCATCHER_FLAG.getName());
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.ON_METERCATCHER_FLAG.getName()));
        param.setDisplayName(EParameterName.ON_METERCATCHER_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(1);
        paramList.add(param);

        // on console
        param = new ElementParameter(elem);
        param.setName(EParameterName.ON_CONSOLE_FLAG.getName());
        param.setValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.ON_CONSOLE_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(2);
        param.setShowIf("(ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);
    }

    private static void statsAndLogsParametersFilePart(Element elem) {
        ElementParameter param;
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();

        List<IElementParameter> paramList = (List<IElementParameter>) elem.getElementParameters();

        String languagePrefix = LanguageManager.getCurrentLanguage().toString() + "_"; //$NON-NLS-1$
        // on files
        param = new ElementParameter(elem);
        param.setName(EParameterName.ON_FILES_FLAG.getName());
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.ON_FILES_FLAG.getName()));
        param.setDisplayName(EParameterName.ON_FILES_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(10);
        param.setShowIf("(ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        // file path
        param = new ElementParameter(elem);
        param.setName(EParameterName.FILE_PATH.getName());
        param.setValue(addQuotes(replaceSlash(preferenceStore.getString(languagePrefix + EParameterName.FILE_PATH.getName()))));
        param.setDisplayName(EParameterName.FILE_PATH.getDisplayName());
        param.setFieldType(EParameterFieldType.DIRECTORY);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setShowIf("(ON_FILES_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        param.setNumRow(11);
        paramList.add(param);

        // stats file name
        param = new ElementParameter(elem);
        param.setName(EParameterName.FILENAME_STATS.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.FILENAME_STATS.getName())));
        param.setDisplayName(EParameterName.FILENAME_STATS.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setShowIf("(ON_FILES_FLAG == 'true' and ON_STATCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        param.setRequired(true);
        param.setNumRow(12);

        paramList.add(param);

        param = new ElementParameter(elem);
        param.setName(EParameterName.FILENAME_LOGS.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.FILENAME_LOGS.getName())));
        param.setDisplayName(EParameterName.FILENAME_LOGS.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setShowIf("(ON_FILES_FLAG == 'true' and ON_LOGCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        param.setNumRow(13);
        param.setRequired(true);
        paramList.add(param);

        param = new ElementParameter(elem);
        param.setName(EParameterName.FILENAME_METTER.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.FILENAME_METTER.getName())));
        param.setDisplayName(EParameterName.FILENAME_METTER.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setShowIf("(ON_FILES_FLAG == 'true' and ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        param.setRequired(true);
        param.setNumRow(14);
        paramList.add(param);

        // stats log encoding
        ElementParameter encodingParam = new ElementParameter(elem);
        encodingParam.setName(EParameterName.ENCODING.getName());
        encodingParam.setDisplayName(EParameterName.ENCODING.getDisplayName());
        encodingParam.setCategory(EComponentCategory.STATSANDLOGS);
        encodingParam.setFieldType(EParameterFieldType.ENCODING_TYPE);
        encodingParam
                .setShowIf("(ON_FILES_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        encodingParam.setValue(ENCODING_TYPE_ISO_8859_15);
        encodingParam.setNumRow(15);
        paramList.add(encodingParam);

        ElementParameter childPram = new ElementParameter(elem);
        childPram.setName(EParameterName.ENCODING_TYPE.getName());
        childPram.setDisplayName(EParameterName.ENCODING_TYPE.getDisplayName());
        childPram.setFieldType(EParameterFieldType.TECHNICAL);
        childPram.setCategory(EComponentCategory.STATSANDLOGS);
        childPram.setListItemsDisplayName(new String[] { ENCODING_TYPE_ISO_8859_15, ENCODING_TYPE_UTF_8, ENCODING_TYPE_CUSTOM });
        childPram
                .setListItemsDisplayCodeName(new String[] { ENCODING_TYPE_ISO_8859_15, ENCODING_TYPE_UTF_8, ENCODING_TYPE_CUSTOM });
        childPram.setListItemsValue(new String[] { ENCODING_TYPE_ISO_8859_15, ENCODING_TYPE_UTF_8, ENCODING_TYPE_CUSTOM });
        childPram.setValue(ENCODING_TYPE_ISO_8859_15);
        childPram.setNumRow(15);
        childPram
                .setShowIf("(ON_FILES_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        childPram.setParentParameter(encodingParam);
    }

    static void changeRepositoryConnection(Element process, StatsAndLogsComposite statsComposite) {
        String propertyType = (String) ElementParameter2ParameterType.getParameterValue(process,
                EParameterName.PROPERTY_TYPE.getName());

        String id = (String) (ElementParameter2ParameterType.getParameterValue(process, EParameterName.PROPERTY_TYPE.getName()));

        Connection repositoryConnection = null;
        /* 16969 */
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
        ChangeValuesFromRepository cmd1 = new ChangeValuesFromRepository(process, repositoryConnection,
                ImplicitContextLoadHelper.getExtraParameterName(EParameterName.PROPERTY_TYPE)
                        + ":" + EParameterName.PROPERTY_TYPE.getName(), propertyType); //$NON-NLS-1$

        ChangeValuesFromRepository cmd2 = new ChangeValuesFromRepository(process, repositoryConnection,
                ImplicitContextLoadHelper.getExtraParameterName(EParameterName.PROPERTY_TYPE)
                        + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), id); //$NON-NLS-1$

        AbstractMultiPageTalendEditor part = (AbstractMultiPageTalendEditor) ((IProcess2) process).getEditor();
        if (part instanceof AbstractMultiPageTalendEditor) {
            Object adapter = (part).getTalendEditor().getAdapter(CommandStack.class);
            if (adapter != null) {
                CommandStack commandStack = ((CommandStack) adapter);
                commandStack.execute(cmd1);
                commandStack.execute(cmd2);
            }
        }
    }

    private static void statsAndLogsParametersDBPart(Element elem) {
        ElementParameter param;
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();

        List<IElementParameter> paramList = (List<IElementParameter>) elem.getElementParameters();

        // checks current language, if it is perl, set languageType to 0(default value), otherwise to 1.
        int languageType = 0;
        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
            languageType = 1;
        }

        String languagePrefix = LanguageManager.getCurrentLanguage().toString() + "_"; //$NON-NLS-1$

        // on database
        param = new ElementParameter(elem);
        param.setName(EParameterName.ON_DATABASE_FLAG.getName());
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.ON_DATABASE_FLAG.getName()));
        param.setDisplayName(EParameterName.ON_DATABASE_FLAG.getDisplayName()); // On Database
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(50);
        param.setShowIf("(ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        ElementParameter parentPropertyType = new ElementParameter(elem);
        parentPropertyType.setName(EParameterName.PROPERTY_TYPE.getName());
        parentPropertyType.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        parentPropertyType.setValue(""); //$NON-NLS-1$
        parentPropertyType.setCategory(EComponentCategory.STATSANDLOGS);
        parentPropertyType.setFieldType(EParameterFieldType.PROPERTY_TYPE);
        parentPropertyType.setRepositoryValue(ERepositoryCategoryType.DATABASE.getName());
        parentPropertyType.setNumRow(51);
        parentPropertyType
                .setShowIf("(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(parentPropertyType);

        param = new ElementParameter(elem);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setName(EParameterName.PROPERTY_TYPE.getName());
        param.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        param.setListItemsDisplayName(new String[] { EmfComponent.TEXT_BUILTIN, EmfComponent.TEXT_REPOSITORY });
        param.setListItemsDisplayCodeName(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        param.setListItemsValue(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        param.setValue(preferenceStore.getString(languagePrefix + EParameterName.PROPERTY_TYPE.getName()));
        param.setNumRow(51);
        param.setFieldType(EParameterFieldType.TECHNICAL);
        param.setRepositoryValue(ERepositoryCategoryType.DATABASE.getName());
        param.setShowIf("(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$

        param.setParentParameter(parentPropertyType);
        // paramList.add(param);

        param = new ElementParameter(elem);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setName(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        param.setDisplayName(EParameterName.REPOSITORY_PROPERTY_TYPE.getDisplayName());
        param.setListItemsDisplayName(new String[] {});
        param.setListItemsValue(new String[] {});
        param.setNumRow(51);
        param.setFieldType(EParameterFieldType.TECHNICAL);
        param.setValue(preferenceStore.getString(languagePrefix + EParameterName.REPOSITORY_PROPERTY_TYPE.getName()// +
        // ProjectSettingManager
        // .
        // CONNECTION_ITEM_LABEL
                ));
        param.setShow(false);
        param.setRequired(true);
        // paramList.add(param);
        param.setParentParameter(parentPropertyType);

        // dbType
        param = new ElementParameter(elem);
        param.setName(EParameterName.DB_TYPE.getName());
        String type = preferenceStore.getString(languagePrefix + EParameterName.DB_TYPE.getName());
        if (type == null || "".equals(type.trim())) { //$NON-NLS-1$
            type = StatsAndLogsConstants.DB_COMPONENTS[languageType][0];
        }
        param.setValue(type);
        param.setDisplayName(EParameterName.DB_TYPE.getDisplayName());
        param.setFieldType(EParameterFieldType.CLOSED_LIST);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setListItemsDisplayName(StatsAndLogsConstants.DISPLAY_DBNAMES[languageType]);
        param.setListItemsValue(StatsAndLogsConstants.DB_COMPONENTS[languageType]);
        param.setListRepositoryItems(StatsAndLogsConstants.REPOSITORY_ITEMS[languageType]);
        param.setListItemsDisplayCodeName(StatsAndLogsConstants.CODE_LIST[languageType]);
        param.setNumRow(52);
        param.setRepositoryValue("TYPE"); //$NON-NLS-1$
        param.setRequired(true);
        param.setShowIf("(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        // dbVersion
        param = new ElementParameter(elem);
        param.setName(EParameterName.DB_VERSION.getName());
        param.setValue(StatsAndLogsConstants.DB_VERSION_DRIVER[1]);
        param.setDisplayName(EParameterName.DB_VERSION.getDisplayName());
        param.setFieldType(EParameterFieldType.CLOSED_LIST);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setListItemsDisplayName(StatsAndLogsConstants.DB_VERSION_DISPLAY);
        param.setListItemsValue(StatsAndLogsConstants.DB_VERSION_DRIVER);
        param.setListItemsDisplayCodeName(StatsAndLogsConstants.DB_VERSION_CODE);
        param.setNumRow(52);
        param.setRepositoryValue("DB_VERSION"); //$NON-NLS-1$
        param.setRequired(true);
        param.setShowIf("(ON_DATABASE_FLAG == 'true') and (DB_TYPE == 'POSTGRESQL' or DB_TYPE == 'SYBASE' or DB_TYPE == 'OCLE' or DB_TYPE == 'OCLE_OCI' or DB_TYPE == 'ACCESS' or DB_TYPE == 'MSSQL' or DB_TYPE == 'MYSQL') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        // jdbc url
        param = new ElementParameter(elem);
        param.setName(EParameterName.URL.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.URL.getName())));
        param.setDisplayName(EParameterName.URL.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(53);
        param.setRepositoryValue(EConnectionParameterName.GENERIC_URL.getDisplayName()); 
        param.setShowIf("(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and (DB_TYPE=='JDBC')"); //$NON-NLS-1$
        paramList.add(param);

        // jdbc child param
        Set<ModuleNeeded> moduleNeededList = ModulesNeededProvider.getModulesNeeded();
        Set<String> moduleNameList = new TreeSet<String>();
        Set<String> moduleValueList = new TreeSet<String>();
        for (ModuleNeeded module : moduleNeededList) {
            String moduleName = module.getModuleName();
            if (moduleName != null) {
                moduleNameList.add(moduleName);
                String moduleValue = TalendTextUtils.addQuotes(module.getMavenUri());
                if (!moduleValueList.contains(moduleValue)) {
                    moduleValueList.add(moduleValue);
                }
            }
        }
        Comparator<String> comprarator = new IgnoreCaseComparator();
        String[] moduleNameArray = moduleNameList.toArray(new String[0]);
        String[] moduleValueArray = moduleValueList.toArray(new String[0]);
        Arrays.sort(moduleNameArray, comprarator);
        Arrays.sort(moduleValueArray, comprarator);
        ElementParameter childParam = new ElementParameter(elem);
        childParam.setName("drivers");
        childParam.setDisplayName("drivers");
        childParam.setFieldType(EParameterFieldType.MODULE_LIST);
        childParam.setListItemsDisplayName(moduleNameArray);
        childParam.setListItemsValue(moduleValueArray);
        // driver jar for jdbc
        param = new ElementParameter(elem);
        param.setName(EParameterName.DRIVER_JAR.getName());
        param.setDisplayName(EParameterName.DRIVER_JAR.getDisplayName());
        param.setFieldType(EParameterFieldType.TABLE);
        param.setListItemsDisplayCodeName(new String[] { "drivers" });
        param.setListItemsDisplayName(new String[] { "drivers" });
        param.setListItemsValue(new ElementParameter[] { childParam });
        param.setValue(new ArrayList<Map<String, Object>>());
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(54);
        param.setRepositoryValue(EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName()); 
        param.setShowIf("(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and (DB_TYPE=='JDBC')"); //$NON-NLS-1$
        paramList.add(param);

        // class name for jdbc
        param = new ElementParameter(elem);
        param.setName(EParameterName.DRIVER_CLASS.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.DRIVER_CLASS.getName())));
        param.setDisplayName(EParameterName.DRIVER_CLASS.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(57);
        param.setRepositoryValue(EConnectionParameterName.GENERIC_DRIVER_CLASS.getDisplayName()); 
        param.setShowIf("(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and (DB_TYPE=='JDBC')"); //$NON-NLS-1$
        paramList.add(param);

        // host
        param = new ElementParameter(elem);
        param.setName(EParameterName.HOST.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.HOST.getName())));
        param.setDisplayName(EParameterName.HOST.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(53);
        param.setRepositoryValue("SERVER_NAME"); //$NON-NLS-1$
        param.setShowIf("(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and (DB_TYPE!='SQLITE' and DB_TYPE!='ACCESS' and DB_TYPE!='OCLE_OCI'  and DB_TYPE!='JDBC')"); //$NON-NLS-1$
        paramList.add(param);

        // port
        param = new ElementParameter(elem);
        param.setName(EParameterName.PORT.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.PORT.getName())));
        param.setDisplayName(EParameterName.PORT.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(53);
        param.setRepositoryValue("PORT"); //$NON-NLS-1$
        param.setShowIf(
                "(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and (DB_TYPE!='SQLITE' and DB_TYPE!='ACCESS' and DB_TYPE!='FIREBIRD' and DB_TYPE!='OCLE_OCI'  and DB_TYPE!='JDBC' and DB_TYPE!='TERADATA')"); //$NON-NLS-1$
        paramList.add(param);

        // dbName
        param = new ElementParameter(elem);
        param.setName(EParameterName.DBNAME.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.DBNAME.getName())));
        param.setDisplayName(EParameterName.DBNAME.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(54);
        param.setRepositoryValue("SID"); //$NON-NLS-1$
        param.setShowIf("(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and (DB_TYPE!='SQLITE' and DB_TYPE!='ACCESS' and DB_TYPE!='FIREBIRD' and DB_TYPE!='OCLE_OCI'  and DB_TYPE!='JDBC')"); //$NON-NLS-1$
        paramList.add(param);

        // local service name
        param = new ElementParameter(elem);
        param.setName(EParameterName.LOCAL_SERVICE_NAME.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.LOCAL_SERVICE_NAME.getName())));
        param.setDisplayName(EParameterName.LOCAL_SERVICE_NAME.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(54);
        param.setRepositoryValue("SID"); //$NON-NLS-1$
        param.setShowIf("(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and DB_TYPE =='OCLE_OCI'"); //$NON-NLS-1$
        paramList.add(param);

        // additional parameters
        param = new ElementParameter(elem);
        param.setName(EParameterName.PROPERTIES.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.PROPERTIES.getName())));
        param.setDisplayName(EParameterName.PROPERTIES.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(54);
        param.setRepositoryValue("PROPERTIES_STRING"); //$NON-NLS-1$
        param.setShowIf("(DB_TYPE=='MSSQL' or DB_TYPE=='MYSQL' or DB_TYPE=='INFORMIX' or DB_TYPE=='OCLE' or DB_TYPE=='OCLE_OCI' or DB_TYPE=='SYBASE') and (ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);
        // schema
        param = new ElementParameter(elem);
        param.setName(EParameterName.SCHEMA_DB.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.SCHEMA_DB.getName())));
        param.setDisplayName(EParameterName.SCHEMA_DB.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(54);
        param.setRepositoryValue("SCHEMA"); //$NON-NLS-1$
        param.setShowIf("(DB_TYPE=='OCLE' or DB_TYPE=='OCLE_OCI' or DB_TYPE=='POSTGRESQL' or DB_TYPE=='POSTGRESPLUS' or DB_TYPE=='MSSQL' or DB_TYPE=='INFORMIX' or DB_TYPE=='IBM_DB2' or DB_TYPE=='SYBASE') and (ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        // username
        param = new ElementParameter(elem);
        param.setName(EParameterName.USER.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.USER.getName())));
        param.setDisplayName(EParameterName.USER.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(55);
        param.setRequired(true);
        param.setRepositoryValue("USERNAME"); //$NON-NLS-1$
        param.setShowIf("(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')and (DB_TYPE!='SQLITE')"); //$NON-NLS-1$
        paramList.add(param);

        // password
        param = new ElementParameter(elem);
        param.setName(EParameterName.PASS.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.PASS.getName())));
        param.setDisplayName(EParameterName.PASS.getDisplayName());
        param.setFieldType(EParameterFieldType.PASSWORD);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(55);
        param.setRequired(true);
        param.setRepositoryValue("PASSWORD"); //$NON-NLS-1$
        param.setShowIf("(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and (DB_TYPE!='SQLITE')"); //$NON-NLS-1$
        paramList.add(param);
        // databse file path
        param = new ElementParameter(elem);
        param.setName(EParameterName.DBFILE.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.DBFILE.getName())));
        param.setDisplayName(EParameterName.DBFILE.getDisplayName());
        param.setFieldType(EParameterFieldType.FILE);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(56);
        param.setRepositoryValue("FILE"); //$NON-NLS-1$
        param.setShowIf("(DB_TYPE=='SQLITE' or DB_TYPE=='ACCESS' or DB_TYPE=='FIREBIRD') and (ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);
        // Stats table
        param = new ElementParameter(elem);
        param.setName(EParameterName.TABLE_STATS.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.TABLE_STATS.getName())));
        param.setDisplayName(EParameterName.TABLE_STATS.getDisplayName());
        param.setFieldType(EParameterFieldType.DBTABLE);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(57);
        param.setShowIf("(ON_DATABASE_FLAG == 'true' and ON_STATCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        // Log table
        param = new ElementParameter(elem);
        param.setName(EParameterName.TABLE_LOGS.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.TABLE_LOGS.getName())));
        param.setDisplayName(EParameterName.TABLE_LOGS.getDisplayName());
        param.setFieldType(EParameterFieldType.DBTABLE);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(58);
        param.setShowIf("(ON_DATABASE_FLAG == 'true' and ON_LOGCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        // Metter table
        param = new ElementParameter(elem);
        param.setName(EParameterName.TABLE_METER.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.TABLE_METER.getName())));
        param.setDisplayName(EParameterName.TABLE_METER.getDisplayName());
        param.setFieldType(EParameterFieldType.DBTABLE);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(59);
        param.setShowIf("(ON_DATABASE_FLAG == 'true' and ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);
    }

    private final static class IgnoreCaseComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    }

    private static void statsAndLogsParametersFinalPart(Element elem) {
        ElementParameter param;
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();

        List<IElementParameter> paramList = (List<IElementParameter>) elem.getElementParameters();

        String languagePrefix = LanguageManager.getCurrentLanguage().toString() + "_"; //$NON-NLS-1$

        // Catch runtime errors
        param = new ElementParameter(elem);
        param.setName("CATCH_RUNTIME_ERRORS"); //$NON-NLS-1$
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.CATCH_RUNTIME_ERRORS.getName()));
        param.setDisplayName(EParameterName.CATCH_RUNTIME_ERRORS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(90);
        param.setShowIf("ON_LOGCATCHER_FLAG == 'true'"); //$NON-NLS-1$
        paramList.add(param);

        // Catch user errors
        param = new ElementParameter(elem);
        param.setName("CATCH_USER_ERRORS"); //$NON-NLS-1$
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.CATCH_USER_ERRORS.getName()));
        param.setDisplayName(EParameterName.CATCH_USER_ERRORS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(90);
        param.setShowIf("ON_LOGCATCHER_FLAG == 'true'"); //$NON-NLS-1$
        paramList.add(param);

        // Catch user warning
        param = new ElementParameter(elem);
        param.setName("CATCH_USER_WARNING"); //$NON-NLS-1$
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.CATCH_USER_WARNING.getName()));
        param.setDisplayName(EParameterName.CATCH_USER_WARNING.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(90);
        param.setShowIf("ON_LOGCATCHER_FLAG == 'true'"); //$NON-NLS-1$
        paramList.add(param);

        // Catch realtime statistics
        param = new ElementParameter(elem);
        param.setName("CATCH_REALTIME_STATS"); //$NON-NLS-1$
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.CATCH_REALTIME_STATS.getName()));
        param.setDisplayName(EParameterName.CATCH_REALTIME_STATS.getDisplayName() + " (" //$NON-NLS-1$
                + EParameterName.TSTATCATCHER_STATS.getDisplayName() + ")"); //$NON-NLS-1$
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(91);
        param.setShowIf("ON_STATCATCHER_FLAG == 'true'"); //$NON-NLS-1$
        paramList.add(param);

    }

}
