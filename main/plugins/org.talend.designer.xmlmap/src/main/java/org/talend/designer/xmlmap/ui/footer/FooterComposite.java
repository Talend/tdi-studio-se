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
package org.talend.designer.xmlmap.ui.footer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.talend.designer.xmlmap.ui.MapperUI;
import org.talend.designer.xmlmap.ui.footer.StatusBar.STATUS;
import org.talend.designer.xmlmap.util.problem.ProblemsAnalyser;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: FooterComposite.java 38013 2010-03-05 14:21:59Z mhirt $
 *
 */
public class FooterComposite extends Composite {

    private MapperUI mapperUi;

    private StatusBar statusBar;

    public FooterComposite(Composite parent, MapperUI mapperUi) {
        super(parent, SWT.NONE);
        this.mapperUi = mapperUi;
        createComponents();
    }

    /**
     * DOC amaumont Comment method "createComponents".
     */
    private void createComponents() {
        FormLayout formLayout = new FormLayout();
        formLayout.spacing = 15;
        this.setLayout(formLayout);
        Button applyButton = new Button(this, SWT.NONE);
        applyButton.setEnabled(!mapperUi.getMapperComponent().isReadOnly());
        applyButton.setText("Apply"); //$NON-NLS-1$
        FormData applyFormData = new FormData();
        applyFormData.width = 100;
        applyButton.setLayoutData(applyFormData);
        applyButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                mapperUi.closeMapperDialog(SWT.APPLICATION_MODAL);
            }

        });

        statusBar = new StatusBar(this, SWT.NONE);
        final ProblemsAnalyser problemsAnalyser = mapperUi.getMapperManager().getProblemsAnalyser();
        if (!problemsAnalyser.getProblems().isEmpty()) {
            statusBar.setValues(STATUS.ERROR, problemsAnalyser.getErrorMessage());
        }

        Button okButton = new Button(this, SWT.NONE);
        okButton.setEnabled(!mapperUi.getMapperComponent().isReadOnly());
        okButton.setText("OK"); //$NON-NLS-1$
        FormData okFormData = new FormData();

        okFormData.width = 100;
        okButton.setLayoutData(okFormData);
        okButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                mapperUi.closeMapperDialog(SWT.OK);
            }

        });

        Button cancelButton = new Button(this, SWT.NONE);
        cancelButton.setText("Cancel"); //$NON-NLS-1$
        cancelButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                mapperUi.closeMapperDialog(SWT.CANCEL);
            }

        });
        FormData cancelFormData = new FormData();
        cancelFormData.width = 100;
        cancelButton.setLayoutData(cancelFormData);

        cancelFormData.right = new FormAttachment(100, -5);
        okFormData.right = new FormAttachment(cancelButton, -5);
        applyFormData.right = new FormAttachment(okButton, -5);

    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

}
