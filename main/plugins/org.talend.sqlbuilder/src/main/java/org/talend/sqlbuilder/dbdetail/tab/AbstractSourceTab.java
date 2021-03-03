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
package org.talend.sqlbuilder.dbdetail.tab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.sqlbuilder.Messages;

/**
 *
 * AbstractDataSetTab.
 *
 * @author yzhang
 *
 */
public abstract class AbstractSourceTab extends AbstractTab {

    private String pSource = null;

    private Text pViewer = null;

    public final void fillDetailComposite(Composite parent) {

        if (pSource == null) {
            pSource = getSource();
        }

        Composite composite = new Composite(parent, SWT.FILL);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginLeft = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;

        composite.setLayout(layout);
        composite.setLayoutData(gridData);

        pViewer = new Text(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.WRAP);
        if (pSource != null) {
            pViewer.setText(pSource);
        } else {
            pViewer.setText(""); //$NON-NLS-1$
        }
        pViewer.setLayoutData(gridData);

        // add status bar labels
        String info = getStatusMessage();
        if (info == null) {
            info = ""; //$NON-NLS-1$
        }
        Label infoLabel = new Label(composite, SWT.NULL);
        infoLabel.setText(info);
        infoLabel.setLayoutData(new GridData(SWT.LEFT, SWT.NULL, true, false));
    }

    public String getLabelText() {
        return Messages.getString("DatabaseDetailView.Tab.Source"); //$NON-NLS-1$
    }

    public abstract String getSource();

    public final void refresh() {
        pSource = null;
    }

    public String getStatusMessage() {
        return Messages.getString("DatabaseDetailView.Tab.SourceFor") + " " + getNode().getQualifiedName(); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
