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
package org.talend.designer.core.ui.views.properties;

import java.util.List;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.process.Element;
import org.talend.designer.core.ui.editor.process.JobTemplateViewsAndProcessUtil;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ComponentSettings {

    public static void switchToCurComponentSettingsView() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
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
}
