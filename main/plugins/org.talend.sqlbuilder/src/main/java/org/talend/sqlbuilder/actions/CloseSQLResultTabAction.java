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
package org.talend.sqlbuilder.actions;

import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.util.URLUtil;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: CloseSQLResultTabAction.java,v 1.6 2006/11/03 10:09:21 yi.zhang Exp $
 *
 */
public class CloseSQLResultTabAction extends Action {

    private ImageDescriptor img = getDescriptor(Messages.getString("Images.CloseIcon")); //$NON-NLS-1$

    /**
     * Holds a reference to the TableItem we should close.
     */
    private CTabItem tabItem;

    /**
     * Default Constructor.
     *
     */
    public CloseSQLResultTabAction() {
        super();
    }

    /**
     * Constructor.
     *
     * @param tabItem
     */
    public CloseSQLResultTabAction(CTabItem tabItem) {
        super();
        this.tabItem = tabItem;
    }

    public ImageDescriptor getHoverImageDescriptor() {
        return img;
    }

    public ImageDescriptor getImageDescriptor() {
        return img;
    }

    public String getToolTipText() {
        return "Close"; //$NON-NLS-1$
    }

    private ImageDescriptor getDescriptor(String path) {
        if (path == null || path.trim().length() == 0) {
            SqlBuilderPlugin.log(Messages.getString("CloseSQLResultTabAction.logMessageMissingImagePath", path), null); //$NON-NLS-1$
            return null;
        }

        // create image
        URL url = URLUtil.getFragmentResourceURL(SqlBuilderPlugin.PLUGIN_ID, path);
        return ImageDescriptor.createFromURL(url);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

            public void run() {
                tabItem.dispose();
            }
        });
    }
}
