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
package org.talend.designer.abstractmap.managers;

import java.util.List;
import java.util.Set;

import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.visualmap.link.IMapperLink;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public interface ILinkManager {

    /**
     * DOC amaumont Comment method "addLink".
     *
     * @param link
     */
    public void addLink(IMapperLink link);

    /**
     * DOC amaumont Comment method "addLink".
     *
     * @param link
     */
    public void removeLink(IMapperLink link);

    /**
     * DOC amaumont Comment method "getGraphicalLinks".
     *
     * @param targetTableEntry
     * @return
     */
    public Set<IMapperLink> getLinksFromTarget(ITableEntry dataMapTableEntry);

    public Set<IMapperLink> getLinksFromSource(ITableEntry dataMapTableEntry);

    /**
     * DOC amaumont Comment method "clearLinks".
     */
    public void clearLinks();

    /**
     * DOC amaumont Comment method "getLinks".
     *
     * @return
     */
    public List<IMapperLink> getLinks();

    /**
     * DOC amaumont Comment method "getSourcesForTarget".
     *
     * @param dataMapTableEntry
     */
    public Set<ITableEntry> getSourcesForTarget(ITableEntry dataMapTableEntry);

    /**
     * DOC amaumont Comment method "orderLinks".
     */
    public void orderLinks();

    /**
     * Getter for currentNumberLinks.
     *
     * @return the currentNumberLinks
     */
    public int getCurrentNumberLinks();

    /**
     * Get the count of inputs levels.
     *
     * @return the count of inputs levels
     */
    public int getCountOfInputLevels();

}
