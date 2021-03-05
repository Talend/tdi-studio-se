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
package org.talend.sqlbuilder.ui.proposal;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: SQLEditorProposalProvider.java,v 1.1 2006/11/06 11:41:16 qiang.zhang Exp $
 *
 */
public class SQLEditorProposalProvider implements IContentProposalProvider {

    // private SessionTreeNode sessionTreeNode;
    private String language;

    private RepositoryNode node;

    /**
     * DOC dev SQLEditorContentProposalProvider constructor comment.
     */
    public SQLEditorProposalProvider(RepositoryNode node, String language) {
        this.language = language;
        this.node = node;
    }

    /**
     * Get IContentProposal[] will showed in PopupDialog.
     *
     * @param contents colorText's all Content
     * @param cousor current position
     * @return contentProposal[]
     */
    public IContentProposal[] getProposals(String contents, int position) {
        // TODO
        return new SQLEditorProposalUtil(node, language).getSQLEditorContentProposals(contents, position);
    }
}
