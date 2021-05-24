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
package org.talend.sqlbuilder.actions.explain;
import java.util.ArrayList;
/**
 * DOC dev  class global comment. Detailled comment
 * <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
 *
 */
public class ExplainNode {

    int cardinality, cost;

    int id, parentId;

    ArrayList ls = new ArrayList();

    String objectType, operation, options, objectOwner, objectName, optimizer;

    ExplainNode parent;


    public ExplainNode(ExplainNode parent) {

        this.parent = parent;
    }


    @SuppressWarnings("unchecked") //$NON-NLS-1$
	public void add(ExplainNode nd) {

        ls.add(nd);
    }


    /**
     * @return
     */
    public int getCardinality() {

        return cardinality;
    }


    @SuppressWarnings("unchecked") //$NON-NLS-1$
	public ExplainNode[] getChildren() {

        return (ExplainNode[]) ls.toArray(new ExplainNode[ls.size()]);
    }


    /**
     * @return
     */
    public int getCost() {

        return cost;
    }


    /**
     * @return
     */
    public String getOperation() {

        return operation;
    }


    /**
     * @return
     */
    public String getOptimizer() {

        return optimizer;
    }


    /**
     * @return
     */
    public String getOptions() {

        return options;
    }


    public ExplainNode getParent() {

        return parent;
    }


    /**
     * @param i
     */
    public void setCardinality(int i) {

        cardinality = i;
    }


    /**
     * @param i
     */
    public void setCost(int i) {

        cost = i;
    }


    /**
     * @param i
     */
    public void setId(int i) {

        id = i;
    }


    /**
     * @param string
     */
    public void setOperation(String string) {

        operation = string;
    }


    /**
     * @param string
     */
    public void setOptimizer(String string) {

        optimizer = string;
    }


    /**
     * @param string
     */
    public void setOptions(String string) {

        options = string;
    }


    public String toString() {

        StringBuffer sb = new StringBuffer(50);
        if (objectType != null) {
            sb.append(objectType).append(" "); //$NON-NLS-1$
        }
        if (operation != null) {
            sb.append(operation).append(" "); //$NON-NLS-1$
        }
        if (options != null) {
            sb.append(options).append(" "); //$NON-NLS-1$
        }
        if (objectOwner != null && objectName != null) {
            sb.append(objectOwner + "." + objectName).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (optimizer != null) {
            sb.append("[" + optimizer + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return sb.toString();
    }


	/**
	 * Getter for objectName.
	 * @return the objectName
	 */
	public String getObjectName() {
		return this.objectName;
	}


	/**
	 * Sets the objectName.
	 * @param objectName the objectName to set
	 */
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}


	/**
	 * Getter for objectOwner.
	 * @return the objectOwner
	 */
	public String getObjectOwner() {
		return this.objectOwner;
	}


	/**
	 * Sets the objectOwner.
	 * @param objectOwner the objectOwner to set
	 */
	public void setObjectOwner(String objectOwner) {
		this.objectOwner = objectOwner;
	}


	/**
	 * Getter for objectType.
	 * @return the objectType
	 */
	public String getObjectType() {
		return this.objectType;
	}


	/**
	 * Sets the objectType.
	 * @param objectType the objectType to set
	 */
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}


	/**
	 * Getter for id.
	 * @return the id
	 */
	public int getId() {
		return this.id;
	}

}
