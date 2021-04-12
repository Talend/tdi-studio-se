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

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: ExpressionContentProposal.java 上午09:43:29 2007-7-30 +0000 (2007-7-30) yzhang $
 *
 */
public class ExpressionContentProposal implements IContentProposal {

    private String content;

    private String description;

    private int position;

    /**
     * yzhang ExpressionContentProposal constructor comment.
     */
    public ExpressionContentProposal(String con, String des, int pos) {
        content = con;
        description = des;
        position = pos;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposal#getContent()
     */
    public String getContent() {
        return content;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposal#getCursorPosition()
     */
    public int getCursorPosition() {
        return getContent().length();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposal#getDescription()
     */
    public String getDescription() {
        return description;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposal#getLabel()
     */
    public String getLabel() {
        // TODO Auto-generated method stub
        return null;
    }

}
