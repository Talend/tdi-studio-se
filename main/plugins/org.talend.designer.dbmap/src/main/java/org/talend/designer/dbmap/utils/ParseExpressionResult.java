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
package org.talend.designer.dbmap.utils;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: ParseExpressionResult.java 968 2006-12-12 10:59:26Z amaumont $
 *
 */
public class ParseExpressionResult {

    private boolean atLeastOneLinkAdded;

    private boolean atLeastOneLinkRemoved;

    /**
     * DOC amaumont ProcessExpressionResult constructor comment.
     *
     * @param atLeastOneLinkAdded
     * @param atLeastOneLinkRemoved
     */
    public ParseExpressionResult(boolean atLeastOneLinkAdded, boolean atLeastOneLinkRemoved) {
        super();
        this.atLeastOneLinkAdded = atLeastOneLinkAdded;
        this.atLeastOneLinkRemoved = atLeastOneLinkRemoved;
    }

    public boolean isAtLeastOneLinkHasBeenAddedOrRemoved() {
        return atLeastOneLinkAdded || atLeastOneLinkRemoved;
    }

    public boolean isAtLeastOneLinkAdded() {
        return this.atLeastOneLinkAdded;
    }

    public boolean isAtLeastOneLinkRemoved() {
        return this.atLeastOneLinkRemoved;
    }

}
