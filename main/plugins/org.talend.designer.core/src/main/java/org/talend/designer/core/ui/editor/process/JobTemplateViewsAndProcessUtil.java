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
package org.talend.designer.core.ui.editor.process;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.views.properties.ComponentSettingsView;

/**
 * DOC YeXiaowei class global comment. Detailled comment
 */
public class JobTemplateViewsAndProcessUtil {

    private static JobTemplateViewsAndProcessUtil instance = null;

    private static final String DEFAULT_JOB_NAME = Messages.getString("JobTemplateViewsAndProcessUtil.jobName"); //$NON-NLS-1$

    private JobTemplateViewsAndProcessUtil() {
        processInstance = new org.talend.designer.core.ui.editor.process.Process(getNewMockProperty());
    }

    public static JobTemplateViewsAndProcessUtil getInstance() {
        if (instance == null) {
            instance = new JobTemplateViewsAndProcessUtil();
        }
        return instance;
    }

    private final List<ComponentSettingsView> cacheViews = new ArrayList<ComponentSettingsView>();

    public void registerView(ComponentSettingsView view) {
        if (!cacheViews.contains(view)) {
            cacheViews.add(view);
        }
    }

    public void removeView(ComponentSettingsView view) {
        if (cacheViews.contains(view)) {
            cacheViews.remove(view);
        }
    }

    public List<ComponentSettingsView> getAllViews() {
        return this.cacheViews;
    }

    public void clearAllViews() {
        this.cacheViews.clear();
    }

    private org.talend.designer.core.ui.editor.process.Process processInstance = null;

    /**
     * Supply a single process to help doing something(such as construct a node .etc)
     * <p>
     * DOC YeXiaowei Comment method "getHelpProcess".
     *
     * @return
     */
    public org.talend.designer.core.ui.editor.process.Process getHelpProcess() {
        if (processInstance == null) {
            processInstance = new org.talend.designer.core.ui.editor.process.Process(getNewMockProperty());
        }
        return this.processInstance;
    }

    private Property getNewMockProperty() {

        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$
        property.setId("ID"); //$NON-NLS-1$
        property.setLabel(DEFAULT_JOB_NAME);

        return property;
    }

    public void setProcessLabel(String validLabel) {
        processInstance.getProperty().setLabel(validLabel);
    }

}
