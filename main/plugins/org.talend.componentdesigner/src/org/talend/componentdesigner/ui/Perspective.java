// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.talend.componentdesigner.ui.view.ComponentNavigator;

/**
 * The class define for the amc perspective. <br/>
 * 
 * $Id: Perspective.java,v 1.9 2007/03/23 07:48:54 pub Exp $
 * 
 */
public class Perspective implements IPerspectiveFactory {

    public static final String ID = "org.talend.componentdesigner.perspective"; //$NON-NLS-1$

    public void createInitialLayout(IPageLayout layout) {
        layout.setEditorAreaVisible(false);
        IFolderLayout leftTopLayout = layout.createFolder("navigatorLayout", IPageLayout.LEFT, new Float(0.3), //$NON-NLS-1$
                IPageLayout.ID_EDITOR_AREA);
        leftTopLayout.addView(ComponentNavigator.ID);
    }

}
