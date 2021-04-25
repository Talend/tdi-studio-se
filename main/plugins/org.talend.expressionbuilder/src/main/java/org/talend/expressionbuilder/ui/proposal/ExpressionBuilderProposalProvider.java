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
package org.talend.expressionbuilder.ui.proposal;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.talend.expressionbuilder.ui.CategoryComposite;
import org.talend.expressionbuilder.ui.ExpressionBuilderDialog;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: ExpressionBuilderProposalProvider.java 下午06:16:45 2007-7-27 +0000 (2007-7-27) yzhang $
 *
 */
public class ExpressionBuilderProposalProvider implements IContentProposalProvider {

    private CategoryComposite composite;

    /**
     * yzhang ExpressionBuilderProposalProvider constructor comment.
     */
    public ExpressionBuilderProposalProvider() {
        composite = ExpressionBuilderDialog.getCategoryComposite();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposalProvider#getProposals(java.lang.String, int)
     */
    public IContentProposal[] getProposals(String contents, int position) {
        String sub = contents.substring(0, position);

        if (sub.endsWith(" ") || sub.endsWith("+")) { //$NON-NLS-1$ //$NON-NLS-2$
            sub = "*C"; //$NON-NLS-1$
        } else {
            String[] arrays = sub.split("\" \"|\\+"); //$NON-NLS-1$
            sub = arrays[arrays.length - 1].trim();
        }

        return composite.getProposals(sub, position);
    }
}
