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
package org.talend.designer.dbmap.ui.proposal.expression;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.dbmap.MapperMain;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.language.DbLanguageConstants;
import org.talend.designer.dbmap.language.IDbLanguage;
import org.talend.designer.dbmap.model.tableentry.InputColumnTableEntry;
import org.talend.designer.dbmap.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.dbmap.model.tableentry.VarTableEntry;

/**
 * Content proposal based on a IContextParameter. <br/>
 *
 * $Id: EntryContentProposal.java 1795 2007-02-04 04:15:56Z bqian $
 *
 */
public class EntryContentProposal implements IContentProposal {

    private ITableEntry entry;

    private IDbLanguage language;

    private String content;

    /**
     * Constructs a new ContextParameterProposal.
     *
     * @param language
     * @param control
     */
    public EntryContentProposal(ITableEntry entry, IDbLanguage language) {
        super();
        this.entry = entry;
        this.language = language;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposal#getContent()
     */
    public String getContent() {
        content = language.getLocation(entry.getParentName(), entry.getName()) + " "; //$NON-NLS-1$
        return content;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposal#getCursorPosition()
     */
    public int getCursorPosition() {
        return content.length() + 1;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposal#getDescription()
     */
    public String getDescription() {

        StringBuilder sb = new StringBuilder();

        String separator = " - "; //$NON-NLS-1$
        if (entry instanceof InputColumnTableEntry) {

            InputColumnTableEntry inputEntry = (InputColumnTableEntry) entry;

            IMetadataColumn metadataColumn = inputEntry.getMetadataColumn();
            sb.append(Messages.getString("EntryContentProposal.metadataColumn")).append(" '").append(metadataColumn.getLabel()) //$NON-NLS-1$ //$NON-NLS-2$
                    .append("' "); //$NON-NLS-1$
            sb.append(Messages.getString("EntryContentProposal.properties")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            sb.append(DbLanguageConstants.CARRIAGE_RETURN);
            sb.append(separator).append(Messages.getString("EntryContentProposal.column")).append(metadataColumn.getLabel()); //$NON-NLS-1$
            sb.append(DbLanguageConstants.CARRIAGE_RETURN);
            sb.append(separator).append(Messages.getString("EntryContentProposal.key")).append(metadataColumn.isKey()); //$NON-NLS-1$
            if (!MapperMain.isStandAloneMode()) {
                sb.append(DbLanguageConstants.CARRIAGE_RETURN);
                sb.append(separator)
                        .append(Messages.getString("EntryContentProposal.type")).append(format(metadataColumn.getTalendType())); //$NON-NLS-1$
            }
            sb.append(DbLanguageConstants.CARRIAGE_RETURN);
            sb.append(separator)
                    .append(Messages.getString("EntryContentProposal.dbType")).append(format(metadataColumn.getType())); //$NON-NLS-1$
            sb.append(DbLanguageConstants.CARRIAGE_RETURN);
            sb.append(separator).append(Messages.getString("EntryContentProposal.length")); //$NON-NLS-1$
            if (metadataColumn.getLength() != null && metadataColumn.getLength() > 0) {
                sb.append(format(metadataColumn.getLength()));
            }

            sb.append(DbLanguageConstants.CARRIAGE_RETURN);
            sb.append(separator).append(Messages.getString("EntryContentProposal.precision")); //$NON-NLS-1$
            if (metadataColumn.getPrecision() != null && metadataColumn.getPrecision() > 0) {
                sb.append(format(metadataColumn.getPrecision()));
            }
            sb.append(DbLanguageConstants.CARRIAGE_RETURN);
            sb.append(separator)
                    .append(Messages.getString("EntryContentProposal.default")).append(format(metadataColumn.getDefault())); //$NON-NLS-1$
            sb.append(DbLanguageConstants.CARRIAGE_RETURN);
            sb.append(separator)
                    .append(Messages.getString("EntryContentProposal.comment")).append(format(metadataColumn.getComment())); //$NON-NLS-1$
            sb.append(DbLanguageConstants.CARRIAGE_RETURN);
            sb.append(separator).append(Messages.getString("EntryContentProposal.expressionKey")); //$NON-NLS-1$
            sb.append(DbLanguageConstants.CARRIAGE_RETURN);
            sb.append(format(entry.getExpression()));
            sb.append(DbLanguageConstants.CARRIAGE_RETURN);
        } else if (entry instanceof VarTableEntry) {
            sb.append(Messages.getString("EntryContentProposal.variable")).append(" '").append(entry.getName()).append("' :"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            sb.append(DbLanguageConstants.CARRIAGE_RETURN);
            sb.append(separator).append(Messages.getString("EntryContentProposal.expressionKey")); //$NON-NLS-1$
            sb.append(DbLanguageConstants.CARRIAGE_RETURN);
            sb.append(format(entry.getExpression()));
        }
        return sb.toString();
    }

    public String format(Object object) {
        if (object == null) {
            return ""; //$NON-NLS-1$
        }
        return String.valueOf(object);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposal#getLabel()
     */
    public String getLabel() {
        String label = language.getLocation(entry.getParentName(), entry.getName());
        if (entry instanceof OutputColumnTableEntry) {
            label += "            " + Messages.getString("EntryContentProposal.onlyAvailable"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return label;

    }

}
