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
package org.talend.designer.core.ui.views.problems;

import org.talend.core.model.process.Problem;

/**
 * This is subclass of ProblemCategory.
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class SeverityProblemCategory extends ProblemCategory {

    ProblemStatus status;

    /**
     * bqian SeverityProblemCategory constructor comment.
     *
     * @param problems
     * @param group
     */
    public SeverityProblemCategory(ProblemList problems, ProblemStatus status) {
        super(problems);
        this.status = status;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.Problem#getChildren()
     */
    @Override
    public Problem[] getChildren() {
        return getProblems().getProblemsBySeverity(status).toArray(new Problem[0]);
    }

}
