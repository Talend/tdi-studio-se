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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.runtime.projectsetting.AbstractProjectSettingPage;
import org.talend.repository.i18n.Messages;

public class JavaVersionProjectSettingPage extends AbstractProjectSettingPage {

    private Combo combo;

    private String version;

    public JavaVersionProjectSettingPage() {
        noDefaultAndApplyButton();
    }

    @Override
    protected void createFieldEditors() {
        version = JavaUtils.getProjectJavaVersion();
        if (version == null) {
            version = JavaUtils.DEFAULT_VERSION;
            JavaUtils.updateProjectJavaVersion(version);
        }
        Composite parent = getFieldEditorParent();
        parent.setLayout(new GridLayout(2, true));
        Label label = new Label(parent, SWT.NONE);
        label.setText(Messages.getString("JavaVersionProjectSettingPage.versionLabel")); //$NON-NLS-1$
        GridData labelData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1);
        label.setLayoutData(labelData);
        combo = new Combo(parent, SWT.READ_ONLY);
        GridData comboData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1);
        GC gc = new GC(combo);
        Point labelSize = gc.stringExtent(JavaUtils.DEFAULT_VERSION);
        gc.dispose();
        int hint = labelSize.x + (ITabbedPropertyConstants.HSPACE * 14);
        comboData.widthHint = hint;
        combo.setLayoutData(comboData);
        combo.setItems(JavaUtils.AVAILABLE_VERSIONS.toArray(new String[] {}));
        combo.select(JavaUtils.AVAILABLE_VERSIONS.indexOf(version));
    }

    @Override
    public boolean performOk() {
        if (combo == null) {
            return true;
        }
        String newVersion = combo.getItem(combo.getSelectionIndex());
        if (!newVersion.equals(version)) {
            JavaUtils.updateProjectJavaVersion(newVersion);
        }
        return true;
    }

}
