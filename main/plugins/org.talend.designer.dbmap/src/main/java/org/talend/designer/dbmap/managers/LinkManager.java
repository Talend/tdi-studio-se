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
package org.talend.designer.dbmap.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.visualmap.link.IMapperLink;
import org.talend.designer.dbmap.model.tableentry.InputColumnTableEntry;
import org.talend.designer.dbmap.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.dbmap.model.tableentry.VarTableEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: LinkManager.java 1309 2007-01-05 17:04:10Z amaumont $
 *
 */
public class LinkManager extends org.talend.designer.abstractmap.managers.LinkManager {

    /**
     * DOC amaumont Comment method "registerLevelForNewLink".
     *
     * @param link
     * @param graphicalLinksFromTarget
     */
    protected void registerLevelForNewLink(IMapperLink link, Set<IMapperLink> graphicalLinksFromTarget) {
        boolean hasAlreadyInputTarget = false;
        boolean hasAlreadyVarTarget = false;
        ITableEntry sourceEntry = link.getPointLinkDescriptor1().getTableEntry();
        ITableEntry targetEntry = link.getPointLinkDescriptor2().getTableEntry();
        boolean hasSameZone = sourceEntry.getClass() == targetEntry.getClass();
        boolean isInput = targetEntry instanceof InputColumnTableEntry;
        boolean isOutput = targetEntry instanceof OutputColumnTableEntry;
        if (hasSameZone) {

            List<List<IMapperLink>> leveledLinks = null;
            if (isInput) {
                leveledLinks = getInputLinksForLevels();
            }
            if (isOutput) {
                leveledLinks = getOutputLinksForLevels();
            }

            int lstSize = leveledLinks.size();
            for (int indexOfLeveledLink = 0; indexOfLeveledLink < lstSize; indexOfLeveledLink++) {
                List<IMapperLink> linksFromLevelsList = leveledLinks.get(indexOfLeveledLink);
                if (linksFromLevelsList != null && linksFromLevelsList.size() > 0) {
                    IMapperLink linkFromLevelsList = linksFromLevelsList.get(0);
                    ITableEntry sourceTableEntry = linkFromLevelsList.getPointLinkDescriptor1().getTableEntry();
                    ITableEntry targetTableEntry = linkFromLevelsList.getPointLinkDescriptor2().getTableEntry();
                    if (targetEntry == targetTableEntry) {

                        if (sourceTableEntry instanceof InputColumnTableEntry
                                && targetTableEntry instanceof InputColumnTableEntry) {
                            hasAlreadyInputTarget = true;
                        }
                        if (sourceTableEntry instanceof VarTableEntry && targetTableEntry instanceof VarTableEntry) {
                            hasAlreadyVarTarget = true;
                        }
                        if (hasAlreadyInputTarget || hasAlreadyVarTarget) {
                            linksFromLevelsList.add(link);
                            link.setLevel(indexOfLeveledLink + 1);
                            break;
                        }
                    }
                }
            }

            if (isInput && !hasAlreadyInputTarget) {
                ArrayList<IMapperLink> list = new ArrayList<IMapperLink>();
                int firstEmptyIndex = searchFirstEmptyIndexLeveledList(leveledLinks);
                link.setLevel(firstEmptyIndex + 1);
                list.add(link);
                if (firstEmptyIndex < leveledLinks.size()) {
                    leveledLinks.set(firstEmptyIndex, list);
                } else {
                    leveledLinks.add(list);
                }
            }
        }
    }

    /**
     * DOC amaumont Comment method "unregisterLevelForRemovedLink".
     *
     * @param link
     * @param sourceGraphicalLinks
     */
    protected void unregisterLevelForRemovedLink(IMapperLink link, Set<IMapperLink> sourceGraphicalLinks) {
        ITableEntry targetEntry = link.getPointLinkDescriptor2().getTableEntry();
        boolean hasSameZone = link.getPointLinkDescriptor1().getTableEntry().getClass() == targetEntry.getClass();

        if (hasSameZone) {
            boolean isInput = targetEntry instanceof InputColumnTableEntry;
            boolean isOutput = targetEntry instanceof OutputColumnTableEntry;

            List<List<IMapperLink>> leveledLinks = null;
            if (isInput) {
                leveledLinks = getInputLinksForLevels();
            }

            if (isOutput) {
                leveledLinks = getOutputLinksForLevels();
            }

            boolean breakAll = false;

            int lstSize = leveledLinks.size();
            for (int indexOfLeveledLink = 0; indexOfLeveledLink < lstSize; indexOfLeveledLink++) {
                List<IMapperLink> linksFromLevelsList = leveledLinks.get(indexOfLeveledLink);
                if (linksFromLevelsList != null && linksFromLevelsList.size() > 0) {

                    int lstSizeInternal = linksFromLevelsList.size();
                    for (int i = 0; i < lstSizeInternal; i++) {
                        IMapperLink currentLink = linksFromLevelsList.get(i);
                        if (currentLink == link) {
                            linksFromLevelsList.remove(i);
                            if (linksFromLevelsList.size() == 0) {
                                leveledLinks.set(indexOfLeveledLink, null);
                            }
                            breakAll = true;
                            break;
                        }
                    } // for (int i = 0; i < lstSizeInternal; i++) {
                    if (breakAll) {
                        break;
                    }
                }
            } // for (int indexOfLeveledLink = 0; indexOfLeveledLink < lstSize; indexOfLeveledLink++) {
        }

    } // method

}
