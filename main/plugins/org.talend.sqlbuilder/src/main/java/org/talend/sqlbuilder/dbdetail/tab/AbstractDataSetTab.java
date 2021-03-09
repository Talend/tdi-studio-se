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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dataset.dataset.DataSet;
import org.talend.sqlbuilder.dataset.dataset.DataSetTable;

/**
 * Details Tab.
 *
 * <br/>
 *
 * $Id: AbstractDataSetTab.java,v 1.7 2006/11/01 01:24:10 yi.zhang Exp $
 *
 * @author yzhang
 *
 */
public abstract class AbstractDataSetTab extends AbstractTab {

    private DataSet pDataSet;

    private Composite pComposite;

    public final void fillDetailComposite(Composite composite) {

        try {

            pComposite = composite;

            DataSet dataSet = getCachedDataSet();
            if (dataSet == null) {
                return;
            }

            // store for later use in dataset table
            composite.setData("IDetailTab", this); //$NON-NLS-1$

            new DataSetTable(composite, dataSet, getStatusMessage(), false);

        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("AbstractDataSetTab.logMessage1"), e); //$NON-NLS-1$
            // couldn't get results.. clean mess up
            Control[] controls = composite.getChildren();
            for (int i = 0; i < controls.length; i++) {
                controls[i].dispose();
            }

            // and show error message
            Label label = new Label(composite, SWT.FILL);
            label.setText(Messages.getString("DatabaseDetailView.Tab.Unavailable") + " " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

            SqlBuilderPlugin.log(Messages.getString("AbstractDataSetTab.logMessage2"), e); //$NON-NLS-1$

        }

    }

    /**
     * Returns dataset. if it doesn't exist yet, it is initialized first.
     */
    public final DataSet getCachedDataSet() throws Exception {

        // CONSTANT_LOGGER.debug("getting cached data for " + this.getClass().getName());

        if (pDataSet != null) {
            return pDataSet;
        }

        pDataSet = getDataSet();
        return pDataSet;
    }

    /*
     * Implement this method to initialzie the dataset; @return @throws Exception
     */
    public abstract DataSet getDataSet() throws Exception;

    /**
     * Refresh the contents of the dataset.
     */
    public final void refresh() {
        pDataSet = null;

        Control[] controls = pComposite.getChildren();
        for (int i = 0; i < controls.length; i++) {
            controls[i].dispose();
        }

        fillComposite(pComposite);
        pComposite.layout();
        pComposite.redraw();
    }

    /**
     * Implement this method to add a status message on the bottom of the dataset tab.
     */
    public abstract String getStatusMessage();
}
