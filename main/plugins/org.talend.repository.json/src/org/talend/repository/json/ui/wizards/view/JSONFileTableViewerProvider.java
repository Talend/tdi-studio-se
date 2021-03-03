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
package org.talend.repository.json.ui.wizards.view;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;

/**
 * wzhang class global comment. Detailled comment
 */
public class JSONFileTableViewerProvider extends LabelProvider implements ITableLabelProvider, IStructuredContentProvider {

    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    public String getColumnText(Object element, int columnIndex) {
        MetadataColumn metadataColumn = (MetadataColumn) element;
        switch (columnIndex) {
        case 0:
            return metadataColumn.getLabel();
        }
        return "<none>";
    }

    public Object[] getElements(Object inputElement) {
        List list = (List) inputElement;
        return list.toArray();
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }

}
