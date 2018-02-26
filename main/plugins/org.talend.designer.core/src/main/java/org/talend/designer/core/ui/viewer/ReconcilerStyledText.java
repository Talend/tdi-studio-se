// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.viewer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

/**
 * DOC nrousseau ReconcilerViewer class global comment. Detailled comment
 */
public class ReconcilerStyledText extends StyledText {

    private ReconcilerViewer viewer;

    /**
     * DOC nrousseau ReconcilerStyledText constructor comment.
     * 
     * @param parent
     * @param style
     * @param reconcilerViewer TODO
     */
    public ReconcilerStyledText(Composite parent, int style, ReconcilerViewer viewer) {
        super(parent, style);
        this.viewer = viewer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.custom.StyledText#getText()
     */
    @Override
    public String getText() {
        IRegion region = viewer.getViewerRegion();
        try {
            return viewer.getDocument().get(region.getOffset(), region.getLength());
        } catch (BadLocationException e) {
            return super.getText();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.custom.StyledText#setText(java.lang.String)
     */
    @Override
    public void setText(String text) {
        super.setText(text);
        if (viewer.getUndoManager() != null) {
            viewer.getUndoManager().reset();
        }
    }
}
