// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.component.ui.wizard.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.talend.component.ui.wizard.model.FakeElement;
import org.talend.component.ui.wizard.ui.common.GenericWizardPage;
import org.talend.component.ui.wizard.ui.context.ContextComposite;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.properties.ConnectionItem;

/**
 * 
 * created by ycbai on 2015年9月21日 Detailled comment
 *
 */
public class GenericConnWizardPage extends GenericWizardPage {

    private DynamicComposite dynamicComposite;

    public GenericConnWizardPage(ConnectionItem connectionItem, boolean isRepositoryObjectEditable, String[] existingNames,
            boolean creation, Form form, ComponentService compService, boolean addContextFields) {
        super(connectionItem, isRepositoryObjectEditable, existingNames, creation, form, compService, addContextFields);
    }

    @Override
    public void createControl(final Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        container.setLayout(new FormLayout());
        setControl(container);

        Element element = new FakeElement(form.getName());
        dynamicComposite = new DynamicComposite(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, EComponentCategory.BASIC,
                element, true, container.getBackground(), form);
        dynamicComposite.setLayoutData(createMainFormData(addContextFields));
        dynamicComposite.setConnectionItem(connectionItem);
        addCheckListener(dynamicComposite.getChecker());

        if (addContextFields) {
            Composite contextParentComp = new Composite(container, SWT.NONE);
            contextParentComp.setLayoutData(createFooterFormData(dynamicComposite));
            contextParentComp.setLayout(new GridLayout());
            ContextComposite contextComp = addContextFields(contextParentComp);
            contextComp.addPropertyChangeListener(dynamicComposite);
            contextComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }
    }

    private FormData createMainFormData(boolean addContextSupport) {
        FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(0, 0);
        if (addContextSupport) {
            data.bottom = new FormAttachment(85, 0);
        } else {
            data.bottom = new FormAttachment(100, 0);
        }
        return data;
    }

    private FormData createFooterFormData(Composite topComposite) {
        FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(topComposite, 0);
        data.bottom = new FormAttachment(100, 0);
        return data;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            parameters = dynamicComposite.resetParameters();
            dynamicComposite.refresh();
            updateContextFields();
        }
    }

}
