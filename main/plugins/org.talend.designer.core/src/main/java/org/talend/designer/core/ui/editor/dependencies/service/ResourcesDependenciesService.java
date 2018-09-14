package org.talend.designer.core.ui.editor.dependencies.service;

import org.apache.commons.lang.StringUtils;
import org.talend.camel.core.model.camelProperties.RouteResourceItem;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.service.IResourcesDependenciesService;
import org.talend.designer.core.ui.editor.dependencies.model.JobResourceDependencyModel;
import org.talend.designer.core.ui.editor.dependencies.util.ResourceDependenciesUtil;

public class ResourcesDependenciesService implements IResourcesDependenciesService {

    @Override
    public void copyToExtResourceFolder(IRepositoryViewObject repoObject, String jobLabel, String version) {
        ResourceDependenciesUtil.copyToExtResourceFolder(repoObject, jobLabel, version);
    }

    @Override
    public String getResourcePathForContext(IProcess process, String contextName) {
        String resPath = null;
        if (process instanceof IProcess2) {
            IProcess2 process2 = (IProcess2) process;
            Property property = process2.getProperty();
            String resources = (String) process2.getAdditionalProperties().get("RESOURCES_PROP");
            if (StringUtils.isBlank(resources)) {
                return null;
            }
            try {
                for (String res : resources.split(",")) {
                    String[] parts = res.split("\\|");
                    if (parts.length > 2) {
                        if (contextName.equals((parts[2].split(":"))[0])) {
                            IRepositoryViewObject repoObject = null;
                            if (RelationshipItemBuilder.LATEST_VERSION.equals(parts[1])) {
                                repoObject = ProxyRepositoryFactory.getInstance().getLastVersion(parts[0]);
                            } else {
                                repoObject = ProxyRepositoryFactory.getInstance().getSpecificVersion(parts[0], parts[1], true);
                            }
                            if (repoObject != null) {
                                JobResourceDependencyModel model = new JobResourceDependencyModel(
                                        (RouteResourceItem) repoObject.getProperty().getItem());
                                StringBuffer joblabel = new StringBuffer();
                                if (StringUtils.isNotBlank(property.getItem().getState().getPath())) {
                                    joblabel.append(property.getItem().getState().getPath() + "/");
                                }
                                joblabel.append(property.getLabel() + "_" + property.getVersion());
                                resPath = ResourceDependenciesUtil.getResourcePath(model, joblabel.toString(), parts[1]);
                                // to check if file exist, if not copy it
                                ResourceDependenciesUtil.copyToExtResourceFolder(model, joblabel.toString(), parts[1]);
                            }
                        }

                    }
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        return resPath;
    }

}
