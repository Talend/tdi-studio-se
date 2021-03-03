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
package org.talend.repository.preference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.DynaEnum;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.token.AbstractTokenCollector;
import org.talend.core.ui.token.TokenInforUtil;
import org.talend.core.ui.token.TokenKey;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.IProxyRepositoryFactory;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * ggu class global comment. Detailled comment
 */
public class TosTokenCollector extends AbstractTokenCollector {

    private static final String PREF_TOS_JOBS_RECORDS = "TOS_Jobs_Records"; //$NON-NLS-1$

    private static final TokenKey PROJECTS = new TokenKey("projects"); //$NON-NLS-1$

    private static final TokenKey TYPE = new TokenKey("type"); //$NON-NLS-1$

    /**
     * ggu JobTokenCollector constructor comment.
     */
    public TosTokenCollector() {
    }

    @Override
    public void priorCollect() throws Exception {
        // for all projects
        JSONObject allProjectRecords = null;

        IPreferenceStore preferenceStore = RepositoryPlugin.getDefault().getPreferenceStore();
        String records = preferenceStore.getString(PREF_TOS_JOBS_RECORDS);
        try {
            // reset
            allProjectRecords = new JSONObject(records);
        } catch (Exception e) {
            // the value is not set, or is empty
            allProjectRecords = new JSONObject();
        }

        JSONObject currentProjectObject = collectProjectDetails();

        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        allProjectRecords.put(currentProject.getTechnicalLabel(), currentProjectObject);

        //
        preferenceStore.setValue(PREF_TOS_JOBS_RECORDS, allProjectRecords.toString());
    }

    private JSONObject collectProjectDetails() throws PersistenceException, JSONException {
        JSONObject jObject = new JSONObject();

        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        JSONObject repoStats = new JSONObject();
        // metadata
        for (DynaEnum type : ERepositoryObjectType.values()) {
            if (type instanceof ERepositoryObjectType && ((ERepositoryObjectType) type).isResourceItem()) {
                try {
                    List<IRepositoryViewObject> all = factory.getAll(currentProject, (ERepositoryObjectType) type);

                    int nb = all.size();
                    if (ERepositoryObjectType.TDQ_INDICATOR_ELEMENT.equals(type)
                            || ERepositoryObjectType.TDQ_PATTERN_ELEMENT.equals(type)
                            || ERepositoryObjectType.TDQ_RULES.equals(type) || "TDQ_SOURCE_FILE_ELEMENT".equals(type.getType())) { //$NON-NLS-1$
                        continue;
                    }
                    if (ERepositoryObjectType.ROUTINES.equals(type)) {
                        nb = 0;
                        List<IRepositoryViewObject> newList = new ArrayList<IRepositoryViewObject>();
                        for (IRepositoryViewObject object : all) {
                            RoutineItem rItem = (RoutineItem) object.getProperty().getItem();
                            if (!rItem.isBuiltIn()) {
                                nb++;
                                newList.add(object);
                            }
                        }
                        all = newList;
                    }
                    if (ERepositoryObjectType.SQLPATTERNS.equals(type)) {
                        nb = 0;
                        for (IRepositoryViewObject object : all) {
                            SQLPatternItem spItem = (SQLPatternItem) object.getProperty().getItem();
                            if (!spItem.isSystem()) {
                                nb++;
                            }
                        }
                    }
                    if ("MDM.DataModel".equals(type.getType())) { //$NON-NLS-1$
                        nb = 0;
                        for (IRepositoryViewObject object : all) {
                            String path = object.getProperty().getItem().getState().getPath();
                            if (!"System".equals(path)) { //$NON-NLS-1$
                                nb++;
                            }
                        }
                    }
                    if (nb > 0) {
                        JSONObject typeStats = new JSONObject();
                        typeStats.put("nb", nb); //$NON-NLS-1$
                        if (ERepositoryObjectType.getAllTypesOfProcess().contains(type)) {
                            JSONObject jobDetails = new JSONObject();
                            collectJobDetails(all, jobDetails);
                            typeStats.put("details", jobDetails); //$NON-NLS-1$
                        }

                        if (ERepositoryObjectType.ROUTINES.equals(type)
                                || ((ERepositoryObjectType) type).getFolder().startsWith("metadata/") //$NON-NLS-1$
                                || ERepositoryObjectType.CONTEXT.equals(type) || type.equals(ERepositoryObjectType.JOBLET)) {
                            int nbUsed = 0;
                            for (IRepositoryViewObject object : all) {
                                List<Relation> relations = RelationshipItemBuilder.getInstance().getItemsHaveRelationWith(
                                        object.getId());
                                relations.addAll(RelationshipItemBuilder.getInstance()
                                        .getItemsHaveRelationWith(object.getLabel()));
                                if (relations.size() > 0) {
                                    nbUsed++;
                                }
                            }
                            typeStats.put("nb.used", nbUsed); //$NON-NLS-1$
                        }
                        if (ERepositoryObjectType.METADATA_CONNECTIONS.equals(type)) {
                            JSONObject objects = new JSONObject();
                            for (IRepositoryViewObject object : all) {
                                DatabaseConnectionItem item = (DatabaseConnectionItem) object.getProperty().getItem();
                                String dbType = ((DatabaseConnection) item.getConnection()).getDatabaseType();
                                int nbDbTypes = 1;
                                if (objects.has(dbType)) {
                                    nbDbTypes = objects.getInt(dbType);
                                    nbDbTypes++;
                                }
                                objects.put(dbType, nbDbTypes);
                            }
                            typeStats.put("types", objects); //$NON-NLS-1$
                        }
                        repoStats.put(type.getType(), typeStats);
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        jObject.put(PROJECTS.getKey(), repoStats); //$NON-NLS-1$
        jObject.put(TYPE.getKey(), ProjectManager.getInstance().getProjectType(currentProject));
        int nbRef = ProjectManager.getInstance().getAllReferencedProjects().size();
        if (nbRef > 0) {
            jObject.put("nb.refProjects", nbRef);
        }

        return jObject;
    }

    /**
     * DOC nrousseau Comment method "collectJobDetails".
     *
     * @param all
     * @param jobDetails
     * @throws JSONException
     */
    private void collectJobDetails(List<IRepositoryViewObject> allRvo, JSONObject jobDetails) throws JSONException {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IEditorReference[] reference = new IEditorReference[0];
        if (ww != null) {
            IWorkbenchPage page = ww.getActivePage();
            reference = page.getEditorReferences();
        }
        List<IProcess2> processes = RepositoryPlugin.getDefault().getDesignerCoreService().getOpenedProcess(reference);
        Set<String> idsOpened = new HashSet<String>();
        for (IProcess2 process : processes) {
            idsOpened.add(process.getId());
        }

        JSONArray components = new JSONArray();

        int contextVarsNum = 0;
        int nbComponentsUsed = 0;
        for (IRepositoryViewObject rvo : allRvo) {
            Item item = rvo.getProperty().getItem();
            if (item instanceof ProcessItem) {
                ProcessType processType = ((ProcessItem) item).getProcess();

                for (NodeType node : (List<NodeType>) processType.getNode()) {
                    JSONObject component_names = null;
                    String componentName = node.getComponentName();
                    int nbComp = 0;
                    for(int i = 0;i<components.length();i++){
                        JSONObject temp = components.getJSONObject(i);
                        if(temp.get("component_name").equals(componentName)){//$NON-NLS-1$
                           nbComp = temp.getInt("count");//$NON-NLS-1$
                           component_names = temp;
                           break;
                        }
                    }
                     if(component_names == null){
                         component_names = new JSONObject();
                         components.put(component_names);
                    }
                    component_names.put("component_name", componentName);
                    component_names.put("count", nbComp + 1);
                    nbComponentsUsed++;
                }
                // context variable per job
                EList contexts = processType.getContext();
                if (contexts.size() > 0) {
                    ContextType contextType = (ContextType) contexts.get(0);
                    contextVarsNum += contextType.getContextParameter().size();
                }

            }
            if (factory.getStatus(item) != ERepositoryStatus.LOCK_BY_USER && !idsOpened.contains(item.getProperty().getId())) {
                // job is not locked and not opened by editor, so we can unload.
                if (item.getParent() instanceof FolderItem) {
                    ((FolderItem) item.getParent()).getChildren().remove(item);
                    item.setParent(null);
                }
                item.eResource().unload();
            }
        }
        jobDetails.put("components", components);
        jobDetails.put("nb.contextVars", contextVarsNum);
        jobDetails.put("nb.components", nbComponentsUsed);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.token.AbstractTokenCollector#collect()
     */
    @Override
    public JSONObject collect() throws Exception {
        JSONObject finalToken = new JSONObject();
        JSONObject mergedData = new JSONObject();

        IPreferenceStore preferenceStore = RepositoryPlugin.getDefault().getPreferenceStore();
        String records = preferenceStore.getString(PREF_TOS_JOBS_RECORDS);
        JSONObject allProjectRecords = null;
        try {
            // reset
            allProjectRecords = new JSONObject(records);
        } catch (Exception e) {
            // the value is not set, or is empty
            allProjectRecords = new JSONObject();
        }

        Iterator<String> keys = allProjectRecords.keys();
        JSONObject projectTypes = new JSONObject();
        while (keys.hasNext()) {
            String projectName = keys.next();
            JSONObject object = (JSONObject) allProjectRecords.get(projectName);
            if (object != null) {
                TokenInforUtil.mergeJSON(object, mergedData);
                if (object.has(TYPE.getKey())) {
                    String type = object.getString(TYPE.getKey());
                    // count the number of project for each type
                    if (!projectTypes.has(type)) {
                        projectTypes.put(type, 1);
                    } else {
                        int nb = projectTypes.getInt(type);
                        nb++;
                        projectTypes.put(type, nb);
                    }
                }
            }
        }
        if (mergedData.has(PROJECTS.getKey())) {
            finalToken.put(PROJECTS_REPOSITORY.getKey(), mergedData.get(PROJECTS.getKey()));
        }
        finalToken.put("projects.type", projectTypes);
        return finalToken;
    }
}
