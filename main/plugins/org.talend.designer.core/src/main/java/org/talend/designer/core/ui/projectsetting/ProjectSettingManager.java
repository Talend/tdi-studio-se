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
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ImplicitContextSettings;
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
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants.ContextLoadInfo;
import org.talend.designer.core.model.process.statsandlogs.StatsAndLogsManager;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.preferences.StatsAndLogsConstants;
import org.talend.designer.core.ui.views.jobsettings.ExtraComposite;
import org.talend.designer.core.ui.views.jobsettings.ImplicitContextLoadHelper;
import org.talend.designer.core.ui.views.statsandlogs.StatsAndLogsComposite;
import org.talend.librariesmanager.model.ModulesNeededProvider;
import org.talend.repository.ProjectManager;
import org.talend.repository.UpdateRepositoryUtils;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC aimingchen class global comment. Detailled comment
 */
public class ProjectSettingManager extends Utils {

    private static final String IMPLICIT_GROUP = "IMPLICIT_GROUP"; //$NON-NLS-1$

    // public static final String CONNECTION_ITEM_LABEL = "_CONNECTION_ITEM_LABEL";

    private static final String CONTEXTLOAD_CONDITION = EParameterName.IMPLICIT_TCONTEXTLOAD.getName() + " == 'true'"; //$NON-NLS-1$

    private static IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();

    private static String languagePrefix = LanguageManager.getCurrentLanguage().toString() + "_"; //$NON-NLS-1$

    private static final String ENCODING_TYPE_UTF_8 = "UTF-8"; //$NON-NLS-1$

    private static final String ENCODING_TYPE_ISO_8859_15 = "ISO-8859-15"; //$NON-NLS-1$

    private static final String ENCODING_TYPE_CUSTOM = "CUSTOM"; //$NON-NLS-1$

    public static void saveProject() {
        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                Context.REPOSITORY_CONTEXT_KEY);
        IProxyRepositoryFactory prf = CorePlugin.getDefault().getProxyRepositoryFactory();
        try {
            prf.saveProject(repositoryContext.getProject());
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     *
     * Load ImplicitContextLoad Preference setting to Project only Load Once
     *
     * @param pro
     */
    private static void loadImplicitContextLoadPreferenceToProject(Project pro) {

        TalendFileFactory talendF = TalendFileFactory.eINSTANCE;

        ImplicitContextSettings implicit = PropertiesFactory.eINSTANCE.createImplicitContextSettings();
        pro.getEmfProject().setImplicitContextSettings(implicit);
        implicit.setParameters(talendF.createParametersType());

        ParametersType pType = implicit.getParameters();
        IElement elem = pro.getInitialContextLoad();
        if (elem == null) {
            elem = new ImplicitContextLoadElement();
            ProjectSettingManager.createImplicitContextLoadParameters((ImplicitContextLoadElement) elem);
            pro.setInitialContextLoad(elem);
        }
        ElementParameter2ParameterType.saveElementParameters((Element) elem, pType);
    }

    /**
     *
     * create implicitContextLoad Element for project
     *
     * @param pro
     * @return
     */
    public static Element createImplicitContextLoadElement(Project pro) {
        ImplicitContextSettings implicit = pro.getEmfProject().getImplicitContextSettings();
        if (implicit == null) {
            loadImplicitContextLoadPreferenceToProject(pro);
        }
        Element elem = (Element) pro.getInitialContextLoad();
        if (elem == null) {
            elem = new ImplicitContextLoadElement();
            ProjectSettingManager.createImplicitContextLoadParameters(elem);
            pro.setInitialContextLoad(elem);
        }
        return elem;
    }

    /**
     *
     * create StatsAndLogsElement for project
     *
     * @param pro
     * @return
     */
    public static Element createStatsAndLogsElement(Project pro) {
        StatAndLogsSettings stats = pro.getEmfProject().getStatAndLogsSettings();
        // load Project's StatsAndLogsSetting to StatsAndLogsElement
        if (stats == null) {
            StatsAndLogsHelper.loadPreferenceToProject(pro);
        } else {
            // 10927
            if (stats.getParameters() == null) {
                stats.setParameters(TalendFileFactory.eINSTANCE.createParametersType());
            }

        }
        Element elem = (Element) pro.getStatsAndLog();
        if (elem == null) {
            elem = new StatsAndLogsElement();
            StatsAndLogsHelper.createStatsAndLogsParameters(elem);
            pro.setStatsAndLog(elem);
        }
        return elem;
    }

    public static void saveStatsAndLogToProjectSettings(Element process, Project pro) {
        createStatsAndLogsElement(pro);
        ParametersType stats = pro.getEmfProject().getStatAndLogsSettings().getParameters();
        // load the project settings to process
        ElementParameter2ParameterType.saveElementParameters(process, stats);
        saveProject();
    }

    public static void saveImplicitValuesToProjectSettings(Element process, Project pro) {
        createImplicitContextLoadElement(pro);
        ParametersType implicitType = pro.getEmfProject().getImplicitContextSettings().getParameters();
        // load the project settings to process
        ElementParameter2ParameterType.saveElementParameters(process, implicitType);
        saveProject();
    }

    public static void reloadStatsAndLogFromProjectSettings(Element process, Project pro, StatsAndLogsComposite statsComposite) {
        Element elem = createStatsAndLogsElement(pro);
        ParametersType stats = pro.getEmfProject().getStatAndLogsSettings().getParameters();
        ElementParameterType eleType = ElementParameter2ParameterType.findElementParameterType(stats,
                EParameterName.PROPERTY_TYPE.getName() + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName()); //$NON-NLS-1$
        if (eleType != null) {
            String value = eleType.getValue();
            if (value != null && !"".equals(value) && !RepositoryNode.NO_ID.equals(value)) { //$NON-NLS-1$
                ElementParameterType propertyType = ElementParameter2ParameterType.findElementParameterType(stats,
                        EParameterName.PROPERTY_TYPE.getName() + ":" + EParameterName.PROPERTY_TYPE.getName()); //$NON-NLS-1$
                if (propertyType != null) {
                    String propertyTypeValue = propertyType.getValue();
                    if (propertyTypeValue != null && !"".equals(propertyTypeValue)//$NON-NLS-1$
                            && !EmfComponent.BUILTIN.equals(propertyTypeValue)) {
                        IRepositoryViewObject lastVersion = UpdateRepositoryUtils.getRepositoryObjectById(value);
                        if (lastVersion == null || lastVersion.getProperty() == null
                                || lastVersion.getProperty().getItem() == null) {
                            ChangeValuesFromRepository changeValuesFromRepository = new ChangeValuesFromRepository(elem, null,
                                    EParameterName.PROPERTY_TYPE.getName() + ":" //$NON-NLS-1$
                                            + EParameterName.PROPERTY_TYPE.getName(), EmfComponent.BUILTIN);
                            changeValuesFromRepository.execute();
                            saveStatsAndLogToProjectSettings(elem, pro);
                        }
                    }
                }
            }
            // load the project settings to process
            ElementParameter2ParameterType.loadElementParameters(process, stats, EParameterName.PROPERTY_TYPE.getName() + ":" //$NON-NLS-1$
                    + EParameterName.PROPERTY_TYPE.getName());
            // change repository item
            // TODO
            // StatsAndLogsHelper.changeRepositoryConnection(process, statsComposite);
        }
    }

    public static void reloadStatsAndLogFromProjectSettings(ParametersType processType, Project pro) {
        createStatsAndLogsElement(pro);
        ParametersType stats = pro.getEmfProject().getStatAndLogsSettings().getParameters();
        ElementParameter2ParameterType.loadElementParameters(processType, stats, EParameterName.STATANDLOG_USE_PROJECT_SETTINGS);
    }

    public static void reloadImplicitValuesFromProjectSettings(Element process, Project pro, ExtraComposite extraComposite) {

        createImplicitContextLoadElement(pro);
        ParametersType implicitType = pro.getEmfProject().getImplicitContextSettings().getParameters();

        // load the project settings to process
        ElementParameter2ParameterType.loadElementParameters(process, implicitType,
                JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName()) + ":"
                        + EParameterName.PROPERTY_TYPE.getName());
        // TODO
        // changeImplicitContextRepositoryItem(process, extraComposite);
    }

    public static void reloadImplicitValuesFromProjectSettings(ParametersType processType, Project pro) {
        createImplicitContextLoadElement(pro);
        ParametersType implicitType = pro.getEmfProject().getImplicitContextSettings().getParameters();
        ElementParameter2ParameterType.loadElementParameters(processType, implicitType,
                EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS);
    }

    static void changeImplicitContextRepositoryItem(Element process, ExtraComposite extraComposite) {
        // change repository item
        String propertyType = (String) ElementParameter2ParameterType.getParameterValue(process,
                JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName()));

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

    /**
     *
     * when create a new job default use project settings
     *
     * @param pItem
     */
    public static void defaultUseProjectSetting(org.talend.designer.core.ui.editor.process.Process process) {
        if (process == null) {
            return;
        }
        ImplicitContextSettings implicit = ProjectManager.getInstance().getCurrentProject().getEmfProject()
                .getImplicitContextSettings();
        Boolean bImplicit = false;
        if (implicit != null) {
            String v = ElementParameter2ParameterType.getParameterValue(implicit.getParameters(),
                    EParameterName.IMPLICT_DEFAULT_PROJECTSETTING.getName());
            if (v != null && Boolean.valueOf(v)) {
                bImplicit = Boolean.valueOf(v);
            }
        }
        if (bImplicit) {
            ElementParameter2ParameterType.setParameterValue(process,
                    EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName(), bImplicit);
            reloadImplicitValuesFromProjectSettings(process, ProjectManager.getInstance().getCurrentProject(), null);

        }
        // stat and log
        StatAndLogsSettings stat = ProjectManager.getInstance().getCurrentProject().getEmfProject().getStatAndLogsSettings();
        Boolean bStat = false;
        if (stat != null) {
            String v = ElementParameter2ParameterType.getParameterValue(stat.getParameters(),
                    EParameterName.STATS_DEFAULT_PROJECTSETTING.getName());
            if (v != null || Boolean.valueOf(v)) {
                bStat = Boolean.valueOf(v);
            }
        }
        if (bStat) {
            ElementParameter2ParameterType.setParameterValue(process, EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName(),
                    bStat);
            reloadStatsAndLogFromProjectSettings(process, ProjectManager.getInstance().getCurrentProject(), null);
        }
    }

    /**
     *
     * create parameter for ImplicitContextLoad.
     */
    static void createImplicitContextLoadParameters(Element elem) {
        ElementParameter param;
        List<IElementParameter> paramList = (List<IElementParameter>) elem.getElementParameters();

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
        param.setName(EParameterName.IMPLICIT_TCONTEXTLOAD.getName());
        param.setValue(preferenceStore.getBoolean(getPreferenceName(EParameterName.IMPLICIT_TCONTEXTLOAD)));
        param.setGroupDisplayName(EParameterName.IMPLICIT_TCONTEXTLOAD.getDisplayName());
        param.setDisplayName(EParameterName.IMPLICIT_TCONTEXTLOAD.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setGroup(IMPLICIT_GROUP);
        param.setNumRow(3);
        param.setShow(true);
        paramList.add(param);

        param = new ElementParameter(elem);
        param.setName(EParameterName.IMPLICT_DEFAULT_PROJECTSETTING.getName());
        param.setValue(Boolean.TRUE);
        param.setGroupDisplayName(EParameterName.IMPLICT_DEFAULT_PROJECTSETTING.getDisplayName());
        param.setDisplayName(EParameterName.IMPLICT_DEFAULT_PROJECTSETTING.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setGroup(IMPLICIT_GROUP);
        param.setNumRow(3);
        param.setShow(false);
        paramList.add(param);

        // on files
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.FROM_FILE_FLAG.getName()));
        param.setValue(preferenceStore.getBoolean(getPreferenceName(EParameterName.FROM_FILE_FLAG)));
        param.setDisplayName(EParameterName.FROM_FILE_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.RADIO);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(4);
        param.setShowIf(JobSettingsConstants.addBrackets(CONTEXTLOAD_CONDITION));
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // on database
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.FROM_DATABASE_FLAG.getName()));
        param.setValue(preferenceStore.getBoolean(getPreferenceName(EParameterName.FROM_DATABASE_FLAG)));
        param.setDisplayName(EParameterName.FROM_DATABASE_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.RADIO);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(5);
        param.setShowIf(JobSettingsConstants.addBrackets(CONTEXTLOAD_CONDITION));
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // on file
        createExtraOnFileParameters(elem);
        // on database
        createExtraOnDBParameters(elem);
        // tContextLoad
        createExtraContextLoadParameters(elem);

    }

    private static void createExtraOnFileParameters(Element elem) {
        ElementParameter param;
        List<IElementParameter> paramList = (List<IElementParameter>) elem.getElementParameters();
        // // set Implicit tContextLoad file
        //        String fileName = ElementParameterParser.parse(elem, "__COMP_DEFAULT_FILE_DIR__/in.csv"); //$NON-NLS-1$
        // IPath path = Path.fromOSString(fileName);
        // fileName = TalendTextUtils.addQuotes(path.toPortableString());
        // set Implicit tContextLoad file
        param = new ElementParameter(elem);
        param.setName(EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getName());
        param.setValue(addQuotes(replaceSlash(preferenceStore
                .getString(getPreferenceName(EParameterName.IMPLICIT_TCONTEXTLOAD_FILE)))));
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

        param = new ElementParameter(elem);
        param.setName(EParameterName.FIELDSEPARATOR.getName());
        param.setValue(addQuotes(preferenceStore.getString(getPreferenceName(EParameterName.FIELDSEPARATOR))));
        param.setDisplayName(EParameterName.FIELDSEPARATOR.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setGroup(IMPLICIT_GROUP);
        param.setNumRow(32);
        param.setShowIf(condition);
        paramList.add(param);

        // begin Override encoding checkbox
        param = new ElementParameter(elem);
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
        ElementParameter encodingParam = new ElementParameter(elem);
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

        ElementParameter childParam = new ElementParameter(elem);
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
    }

    private static void createExtraOnDBParameters(Element elem) {
        ElementParameter param;
        List<IElementParameter> paramList = (List<IElementParameter>) elem.getElementParameters();
        // checks current language, if it is perl, set languageType to 0(default value), otherwise to 1.
        int languageType = 0;
        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
            languageType = 1;
        }
        final String onDBCondition = JobSettingsConstants.getExtraParameterName(EParameterName.FROM_DATABASE_FLAG.getName())
                + " == 'true'"; //$NON-NLS-1$
        final String dbCondition = JobSettingsConstants.addBrackets(CONTEXTLOAD_CONDITION)
                + " and " + JobSettingsConstants.addBrackets(onDBCondition); //$NON-NLS-1$

        // property type
        ElementParameter parentPropertyType = new ElementParameter(elem);
        parentPropertyType.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName()));
        parentPropertyType.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        parentPropertyType.setValue(preferenceStore.getString(getPreferenceName(EParameterName.PROPERTY_TYPE)));
        parentPropertyType.setCategory(EComponentCategory.EXTRA);
        parentPropertyType.setFieldType(EParameterFieldType.PROPERTY_TYPE);
        parentPropertyType.setRepositoryValue(ERepositoryCategoryType.DATABASE.getName());
        parentPropertyType.setNumRow(41);
        parentPropertyType.setShowIf(dbCondition);
        parentPropertyType.setGroup(IMPLICIT_GROUP);
        paramList.add(parentPropertyType);

        param = new ElementParameter(elem);
        param.setName(EParameterName.PROPERTY_TYPE.getName());
        param.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        param.setListItemsDisplayName(new String[] { EmfComponent.TEXT_BUILTIN, EmfComponent.TEXT_REPOSITORY });
        param.setListItemsDisplayCodeName(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        param.setListItemsValue(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        param.setValue(preferenceStore.getString(languagePrefix + EParameterName.PROPERTY_TYPE.getName()));
        param.setCategory(EComponentCategory.EXTRA);
        param.setFieldType(EParameterFieldType.TECHNICAL);
        param.setRepositoryValue(ERepositoryCategoryType.DATABASE.getName());
        param.setNumRow(41);
        param.setShowIf(dbCondition);
        param.setGroup(IMPLICIT_GROUP);
        param.setParentParameter(parentPropertyType);
        // paramList.add(param);

        // repository property type
        param = new ElementParameter(elem);
        param.setName(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        param.setDisplayName(EParameterName.REPOSITORY_PROPERTY_TYPE.getDisplayName());
        param.setListItemsDisplayName(new String[] {});
        param.setListItemsValue(new String[] {});
        param.setValue(preferenceStore.getString(getPreferenceName(EParameterName.REPOSITORY_PROPERTY_TYPE)// +
                // CONNECTION_ITEM_LABEL
                ));
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
        param = new ElementParameter(elem);
        param.setName(dbTypeName);
        param.setDisplayName(EParameterName.DB_TYPE.getDisplayName());
        param.setFieldType(EParameterFieldType.CLOSED_LIST);
        param.setCategory(EComponentCategory.EXTRA);
        param.setListItemsDisplayName(StatsAndLogsConstants.DISPLAY_DBNAMES[languageType]);
        param.setListItemsValue(JobSettingsConstants.DB_INPUT_COMPONENTS[languageType]);
        param.setListRepositoryItems(StatsAndLogsConstants.REPOSITORY_ITEMS[languageType]);
        param.setListItemsDisplayCodeName(StatsAndLogsConstants.CODE_LIST[languageType]);
        param.setValue(preferenceStore.getString(getPreferenceName(EParameterName.DB_TYPE)));
        param.setNumRow(42);
        param.setRepositoryValue("TYPE"); //$NON-NLS-1$
        param.setRequired(true);
        param.setShowIf(dbCondition);
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // dbVersion
        final String dbVersionName = JobSettingsConstants.getExtraParameterName(EParameterName.DB_VERSION.getName());
        param = new ElementParameter(elem);
        param.setName(dbVersionName);
        param.setValue(preferenceStore.getString(getPreferenceName(EParameterName.DB_VERSION)));
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
                + " and (" + dbTypeName + " == 'OCLE' or " +dbTypeName + " == 'SYBASE' or " + dbTypeName + " == 'OCLE_OCI' or " + dbTypeName + " == 'POSTGRESQL' or "+ dbTypeName + " =='ACCESS' or " + dbTypeName + " =='MSSQL' or " + dbTypeName + " =='MYSQL')"); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // jdbc url
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.URL.getName()));
        param.setValue(StatsAndLogsManager.addQuotes(preferenceStore.getString(languagePrefix + EParameterName.URL.getName())));
        param.setDisplayName(EParameterName.URL.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(43);
        param.setRepositoryValue("URL"); //$NON-NLS-1$
        String dbCon = dbTypeName + " == 'JDBC'";
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
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
        childParam.setName("drivers"); //$NON-NLS-1$
        childParam.setDisplayName("drivers"); //$NON-NLS-1$
        childParam.setFieldType(EParameterFieldType.MODULE_LIST);
        childParam.setListItemsDisplayName(moduleNameArray);
        childParam.setListItemsValue(moduleValueArray);
        // driver jar for jdbc
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DRIVER_JAR.getName()));
        param.setDisplayName(EParameterName.DRIVER_JAR.getDisplayName());
        param.setFieldType(EParameterFieldType.TABLE);
        param.setListItemsDisplayCodeName(new String[] { "drivers" }); //$NON-NLS-1$
        param.setListItemsDisplayName(new String[] { "drivers" }); //$NON-NLS-1$
        param.setListItemsValue(new ElementParameter[] { childParam });
        param.setValue(new ArrayList<Map<String, Object>>());
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(44);
        param.setRepositoryValue("DRIVER_JAR"); //$NON-NLS-1$
        dbCon = dbTypeName + " == 'JDBC'"; //$NON-NLS-1$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // class name for jdbc
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DRIVER_CLASS.getName()));
        param.setValue(StatsAndLogsManager.addQuotes(preferenceStore.getString(languagePrefix
                + EParameterName.DRIVER_CLASS.getName())));
        param.setDisplayName(EParameterName.DRIVER_CLASS.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(45);
        param.setRepositoryValue("DRIVER_CLASS"); //$NON-NLS-1$
        dbCon = dbTypeName + " == 'JDBC'";
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // host
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.HOST.getName()));
        param.setValue(addQuotes(preferenceStore.getString(getPreferenceName(EParameterName.HOST))));
        param.setDisplayName(EParameterName.HOST.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(43);
        param.setRepositoryValue("SERVER_NAME"); //$NON-NLS-1$
        dbCon = dbTypeName + " != 'SQLITE'" + " and " + dbTypeName + " != 'ACCESS'" + " and " + dbTypeName + "!='OCLE_OCI'" //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
                + " and " + dbTypeName + "!='JDBC'" + " and " + dbTypeName + "!='ODBC'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // port
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PORT.getName()));
        param.setValue(addQuotes(preferenceStore.getString(getPreferenceName(EParameterName.PORT))));
        param.setDisplayName(EParameterName.PORT.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(43);
        param.setRepositoryValue("PORT"); //$NON-NLS-1$
        dbCon = dbTypeName
                + " != 'SQLITE'" + " and " + dbTypeName + " != 'ACCESS'" + " and " + dbTypeName + " != 'FIREBIRD'" + " and " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$//$NON-NLS-6$
                + dbTypeName + "!='OCLE_OCI'" + " and " + dbTypeName + "!='JDBC'" + " and " + dbTypeName + "!='ODBC'" + " and " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                + dbTypeName + "!='TERADATA'"; //$NON-NLS-1$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // dbName
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DBNAME.getName()));
        param.setValue(addQuotes(preferenceStore.getString(getPreferenceName(EParameterName.DBNAME))));
        param.setDisplayName(EParameterName.DBNAME.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(44);
        param.setRepositoryValue("SID"); //$NON-NLS-1$
        dbCon = dbTypeName
                + " != 'SQLITE'" + " and " + dbTypeName + " != 'ACCESS'" + " and " + dbTypeName + " != 'FIREBIRD'" + " and " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$//$NON-NLS-6$
                + dbTypeName + "!='OCLE_OCI'" + " and " + dbTypeName + "!='JDBC'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // local service name
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.LOCAL_SERVICE_NAME.getName()));
        param.setValue(StatsAndLogsManager.addQuotes(""));
        param.setDisplayName(EParameterName.LOCAL_SERVICE_NAME.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(44);
        param.setRepositoryValue("SID"); //$NON-NLS-1$
        dbCon = dbTypeName + " == 'OCLE_OCI' "; //$NON-NLS-1$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {

            // additional parameters
            param = new ElementParameter(elem);
            param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTIES.getName()));
            param.setValue(addQuotes(preferenceStore.getString(getPreferenceName(EParameterName.PROPERTIES))));
            param.setDisplayName(EParameterName.PROPERTIES.getDisplayName());
            param.setFieldType(EParameterFieldType.TEXT);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(44);
            param.setRepositoryValue("PROPERTIES_STRING"); //$NON-NLS-1$
            dbCon = dbTypeName
                    + " == 'MSSQL'" + " or " + dbTypeName + " == 'MYSQL'" + " or " + dbTypeName //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    + " == 'INFORMIX'" + " or " + dbTypeName + " == 'OCLE'" + " or " + dbTypeName + " == 'OCLE_OCI'" + " or " + dbTypeName + " == 'SYBASE'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

            param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
            param.setGroup(IMPLICIT_GROUP);
            paramList.add(param);
        }
        // schema
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.SCHEMA_DB.getName()));
        param.setValue(addQuotes(preferenceStore.getString(getPreferenceName(EParameterName.SCHEMA_DB))));
        param.setDisplayName(EParameterName.SCHEMA_DB.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(44);
        param.setRepositoryValue("SCHEMA"); //$NON-NLS-1$
        final String schemaCondition = JobSettingsConstants
                .addBrackets(dbTypeName
                        + " =='OCLE' or " + dbTypeName + " =='OCLE_OCI' or " + dbTypeName + " =='POSTGRESQL' or " + dbTypeName + " =='POSTGRESPLUS' or " + dbTypeName + " =='MSSQL' or " + dbTypeName + " =='INFORMIX' or " + dbTypeName + " =='IBM_DB2' or " + dbTypeName + " =='SYBASE'"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$//$NON-NLS-6$//$NON-NLS-7$//$NON-NLS-8$
        param.setShowIf(schemaCondition + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // username
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.USER.getName()));
        param.setValue(addQuotes(preferenceStore.getString(getPreferenceName(EParameterName.USER))));
        param.setDisplayName(EParameterName.USER.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(45);
        param.setRequired(true);
        param.setRepositoryValue("USERNAME"); //$NON-NLS-1$
        dbCon = dbTypeName + " != 'SQLITE'"; //$NON-NLS-1$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // password
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PASS.getName()));
        param.setValue(addQuotes(preferenceStore.getString(getPreferenceName(EParameterName.PASS))));
        param.setDisplayName(EParameterName.PASS.getDisplayName());
        param.setFieldType(EParameterFieldType.PASSWORD);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(45);
        param.setRequired(true);
        param.setRepositoryValue("PASSWORD"); //$NON-NLS-1$
        dbCon = dbTypeName + " != 'SQLITE'"; //$NON-NLS-1$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);
        // databse file path
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DBFILE.getName()));
        param.setValue(addQuotes(preferenceStore.getString(getPreferenceName(EParameterName.DBFILE))));
        param.setDisplayName(EParameterName.DBFILE.getDisplayName());
        param.setFieldType(EParameterFieldType.FILE);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(46);
        param.setRepositoryValue("FILE"); //$NON-NLS-1$
        dbCon = dbTypeName + " == 'SQLITE'" + " or " + dbTypeName + " == 'ACCESS'" + " or " + dbTypeName + " == 'FIREBIRD'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // table
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DBTABLE.getName()));
        param.setValue(addQuotes(preferenceStore.getString(getPreferenceName(EParameterName.DBTABLE))));
        param.setDisplayName(EParameterName.DBTABLE.getDisplayName());
        param.setFieldType(EParameterFieldType.DBTABLE);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(47);
        param.setShowIf(dbCondition);
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // query condition
        param = new ElementParameter(elem);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.QUERY_CONDITION.getName()));
        param.setValue(addQuotes(preferenceStore.getString(getPreferenceName(EParameterName.QUERY_CONDITION))));
        param.setDisplayName(EParameterName.QUERY_CONDITION.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(48);
        param.setShowIf(dbCondition);
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

    }

    private static void createExtraContextLoadParameters(Element elem) {
        ElementParameter param;
        List<IElementParameter> paramList = (List<IElementParameter>) elem.getElementParameters();

        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
            final String[] itemValues = new String[] { ContextLoadInfo.ERROR.getDisplayName(),
                    ContextLoadInfo.WARNING.getDisplayName(), ContextLoadInfo.INFO.getDisplayName() };

            //
            param = new ElementParameter(elem);
            param.setName(EParameterName.LOAD_NEW_VARIABLE.getName());
            param.setDisplayName(EParameterName.LOAD_NEW_VARIABLE.getDisplayName());
            param.setValue(preferenceStore.getString(getPreferenceName(EParameterName.LOAD_NEW_VARIABLE)));
            param.setListItemsDisplayName(itemValues);
            param.setListItemsValue(itemValues);
            param.setFieldType(EParameterFieldType.CLOSED_LIST);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(80);
            param.setShowIf(CONTEXTLOAD_CONDITION);
            param.setGroup(IMPLICIT_GROUP);
            paramList.add(param);
            //
            param = new ElementParameter(elem);
            param.setName(EParameterName.NOT_LOAD_OLD_VARIABLE.getName());
            param.setDisplayName(EParameterName.NOT_LOAD_OLD_VARIABLE.getDisplayName());
            param.setValue(preferenceStore.getString(getPreferenceName(EParameterName.NOT_LOAD_OLD_VARIABLE)));
            param.setListItemsDisplayName(itemValues);
            param.setListItemsValue(itemValues);
            param.setFieldType(EParameterFieldType.CLOSED_LIST);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(81);
            param.setShowIf(CONTEXTLOAD_CONDITION);
            param.setGroup(IMPLICIT_GROUP);
            paramList.add(param);
        }
        // print operations
        param = new ElementParameter(elem);
        param.setName(EParameterName.PRINT_OPERATIONS.getName());
        param.setValue(preferenceStore.getBoolean(getPreferenceName(EParameterName.PRINT_OPERATIONS)));
        param.setDisplayName(EParameterName.PRINT_OPERATIONS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(82);
        param.setRequired(true);
        param.setShowIf(CONTEXTLOAD_CONDITION);
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
            // disable error
            param = new ElementParameter(elem);
            param.setName(EParameterName.DISABLE_ERROR.getName());
            param.setValue(preferenceStore.getBoolean(getPreferenceName(EParameterName.DISABLE_ERROR)));
            param.setDisplayName(EParameterName.DISABLE_ERROR.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(83);
            param.setRequired(true);
            param.setShowIf(CONTEXTLOAD_CONDITION);
            param.setGroup(IMPLICIT_GROUP);
            paramList.add(param);
        }

        // disable warnings
        param = new ElementParameter(elem);
        param.setName(EParameterName.DISABLE_WARNINGS.getName());
        param.setValue(preferenceStore.getBoolean(getPreferenceName(EParameterName.DISABLE_WARNINGS)));
        param.setDisplayName(EParameterName.DISABLE_WARNINGS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(83);
        param.setRequired(true);
        param.setShowIf(CONTEXTLOAD_CONDITION);
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
            // disable info
            param = new ElementParameter(elem);
            param.setName(EParameterName.DISABLE_INFO.getName());
            param.setValue(preferenceStore.getBoolean(getPreferenceName(EParameterName.DISABLE_INFO)));
            param.setDisplayName(EParameterName.DISABLE_INFO.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(83);
            param.setRequired(true);
            param.setShowIf(CONTEXTLOAD_CONDITION);
            param.setGroup(IMPLICIT_GROUP);
            paramList.add(param);
        }

    }

    private static String getPreferenceName(EParameterName param) {
        return languagePrefix + ImplicitContextLoadHelper.getExtraParameterName(param);
    }

    private final static class IgnoreCaseComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    }

}
