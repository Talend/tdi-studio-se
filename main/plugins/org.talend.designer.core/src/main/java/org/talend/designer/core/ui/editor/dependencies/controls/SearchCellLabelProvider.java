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
package org.talend.designer.core.ui.editor.dependencies.controls;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public abstract class SearchCellLabelProvider extends StyledCellLabelProvider implements ILabelProvider {

    private static Color hightLight;
    private static Font builtInFont;

    private String filterString;
    private boolean showBuiltIn = true;

    public SearchCellLabelProvider(final StructuredViewer structuredViewer) {
        if (hightLight == null) {
            hightLight = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
        }
        if (builtInFont == null) {
            FontData[] fontData = structuredViewer.getControl().getFont().getFontData();
            fontData[0].setStyle(fontData[0].getStyle() | SWT.ITALIC);
            builtInFont = new Font(Display.getCurrent(), fontData[0]);
        }
        structuredViewer.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (!showBuiltIn && isBuiltIn(element)) {
                    return false;
                }
                if (filterString == null || filterString.isEmpty()) {
                    return true;
                }
                return getText(element).contains(filterString);
            }
        });
    }

    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }

    public void setShowBuiltIn(boolean showBuiltIn) {
        this.showBuiltIn = showBuiltIn;
    }

    @Override
    public String getText(Object element) {
        return element.toString();
    }

    //@Override
    public Font getFont(Object element) {
        if (isBuiltIn(element)) {
            return builtInFont;
        }
        return null;
    }

    @Override
    public void update(ViewerCell cell) {
        Object element = cell.getElement();
        final String text = getText(element);
        cell.setText(text);
        cell.setImage(getImage(element));
        cell.setFont(getFont(element));
        if (filterString != null && !filterString.isEmpty()) {
            int filterIndex = text.indexOf(filterString);
            StyleRange styleRange = new StyleRange(filterIndex, filterString.length(), null, hightLight);
            cell.setStyleRanges(new StyleRange[] { styleRange });
        } else {
            cell.setStyleRanges(null);
        }
    }

    abstract protected boolean isBuiltIn(Object element);

}
