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
package org.talend.designer.mapper.ui.proposal.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.talend.commons.runtime.model.expressionbuilder.Variable;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.mapper.language.ILanguage;
import org.talend.designer.mapper.language.LanguageProvider;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.model.table.AbstractDataMapTable;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.VarsTable;
import org.talend.designer.mapper.model.tableentry.AbstractInOutTableEntry;
import org.talend.designer.mapper.model.tableentry.GlobalMapEntry;
import org.talend.designer.mapper.model.tableentry.TableEntryLocation;
import org.talend.designer.mapper.model.tableentry.VarTableEntry;
import org.talend.designer.mapper.ui.visualmap.zone.Zone;

/**
 * ContentProposalProvider which initialize valid locations of Mapper. <br/>
 *
 * $Id$
 *
 */
public class ExpressionProposalProvider implements IContentProposalProvider {

    private final MapperManager mapperManager;

    private List<AbstractDataMapTable> tables;

    private final ILanguage currentLanguage;

    private final IContentProposalProvider[] otherContentProposalProviders;

    private ITableEntry currentModifiedEntry;

    /**
     * Constructs a new ProcessProposalProvider.
     *
     * @param tables
     * @param control
     */
    public ExpressionProposalProvider(MapperManager mapperManager, IContentProposalProvider[] otherContentProposalProviders) {
        super();
        this.mapperManager = mapperManager;
        this.currentLanguage = LanguageProvider.getCurrentLanguage();
        this.otherContentProposalProviders = otherContentProposalProviders;
    }

    public void init(IDataMapTable currentTable, Zone[] zones, ITableEntry currentEntry) {

        tables = new ArrayList<AbstractDataMapTable>();
        for (int i = 0; i < zones.length; i++) {
            if (zones[i] == Zone.INPUTS) {
                tables.addAll(mapperManager.getInputTables());
            } else if (zones[i] == Zone.OUTPUTS) {
                tables.addAll(mapperManager.getOutputTables());
            } else if (zones[i] == Zone.VARS) {
                tables.addAll(mapperManager.getVarsTables());
            }
        }
        // if (!(currentTable instanceof VarsTable) && !(currentEntry instanceof
        // ExpressionFilterEntry)) {
        // tables.remove(currentTable);
        // }

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

        for (IDataMapTable table : this.tables) {
            List<IColumnEntry> dataMapTableEntries = table.getColumnEntries();
            for (IColumnEntry entrySource : dataMapTableEntries) {
                sourceEntryLocation.tableName = entrySource.getParentName();
                sourceEntryLocation.columnName = entrySource.getName();
                if (mapperManager.getUiManager().checkSourceLocationIsValid(entrySource, currentModifiedEntry)) {
                    proposals.add(new EntryContentProposal(entrySource, this.currentLanguage));
                }
            }
            // for the globalMap
            if (table instanceof InputTable) {
                InputTable inputTable = (InputTable) table;
                List<GlobalMapEntry> globalMapEntries = inputTable.getGlobalMapEntries();
                for (GlobalMapEntry entry : globalMapEntries) {
                    proposals.add(new EntryContentProposal(entry, this.currentLanguage));
                }
            }
        }

        for (IContentProposalProvider contentProposalProvider : otherContentProposalProviders) {
            proposals.addAll(Arrays.asList(contentProposalProvider.getProposals(contents, position)));
        }
        IContentProposal[] res = new IContentProposal[proposals.size()];
        res = proposals.toArray(res);
        return res;
    }

    public List<Variable> getVariables() {
        List<Variable> variables = new ArrayList<Variable>();
        for (IDataMapTable table : this.tables) {
            List<IColumnEntry> dataMapTableEntries = table.getColumnEntries();
            for (IColumnEntry entrySource : dataMapTableEntries) {
                String variable = null;
                if (table instanceof VarsTable) {
                    variable = entrySource.getExpression();
                } else {
                    variable = LanguageProvider.getCurrentLanguage().getLocation(entrySource.getParentName(),
                            entrySource.getName());
                }
                String talendType = null;
                boolean nullable = true;
                if (entrySource instanceof AbstractInOutTableEntry) {
                    talendType = ((AbstractInOutTableEntry) entrySource).getMetadataColumn().getTalendType();
                    nullable = ((AbstractInOutTableEntry) entrySource).getMetadataColumn().isNullable();
                } else if (entrySource instanceof VarTableEntry) {
                    talendType = ((VarTableEntry) entrySource).getType();
                    nullable = ((VarTableEntry) entrySource).isNullable();
                }
                if (talendType != null) {
                    if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                        boolean exist = false;
                        for (Variable v : variables) {
                            if (variable != null && v.getName() != null && v.getName().trim().equals(variable.trim())) {
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            variables.add(new Variable(variable, JavaTypesManager.getDefaultValueFromJavaIdType(talendType,
                                    nullable).toString(), talendType, nullable));
                        }

                    } else {
                        variables.add(new Variable(variable, "", talendType, nullable)); //$NON-NLS-1$
                    }
                }
            }
        }

        return variables;
    }
}
