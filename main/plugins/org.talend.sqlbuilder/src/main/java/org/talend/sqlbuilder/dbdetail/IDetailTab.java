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
package org.talend.sqlbuilder.dbdetail;

import org.eclipse.swt.widgets.Composite;
import org.talend.sqlbuilder.dbstructure.nodes.INode;

/**
 * Interface for all the Tabs used in the DB details composite. <br/>
 *
 * $Id: IDetailTab.java,v 1.3 2006/10/31 10:09:05 qianbing Exp $
 *
 */
public interface IDetailTab {

    public void setNode(INode node);

    /**
     * @return label text for this tab.
     */
    public String getLabelText();

    /**
     * @return tooltip text for this tab.
     */
    public String getLabelToolTipText();

    /**
     * Fill composite with information..
     *
     * @param composite Composite
     */
    public void fillComposite(Composite composite);

    /**
     * @return string, usually shown at bottom of tab
     */
    public String getStatusMessage();

    /**
     * Refresh tab.
     */
    public void refresh();
}
