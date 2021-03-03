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
package org.talend.designer.webservice.ui.tree;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class WebServiceTreeLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {

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
        if (element instanceof ParameterInfo) {
            ParameterInfo para = (ParameterInfo) element;
            if (columnIndex == 0) {
                if (para.getArraySize() != 0) {
                    int size = para.getArraySize();
                    if (size == -1) {
                        return para.getName() + "(Array:" + " infinite)";
                    } else {
                        return para.getName() + "(Array:" + size + ")";
                    }

                } else if (para.getParameterInfos().size() > 0) {
                    return para.getName() + "(List)";
                } else {
                    return para.getName();
                }
            } else if (columnIndex == 1) {
                if (para.getArraySize() != 0) {
                    return para.getIndex() == null ? "*" : para.getIndex();
                } else {
                    return null;
                }
            }

        }
        return super.getText(element);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
     */
    public Color getBackground(Object element, int columnIndex) {
        if (element instanceof ParameterInfo) {
            ParameterInfo para = (ParameterInfo) element;
            if (columnIndex == 1 && para.getArraySize() == 0) {
                return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
     */
    public Color getForeground(Object element, int columnIndex) {
        if (element instanceof ParameterInfo) {
            ParameterInfo para = (ParameterInfo) element;
            if (para.getName().contains("anyType")) {
                return Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
            }
        }
        return null;
    }

}
