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
package org.talend.expressionbuilder.test;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.runtime.model.expressionbuilder.Variable;

/**
 * The argument that transfered to code generator.
 *
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: ExpressionArguments.java 上午09:55:03 2007-6-29 +0000 (2007-6-29) yzhang $
 *
 */
public class ExpressionArguments {

    private String expression;

    private List<Variable> variables;

    /**
     * Initialize member variables.
     *
     * yzhang ExpressionArguments constructor comment.
     */
    public ExpressionArguments() {
        variables = new ArrayList<Variable>();
    }

    /**
     * Getter for expression.
     *
     * @return the expression
     */
    public String getExpression() {
        return this.expression;
    }

    /**
     * Getter for variables.
     *
     * @return the variables
     */
    public List<Variable> getVariables() {
        return this.variables;
    }

    /**
     * Sets the variables.
     *
     * @param variables the variables to set
     */
    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    /**
     * Sets the expression.
     *
     * @param expression the expression to set
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

}
