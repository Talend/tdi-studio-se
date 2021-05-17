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
package org.talend.sqlbuilder.dataset.dataset;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.talend.sqlbuilder.IConstants;
import org.talend.sqlbuilder.SqlBuilderPlugin;

/**
 * Label Provider for DataSet.
 *
 * @author Davy Vanherbergen
 */
/**
 * @author Davy Vanherbergen
 *
 */
public class DataSetTableLabelProvider implements ITableLabelProvider {

    private SimpleDateFormat pdateFormatter = new SimpleDateFormat(SqlBuilderPlugin.getDefault().getPluginPreferences()
            .getString(IConstants.DATASETRESULT_DATE_FORMAT));

    private DecimalFormat pdecimalFormat = new DecimalFormat();

    private boolean pformatDates = SqlBuilderPlugin.getDefault().getPluginPreferences()
            .getBoolean(IConstants.DATASETRESULT_FORMAT_DATES);

    public DataSetTableLabelProvider() {

        pdecimalFormat.setGroupingUsed(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {

        // noop
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {

        // noop

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex) {

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex) {

        DataSetRow row = (DataSetRow) element;

        Object tmp = row.getObjectValue(columnIndex);

        if (tmp != null) {

            Class clazz = tmp.getClass();

            // modified by nma, order 9183.
            // filter out scientific values
            // if (clazz == Double.class || clazz == Integer.class) {
            // return pdecimalFormat.format(tmp);
            // }

            // format dates
            // if (pformatDates && clazz == Timestamp.class) {
            // return pdateFormatter.format(new java.util.Date(((Timestamp) tmp).getTime()));
            // }
            if (pformatDates && clazz == Date.class) {
                return pdateFormatter.format(new java.util.Date(((Date) tmp).getTime()));
            }

            return tmp.toString();
        }
        return "<null>"; //$NON-NLS-1$

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    public boolean isLabelProperty(Object element, String property) {

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {

        // noop
    }

}
