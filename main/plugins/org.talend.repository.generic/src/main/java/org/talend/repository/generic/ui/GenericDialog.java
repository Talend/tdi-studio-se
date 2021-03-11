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
package org.talend.repository.generic.ui;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.daikon.properties.presentation.Form;
import org.talend.designer.core.model.FakeElement;
import org.talend.repository.generic.internal.service.GenericWizardInternalService;

/**
 * created by ycbai on 2015年10月12日 Detailled comment
 *
 */
public class GenericDialog extends TitleAreaDialog {

    private Form form;

    private ComponentService componentService;

    private ConnectionItem connectionItem;

    private DynamicComposite dynamicComposite;

    public GenericDialog(Shell parentShell, Form form) {
        super(parentShell);
        this.form = form;
        componentService = new GenericWizardInternalService().getComponentService();
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MIN | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(form.getTitle());
        setHelpAvailable(false);
    }

    @Override
    public void create() {
        super.create();
        setTitle(form.getTitle());
        setMessage(form.getSubtitle());
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        Composite comp = new Composite(composite, SWT.NONE);
        comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        comp.setLayout(new FormLayout());

        Element element = new FakeElement(form.getName());
        dynamicComposite = new DynamicComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, EComponentCategory.BASIC,
                element, true, comp.getBackground(), form, true);
        FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(0, 0);
        data.bottom = new FormAttachment(100, 0);
        dynamicComposite.setLayoutData(data);
        dynamicComposite.setConnectionItem(connectionItem);

        init();

        return parent;
    }

    private void init() {
        if (form.isCallBeforeFormPresent()) {
            try {
                componentService.beforeFormPresent(form.getName(), form.getProperties());
            } catch (Throwable e) {
                ExceptionHandler.process(e);
            }
        }
        dynamicComposite.resetParameters(false);
        dynamicComposite.refresh();
    }

    @Override
    protected void initializeBounds() {
        super.initializeBounds();
        Point size = getShell().getSize();
        Point location = getInitialLocation(size);
        getShell().setBounds(getConstrainedShellBounds(new Rectangle(location.x, location.y, size.x, size.y)));
    }

    @Override
    protected void okPressed() {
        if (form.isCallAfterFormFinish()) {
            try {
                componentService.afterFormFinish(form.getName(), form.getProperties());
            } catch (Throwable e) {
                ExceptionHandler.process(e);
            }
        }
        super.okPressed();
    }

    @Override
    protected void cancelPressed() {
        componentService.cancelFormValues(form.getProperties(), form.getName());
        super.cancelPressed();
    }

    public void setConnectionItem(ConnectionItem connectionItem) {
        this.connectionItem = connectionItem;
    }

}
