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
package org.talend.designer.mapper.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.visualmap.link.IMapperLink;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.tableentry.ExpressionFilterEntry;
import org.talend.designer.mapper.model.tableentry.GlobalMapEntry;
import org.talend.designer.mapper.model.tableentry.InputColumnTableEntry;
import org.talend.designer.mapper.model.tableentry.VarTableEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class LinkManager extends org.talend.designer.abstractmap.managers.LinkManager {

    /**
     * DOC amaumont Comment method "registerLevelForNewLink".
     *
     * @param link
     * @param graphicalLinksFromTarget
     */
    public void registerLevelForNewLink(IMapperLink link, Set<IMapperLink> graphicalLinksFromTarget) {
        boolean hasAlreadyInputTarget = false;
        boolean hasAlreadyVarTarget = false;
        ITableEntry sourceEntry = link.getPointLinkDescriptor1().getTableEntry();
        ITableEntry targetEntry = link.getPointLinkDescriptor2().getTableEntry();
        boolean hasSameZone = sourceEntry.getParent().getClass() == targetEntry.getParent().getClass();

        if (hasSameZone) {
            boolean isInputTarget = targetEntry instanceof InputColumnTableEntry
                    || targetEntry instanceof ExpressionFilterEntry
                    || targetEntry instanceof GlobalMapEntry
                    && targetEntry.getParent() instanceof InputTable;
            boolean isVarTarget = targetEntry instanceof VarTableEntry;

            List<List<IMapperLink>> leveledLinks = null;
            if (isInputTarget) {
                leveledLinks = getInputLinksForLevels();
            }

            if (isVarTarget) {
                leveledLinks = getVarLinksForLevels();
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
                                && (targetTableEntry instanceof InputColumnTableEntry || targetEntry instanceof ExpressionFilterEntry
                                        && targetEntry.getParent() instanceof InputTable)) {
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

            if (isInputTarget && !hasAlreadyInputTarget || isVarTarget && !hasAlreadyVarTarget) {
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
    public void unregisterLevelForRemovedLink(IMapperLink link, Set<IMapperLink> sourceGraphicalLinks) {
        ITableEntry targetEntry = link.getPointLinkDescriptor2().getTableEntry();
        boolean hasSameZone = link.getPointLinkDescriptor1().getTableEntry().getClass() == targetEntry.getClass();

        if (hasSameZone) {
            boolean isInput = targetEntry instanceof InputColumnTableEntry;
            boolean isVar = targetEntry instanceof VarTableEntry;

            List<List<IMapperLink>> leveledLinks = null;
            if (isInput) {
                leveledLinks = getInputLinksForLevels();
            }

            if (isVar) {
                leveledLinks = getVarLinksForLevels();
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
