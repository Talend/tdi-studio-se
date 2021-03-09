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
package org.talend.designer.xmlmap.ui.tabs.table.toolbar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.advanced.dataeditor.control.ExtendedPushButton;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;

/**
 * created by Administrator on 2013-1-25 Detailled comment
 *
 */
public class XmlMapExtendedToolbarView extends ExtendedToolbarView {

    private PreviousPageButton previousButton;

    private NextPageButton nextButton;

    /**
     * DOC Administrator XmlMapExtendedToolbarView constructor comment.
     *
     * @param parent
     * @param style
     * @param extendedTableViewer
     */
    public XmlMapExtendedToolbarView(Composite parent, int style, AbstractExtendedTableViewer extendedTableViewer) {
        super(parent, style, extendedTableViewer);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView#createComponents(org.eclipse.swt.widgets.Composite
     * )
     */
    @Override
    protected void createComponents(Composite parent) {
        previousButton = new PreviousPageButton(parent, getExtendedTableViewer());
        nextButton = new NextPageButton(parent, getExtendedTableViewer());

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView#getButtons()
     */
    @Override
    public List<ExtendedPushButton> getButtons() {
        List<ExtendedPushButton> buttons = new ArrayList<ExtendedPushButton>();
        buttons.add(previousButton);
        buttons.add(nextButton);
        return buttons;
    }

}
