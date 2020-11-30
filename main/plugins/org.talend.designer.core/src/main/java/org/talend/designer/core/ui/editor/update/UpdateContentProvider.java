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
package org.talend.designer.core.ui.editor.update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Priority;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.service.IMRProcessService;
import org.talend.core.service.IStormProcessService;
import org.talend.core.ui.ISparkJobletProviderService;
import org.talend.core.ui.ISparkStreamingJobletProviderService;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * ggu class global comment. Detailled comment
 */
public class UpdateContentProvider implements ITreeContentProvider, ILazyTreeContentProvider {
    private TreeViewer viewer;
    private List<Job> treeViewerInput = new ArrayList<Job>();
    private Map<String,Job> nameToJobMap = new HashMap<String, Job>();
    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof Job) {
            return ((Job) parentElement).getCategories().toArray();
        } else if (parentElement instanceof Category) {
            return ((Category) parentElement).getItems().toArray();
        }
        return null;
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof Job) {
            return null;
        } else if (element instanceof Category) {
            return ((Category) element).getParent();
        } else if (element instanceof Item) {
            return ((Item) element).getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof Job) {
            return !((Job) element).getCategories().isEmpty();
        } else if (element instanceof Category) {
            return !((Category) element).getItems().isEmpty();
        } else if (element instanceof Item) {
            return false;
        }
        return false;
    }

    public Object[] getElements(Object inputElement) {
        return treeViewerInput.toArray();
    }

    @Override
    public void dispose() {

    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.viewer = (TreeViewer)viewer;
        initViewerData(newInput);
    }
    
    private void initViewerData(Object newInput) {
        treeViewerInput.clear();
        nameToJobMap.clear();
        if (newInput instanceof Collection) {
            for (UpdateResult result : (List<UpdateResult>) newInput) {
                String jobName = result.getJobInfor();
                if (jobName == null) {
                    jobName = UpdatesConstants.EMPTY;
                }
                Job job = nameToJobMap.get(jobName);
                if (job == null) {
                    job = new Job(jobName);
                    Object job2 = result.getJob();
                    if (job2 != null) {
                        if (job2 instanceof IProcess2) {
                            job.setJoblet(((IProcess2) job2).disableRunJobView()); // ?? joblet
                            job.setReadOnlyProcess(result.isReadOnlyProcess());
                            IProcess2 process = (IProcess2) job2;
                            org.talend.core.model.properties.Item processItem = process.getProperty().getItem();
                            job.setModelItem(processItem);
                            boolean isMRProcess = false;
                            if (GlobalServiceRegister.getDefault().isServiceRegistered(IMRProcessService.class)) {
                                IMRProcessService mrProcessService = (IMRProcessService) GlobalServiceRegister.getDefault()
                                        .getService(IMRProcessService.class);
                                isMRProcess = mrProcessService.isMapReduceItem(processItem);
                            }
                            boolean isStreamingProcess = false;
                            if (GlobalServiceRegister.getDefault().isServiceRegistered(IStormProcessService.class)) {
                                IStormProcessService streamingService = (IStormProcessService) GlobalServiceRegister.getDefault()
                                        .getService(IStormProcessService.class);
                                isStreamingProcess = streamingService.isStormItem(processItem);
                            }
                            if(isMRProcess){
                                job.setMR(isMRProcess);
                            }else if(isStreamingProcess){
                                job.setStreaming(isStreamingProcess);
                            }else if (processItem instanceof ProcessItem) {
                                job.setJoblet(false);
                                job.setMR(false);
                                job.setStreaming(false);
                                job.setSparkJoblet(false);
                                job.setSparkStreamingJoblet(false);
                            } else if (processItem instanceof JobletProcessItem) {
                                boolean isSpark = false;
                                boolean isSparkStreaming = false;
                                if (GlobalServiceRegister.getDefault().isServiceRegistered(ISparkJobletProviderService.class)) {
                                    ISparkJobletProviderService sparkJobletService = (ISparkJobletProviderService) GlobalServiceRegister
                                            .getDefault().getService(ISparkJobletProviderService.class);
                                    if (sparkJobletService != null && sparkJobletService.isSparkJobletItem(processItem)) {
                                        isSpark = true;
                                    }
                                }
                                if (GlobalServiceRegister.getDefault().isServiceRegistered(ISparkStreamingJobletProviderService.class)) {
                                    ISparkStreamingJobletProviderService sparkStreamingJobletService = (ISparkStreamingJobletProviderService) GlobalServiceRegister
                                            .getDefault().getService(ISparkStreamingJobletProviderService.class);
                                    if (sparkStreamingJobletService != null && sparkStreamingJobletService.isSparkStreamingJobletItem(processItem)) {
                                        isSparkStreaming = true;
                                    }
                                }
                                if(isSpark){
                                    job.setSparkJoblet(true);
                                    job.setJoblet(false);
                                }else if(isSparkStreaming){
                                    job.setSparkStreamingJoblet(true);
                                    job.setJoblet(false);
                                }else{
                                    job.setJoblet(true);
                                }
                            }
                        }

                    } else {
                        String itemId = result.getObjectId();
                        if (itemId != null) {
                            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                            try {
                                IRepositoryViewObject repoViewObject = factory.getLastVersion(itemId);
                                if (repoViewObject != null) {
                                    Property property = repoViewObject.getProperty();
                                    if (property != null) {
                                        job.setModelItem(property.getItem());
                                    }
                                }
                            } catch (PersistenceException e) {
                                CommonExceptionHandler.process(e, Priority.WARN);
                            }
                        }
                        job.setJoblet(result.isJoblet());
                        job.setMR(result.isMR());
                        job.setStreaming(result.isStreaming());
                        job.setSparkJoblet(result.isSparkJoblet());
                        job.setSparkStreamingJoblet(result.isSparkStreamingJoblet());
                    }
                    treeViewerInput.add(job);
                    nameToJobMap.put(job.getName(), job);
                }

                Category category = job.getCategory(result.getCategory());
                if (category == null) {
                    category = new Category(job, result.getCategory());
                    category.setType(result.getUpdateType()); // for icon
                    if (result.getUpdateObject() instanceof Node) { // for node icon
                        category.setNode(result.getUpdateObject());
                    }
                    if (result.getUpdateObject() instanceof NodeType) { // for node icon
                        category.setNode(result.getUpdateObject());
                    }
                    if (result.getUpdateObject() instanceof List) { // for node icon
                        List list = (List) result.getUpdateObject();
                        if (list.size() > 0) {
                            Object object = list.get(0);
                            if (object instanceof Node) {
                                category.setNode(object);
                            }
                        }
                    }
                    job.addCategory(category);
                }
                Item item = new Item(category, result);
                category.addItem(item);
            }
        }
    }

    @Override
    public void updateElement(Object parent, int index) {
        Object element = null;
        if (parent instanceof Job) {
            element = ((Job) parent).getCategories().get(index);
        } else if (parent instanceof Category) {
            element = ((Category) parent).getItems().get(index);
        } else if (parent instanceof Collection && treeViewerInput.size() > index) {
            element = treeViewerInput.get(index);
        }
        if (element != null) {
            viewer.replace(parent, index, element);
            updateChildCount(element, -1);
        }
    }

    @Override
    public void updateChildCount(Object element, int currentChildCount) {
        int count = 0;
        if (element instanceof Job) {
            Job job = (Job) element;
            count = job.getCategories().size();
        } else if (element instanceof Category) {
            Category category = (Category) element;
            count = category.getItems().size();
        } else if (element instanceof Item) {
            count = 0;
        } else if (element instanceof Collection) {
            count = treeViewerInput.size();
        }
        viewer.setChildCount(element, count);
    }

    public List<Job> getTreeViewerInput() {
        return treeViewerInput;
    }   
}
