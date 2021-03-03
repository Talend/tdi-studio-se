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
package org.talend.designer.dbmap.external.data;

import java.io.Serializable;

import org.talend.core.model.process.node.IExternalMapEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: ExternalMapperTableEntry.java 2010 2007-02-12 13:18:38Z amaumont $
 *
 */
public class ExternalDbMapEntry implements IExternalMapEntry, Serializable, Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = -6108773358595740424L;

    private String name;

    private String expression;

    private String operator;

    private boolean join;

    public ExternalDbMapEntry() {
        super();
    }

    public ExternalDbMapEntry(String expression) {
        super();
        this.expression = expression;
    }

    public ExternalDbMapEntry(String name, String expression) {
        super();
        this.name = name;
        this.expression = expression;
    }

    /**
     * DOC amaumont ExternalDbMapEntry constructor comment.
     *
     * @param name
     * @param expression
     * @param operator
     */
    public ExternalDbMapEntry(String name, String expression, String operator) {
        super();
        this.name = name;
        this.expression = expression;
        this.operator = operator;
    }

    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String metadataName) {
        this.name = metadataName;
    }

    /**
     * Talend types, used for var entries only.
     *
     * @return
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Talend types, used for var entries only.
     *
     * @param type the type to set
     */
    public void setOperator(String type) {
        this.operator = type;
    }

    /**
     * Getter for join.
     *
     * @return the join
     */
    public boolean isJoin() {
        return this.join;
    }

    /**
     * Sets the join.
     *
     * @param join the join to set
     */
    public void setJoin(boolean join) {
        this.join = join;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ExternalDbMapEntry other = (ExternalDbMapEntry) obj;
        if (this.expression == null) {
            if (other.expression != null)
                return false;
        } else if (!this.expression.equals(other.expression))
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        } else if (!this.name.equals(other.name))
            return false;
        if (this.isJoin() != other.isJoin())
            return false;
        if (this.operator == null) {
            if (other.operator != null)
                return false;
        } else if (!this.operator.equals(other.operator))
            return false;
        return true;
    }


}
