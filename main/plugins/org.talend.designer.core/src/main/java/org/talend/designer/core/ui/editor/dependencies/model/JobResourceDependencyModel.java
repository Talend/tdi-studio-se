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
package org.talend.designer.core.ui.editor.dependencies.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * @author xpli
 *
 */
public class JobResourceDependencyModel {

    public static final String LATEST_VERSION = RelationshipItemBuilder.LATEST_VERSION;

    private final ResourceItem item;

    private String selectedVersion = LATEST_VERSION;

    private String contextVar = "";

    private String contextSource = "";

    private String resourceDepPath = "";

    /**
     * Built in type can not be deleted.
     */
    private boolean isBuiltIn;

    private final Collection<String> refNodes = new HashSet<String>();

    public JobResourceDependencyModel(ResourceItem item) {
        this.item = item;
    }

    /**
     * @return the item
     */
    public ResourceItem getItem() {
        return item;
    }

    /**
     * Get the relative context variable name DOC jding Comment method "getContextVar".
     *
     * @return
     */
    public String getContextVar() {
        return contextVar;
    }

    public void setContextVar(String contextVar) {
        this.contextVar = contextVar;
    }

    /**
     * Get the context relative variable source (built-in/repository id) DOC jding Comment method "getContextSource".
     *
     * @return
     */
    public String getContextSource() {
        return contextSource;
    }

    public void setContextSource(String contextSource) {
        this.contextSource = contextSource;
    }

    /**
     * Get the resource path DOC jding Comment method "getResourceDepPath".
     *
     * @return
     */
    public String getResourceDepPath() {
        return resourceDepPath;
    }

    public void setResourceDepPath(String resourceDepPath) {
        this.resourceDepPath = resourceDepPath;
    }

    /**
     * @return the selectedVersion
     */
    public String getSelectedVersion() {
        return selectedVersion;
    }

    /**
     * @param selectedVersion
     *            the selectedVersion to set
     */
    public void setSelectedVersion(String selectedVersion) {
        this.selectedVersion = selectedVersion;
    }

    /**
     * @return the isBuiltIn
     */
    public boolean isBuiltIn() {
        return isBuiltIn;
    }

    /**
     * @param isBuiltIn
     *            the isBuiltIn to set
     */
    public void setBuiltIn(boolean isBuiltIn) {
        this.isBuiltIn = isBuiltIn;
    }


    /**
     * @return the classPathUrl without version
     */
    public String getPathUrl() {
        // will always be resouceClasspath + fileName (test.txt) not include version
        return JavaResourcesHelper.getResouceClasspath(item, RelationshipItemBuilder.LATEST_VERSION);
    }

    /**
     * @return the versions
     */
    public Collection<String> getVersions() {
        final List<String> versions = new ArrayList<String>();
        try {
            for (IRepositoryViewObject obj : ProxyRepositoryFactory.getInstance().getAllVersion(
                item.getProperty().getId())) {
                versions.add(obj.getVersion());
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        versions.add(LATEST_VERSION);
        Collections.sort(versions);
        return versions;
    }

    /**
     * @return the refNodes
     */
    public Collection<String> getRefNodes() {
        return refNodes;
    }

    @Override
    public String toString() {
        return getItem().getProperty().getDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JobResourceDependencyModel) {
            final JobResourceDependencyModel model = (JobResourceDependencyModel) obj;
            return model.getItem().getProperty().getId().equals(item.getProperty().getId());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return item.getProperty().getId().hashCode();
    }

}
