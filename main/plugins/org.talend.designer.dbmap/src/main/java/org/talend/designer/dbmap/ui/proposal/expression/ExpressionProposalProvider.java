// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.ui.proposal.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.ui.proposal.ContextParameterProposal;
import org.talend.core.ui.proposal.IExternalProposals;
import org.talend.core.ui.proposal.JavaGlobalUtils;
import org.talend.core.ui.proposal.NodeReturnProposal;
import org.talend.core.ui.proposal.ProposalFactory;
import org.talend.core.ui.proposal.RoutinesFunctionProposal;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.dbmap.language.IDbLanguage;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.model.tableentry.TableEntryLocation;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.FunctionManager;
import org.talend.designer.rowgenerator.data.TalendType;

/**
 * ContentProposalProvider which initialize valid locations of Mapper. <br/>
 *
 * $Id: ExpressionProposalProvider.java 968 2006-12-12 10:59:26Z amaumont $
 *
 */
public class ExpressionProposalProvider implements IContentProposalProvider {

    private MapperManager mapperManager;

    private List<IDataMapTable> tables;

    private IDbLanguage currentLanguage;

    private IContentProposalProvider[] otherContentProposalProviders;

    private ITableEntry currentModifiedEntry;

    /**
     * Constructs a new ProcessProposalProvider.
     *
     * @param tables
     * @param control
     */
    public ExpressionProposalProvider(MapperManager mapperManager,
            IContentProposalProvider[] otherContentProposalProviders) {
        super();
        this.mapperManager = mapperManager;
        this.currentLanguage = mapperManager.getCurrentLanguage();
        this.otherContentProposalProviders = otherContentProposalProviders;
    }

    public void init(IDataMapTable currentTable, Zone[] zones, ITableEntry currentEntry) {

        tables = new ArrayList<IDataMapTable>();
        for (int i = 0; i < zones.length; i++) {
            if (zones[i] == Zone.INPUTS) {
                tables.addAll(mapperManager.getInputTables());
            } else if (zones[i] == Zone.OUTPUTS) {
                tables.addAll(mapperManager.getOutputTables());
            }
        }
        this.currentModifiedEntry = currentEntry;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposalProvider#getProposals(java.lang.String, int)
     */
    public IContentProposal[] getProposals(String contents, int position) {
        List<IContentProposal> proposals = new ArrayList<IContentProposal>();

        TableEntryLocation sourceEntryLocation = new TableEntryLocation();

        // Proposals based on process context
        for (IDataMapTable table : this.tables) {
            // proposals.add(new TableContentProposal(table, this.currentLanguage));
            List<IColumnEntry> dataMapTableEntries = table.getColumnEntries();
            for (IColumnEntry entrySource : dataMapTableEntries) {

                sourceEntryLocation.tableName = entrySource.getParentName();
                sourceEntryLocation.columnName = entrySource.getName();
                if (mapperManager.getUiManager().checkSourceLocationIsValid(entrySource, currentModifiedEntry)) {
                    proposals.add(new EntryContentProposal(entrySource, this.currentLanguage));
                }
            }
        }

        for (IContentProposalProvider contentProposalProvider : otherContentProposalProviders) {
            proposals.addAll(Arrays.asList(contentProposalProvider.getProposals(contents, position)));
        }
        
        proposals.addAll(getContextProposal());
        
        IContentProposal[] res = new IContentProposal[proposals.size()];
        res = proposals.toArray(res);
        return res;
    }
    
    private List<IContentProposal> getContextProposal() {
    	List<IContentProposal> proposals = new ArrayList<IContentProposal>();
    	IProcess process = mapperManager.getComponent().getProcess();
    	if(mapperManager == null || mapperManager.getComponent() == null) {
    		return proposals;
    	}
        if (process != null) {
            // Proposals based on process context
            List<IContextParameter> ctxParams = process.getContextManager().getDefaultContext().getContextParameterList();
            for (IContextParameter ctxParam : ctxParams) {
                proposals.add(new ContextParameterProposal(ctxParam));
            }

            // Proposals based on global variables
            List<? extends INode> nodes = process.getGraphicalNodes();
            for (INode node : nodes) {
                List<? extends INodeReturn> nodeReturns = node.getReturns();
                for (INodeReturn nodeReturn : nodeReturns) {
                    proposals.add(new NodeReturnProposal(node, nodeReturn));
                }
            }

        } else {
            List<ContextItem> allContextItem = ContextUtils.getAllContextItem();
            List<IContextParameter> ctxParams = new ArrayList<IContextParameter>();
            if (allContextItem != null) {
                for (ContextItem item : allContextItem) {
                    List<IContextParameter> tmpParams = new JobContextManager(item.getContext(), item.getDefaultContext())
                            .getDefaultContext().getContextParameterList();
                    ctxParams.addAll(tmpParams);
                }
            }
            for (IContextParameter ctxParam : ctxParams) {
                proposals.add(new ContextParameterProposal(ctxParam));
            }
        }

        // Proposals based on global variables(only perl ).
        // add proposals on global variables in java (bugtracker 2554)
        // add variables in java
        IContentProposal[] javavars = JavaGlobalUtils.getProposals();
        for (IContentProposal javavar : javavars) {
            proposals.add(javavar);
        }

        // Proposals based on routines
        FunctionManager functionManager = new FunctionManager();

        List<TalendType> talendTypes = functionManager.getTalendTypes();
        for (TalendType type : talendTypes) {
            for (Object objectFunction : type.getFunctions()) {
                Function function = (Function) objectFunction;
                proposals.add(new RoutinesFunctionProposal(function));
            }
        }

        for (IExternalProposals externalProposals : ProposalFactory.getInstances()) {
            proposals.addAll(externalProposals.getStandardProposals());
        }

        // sort the list
        Collections.sort(proposals, new Comparator<IContentProposal>() {

            @Override
            public int compare(IContentProposal arg0, IContentProposal arg1) {
                return compareRowAndContextProposal(arg0.getLabel(), arg1.getLabel());
            }

        });
        return proposals;
    }
    
    private int compareRowAndContextProposal(String label0, String label1) {
        if (label0.startsWith("$row[") && label1.startsWith("context")) { //$NON-NLS-1$ //$NON-NLS-2$
            return 1;
        } else if (label1.startsWith("$row[") && label0.startsWith("context")) { //$NON-NLS-1$ //$NON-NLS-2$
            return -1;
        } else {
            return label0.compareToIgnoreCase(label1);
        }
    }

}
