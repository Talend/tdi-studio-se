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
package org.talend.designer.mapper.external.data;

import java.io.Serializable;

import org.talend.core.model.process.node.IExternalMapEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ExternalMapperTableEntry implements Serializable, Cloneable, IExternalMapEntry {

    /**
     *
     */
    private static final long serialVersionUID = -6108773358595740424L;

    private String name;

    private String expression;

    private String operator;

    /**
     * Talend types, used for var entries in all cases, and output entries for case : not connection attached.
     */
    private String type;

    /**
     * Nullable, used for var entries in all cases, and output entries for case : not connection attached.
     */
    private boolean nullable;

    public ExternalMapperTableEntry() {
        super();
    }

    public ExternalMapperTableEntry(String expression) {
        super();
        this.expression = expression;
    }

    public ExternalMapperTableEntry(String name, String expression) {
        super();
        this.name = name;
        this.expression = expression;
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
    public String getType() {
        return type;
    }

    /**
     * Talend types, used for var entries only.
     *
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter for nullable.
     *
     * @return the nullable
     */
    public boolean isNullable() {
        return this.nullable;
    }

    /**
     * Sets the nullable.
     *
     * @param nullable the nullable to set
     */
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * Getter for operator.
     *
     * @return the operator
     */
    public String getOperator() {
        return this.operator;
    }

    /**
     * Sets the operator.
     *
     * @param operator the operator to set
     */
    public void setOperator(String operator) {
        this.operator = operator;
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
        final ExternalMapperTableEntry other = (ExternalMapperTableEntry) obj;
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
        if (this.nullable != other.nullable)
            return false;
        if (this.type == null) {
            if (other.type != null)
                return false;
        } else if (!this.type.equals(other.type))
            return false;
        return true;
    }

}
