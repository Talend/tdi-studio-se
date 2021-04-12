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
package org.talend.repository.json.ui.wizards.extraction;

import org.eclipse.jface.fieldassist.IContentProposal;

/**
 * created by cmeng on Jul 9, 2015 Detailled comment
 *
 */
public class JsonPathContentProposal implements IContentProposal {

    private String content;

    /**
     * DOC cmeng JsonPathContentProposal constructor comment.
     */
    public JsonPathContentProposal(String content) {
        super();
        this.content = content;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposal#getContent()
     */
    @Override
    public String getContent() {
        return content;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposal#getCursorPosition()
     */
    @Override
    public int getCursorPosition() {
        return content.length() + 1;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposal#getLabel()
     */
    @Override
    public String getLabel() {
        return content;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposal#getDescription()
     */
    @Override
    public String getDescription() {
        return null;
    }
}
