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
package org.talend.designer.filemultischemas.ui.provider.property;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * cLi class global comment. Detailled comment
 */
public class SchemaDetailsViewerSorter extends ViewerSorter {

    private int direction;

    private TreeViewer sortViewer;

    private TreeColumn column;

    private int index;

    public SchemaDetailsViewerSorter(TreeViewer sortViewer, TreeColumn column, boolean direction) {
        super();
        this.sortViewer = sortViewer;
        this.column = column;
        this.direction = direction ? 1 : -1;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object,
     * java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (sortViewer != null) {
            ITableLabelProvider labelProvider = (ITableLabelProvider) sortViewer.getLabelProvider();

            final String text1 = labelProvider.getColumnText(e1, index);
            final String text2 = labelProvider.getColumnText(e2, index);

            return getComparator().compare((text1 != null ? text1 : ""), (text2 != null ? text2 : "")) * direction; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ViewerComparator#sort(org.eclipse.jface.viewers.Viewer, java.lang.Object[])
     */
    @Override
    public void sort(Viewer viewer, Object[] elements) {
        if (sortViewer != null) {
            final Tree tree = sortViewer.getTree();
            while (index < tree.getColumns().length && tree.getColumn(index) != column) {
                index++;
            }
        }
        super.sort(viewer, elements);
    }

}
