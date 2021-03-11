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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.visualmap.link.ILinkState;
import org.talend.designer.abstractmap.ui.visualmap.link.IMapperLink;
import org.talend.designer.abstractmap.ui.visualmap.link.LinkState;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: LinkManager.java 1309 2007-01-05 17:04:10Z amaumont $
 *
 */
public abstract class LinkManager implements ILinkManager {

    private static final Comparator<IMapperLink> COMPARATOR = new Comparator<IMapperLink>() {

        public int compare(IMapperLink link1, IMapperLink link2) {
            if (link1.getState() == link2.getState()) {
                return 0;
            }
            if (link1.getState() == LinkState.SELECTED) {
                return 1;
            }
            return -1;
        }

    };

    private List<IMapperLink> links = new ArrayList<IMapperLink>();

    private int currentNumberLinks = 0;

    private Map<ITableEntry, Set<ITableEntry>> targetToSources = new HashMap<ITableEntry, Set<ITableEntry>>();

    private Map<ITableEntry, Set<IMapperLink>> sourceTableEntryToLinks = new HashMap<ITableEntry, Set<IMapperLink>>();

    private Map<ITableEntry, Set<IMapperLink>> targetTableEntryToLinks = new HashMap<ITableEntry, Set<IMapperLink>>();

    // levels

    private Map<IMapperLink, Integer> linkToIndexLevel = new HashMap<IMapperLink, Integer>();

    private List<List<IMapperLink>> inputLinksForLevels = new ArrayList<List<IMapperLink>>();

    private List<List<IMapperLink>> varLinksForLevels = new ArrayList<List<IMapperLink>>();

    private List<List<IMapperLink>> outputLinksForLevels = new ArrayList<List<IMapperLink>>();

    public LinkManager() {
        super();
        currentNumberLinks = 0;
    }

    /**
     * DOC amaumont Comment method "addLink".
     *
     * @param link
     */
    public void addLink(IMapperLink link) {
        currentNumberLinks++;
        // System.out.println(currentNumberLinks + " links");

        links.add(link);
        ITableEntry sourceITableEntry = link.getPointLinkDescriptor1().getTableEntry();
        ITableEntry targetITableEntry = link.getPointLinkDescriptor2().getTableEntry();

        Set<ITableEntry> sourcesDataMapTableEntries = getSourcesCollection(targetITableEntry);
        sourcesDataMapTableEntries.add(sourceITableEntry);

        Set<IMapperLink> graphicalLinksFromTarget = getGraphicalLinksFromTarget(targetITableEntry);
        registerLevelForNewLink(link, graphicalLinksFromTarget);

        graphicalLinksFromTarget.add(link);

        Set<IMapperLink> graphicalLinksFromSources = getGraphicalLinksFromSource(sourceITableEntry);
        graphicalLinksFromSources.add(link);
    }

    /**
     * DOC amaumont Comment method "registerLevelForNewLink".
     *
     * @param link
     * @param graphicalLinksFromTarget
     */
    protected abstract void registerLevelForNewLink(IMapperLink link, Set<IMapperLink> graphicalLinksFromTarget);

    /**
     * DOC amaumont Comment method "searchFirstFreeIndexLeveledList".
     *
     * @param leveledLinks
     */
    protected int searchFirstEmptyIndexLeveledList(List<List<IMapperLink>> leveledLinks) {

        int freeIndex = leveledLinks.size();

        int lstSize = leveledLinks.size();
        for (int i = 0; i < lstSize; i++) {
            if (leveledLinks.get(i) == null) {
                freeIndex = i;
                break;
            }

        }
        return freeIndex;

    }

    /**
     * DOC amaumont Comment method "addLink".
     *
     * @param link
     */
    public void removeLink(IMapperLink link) {
        currentNumberLinks--;

        links.remove(link);
        ITableEntry sourceITableEntry = link.getPointLinkDescriptor1().getTableEntry();
        ITableEntry targetITableEntry = link.getPointLinkDescriptor2().getTableEntry();
        Set<ITableEntry> targetDataMapTableEntries = getSourcesCollection(targetITableEntry);
        targetDataMapTableEntries.remove(sourceITableEntry);
        Set<IMapperLink> sourceGraphicalLinks = getGraphicalLinksFromSource(sourceITableEntry);
        sourceGraphicalLinks.remove(link);
        getGraphicalLinksFromTarget(targetITableEntry).remove(link);

        unregisterLevelForRemovedLink(link, sourceGraphicalLinks);

    }

    /**
     * DOC amaumont Comment method "unregisterLevelForRemovedLink".
     *
     * @param link
     * @param sourceGraphicalLinks
     */
    protected abstract void unregisterLevelForRemovedLink(IMapperLink link, Set<IMapperLink> sourceGraphicalLinks);

    /**
     * DOC amaumont Comment method "getGraphicalLinks".
     *
     * @param targetTableEntry
     * @return
     */
    private Set<IMapperLink> getGraphicalLinksFromTarget(ITableEntry dataMapTableEntry) {
        Set<IMapperLink> graphicalLinks = targetTableEntryToLinks.get(dataMapTableEntry);
        if (graphicalLinks == null) {
            graphicalLinks = new HashSet<IMapperLink>();
            targetTableEntryToLinks.put(dataMapTableEntry, graphicalLinks);
        }
        return graphicalLinks;
    }

    /**
     * DOC amaumont Comment method "getGraphicalLinks".
     *
     * @param targetTableEntry
     * @return
     */
    public Set<IMapperLink> getLinksFromTarget(ITableEntry dataMapTableEntry) {
        return new HashSet<IMapperLink>(getGraphicalLinksFromTarget(dataMapTableEntry));
    }

    /**
     * DOC amaumont Comment method "getGraphicalLinks".
     *
     * @param targetTableEntry
     * @return
     */
    private Set<IMapperLink> getGraphicalLinksFromSource(ITableEntry dataMapTableEntry) {
        Set<IMapperLink> graphicalLinks = sourceTableEntryToLinks.get(dataMapTableEntry);
        if (graphicalLinks == null) {
            graphicalLinks = new HashSet<IMapperLink>();
            sourceTableEntryToLinks.put(dataMapTableEntry, graphicalLinks);
        }
        return graphicalLinks;
    }

    public Set<IMapperLink> getLinksFromSource(ITableEntry dataMapTableEntry) {
        return new HashSet<IMapperLink>(getGraphicalLinksFromSource(dataMapTableEntry));
    }

    /**
     * DOC amaumont Comment method "getSourcesCollection".
     *
     * @param targetITableEntry
     * @return
     */
    private Set<ITableEntry> getSourcesCollection(ITableEntry targetITableEntry) {
        Set<ITableEntry> targetDataMapTableEntries = targetToSources.get(targetITableEntry);
        if (targetDataMapTableEntries == null) {
            targetDataMapTableEntries = new HashSet<ITableEntry>();
            targetToSources.put(targetITableEntry, targetDataMapTableEntries);
        }
        return targetDataMapTableEntries;
    }

    /**
     * DOC amaumont Comment method "clearLinks".
     */
    public void clearLinks() {
        links.clear();
        targetToSources.clear();
        linkToIndexLevel.clear();
    }

    /**
     * DOC amaumont Comment method "getLinks".
     *
     * @return
     */
    public List<IMapperLink> getLinks() {
        return this.links;
    }

    /**
     * DOC amaumont Comment method "getSourcesForTarget".
     *
     * @param dataMapTableEntry
     */
    public Set<ITableEntry> getSourcesForTarget(ITableEntry dataMapTableEntry) {
        return Collections.unmodifiableSet(getSourcesCollection(dataMapTableEntry));

    }

    /**
     * DOC amaumont Comment method "orderLinks".
     */
    public void orderLinks() {
        Collections.sort(links, COMPARATOR);
    }

    /**
     * Getter for currentNumberLinks.
     *
     * @return the currentNumberLinks
     */
    public int getCurrentNumberLinks() {
        return this.currentNumberLinks;
    }

    /**
     * Get the count of inputs levels.
     *
     * @return the count of inputs levels
     */
    public int getCountOfInputLevels() {
        return this.inputLinksForLevels.size();
    }

    /**
     * Getter for inputLinksForLevels.
     *
     * @return the inputLinksForLevels
     */
    protected List<List<IMapperLink>> getInputLinksForLevels() {
        return this.inputLinksForLevels;
    }

    /**
     * Getter for varLinksForLevels.
     *
     * @return the varLinksForLevels
     */
    protected List<List<IMapperLink>> getVarLinksForLevels() {
        return this.varLinksForLevels;
    }

    /**
     * Getter for outputLinksForLevels.
     *
     * @return the outputLinksForLevels
     */
    protected List<List<IMapperLink>> getOutputLinksForLevels() {
        return this.outputLinksForLevels;
    }

}
