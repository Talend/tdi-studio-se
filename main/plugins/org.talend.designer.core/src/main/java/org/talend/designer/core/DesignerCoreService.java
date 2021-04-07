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
package org.talend.designer.core;

import java.beans.PropertyChangeEvent;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.Project;
import org.talend.core.model.genhtml.IJobSettingConstants;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.Problem;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.core.ui.component.ComponentPaletteUtilities;
import org.talend.core.ui.context.view.Contexts;
import org.talend.core.utils.CsvArray;
import org.talend.designer.core.convert.IProcessConvertService;
import org.talend.designer.core.convert.ProcessConvertManager;
import org.talend.designer.core.debug.JobLaunchShortcutManager;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.components.Expression;
import org.talend.designer.core.model.process.DataNode;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.MultiPageTalendEditor;
import org.talend.designer.core.ui.action.CreateProcess;
import org.talend.designer.core.ui.action.SaveJobBeforeRunAction;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.designer.core.ui.editor.connections.TracesConnectionUtils;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.ConvertRepositoryNodeToProcessNode;
import org.talend.designer.core.ui.editor.process.JobTemplateViewsAndProcessUtil;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.properties.GefEditorLabelProvider;
import org.talend.designer.core.ui.editor.properties.RepositoryValueUtils;
import org.talend.designer.core.ui.editor.update.UpdateManagerUtils;
import org.talend.designer.core.ui.projectsetting.ProjectSettingManager;
import org.talend.designer.core.ui.views.jobsettings.JobSettings;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.core.ui.views.properties.ComponentSettings;
import org.talend.designer.core.ui.views.properties.ComponentSettingsView;
import org.talend.designer.core.utils.BigDataJobUtil;
import org.talend.designer.core.utils.JavaProcessUtil;
import org.talend.designer.core.utils.UnifiedComponentUtil;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.librariesmanager.model.ModulesNeededProvider;
import org.talend.repository.ProjectManager;
import org.talend.repository.ui.actions.routines.CreateRoutineAction;
import org.talend.repository.ui.utils.UpdateLog4jJarUtils;

/**
 * Detailled comment <br/>
 * .
 *
 * $Id: DesignerCoreService.java 1 2006 -12 -19 上午10:25:42 bqian
 *
 */
public class DesignerCoreService implements IDesignerCoreService {

    private Map<String, java.util.Date> lastGeneratedJobs = new HashMap<String, java.util.Date>();

    @Override
    public List<IProcess2> getOpenedProcess(IEditorReference[] reference) {
        if (CommonsPlugin.isHeadless() || !ProxyRepositoryFactory.getInstance().isFullLogonFinished()) {
            return Collections.EMPTY_LIST;
        }
        return RepositoryManagerHelper.getOpenedProcess(reference);
    }

    @Override
    public Item getProcessItem(MultiPageEditorPart talendEditor) {
        ProcessEditorInput processEditorInput = (ProcessEditorInput) talendEditor.getEditorInput();
        Item item = processEditorInput.getItem();
        return item;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#getProcessFromProcessItem(org.talend.core.model.properties.
     * ProcessItem )
     */
    @Override
    public IProcess getProcessFromProcessItem(ProcessItem processItem) {
        return getProcessFromItemByExtendion(processItem, false);
    }

    @Override
    public IProcess getProcessFromProcessItem(ProcessItem processItem, boolean loadScreenshots) {
        return getProcessFromItemByExtendion(processItem, loadScreenshots);
    }

    @Override
    public IProcess getProcessFromItem(Item item) {
        return getProcessFromItemByExtendion(item, false);
    }

    public IProcess getProcessFromItemByExtendion(Item item, boolean loadScreenshots) {
        IProcess process = null;
        List<IProcessConvertService> processConvertServices = ProcessConvertManager.getInstance().extractAllConvertServices();
        for (IProcessConvertService service : processConvertServices) {
            process = service.getProcessFromItem(item, loadScreenshots);
            if (process != null) {
                break;
            }
        }
        return process;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.IDesignerCoreService#getProcessContextFromItem(org.talend.core.model.properties.Item)
     */
    @Override
    public IContextManager getProcessContextFromItem(Item item) {
        try {
            Process process = new Process(item.getProperty());
            process.loadContexts();
            return process.getContextManager();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.IDesignerCoreService#getProcessFromJobletProcessItem(org.talend.core.model.properties
     * .JobletProcessItem)
     */
    @Override
    public IProcess getProcessFromJobletProcessItem(JobletProcessItem jobletProcessItem) {
        return getProcessFromItemByExtendion(jobletProcessItem, false);
    }

    @Override
    public ILabelProvider getGEFEditorNodeLabelProvider() {
        return new GefEditorLabelProvider();
    }

    // used for generating HTML only
    /**
     * Constructs a new instance.
     */
    private RepositoryValueUtils repositoryValueUtils = null;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#getQueriesMap()
     */
    @Override
    public List<Map> getMaps() {
        if (repositoryValueUtils == null) {
            repositoryValueUtils = new RepositoryValueUtils();
        }
        List<Map> list = new ArrayList<Map>();
        list.add(repositoryValueUtils.getRepositoryConnectionItemMap());
        list.add(repositoryValueUtils.getRepositoryDBIdAndNameMap());
        return list;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#getRepositoryAliasName(org.talend.core.model.properties.
     * ConnectionItem )
     */
    @Override
    public String getRepositoryAliasName(ConnectionItem connectionItem) {
        return repositoryValueUtils.getRepositoryAliasName(connectionItem);
    }

    @Override
    public void switchToCurContextsView() {
        Contexts.switchToCurContextsView();
    }

    @Override
    public void switchToCurComponentSettingsView() {
        ComponentSettings.switchToCurComponentSettingsView();
    }

    @Override
    public void switchToCurJobSettingsView() {
        JobSettings.switchToCurJobSettingsView();
    }

    @Override
    public void switchToCurProcessView() {
        DesignerPlugin.getDefault().getRunProcessService().switchToCurProcessView();
    }

    @Override
    public void saveJobBeforeRun(IProcess activeProcess) {
        new SaveJobBeforeRunAction(activeProcess).run();
    }

    // ends.

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#getCurrentProcess()
     */
    @Override
    public IProcess getCurrentProcess() {
        IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (!(editor instanceof AbstractMultiPageTalendEditor)) {
            return null;
        }
        IProcess process = ((AbstractMultiPageTalendEditor) editor).getProcess();
        return process;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#refreshDesignerPalette()
     */
    @Override
    public void synchronizeDesignerUI(PropertyChangeEvent evt) {
        ComponentPaletteUtilities.updatePalette();
        // List<String> openJobs = new ArrayList<String>();
        for (IEditorPart editor : ProcessorUtilities.getOpenedEditors()) {
            AbstractTalendEditor abstractTalendEditor = ((AbstractTalendEditor) editor);
            IProcess2 process = abstractTalendEditor.getProcess();
            if (process != null && process.getUpdateManager() != null) {
                process.getUpdateManager().addNodesPropertyChanger(evt);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#getPreferenceStore(java.lang.String)
     */
    @Override
    public String getPreferenceStore(String key) {
        return DesignerPlugin.getDefault().getPreferenceStore().getString(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#getPreferenceStoreBooleanValue(java.lang.String)
     */
    @Override
    public boolean getPreferenceStoreBooleanValue(String key) {
        return DesignerPlugin.getDefault().getPreferenceStore().getBoolean(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#setPreferenceStoreValue(java.lang.String, java.lang.Object)
     */
    @Override
    public void setPreferenceStoreValue(String key, Object value) {
        if (value != null) {
            DesignerPlugin.getDefault().getPreferenceStore().setValue(key, value.toString());
        } else {
            DesignerPlugin.getDefault().getPreferenceStore().setValue(key, ""); //$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#setToDefault(java.lang.String)
     */
    @Override
    public void setPreferenceStoreToDefault(String key) {
        DesignerPlugin.getDefault().getPreferenceStore().setToDefault(key);
    }

    @Override
    public IAction getCreateProcessAction(boolean isToolbar) {
        return new CreateProcess(isToolbar);
    }

    @Override
    public IAction getCreateBeanAction(boolean isToolbar) {

        return new CreateRoutineAction(isToolbar);
    }

    @Override
    public boolean isTalendEditor(IEditorPart activeEditor) {
        if (activeEditor == null) {
            return false;
        }
        return activeEditor.getSite().getId().equals(MultiPageTalendEditor.ID);

    }

    @Override
    public INode getRefrenceNode(String componentName) {

        if (componentName == null) {
            return null;
        }

        IComponentsFactory compFac = CorePlugin.getDefault().getRepositoryService().getComponentsFactory();
        IComponent salesforceComponent = compFac.get(componentName);

        return new DataNode(salesforceComponent, componentName);
    }

    @Override
    public INode getRefrenceNode(String componentName, String paletteType) {
        if (componentName == null) {
            return null;
        }

        IComponentsFactory compFac = CorePlugin.getDefault().getRepositoryService().getComponentsFactory();
        IComponent component = compFac.get(componentName, paletteType);
        if (component == null) {
            return null;
        }

        return new DataNode(component, componentName);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#executeUpdatesManager(java.util.List)
     */
    @Override
    public boolean executeUpdatesManager(List<UpdateResult> results, boolean onlySimpleShow) {
        return UpdateManagerUtils.executeUpdates(results, onlySimpleShow, true);
    }

    @Override
    public boolean executeUpdatesManagerBackgroud(List<UpdateResult> results, boolean onlySimpleShow) {
        return UpdateManagerUtils.executeUpdates(results, onlySimpleShow, true, false, true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#getProcessForJobTemplate()
     */
    @Override
    public List<IProcess> getProcessForJobTemplate() {
        if (JobTemplateViewsAndProcessUtil.getInstance().getHelpProcess() != null) {
            // Everytime return a new list
            List<IProcess> result = new ArrayList<IProcess>();
            result.add(JobTemplateViewsAndProcessUtil.getInstance().getHelpProcess());
            return result;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#getLastGeneratedJobsDateMap()
     */
    @Override
    public Map<String, java.util.Date> getLastGeneratedJobsDateMap() {
        return lastGeneratedJobs;
    }

    /**
     *
     * DOC YeXiaowei Comment method "getDisplayForProcessParameterFromName".
     *
     * @param name
     * @return
     */
    @Override
    public String getDisplayForProcessParameterFromName(final String name) {

        for (EParameterName param : EParameterName.values()) {
            String keyName = name;
            String suffix = ""; //$NON-NLS-1$
            if (name.endsWith("_IMPLICIT_CONTEXT")) { //$NON-NLS-1$
                keyName = name.substring(0, name.indexOf("_IMPLICIT_CONTEXT")); //$NON-NLS-1$
                suffix = " (implict context)"; //$NON-NLS-1$
            }
            if (param.name().equals(keyName)) {
                return param.getDisplayName() + suffix;
            }
        }

        if (name.equals(IJobSettingConstants.PROPERTY_TYPE_IMPLICIT_CONTEXT_PROPERTY_TYPE)) {
            return Messages.getString("DesignerCoreService.property"); //$NON-NLS-1$
        } else if (name.equals(IJobSettingConstants.PROPERTY_TYPE_IMPLICIT_CONTEXT_REPOSITORY_PROPERTY_TYPE)) {
            return Messages.getString("DesignerCoreService.propertySource"); //$NON-NLS-1$
        } else if (name.equals(IJobSettingConstants.PROPERTY_TYPE_PROPERTY_TYPE)) {
            return Messages.getString("DesignerCoreService.property"); //$NON-NLS-1$
        } else if (name.equals(IJobSettingConstants.PROPERTY_TYPE_REPOSITORY_PROPERTY_TYPE)) {
            return Messages.getString("DesignerCoreService.propertySource"); //$NON-NLS-1$
        }

        return name;
    }

    @Override
    public void refreshComponentView(Item item) {
        try {
            IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
            IEditorReference[] editors = activePage.getEditorReferences();
            for (IEditorReference er : editors) {
                IEditorPart part = er.getEditor(false);
                if (part instanceof AbstractMultiPageTalendEditor) {
                    AbstractMultiPageTalendEditor editor = (AbstractMultiPageTalendEditor) part;
                    CommandStack stack = (CommandStack) editor.getTalendEditor().getAdapter(CommandStack.class);
                    if (stack != null) {
                        IProcess process = editor.getProcess();
                        for (final INode processNode : process.getGraphicalNodes()) {
                            if (processNode instanceof Node) {
                                checkRepository((Node) processNode, item, stack);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    public void refreshComponentView() {
        if (!PlatformUI.isWorkbenchRunning()) {
            return;
        }
        final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null) {
            return;
        }
        IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
        if (page == null) {
            return;
        }
        IViewPart view = page.findView(ComponentSettingsView.ID);
        if (view == null) {
            return; // don't do anything. before it made the view appear for nothing even in other product like DQ.
        }
        if (view != null && view instanceof ComponentSettingsView) {
            ComponentSettingsView settingView = (ComponentSettingsView) view;
            Element element = settingView.getElement();
            if (element != null) {
                settingView.cleanDisplay();
                settingView.setElement(element);
            }
        }

        List<ComponentSettingsView> otherViews = JobTemplateViewsAndProcessUtil.getInstance().getAllViews();

        if (otherViews == null || otherViews.isEmpty()) {
            return;
        }

        for (ComponentSettingsView v : otherViews) {
            if (v.getParent() != null && !v.getParent().isDisposed()) {
                Element elem = v.getElement();
                if (elem != null) {
                    v.cleanDisplay();
                    v.setElement(elem);
                }
            }
        }
    }

    /**
     * DOC hcw Comment method "checkRepository".
     *
     * @param node
     * @param item
     * @param stack
     */
    private void checkRepository(final Node node, Item item, CommandStack stack) {
        final String updataComponentParamName = EParameterName.UPDATE_COMPONENTS.getName();
        final List<IElementParameter> repositoryParam = new ArrayList<IElementParameter>();

        for (IElementParameter param : node.getElementParameters()) {
            if (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                    || param.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)) {
                String value = (String) param.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName()).getValue();

                if (value.equals(EmfComponent.REPOSITORY)) {
                    IElementParameter schema = param.getChildParameters().get(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
                    if (schema != null && schema.getValue() != null) {
                        String[] names = ((String) schema.getValue()).split(" - "); //$NON-NLS-1$
                        if (names.length > 0) {
                            if (names[0].equals(item.getProperty().getId())) {
                                repositoryParam.add(schema);
                            }
                        }
                    }
                }

            } else if (param.getFieldType().equals(EParameterFieldType.PROPERTY_TYPE)) {
                Object value = param.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName()).getValue();
                if (value.equals(EmfComponent.REPOSITORY)) {
                    IElementParameter property = param.getChildParameters()
                            .get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                    if (property != null && property.getValue() != null) {

                        if (property.getValue().equals(item.getProperty().getId())) {
                            repositoryParam.add(property);
                        }

                    }
                }
            }
        }

        if (repositoryParam.isEmpty()) {
            return;
        }

        stack.execute(new Command() {

            @Override
            public void execute() {

                node.setPropertyValue(updataComponentParamName, new Boolean(true));
                for (IElementParameter param : repositoryParam) {
                    // force to reload label
                    param.setListItemsDisplayName(new String[0]);
                    param.setListItemsValue(new String[0]);
                }
            }

        });
    }

    /**
     *
     * nrousseau Comment method "removeConnection".
     */
    @Override
    public void removeConnection(INode node, String schemaName) {
        for (IConnection connection : (List<IConnection>) node.getOutgoingConnections()) {
            if (connection.getMetaName().equals(schemaName)) {
                connection.disconnect();
                INode prevNode = connection.getSource();
                INodeConnector nodeConnectorSource, nodeConnectorTarget;
                nodeConnectorSource = prevNode.getConnectorFromType(connection.getLineStyle());
                nodeConnectorSource.setCurLinkNbOutput(nodeConnectorSource.getCurLinkNbOutput() - 1);

                INode nextNode = connection.getTarget();
                nodeConnectorTarget = nextNode.getConnectorFromType(connection.getLineStyle());
                nodeConnectorTarget.setCurLinkNbInput(nodeConnectorTarget.getCurLinkNbInput() - 1);
                break;
            }
        }
        node.getProcess().removeUniqueConnectionName(schemaName);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#convertNode(org.talend.core.model.properties.ConnectionItem,
     * java.lang.String)
     */
    @Override
    public CsvArray convertNode(ConnectionItem connectionItem, IMetadataConnection convertedConnection, String tableName)
            throws ProcessorException {
        ConvertRepositoryNodeToProcessNode convertMove = new ConvertRepositoryNodeToProcessNode(connectionItem,
                convertedConnection, tableName);
        CsvArray array = null;

        array = convertMove.runMockProcess();

        return array;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.IDesignerCoreService#updateTraceColumnValues(org.talend.core.model.process.IConnection,
     * java.util.Map, java.util.Set)
     */
    @Override
    public void updateTraceColumnValues(IConnection conn, Map<String, String> changedNameColumns, Set<String> addedColumns) {
        if (changedNameColumns == null) {
            for (String curColumnName : addedColumns) {
                TracesConnectionUtils.setTraceColumnValues(conn, curColumnName, null, true); // set default
                // (true)
            }
            return;
        } else {
            List<Map<String, Object>> traceFilterValues = TracesConnectionUtils.getTraceConnectionFilterValues(conn);
            if (traceFilterValues != null) {
                for (String newName : changedNameColumns.keySet()) {
                    // update the column name in TRACES_CONNECTION_FILTER parameter.
                    String oldName = changedNameColumns.get(newName);
                    if (oldName != null) {
                        Map<String, Object> foundLine = null;
                        for (Map<String, Object> line : traceFilterValues) {
                            Object column = line.get(IConnection.TRACE_SCHEMA_COLUMN);
                            if (oldName.equals(column)) { // found
                                foundLine = line;
                                break;
                            }
                        }
                        if (foundLine != null) { // found, update
                            foundLine.put(IConnection.TRACE_SCHEMA_COLUMN, newName);
                        }
                    }
                }
            }
            // when create new column
            for (String colName : addedColumns) {
                TracesConnectionUtils.setTraceColumnValues(conn, colName, null, true); // set default
                // (true)
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#getConnection(java.util.List,
     * org.talend.core.model.metadata.IMetadataTable)
     */
    @Override
    public IConnection getConnection(List<? extends IConnection> connections, IMetadataTable table) {
        return TracesConnectionUtils.getConnection(connections, table);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#setTraceFilterParameters(org.talend.core.model.process.INode,
     * org.talend.core.model.metadata.IMetadataTable, java.util.Set, java.util.Map)
     */
    @Override
    public void setTraceFilterParameters(INode node, IMetadataTable table, Set<String> preColumnSet,
            Map<String, String> changedNameColumns) {
        TracesConnectionUtils.setTraceFilterParameters(node, table, preColumnSet, changedNameColumns);
    }

    @Override
    public void createStatsLogAndImplicitParamter(Project project) {
        Element createStatsAndLogsElement = ProjectSettingManager.createStatsAndLogsElement(project);
        ProjectSettingManager.saveStatsAndLogToProjectSettings(createStatsAndLogsElement, project);
        Element createImplicitContextLoadElement = ProjectSettingManager.createImplicitContextLoadElement(project);
        ProjectSettingManager.saveImplicitValuesToProjectSettings(createImplicitContextLoadElement, project);
    }

    @Override
    public void removeJobLaunch(IRepositoryViewObject objToDelete) {
        JobLaunchShortcutManager.removeJobLaunch(objToDelete);
    }

    @Override
    public void renameJobLaunch(IRepositoryViewObject obj, String originalName) {
        JobLaunchShortcutManager.renameJobLaunch(obj, originalName);
    }

    @Override
    public boolean isDummyComponent(IComponent component) {
        if (component == null) {
            return false;
        }
        if (component instanceof DummyComponent) {
            return true;
        }

        return false;
    }

    @Override
    public void addProblems(Problem problem) {
        Problems.add(problem);
        Problems.refreshProblemTreeView();
    }

    @Override
    public void reloadParamFromProjectSettings(ParametersType processType, String paramName) {
        if (EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName().equals(paramName)) {
            ProjectSettingManager.reloadStatsAndLogFromProjectSettings(processType, ProjectManager.getInstance()
                    .getCurrentProject());
        } else if (EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName().equals(paramName)) {
            ProjectSettingManager.reloadImplicitValuesFromProjectSettings(processType, ProjectManager.getInstance()
                    .getCurrentProject());
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.IDesignerCoreService#getNeededLibrariesForProcess(org.talend.core.model.process.IProcess
     * , boolean)
     */
    @Override
    public Set<ModuleNeeded> getNeededLibrariesForProcess(IProcess process, int options) {
        return JavaProcessUtil.getNeededModules(process, options);
    }

    @Override
    public Set<ModuleNeeded> getCodesJarNeededLibrariesForProcess(Item item) {
        return ModulesNeededProvider.getCodesJarModulesNeededForProcess(item);
    }

    @Override
    public Set<ModuleNeeded> getNeededModules(INode node, boolean withChildrens) {
        int options = TalendProcessOptionConstants.MODULES_DEFAULT;
        if (withChildrens) {
            options |= TalendProcessOptionConstants.MODULES_WITH_CHILDREN;
        }
        if (ComponentCategory.CATEGORY_4_MAPREDUCE.getName().equals(node.getProcess().getComponentsType())) {
            options |= TalendProcessOptionConstants.MODULES_FOR_MR;
        }
        return JavaProcessUtil.getNeededModules(node, options);
    }

    @Override
    public boolean evaluate(final String string, List<? extends IElementParameter> listParam) {
        return Expression.evaluate(string, listParam);
    }

    @Override
    public int getDBConnectionTimeout() {
        final IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
        if (preferenceStore != null && preferenceStore.getBoolean(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT_ACTIVED)) {
            return preferenceStore.getInt(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT);
        }
        // disable timeout
        return 0;
    }

    @Override
    public int getHBaseOrMaprDBScanLimit() {
        final IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
        if (preferenceStore != null) {
            return preferenceStore.getInt(ITalendCorePrefConstants.HBASE_OR_MAPRDB_SCAN_LIMIT);
        }
        // disable
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.IDesignerCoreService#getJavadocContentAccessContentReader(org.eclipse.jdt.core.IMember)
     */
    @Override
    public Reader getJavadocContentAccessContentReader(IMember member) throws JavaModelException {
        Reader reader = JavadocContentAccess.getContentReader(member, true);
        return reader;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IDesignerCoreService#resetJobProblemView(org.talend.core.model.repository.
     * IRepositoryViewObject, java.lang.String)
     */
    @Override
    public void resetJobProblemList(IRepositoryViewObject obj, String originalName) {
        JobLaunchShortcutManager.resetJobProblemList(obj, originalName);
    }

    @Override
    public int getTACConnectionTimeout() {
        final IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
        int timeOut = -1;
        if (preferenceStore != null && preferenceStore.contains(ITalendCorePrefConstants.PERFORMANCE_TAC_CONNECTION_TIMEOUT)) {
            timeOut = preferenceStore.getInt(ITalendCorePrefConstants.PERFORMANCE_TAC_CONNECTION_TIMEOUT);
        }
        if (timeOut < 0) {
            timeOut = 0;
        }
        return timeOut;
    }

    @Override
    public void setTACConnectionTimeout(int timeout) {
        final IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
        if (preferenceStore != null) {
            preferenceStore.setValue(ITalendCorePrefConstants.PERFORMANCE_TAC_CONNECTION_TIMEOUT, timeout);
        }
    }

    @Override
    public int getTACReadTimeout() {
        final IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
        int timeOut = -1;
        if (preferenceStore != null && preferenceStore.contains(ITalendCorePrefConstants.PERFORMANCE_TAC_READ_TIMEOUT)) {
            timeOut = preferenceStore.getInt(ITalendCorePrefConstants.PERFORMANCE_TAC_READ_TIMEOUT);
        }
        if (timeOut < 0) {
            timeOut = 0;
        }
        return timeOut;
    }

    @Override
    public void setTACReadTimeout(int timeout) {
        final IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
        if (preferenceStore != null) {
            preferenceStore.setValue(ITalendCorePrefConstants.PERFORMANCE_TAC_READ_TIMEOUT, timeout);
        }
    }

    @Override
    public boolean isDelegateNode(INode node) {
        if (node instanceof Node) {
            Node n = (Node) node;
            return UnifiedComponentUtil.isDelegateComponent(n.getDelegateComponent());
        }
        return false;
    }

    @Override
    public boolean isNeedContextInJar(IProcess process) {
        return new BigDataJobUtil(process).needsToHaveContextInsideJar();
    }
    
    @Override
    public String[] getNeedRemoveModulesForLog4j() {
        return UpdateLog4jJarUtils.NEEDREMOVEMODULES;
    }
}
