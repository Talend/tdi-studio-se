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
package org.talend.designer.core.ui.editor.nodes;

/**
 * Interface used to display a @{link org.talend.core.model.process.Problem} on a @{link Node}
 *
 */

public interface NodeProblem {

    /**
     * This method is used to validate if a Node needs to be checked or not. The goal is to avoid to check a node if a
     * prerequisite is not fullfilled. For example, if a {@link NodeProblem} implementation is made for the tLogRow in
     * Spark only, the validate method could check the component name and its category and return true only if both
     * conditions are true.
     *
     * @param node the @{link Node} to validate.
     * @return true if the @{link Node} needs to be checked.
     */
    boolean needsCheck(Node node);

    /**
     * This method displays a @{link org.talend.core.model.process.Problem} on the given node.
     *
     * @param node the @{link Node} to check.
     */
    void check(Node node);
}
