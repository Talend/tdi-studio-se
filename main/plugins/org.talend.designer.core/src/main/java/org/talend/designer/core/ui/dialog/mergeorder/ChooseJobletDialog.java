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
package org.talend.designer.core.ui.dialog.mergeorder;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class ChooseJobletDialog extends Dialog {

    private Point point;

    private Button jobletBut;

    private Button jobBut;

    private boolean addJoblet = true;

    public ChooseJobletDialog(Shell parentShell, Point point) {
        super(parentShell);
        this.point = point;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite createDialogArea = (Composite) super.createDialogArea(parent);
        createDialogArea.setLayout(new FillLayout());
        Composite composite = new Composite(createDialogArea, SWT.NONE);
        composite.pack();
        GridLayout simGrid = new GridLayout();
        composite.setLayout(simGrid);
        jobletBut = new Button(composite, SWT.RADIO);
        jobletBut.setSelection(true);
        jobletBut.setText("Add to joblet");
        jobBut = new Button(composite, SWT.RADIO);
        GridData titleData = new GridData(GridData.FILL_BOTH);
        jobletBut.setLayoutData(titleData);
        jobBut.setText("Add to job");
        jobBut.setLayoutData(titleData);
        addListeners();
        return createDialogArea;
    }

    private void addListeners() {
        jobletBut.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                addJoblet = jobletBut.getSelection();
            }

        });

        jobBut.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                addJoblet = !jobBut.getSelection();
            }
        });
    }

    @Override
    protected void okPressed() {
        super.okPressed();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("You want to add component to job/joblet?");
        int diaX = point.x + 200;
        int diaY = point.y + 150;
        newShell.setBounds(diaX, diaY, 400, 200);
    }

    public boolean addToJoblet() {
        return addJoblet;
    }
}
