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
package org.talend.designer.xmlmap.ui.tabs.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.core.ui.metadata.editor.MetadataTableEditorView;

/**
 * created by Administrator on 2012-12-7 Detailled comment
 *
 */
public class XmlMapMetadataTableEditorView extends MetadataTableEditorView {

    public XmlMapMetadataTableEditorView(Composite parentComposite, int mainCompositeStyle) {
        super(parentComposite, mainCompositeStyle);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.metadata.editor.MetadataTableEditorView#initToolBar()
     */
    @Override
    protected ExtendedToolbarView initToolBar() {
        return new XmlMapMetadataToolbarEditorView(getMainComposite(), SWT.NONE, this.getExtendedTableViewer(),
                this.getCurrentDbms());
    }
}
