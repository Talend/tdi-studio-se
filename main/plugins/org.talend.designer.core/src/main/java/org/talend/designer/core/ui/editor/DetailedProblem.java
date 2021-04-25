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
package org.talend.designer.core.ui.editor;

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class DetailedProblem {

    private IProblem problem;

    private String key;

    private String source;

    /**
     * DOC amaumont ProblemWithLine constructor comment.
     *
     * @param problem
     * @param line
     * @param source
     */
    public DetailedProblem(IProblem problem, String line, String source) {
        this.problem = problem;
        this.key = line;
        this.source = source;
    }

    /**
     * Getter for line.
     *
     * @return the line
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Getter for problem.
     *
     * @return the problem
     */
    public IProblem getJdtProblem() {
        return this.problem;
    }

    /**
     * Getter for source.
     *
     * @return the source
     */
    public String getSource() {
        return this.source;
    }

}
