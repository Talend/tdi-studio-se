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
package org.talend.sqlbuilder.dbdetail.tab;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.talend.sqlbuilder.dbdetail.IDetailTab;
import org.talend.sqlbuilder.dbstructure.nodes.INode;

/**
 * @author Davy Vanherbergen
 *
 */
public abstract class AbstractTab implements IDetailTab {

    private INode pNode;

    public final void setNode(INode node) {
        pNode = node;
    }

    public final INode getNode() {
        return pNode;
    }

    public abstract void fillDetailComposite(Composite composite);

    public final void fillComposite(final Composite composite) {

        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

            public void run() {
                fillDetailComposite(composite);
            }
        });

    }

    public abstract String getLabelText();

    /*
     * (non-Javadoc)
     *
     * @see net.sourceforge.sqlexplorer.dbdetail.IDetailTab#getLabelToolTipText()
     */
    public String getLabelToolTipText() {
        return getLabelText();
    }

}
