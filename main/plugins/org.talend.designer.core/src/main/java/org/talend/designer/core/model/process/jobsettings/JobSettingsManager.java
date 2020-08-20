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
package org.talend.designer.core.model.process.jobsettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.core.CorePlugin;
import org.talend.core.PluginChecker;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.QueryUtil;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.param.EConnectionParameterName;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.model.process.DataNode;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants.ContextLoadInfo;
import org.talend.designer.core.model.process.statsandlogs.OracleComponentHelper;
import org.talend.designer.core.model.process.statsandlogs.StatsAndLogsManager;
import org.talend.designer.core.ui.preferences.StatsAndLogsConstants;
import org.talend.designer.core.utils.JavaProcessUtil;
import org.talend.librariesmanager.model.ModulesNeededProvider;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class JobSettingsManager {

    private static final String IMPLICIT_GROUP = "IMPLICIT_GROUP"; //$NON-NLS-1$

    private static List<String> moduleNameList;

    private static List<String> moduleValueList;

    private static boolean isTeamEdition = PluginChecker.isTeamEdition();

    public static List<IElementParameter> getJobSettingsParameters(IProcess process) {
        List<IElementParameter> paramList = new ArrayList<IElementParameter>();
        // for extra
        paramList.addAll(getExtraParameters(process));
        // for stats & logs
        boolean isJoblet = AbstractProcessProvider.isExtensionProcessForJoblet(process);
        if (!isJoblet) {
            paramList.addAll(StatsAndLogsManager.getStatsAndLogsParameters(process));
        }

        // for feature 13940
        paramList.addAll(getHeaderFooterParameters(process));

        return paramList;
    }

    private static final String CONTEXTLOAD_CONDITION = EParameterName.IMPLICIT_TCONTEXTLOAD.getName() + " == 'true'"; //$NON-NLS-1$

    private static final String QUOTE = TalendTextUtils.getQuoteChar();

    private static final String CONNECTOR = TalendTextUtils.getStringConnect();

    private static final String ENCODING_TYPE_UTF_8 = "UTF-8"; //$NON-NLS-1$

    private static final String ENCODING_TYPE_ISO_8859_15 = "ISO-8859-15"; //$NON-NLS-1$

    private static final String ENCODING_TYPE_CUSTOM = "CUSTOM"; //$NON-NLS-1$

    private static List<IElementParameter> getHeaderFooterParameters(IProcess process) {
        // for headerFooter Code
        ElementParameter param;
        List<IElementParameter> paramList = new ArrayList<IElementParameter>();

        param = new ElementParameter(process);
        param.setName(EParameterName.HEADERFOOTER_HEADERID.getName());
        param.setValue("");
        param.setDisplayName(EParameterName.FOOTER_ENABLED.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.HEADERFOOTER);
        param.setNumRow(2);
        param.setShow(false);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.HEADER_ENABLED.getName());
        param.setValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.HEADER_ENABLED.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.HEADERFOOTER);
        param.setNumRow(2);
        param.setShow(false);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.HEADER_LIBRARY.getName());
        param.setValue("");
        param.setDisplayName(EParameterName.HEADER_LIBRARY.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.HEADERFOOTER);
        param.setNumRow(3);
        param.setShow(false);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.HEADER_CODE.getName());
        param.setValue("");
        param.setDisplayName(EParameterName.HEADER_CODE.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.HEADERFOOTER);
        param.setNumRow(4);
        param.setShow(false);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.HEADER_IMPORT.getName());
        param.setValue("");
        param.setDisplayName(EParameterName.HEADER_IMPORT.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.HEADERFOOTER);
        param.setNumRow(4);
        param.setShow(false);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.HEADERFOOTER_FOOTERID.getName());
        param.setValue("");
        param.setDisplayName(EParameterName.FOOTER_ENABLED.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.HEADERFOOTER);
        param.setNumRow(2);
        param.setShow(false);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.FOOTER_ENABLED.getName());
        param.setValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.FOOTER_ENABLED.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.HEADERFOOTER);
        param.setNumRow(5);
        param.setShow(false);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.FOOTER_LIBRARY.getName());
        param.setValue("");
        param.setDisplayName(EParameterName.FOOTER_LIBRARY.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.HEADERFOOTER);
        param.setNumRow(6);
        param.setShow(false);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.FOOTER_CODE.getName());
        param.setValue("");
        param.setDisplayName(EParameterName.FOOTER_CODE.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.HEADERFOOTER);
        param.setNumRow(7);
        param.setShow(false);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.FOOTER_IMPORT.getName());
        param.setValue("");
        param.setDisplayName(EParameterName.FOOTER_IMPORT.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.HEADERFOOTER);
        param.setNumRow(4);
        param.setShow(false);
        paramList.add(param);

        return paramList;
    }

    /**
     *
     * create parameter for extra tab.
     */
    private static List<IElementParameter> getExtraParameters(IProcess process) {
        ElementParameter param;
        List<IElementParameter> paramList = new ArrayList<IElementParameter>();

        // use project settings
        param = new ElementParameter(process);
        param.setName(EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName());
        param.setValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(2);
        param.setShow(false);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName());
        param.setValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(2);
        param.setShow(false);
        paramList.add(param);
        // end

        boolean isJoblet = AbstractProcessProvider.isExtensionProcessForJoblet(process);
        IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();

        param = new ElementParameter(process);
        param.setName(EParameterName.MULTI_THREAD_EXECATION.getName());
        param.setValue(preferenceStore.getBoolean(ITalendCorePrefConstants.RUN_IN_MULTI_THREAD));
        param.setDisplayName(EParameterName.MULTI_THREAD_EXECATION.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(2);
        param.setShow(!isJoblet);
        param.setShowIf("(MULTI_THREAD_EXECATION=='true' or MULTI_THREAD_EXECATION=='false')"); //$NON-NLS-1$
        paramList.add(param);

        if (isTeamEdition) {
            param = new ElementParameter(process);
            param.setName(EParameterName.PARALLELIZE_UNIT_SIZE.getName());
            param.setValue("25000"); //$NON-NLS-1$
            param.setDisplayName(EParameterName.PARALLELIZE_UNIT_SIZE.getDisplayName());
            param.setFieldType(EParameterFieldType.TEXT);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(2);
            param.setShow(!isJoblet);
            paramList.add(param);
        }
        // achen modify to fix bug 0006241
        if (isJoblet) {
            param = new ElementParameter(process);
            param.setName(EParameterName.STARTABLE.getName());
            param.setValue(false);
            param.setDisplayName(EParameterName.STARTABLE.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(2);
            paramList.add(param);

            param = new ElementParameter(process);
            param.setName(EParameterName.UPDATE_COMPONENTS.getName());
            param.setValue(Boolean.FALSE);
            param.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(1);
            param.setReadOnly(true);
            param.setRequired(false);
            param.setShow(false);
            paramList.add(param);
            // zywang added to fix feature 5545
            param = new ElementParameter(process);
            param.setName(EParameterName.ICONSELECTION.getName());
            param.setValue(""); //$NON-NLS-1$
            param.setDisplayName(EParameterName.ICONSELECTION.getDisplayName());
            param.setFieldType(EParameterFieldType.ICON_SELECTION);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(3);
            param.setReadOnly(false);
            param.setRequired(false);
            paramList.add(param);
        }

        param = new ElementParameter(process);
        param.setName(EParameterName.IMPLICIT_TCONTEXTLOAD.getName());
        param.setValue(false);
        param.setGroupDisplayName(EParameterName.IMPLICIT_TCONTEXTLOAD.getDisplayName());
        param.setDisplayName(EParameterName.IMPLICIT_TCONTEXTLOAD.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setGroup(IMPLICIT_GROUP);
        param.setNumRow(3);
        param.setShow(!isJoblet);
        paramList.add(param);

        // on files
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.FROM_FILE_FLAG.getName()));
        param.setValue(false);
        param.setDisplayName(EParameterName.FROM_FILE_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.RADIO);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(4);
        param.setShowIf(JobSettingsConstants.addBrackets(CONTEXTLOAD_CONDITION));
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // on database
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.FROM_DATABASE_FLAG.getName()));
        param.setValue(false);
        param.setDisplayName(EParameterName.FROM_DATABASE_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.RADIO);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(5);
        param.setShowIf(JobSettingsConstants.addBrackets(CONTEXTLOAD_CONDITION));
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // on file
        paramList.addAll(getExtraOnFileParameters(process));
        // on database
        paramList.addAll(getExtraOnDBParameters(process));
        // tContextLoad
        paramList.addAll(getExtraContextLoadParameters(process));

        return paramList;
    }

    private static List<IElementParameter> getExtraOnFileParameters(IProcess process) {
        ElementParameter param;
        List<IElementParameter> paramList = new ArrayList<IElementParameter>();
        // set Implicit tContextLoad file
        String fileName = ElementParameterParser.parse(process, "__COMP_DEFAULT_FILE_DIR__/in.csv"); //$NON-NLS-1$
        IPath path = Path.fromOSString(fileName);
        fileName = TalendTextUtils.addQuotes(path.toPortableString());

        param = new ElementParameter(process);
        param.setName(EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getName());
        param.setValue(fileName);
        param.setDisplayName(EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getDisplayName());
        param.setFieldType(EParameterFieldType.FILE);
        param.setCategory(EComponentCategory.EXTRA);
        param.setGroup(IMPLICIT_GROUP);
        param.setNumRow(31);
        String condition = JobSettingsConstants.addBrackets(CONTEXTLOAD_CONDITION)
                + " and " //$NON-NLS-1$
                + JobSettingsConstants.addBrackets(JobSettingsConstants.getExtraParameterName(EParameterName.FROM_FILE_FLAG
                        .getName()) + " == 'true'"); //$NON-NLS-1$

        param.setShowIf(condition);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.FIELDSEPARATOR.getName());

        String value = ";"; //$NON-NLS-1$
        value = TalendTextUtils.addQuotes(value);

        param.setValue(value);
        param.setDisplayName(EParameterName.FIELDSEPARATOR.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setGroup(IMPLICIT_GROUP);
        param.setNumRow(32);
        condition = JobSettingsConstants.addBrackets(CONTEXTLOAD_CONDITION)
                + " and " //$NON-NLS-1$
                + JobSettingsConstants.addBrackets(JobSettingsConstants.getExtraParameterName(EParameterName.FROM_FILE_FLAG
                        .getName()) + " == 'true'"); //$NON-NLS-1$

        param.setShowIf(condition);
        paramList.add(param);

        // begin Override encoding checkbox
        param = new ElementParameter(process);
        param.setName(EParameterName.OVERRIDE_ENCODING_FLAG.getName());
        param.setDisplayName(EParameterName.OVERRIDE_ENCODING_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setGroup(IMPLICIT_GROUP);
        param.setNumRow(33);
        param.setValue(false);
        param.setShowIf(condition);
        paramList.add(param);
        // end Override encoding checkbox

        // begin encoding select
        ElementParameter encodingParam = new ElementParameter(process);
        encodingParam.setName(EParameterName.OVERRIDE_ENCODING_IN_EXTRA.getName());
        encodingParam.setDisplayName(EParameterName.OVERRIDE_ENCODING_IN_EXTRA.getDisplayName());
        encodingParam.setCategory(EComponentCategory.EXTRA);
        encodingParam.setGroup(IMPLICIT_GROUP);
        encodingParam.setFieldType(EParameterFieldType.ENCODING_TYPE);
        StringBuilder sb = new StringBuilder();
        sb.append(JobSettingsConstants.getExtraParameterName(EParameterName.FROM_FILE_FLAG.getName())).append(" == 'true' and ")
                .append(EParameterName.OVERRIDE_ENCODING_FLAG.getName()).append(" == 'true' and ").append(CONTEXTLOAD_CONDITION);
        condition = JobSettingsConstants.addBrackets(sb.toString());
        encodingParam.setShowIf(condition); // $NON-NLS-1$
        encodingParam.setValue(ENCODING_TYPE_ISO_8859_15);
        encodingParam.setNumRow(34);
        paramList.add(encodingParam);

        ElementParameter childParam = new ElementParameter(process);
        childParam.setName(EParameterName.ENCODING_TYPE.getName());
        childParam.setDisplayName(EParameterName.ENCODING_TYPE.getDisplayName());
        childParam.setFieldType(EParameterFieldType.TECHNICAL);
        childParam.setCategory(EComponentCategory.EXTRA);
        childParam.setGroup(IMPLICIT_GROUP);
        childParam.setListItemsDisplayName(new String[] { ENCODING_TYPE_ISO_8859_15, ENCODING_TYPE_UTF_8, ENCODING_TYPE_CUSTOM });
        childParam.setListItemsDisplayCodeName(
                new String[] { ENCODING_TYPE_ISO_8859_15, ENCODING_TYPE_UTF_8, ENCODING_TYPE_CUSTOM });
        childParam.setListItemsValue(new String[] { ENCODING_TYPE_ISO_8859_15, ENCODING_TYPE_UTF_8, ENCODING_TYPE_CUSTOM });
        childParam.setValue(ENCODING_TYPE_ISO_8859_15);
        childParam.setNumRow(34);
        childParam.setShow(true);
        childParam.setShowIf(condition); // $NON-NLS-1$
        childParam.setParentParameter(encodingParam);
        // end encoding select
        return paramList;
    }

    private static List<IElementParameter> getExtraOnDBParameters(IProcess process) {

        ElementParameter param;
        List<IElementParameter> paramList = new ArrayList<IElementParameter>();

        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();

        String languagePrefix = LanguageManager.getCurrentLanguage().toString() + "_"; //$NON-NLS-1$

        final String onDBCondition = JobSettingsConstants.getExtraParameterName(EParameterName.FROM_DATABASE_FLAG.getName())
                + " == 'true'"; //$NON-NLS-1$
        final String dbCondition = JobSettingsConstants.addBrackets(CONTEXTLOAD_CONDITION)
                + " and " + JobSettingsConstants.addBrackets(onDBCondition); //$NON-NLS-1$

        // property type
        ElementParameter parentPropertyType = new ElementParameter(process);
        parentPropertyType.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName()));
        parentPropertyType.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        parentPropertyType.setValue(""); //$NON-NLS-1$
        parentPropertyType.setCategory(EComponentCategory.EXTRA);
        parentPropertyType.setFieldType(EParameterFieldType.PROPERTY_TYPE);
        parentPropertyType.setRepositoryValue(ERepositoryCategoryType.DATABASE.getName());
        parentPropertyType.setNumRow(41);
        parentPropertyType.setShowIf(dbCondition);
        parentPropertyType.setGroup(IMPLICIT_GROUP);
        paramList.add(parentPropertyType);

        param = new ElementParameter(process);
        param.setName(EParameterName.PROPERTY_TYPE.getName());
        param.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        param.setListItemsDisplayName(new String[] { EmfComponent.TEXT_BUILTIN, EmfComponent.TEXT_REPOSITORY });
        param.setListItemsDisplayCodeName(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        param.setListItemsValue(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        param.setValue(EmfComponent.BUILTIN);
        param.setCategory(EComponentCategory.EXTRA);
        param.setFieldType(EParameterFieldType.TECHNICAL);
        param.setRepositoryValue(ERepositoryCategoryType.DATABASE.getName());
        param.setNumRow(41);
        param.setShowIf(dbCondition);
        param.setGroup(IMPLICIT_GROUP);
        param.setParentParameter(parentPropertyType);
        // paramList.add(param);

        // repository property type
        param = new ElementParameter(process);
        param.setName(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        param.setDisplayName(EParameterName.REPOSITORY_PROPERTY_TYPE.getDisplayName());
        param.setListItemsDisplayName(new String[] {});
        param.setListItemsValue(new String[] {});
        // param.setValue(""); //$NON-NLS-1$
        param.setCategory(EComponentCategory.EXTRA);
        param.setFieldType(EParameterFieldType.TECHNICAL);
        param.setShow(false);
        param.setRequired(true);
        param.setNumRow(41);
        param.setGroup(IMPLICIT_GROUP);
        param.setParentParameter(parentPropertyType);
        // paramList.add(param);

        // dbType
        final String dbTypeName = JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName());
        param = new ElementParameter(process);
        param.setName(dbTypeName);
        param.setDisplayName(EParameterName.DB_TYPE.getDisplayName());
        param.setFieldType(EParameterFieldType.CLOSED_LIST);
        param.setCategory(EComponentCategory.EXTRA);
        param.setListItemsDisplayName(StatsAndLogsConstants.DISPLAY_DBNAMES[1]);
        param.setListItemsValue(JobSettingsConstants.DB_INPUT_COMPONENTS[1]);
        param.setListRepositoryItems(StatsAndLogsConstants.REPOSITORY_ITEMS[1]);
        param.setListItemsDisplayCodeName(StatsAndLogsConstants.CODE_LIST[1]);
        param.setValue("");
        param.setNumRow(42);
        param.setRepositoryValue("TYPE"); //$NON-NLS-1$
        param.setRequired(true);
        param.setShowIf(dbCondition);
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // dbVersion
        final String dbVersionName = JobSettingsConstants.getExtraParameterName(EParameterName.DB_VERSION.getName());
        if (process.getElementParameter(dbVersionName) == null) {
            param = new ElementParameter(process);
            param.setName(dbVersionName);
            param.setValue(StatsAndLogsConstants.DB_VERSION_DRIVER[1]);
            param.setDisplayName(EParameterName.DB_VERSION.getDisplayName());
            param.setFieldType(EParameterFieldType.CLOSED_LIST);
            param.setCategory(EComponentCategory.EXTRA);
            param.setListItemsDisplayName(StatsAndLogsConstants.DB_VERSION_DISPLAY);
            param.setListItemsValue(StatsAndLogsConstants.DB_VERSION_DRIVER);
            param.setListItemsDisplayCodeName(StatsAndLogsConstants.DB_VERSION_CODE);
            param.setNumRow(42);
            param.setRepositoryValue("DB_VERSION"); //$NON-NLS-1$
            param.setRequired(true);
            param.setShowIf(dbCondition
                    + " and (" + dbTypeName + " == 'OCLE' or " + dbTypeName + " == 'SYBASE' or "+ dbTypeName + " == 'OCLE_OCI' or " + dbTypeName + " =='ACCESS' or " + dbTypeName + " =='MSSQL' or " + dbTypeName + " =='POSTGRESQL' or "+dbTypeName + " =='MYSQL') "); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$
            param.setGroup(IMPLICIT_GROUP);
            paramList.add(param);
        }

        // jdbc url
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.URL.getName()));
        param.setValue(StatsAndLogsManager.addQuotes(preferenceStore.getString(languagePrefix + EParameterName.URL.getName())));
        param.setDisplayName(EParameterName.URL.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(43);
        param.setRepositoryValue(EConnectionParameterName.GENERIC_URL.getDisplayName()); 
        String dbCon = dbTypeName + " == 'JDBC'";
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // jdbc child param
        if (moduleNameList == null) {
            Set<ModuleNeeded> moduleNeededList = ModulesNeededProvider.getModulesNeeded();
            moduleNameList = new ArrayList<String>();
            moduleValueList = new ArrayList<String>();
            for (ModuleNeeded module : moduleNeededList) {
                String moduleName = module.getModuleName();
                if (moduleName != null) {
                    if (!moduleNameList.contains(moduleName)) {
                        moduleNameList.add(moduleName);
                    }
                    String moduleValue = TalendTextUtils.addQuotes(moduleName);
                    if (!moduleValueList.contains(moduleValue)) {
                        moduleValueList.add(moduleValue);
                    }
                }
            }
            Comparator<String> comprarator = new IgnoreCaseComparator();
            Collections.sort(moduleNameList, comprarator);
            Collections.sort(moduleValueList, comprarator);
        }
        String[] moduleNameArray = moduleNameList.toArray(new String[0]);
        String[] moduleValueArray = moduleValueList.toArray(new String[0]);
        ElementParameter childParam = new ElementParameter(process);
        childParam.setName("drivers");
        childParam.setDisplayName("drivers");
        childParam.setFieldType(EParameterFieldType.MODULE_LIST);
        childParam.setListItemsDisplayName(moduleNameArray);
        childParam.setListItemsValue(moduleValueArray);
        // driver jar for jdbc
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DRIVER_JAR.getName()));
        param.setDisplayName(EParameterName.DRIVER_JAR.getDisplayName());
        param.setFieldType(EParameterFieldType.TABLE);
        param.setListItemsDisplayCodeName(new String[] { "drivers" });
        param.setListItemsDisplayName(new String[] { "drivers" });
        param.setListItemsValue(new ElementParameter[] { childParam });
        param.setValue(new ArrayList<Map<String, Object>>());
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(44);
        param.setRepositoryValue(EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName()); 
        dbCon = dbTypeName + " == 'JDBC'";
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // class name for jdbc
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DRIVER_CLASS.getName()));
        param.setValue(StatsAndLogsManager.addQuotes(preferenceStore.getString(languagePrefix
                + EParameterName.DRIVER_CLASS.getName())));
        param.setDisplayName(EParameterName.DRIVER_CLASS.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(45);
        param.setRepositoryValue(EConnectionParameterName.GENERIC_DRIVER_CLASS.getDisplayName()); 
        dbCon = dbTypeName + " == 'JDBC'";
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // host
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.HOST.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.HOST.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(46);
        param.setRepositoryValue("SERVER_NAME"); //$NON-NLS-1$
        dbCon = dbTypeName
                + " != 'SQLITE'" + " and " + dbTypeName + " != 'ACCESS'" + " and " + dbTypeName + "!='OCLE_OCI'" + " and " + dbTypeName + "!='JDBC'" + " and " + dbTypeName + "!='ODBC'"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // port
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PORT.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.PORT.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(46);
        param.setRepositoryValue("PORT"); //$NON-NLS-1$
        dbCon = dbTypeName
                + " != 'SQLITE'" + " and " + dbTypeName + " != 'ACCESS'" + " and " + dbTypeName + " != 'FIREBIRD'" + " and " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$//$NON-NLS-6$
                + dbTypeName + "!='OCLE_OCI'" + " and " + dbTypeName + "!='JDBC'" + " and " + dbTypeName + "!='ODBC'" + " and " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                + dbTypeName + "!='TERADATA'"; //$NON-NLS-1$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // dbName
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DBNAME.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.DBNAME.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(47);
        param.setRepositoryValue("SID"); //$NON-NLS-1$
        dbCon = dbTypeName
                + " != 'SQLITE'" + " and " + dbTypeName + " != 'ACCESS'" + " and " + dbTypeName + " != 'FIREBIRD'" + " and " + dbTypeName + "!='OCLE_OCI'" + " and " + dbTypeName + "!='JDBC'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$//$NON-NLS-6$//$NON-NLS-7$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // local service name
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.LOCAL_SERVICE_NAME.getName()));
        param.setValue(StatsAndLogsManager.addQuotes(""));
        param.setDisplayName(EParameterName.LOCAL_SERVICE_NAME.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(47);
        param.setRepositoryValue("SID"); //$NON-NLS-1$
        dbCon = dbTypeName + " =='OCLE_OCI' ";
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // additional parameters
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTIES.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.PROPERTIES.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(47);
        param.setRepositoryValue("PROPERTIES_STRING"); //$NON-NLS-1$
        dbCon = dbTypeName
                + " == 'MSSQL'" + " or " + dbTypeName + " == 'MYSQL'" + " or " + dbTypeName //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + " == 'INFORMIX'" + " or " + dbTypeName + " == 'OCLE'" + " or " + dbTypeName + " == 'OCLE_OCI'" + " or " + dbTypeName + " == 'SYBASE'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // schema
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.SCHEMA_DB.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.SCHEMA_DB.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(47);
        param.setRepositoryValue("SCHEMA"); //$NON-NLS-1$
        final String schemaCondition = JobSettingsConstants
                .addBrackets(dbTypeName
                        + " =='OCLE' or " + dbTypeName //$NON-NLS-1$
                        + " =='POSTGRESQL' or " + dbTypeName + " =='POSTGRESPLUS' or " + dbTypeName + " =='OCLE_OCI' or " + dbTypeName + " =='MSSQL' or " + dbTypeName + " =='INFORMIX' or " + dbTypeName + " =='IBM_DB2' or " + dbTypeName + " =='SYBASE'"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$//$NON-NLS-6$//$NON-NLS-7$
        param.setShowIf(schemaCondition + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // username
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.USER.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.USER.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(48);
        param.setRequired(true);
        param.setRepositoryValue("USERNAME"); //$NON-NLS-1$
        dbCon = dbTypeName + " != 'SQLITE'"; //$NON-NLS-1$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // password
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PASS.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.PASS.getDisplayName());
        param.setFieldType(EParameterFieldType.PASSWORD);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(48);
        param.setRequired(true);
        param.setRepositoryValue("PASSWORD"); //$NON-NLS-1$
        dbCon = dbTypeName + " != 'SQLITE'"; //$NON-NLS-1$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);
        // databse file path
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DBFILE.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.DBFILE.getDisplayName());
        param.setFieldType(EParameterFieldType.FILE);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(49);
        param.setRepositoryValue("FILE"); //$NON-NLS-1$
        dbCon = dbTypeName + " == 'SQLITE'" + " or " + dbTypeName + " == 'ACCESS'" + " or " + dbTypeName + " == 'FIREBIRD'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // table
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DBTABLE.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.DBTABLE.getDisplayName());
        param.setFieldType(EParameterFieldType.DBTABLE);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(50);
        param.setShowIf(dbCondition);
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // query condition
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.QUERY_CONDITION.getName()));
        param.setValue(QUOTE + QUOTE);
        param.setDisplayName(EParameterName.QUERY_CONDITION.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(51);
        param.setShowIf(dbCondition);
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        for (IElementParameter currentParam : paramList) {
            if (currentParam.getRepositoryValue() != null) {
                // if any of the parameter of stat&logs is using repository, then force to link it to the name of the
                // property for implicit
                currentParam.setRepositoryProperty(parentPropertyType.getName());
            }
        }

        return paramList;
    }

    private final static class IgnoreCaseComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    }

    private static List<IElementParameter> getExtraContextLoadParameters(IProcess process) {
        ElementParameter param;
        List<IElementParameter> paramList = new ArrayList<IElementParameter>();

        final String[] itemValues = new String[] { ContextLoadInfo.ERROR.getDisplayName(),
                ContextLoadInfo.WARNING.getDisplayName(), ContextLoadInfo.INFO.getDisplayName() };

        //
        param = new ElementParameter(process);
        param.setName(EParameterName.LOAD_NEW_VARIABLE.getName());
        param.setDisplayName(EParameterName.LOAD_NEW_VARIABLE.getDisplayName());
        param.setValue(ContextLoadInfo.WARNING.getDisplayName());
        param.setListItemsDisplayName(itemValues);
        param.setListItemsValue(itemValues);
        param.setFieldType(EParameterFieldType.CLOSED_LIST);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(80);
        param.setShowIf(CONTEXTLOAD_CONDITION);
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);
        //
        param = new ElementParameter(process);
        param.setName(EParameterName.NOT_LOAD_OLD_VARIABLE.getName());
        param.setDisplayName(EParameterName.NOT_LOAD_OLD_VARIABLE.getDisplayName());
        param.setValue(ContextLoadInfo.WARNING.getDisplayName());
        param.setListItemsDisplayName(itemValues);
        param.setListItemsValue(itemValues);
        param.setFieldType(EParameterFieldType.CLOSED_LIST);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(81);
        param.setShowIf(CONTEXTLOAD_CONDITION);
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);
        // print operations
        param = new ElementParameter(process);
        param.setName(EParameterName.PRINT_OPERATIONS.getName());
        param.setValue(false);
        param.setDisplayName(EParameterName.PRINT_OPERATIONS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(82);
        param.setRequired(true);
        param.setShowIf("((PRINT_OPERATIONS == 'true' or PRINT_OPERATIONS == 'false') and " + CONTEXTLOAD_CONDITION + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // disable error
        param = new ElementParameter(process);
        param.setName(EParameterName.DISABLE_ERROR.getName());
        param.setValue(false);
        param.setDisplayName(EParameterName.DISABLE_ERROR.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(83);
        param.setRequired(true);
        param.setShowIf("((DISABLE_ERROR == 'true' or DISABLE_ERROR == 'false') and " + CONTEXTLOAD_CONDITION + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // disable warnings
        param = new ElementParameter(process);
        param.setName(EParameterName.DISABLE_WARNINGS.getName());
        param.setValue(true);
        param.setDisplayName(EParameterName.DISABLE_WARNINGS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(83);
        param.setRequired(true);
        param.setShowIf("((DISABLE_WARNINGS == 'true' or DISABLE_WARNINGS == 'false') and " + CONTEXTLOAD_CONDITION + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // disable info
        param = new ElementParameter(process);
        param.setName(EParameterName.DISABLE_INFO.getName());
        param.setValue(true);
        param.setDisplayName(EParameterName.DISABLE_INFO.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(83);
        param.setRequired(true);
        param.setShowIf("((DISABLE_INFO == 'true' or DISABLE_INFO == 'false') and " + CONTEXTLOAD_CONDITION + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        return paramList;
    }

    /**
     *
     * DOC ggu Comment method "isStatsAndLogsActivated".
     *
     * for stats & logs settings
     */
    public static boolean isStatsAndLogsActivated(IProcess process) {
        return StatsAndLogsManager.isStatsAndLogsActivated(process);
    }

    public static List<DataNode> createStatsAndLogsNodes(IProcess process) {
        return StatsAndLogsManager.getStatsAndLogsNodes(process);
    }

    /**
     *
     * DOC ggu Comment method "isImplicittContextLoadActived".
     *
     * for implictit tContextLoad in extra settings
     */
    public static boolean isImplicittContextLoadActived(IProcess process) {
        String paramName = EParameterName.IMPLICIT_TCONTEXTLOAD.getName();
        if (process.getElementParameter(paramName) == null) {
            return false;
        }
        boolean useContextLoad = ((Boolean) process.getElementParameter(paramName).getValue())
                && process.getElementParameter(paramName).isShow(process.getElementParameters());
        if (!useContextLoad) {
            // not used
            return false;
        }

        // file
        paramName = JobSettingsConstants.getExtraParameterName(EParameterName.FROM_FILE_FLAG.getName());
        boolean fileFlag = ((Boolean) process.getElementParameter(paramName).getValue())
                && process.getElementParameter(paramName).isShow(process.getElementParameters());

        // db
        String dbInput = null;
        paramName = JobSettingsConstants.getExtraParameterName(EParameterName.FROM_DATABASE_FLAG.getName());
        boolean dbFlag = ((Boolean) process.getElementParameter(paramName).getValue())
                && process.getElementParameter(paramName).isShow(process.getElementParameters());
        if (!dbFlag) {
            dbInput = null;
        } else {
            dbInput = (String) process.getElementParameter(
                    JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName())).getValue();
            if (dbInput == null || dbInput.equals("")) { //$NON-NLS-1$
                dbInput = null;
                dbFlag = false;
            }
        }

        if (!fileFlag && !dbFlag) {
            // not used
            return false;
        }

        return true;
    }

    public static List<DataNode> createExtraContextLoadNodes(IProcess process) {
        List<DataNode> nodeList = new ArrayList<DataNode>();

        String paramName = EParameterName.IMPLICIT_TCONTEXTLOAD.getName();
        boolean useContextLoad = ((Boolean) process.getElementParameter(paramName).getValue())
                && process.getElementParameter(paramName).isShow(process.getElementParameters());
        if (!useContextLoad) {
            // not used
            return Collections.emptyList();
        }

        // file
        paramName = JobSettingsConstants.getExtraParameterName(EParameterName.FROM_FILE_FLAG.getName());
        boolean fileFlag = ((Boolean) process.getElementParameter(paramName).getValue())
                && process.getElementParameter(paramName).isShow(process.getElementParameters());

        // db
        String dbInput = null;
        paramName = JobSettingsConstants.getExtraParameterName(EParameterName.FROM_DATABASE_FLAG.getName());
        boolean dbFlag = ((Boolean) process.getElementParameter(paramName).getValue())
                && process.getElementParameter(paramName).isShow(process.getElementParameters());
        if (!dbFlag) {
            dbInput = null;
        } else {

            dbInput = (String) process.getElementParameter(
                    JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName())).getValue();
            dbInput = OracleComponentHelper.filterOracleComponentName(dbInput);
            if (dbInput == null || dbInput.equals("")) { //$NON-NLS-1$
                dbInput = null;
                dbFlag = false;
            }
        }

        if (!fileFlag && !dbFlag) {
            // not used
            return Collections.emptyList();
        }

        IComponent tContextLoadComponent = new JobContextLoadComponent(fileFlag, dbInput, process.getComponentsType());

        final String uniqueName = "Implicit_Context"; //$NON-NLS-1$
        DataNode tContextLoadNode = new DataNode(tContextLoadComponent, uniqueName);
        tContextLoadNode.setStart(true);
        tContextLoadNode.setSubProcessStart(true);
        tContextLoadNode.setActivate(true);

        IMetadataTable table = getSchemaTablefromComponent(JobContextLoadComponent.CONTEXTLOAD_COMPONENT, uniqueName);
        if (table != null) {
            tContextLoadNode.getMetadataList().clear();
            tContextLoadNode.getMetadataList().add(table);

        }
        // set parameters
        IElementParameter param = null;
        if (fileFlag) {
            // is file
            String inputFile = (String) process.getElementParameter(EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getName())
                    .getValue();
            String fileSparator = (String) process.getElementParameter(EParameterName.FIELDSEPARATOR.getName()).getValue();
            tContextLoadNode.getElementParameter(EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getName()).setValue(inputFile);
            String regex = FileSeparator.getSeparatorsRegexp(fileSparator);
            tContextLoadNode.getElementParameter(JobSettingsConstants.IMPLICIT_TCONTEXTLOAD_REGEX).setValue(regex);

            String encoding = (String) process.getElementParameter(EParameterName.OVERRIDE_ENCODING_IN_EXTRA.getName())
                    .getValue();
            if (StringUtils.isNotEmpty(encoding) && !encoding.startsWith(TalendTextUtils.getQuoteChar())) {
                encoding = TalendTextUtils.addQuotes(encoding);
            }

            // override encoding
            paramName = EParameterName.OVERRIDE_ENCODING_FLAG.getName();
            boolean overrideFlag = ((Boolean) process.getElementParameter(paramName).getValue())
                    && process.getElementParameter(paramName).isShow(process.getElementParameters());
            if (overrideFlag) {
                IElementParameter encodingParam = new ElementParameter(tContextLoadNode);
                encodingParam.setName(EParameterName.ENCODING.getName());
                encodingParam.setDisplayName(EParameterName.ENCODING.getDisplayName());
                encodingParam.setFieldType(EParameterFieldType.ENCODING_TYPE);
                encodingParam.setValue(encoding);
                tContextLoadNode.addElementParameter(encodingParam);
            }
        } else {
            // is db
            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.URL.getName());
            param = process.getElementParameter(paramName);
            if (param != null) {
                tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
            }
            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.DRIVER_JAR.getName());
            param = process.getElementParameter(paramName);
            if (param != null) {
                tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
            }

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.DRIVER_CLASS.getName());
            param = process.getElementParameter(paramName);
            if (param != null) {
                tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
            }

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.HOST.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.PORT.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.DBNAME.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.DB_VERSION.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTIES.getName());
            param = process.getElementParameter(paramName);
            if (param != null) {
                tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
            }

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.SCHEMA_DB.getName());
            param = process.getElementParameter(paramName);
            if (param != null) {
                tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
            }
            String schema = (String) process.getElementParameter(paramName).getValue();
            if (schema != null) {
                schema = TalendTextUtils.removeQuotes(schema);
            }

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.USER.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.PASS.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.CONNECTION_TYPE.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(
                    OracleComponentHelper.filterOracleConnectionType((String) process.getElementParameter(
                            JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName())).getValue()));

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.DBTABLE.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

            // query
            String dbTableName = (String) process.getElementParameter(paramName).getValue();
            String realTableName = getCurrentTableName(dbTableName);
            if (realTableName == null) {
                realTableName = QueryUtil.DEFAULT_TABLE_NAME;
            }

            String dbType = getDatabaseTypeFromParameter(process);
            if (dbType != null) {
                // TDI-18161:the SQL script's syntax is not right because of the implicit context of General JDBC.
                if (dbType.equals(EDatabaseTypeName.GENERAL_JDBC.getDisplayName())
                        || dbType.equals(EDatabaseTypeName.GENERAL_JDBC.getProduct())) {
                    dbType = findRealDbTypeForJDBC(process, dbType);
                }

                EDatabaseTypeName dbTypeName = EDatabaseTypeName.getTypeFromDbType(dbType);
                if (EDatabaseTypeName.ORACLE_OCI.equals(dbTypeName) || EDatabaseTypeName.ORACLEFORSID.equals(dbTypeName)
                        || EDatabaseTypeName.ORACLESN.equals(dbTypeName)) {
                    for (IMetadataColumn column : table.getListColumns()) {
                        column.setOriginalDbColumnName(column.getOriginalDbColumnName().toUpperCase());
                    }
                }
                if (realTableName.startsWith(TalendTextUtils.QUOTATION_MARK)
                        && realTableName.endsWith(TalendTextUtils.QUOTATION_MARK) && realTableName.length() > 2) {
                    realTableName = realTableName.substring(1, realTableName.length() - 1);
                }
                String query = TalendTextUtils.addSQLQuotes(QueryUtil
                        .generateNewQuery(null, table, dbType, schema, realTableName));
                paramName = JobSettingsConstants.getExtraParameterName(EParameterName.QUERY_CONDITION.getName());
                String conditionStatement = (String) process.getElementParameter(paramName).getValue();
                if (conditionStatement != null) {
                    String tmp = TalendTextUtils.removeQuotes(conditionStatement);
                    if (!"".equals(tmp)) { //$NON-NLS-1$
                        query = query + CONNECTOR + QUOTE + " WHERE " + QUOTE + CONNECTOR + conditionStatement; //$NON-NLS-1$
                    }
                }
                final String quoteByDBType = TalendTextUtils.getQuoteByDBType(dbType, false);
                if (dbTypeName == EDatabaseTypeName.MSSQL) {
                    query = query.replaceAll("(?i)\bkey\b", //$NON-NLS-1$
                            "\\\\" + quoteByDBType + "key\\\\" + quoteByDBType); //$NON-NLS-1$  //$NON-NLS-2$
                }
                tContextLoadNode.getElementParameter(JobSettingsConstants.QUERY).setValue(query);
            }
        }
        // tContextLoad
        paramName = EParameterName.LOAD_NEW_VARIABLE.getName();
        param = process.getElementParameter(paramName);
        if (param != null) {
            tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
        }

        paramName = EParameterName.NOT_LOAD_OLD_VARIABLE.getName();
        param = process.getElementParameter(paramName);
        if (param != null) {
            tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
        }

        paramName = EParameterName.PRINT_OPERATIONS.getName();
        tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

        paramName = EParameterName.DISABLE_ERROR.getName();
        param = process.getElementParameter(paramName);
        if (param != null) {
            tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
        }

        paramName = EParameterName.DISABLE_INFO.getName();
        param = process.getElementParameter(paramName);
        if (param != null) {
            tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
        }

        paramName = EParameterName.DISABLE_WARNINGS.getName();
        tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

        tContextLoadNode.setProcess(process);
        nodeList.add(tContextLoadNode);

        return nodeList;
    }

    private static IMetadataTable getSchemaTablefromComponent(final String componentName, final String tableName) {
        IComponent tmpComponent = ComponentsFactoryProvider.getInstance().get(componentName,
                ComponentCategory.CATEGORY_4_DI.getName());

        DataNode tmpNode = new DataNode(tmpComponent, "tmp"); //$NON-NLS-1$
        for (int k = 0; k < tmpNode.getElementParameters().size(); k++) {
            IElementParameter currentParam = tmpNode.getElementParameters().get(k);
            if (currentParam.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                    || currentParam.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)) {
                Object value = currentParam.getValue();
                if (value instanceof IMetadataTable) {
                    IMetadataTable table = null;
                    if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
                        table = ((IMetadataTable) value).clone(true);
                    } else {
                        table = ((IMetadataTable) value).clone();
                    }
                    table.setTableName(tableName);
                    table.setAttachedConnector(currentParam.getContext());
                    // fix for TUP-3972: set defult value
                    for (IMetadataColumn column : table.getListColumns()) {
                        column.setDefault("\"\"");
                    }
                    return table;
                }

            }
        }
        return null;
    }

    private static String getCurrentTableName(String dbTableName) {
        String currentTableName = null;
        if (dbTableName == null) {
            dbTableName = QueryUtil.DEFAULT_TABLE_NAME;
        } else {
            switch (LanguageManager.getCurrentLanguage()) {
            case JAVA:
                if (dbTableName.contains(TalendTextUtils.QUOTATION_MARK)) {
                    if (dbTableName.startsWith(TalendTextUtils.QUOTATION_MARK)
                            && dbTableName.endsWith(TalendTextUtils.QUOTATION_MARK) && dbTableName.length() > 2) {
                        currentTableName = dbTableName.substring(1, dbTableName.length() - 1);
                    } else {
                        currentTableName = null;
                    }
                } else {
                    currentTableName = dbTableName;
                }
                break;
            default:
                if (dbTableName.contains(TalendTextUtils.SINGLE_QUOTE)) {
                    if (dbTableName.startsWith(TalendTextUtils.SINGLE_QUOTE)
                            && dbTableName.endsWith(TalendTextUtils.SINGLE_QUOTE) && dbTableName.length() > 2) {
                        currentTableName = dbTableName.substring(1, dbTableName.length() - 1);
                    } else {
                        currentTableName = null;
                    }
                } else {
                    currentTableName = dbTableName;
                }
            }
        }
        return currentTableName;
    }

    private static String getDatabaseTypeFromParameter(IProcess process) {
        String paramName = JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName());
        IElementParameter param = process.getElementParameter(paramName);
        String value = (String) param.getValue();

        if (value != null && param.getName().equals(JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName()))) {
            for (int i = 0; i < param.getListItemsValue().length; i++) {
                Object obj = param.getListItemsValue()[i];
                String itemValue = (String) obj;
                if (itemValue.equals(value)) {
                    return param.getListItemsDisplayName()[i];
                }
            }
        }
        return null;
    }

    private static String findRealDbTypeForJDBC(IProcess process, String originalDbType) {
        String realDbTypeForJDBC = null;
        String driverJarValue = process
                .getElementParameter(JobSettingsConstants.getExtraParameterName(EParameterName.DRIVER_JAR.getName())).getValue()
                .toString();
        String driverClassValue = process
                .getElementParameter(JobSettingsConstants.getExtraParameterName(EParameterName.DRIVER_CLASS.getName()))
                .getValue().toString();

        driverClassValue = TalendTextUtils.removeQuotes(driverClassValue);
        if (driverClassValue != null && !"".equals(driverClassValue)) {
            boolean isContextModeDriverClass = ContextParameterUtils.containContextVariables(driverClassValue);
            if (isContextModeDriverClass) {
                driverClassValue = JavaProcessUtil.getContextOriginalValue(process, driverClassValue);
            }
        }

        if (driverJarValue != null && driverJarValue.startsWith("[") && driverJarValue.endsWith("]")) {
            driverJarValue = driverJarValue.substring(1, driverJarValue.length() - 1);
            if (driverJarValue != null && driverJarValue.startsWith("{") && driverJarValue.endsWith("}")) {
                driverJarValue = driverJarValue.substring(1, driverJarValue.length() - 1);
            }
        }
        if (driverJarValue != null && !"".equals(driverJarValue)) {
            boolean isContextMode = ContextParameterUtils.containContextVariables(driverJarValue);
            if (isContextMode) {
                driverJarValue = JavaProcessUtil.getContextOriginalValue(process, driverJarValue);
            }
            realDbTypeForJDBC = ExtractMetaDataUtils.getInstance().getDbTypeByClassNameAndDriverJar(driverClassValue,
                    driverJarValue);
        } else {
            realDbTypeForJDBC = ExtractMetaDataUtils.getInstance().getDbTypeByClassName(driverClassValue);
        }
        return realDbTypeForJDBC;
    }

    public static class FileSeparator {

        private static List<String> METADATA_CHAR = getMetadataChars();

        static String doRegexpQuote(String separators) {
            if (StringUtils.isEmpty(separators)) {
                return TalendQuoteUtils.addQuotes("");
            }

            String[] splits = separators.split("\\+");
            String[] seqs = new String[splits.length];
            int posit = 0;
            for (String split : splits) {
                String seq = split.trim();
                // don't use seq, might trim space value
                if (seq.length() > 1 && (seq.startsWith("\"") && seq.endsWith("\"")) || seq.startsWith("context.")) {
                    seqs[posit] = split;
                    posit++;
                } else {
                    if (StringUtils.isBlank(seqs[posit])) {
                        seqs[posit] = split;
                    } else {
                        String original = seqs[posit];
                        seqs[posit] = original + "+" + split;
                    }
                }
            }

            StringBuffer seqBuffer = new StringBuffer();
            for (int i = 0; i < seqs.length; i++) {
                if (seqs[i] == null) {
                    continue;
                }
                if (i != 0) {
                    seqBuffer.append("+");
                }
                if (seqs[i].contains("context.")) {
                    seqBuffer.append(seqs[i]);
                } else {
                    seqBuffer.append(doAddMark4SpecialChar(seqs[i]));
                }
            }

            return seqBuffer.toString();
        }

        static String doAddMark4SpecialChar(String sequence) {
            String filedSeparator = TalendQuoteUtils.removeQuotes(sequence);
            for (String charStr : METADATA_CHAR) {
                if (!filedSeparator.contains(charStr)) {
                    continue;
                }
                if("\\".equals(charStr)) {
                    filedSeparator = addMarkWithChar(filedSeparator, charStr, "\\\\\\", true); //$NON-NLS-1$
                }else {
                    filedSeparator = addMarkWithChar(filedSeparator, charStr, "\\\\", true); //$NON-NLS-1$
                }
            }
            filedSeparator = TalendQuoteUtils.addQuotes(filedSeparator);
            return filedSeparator;
        }

        static String getSeparatorsRegexp(String fileSeparator) {
            fileSeparator = doRegexpQuote(fileSeparator);
            return "\"^([^\"+" + fileSeparator + "+\"]*)\"+" + fileSeparator + "+\"(.*)$\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        static String addMarkWithChar(String separatorStr, String charStr, String markStr, boolean beforeChar) {
            String resultStr = "";
            char[] charArray = separatorStr.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                char c = charArray[i];
                if (charStr.equals(String.valueOf(c))) {
                    if (beforeChar) {
                        resultStr = resultStr + markStr + c;
                    } else {
                        resultStr = resultStr + c + markStr;
                    }
                } else {
                    resultStr += c;
                }
            }
            return resultStr;
        }

        private static List<String> getMetadataChars() {
            String[] metaChars = new String[] { "\\", "^", "$", ".", "?", "|", "[", "+", "*", "{", "(", ")", "}", "]", "\"" };
            return Arrays.asList(metaChars);
        }
    }
}


