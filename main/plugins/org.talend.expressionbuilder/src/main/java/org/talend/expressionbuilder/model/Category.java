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
package org.talend.expressionbuilder.model;

import java.util.List;

import org.talend.core.i18n.Messages;
import org.talend.designer.rowgenerator.data.Function;

/**
 * class global comment. Detailled comment <br/> $Id: TalendType.java,v 1.3 2007/01/31 05:20:52 pub Exp $
 */
public class Category {

    /**
     * @uml.property name="name"
     */
    private String name = ""; //$NON-NLS-1$

    /**
     * Getter of the property <tt>name</tt>.
     *
     * @return Returns the name.
     * @uml.property name="name"
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter of the property <tt>name</tt>.
     *
     * @param name The name to set.
     * @uml.property name="name"
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @uml.property name="functions"
     * @uml.associationEnd multiplicity="(1 -1)" ordering="true"
     * inverse="talendType:org.talend.designer.rowgenerator.data.Function"
     */
    private List<Function> functions = new java.util.ArrayList<Function>();

    /**
     * Getter of the property <tt>functions</tt>.
     *
     * @return Returns the functions.
     * @uml.property name="functions"
     */
    public List<Function> getFunctions() {
        return this.functions;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of element to return.
     * @return the element at the specified position in this list.
     * @see java.util.List#get(int)
     * @uml.property name="functions"
     */
    public Function getFunctions(int i) {
        return (Function) this.functions.get(i);
    }

    /**
     * Returns an array containing all of the elements in this list in proper sequence.
     *
     * @return an array containing all of the elements in this list in proper sequence.
     * @see java.util.List#toArray()
     * @uml.property name="functions"
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public Function[] functionsToArray() {
        return (Function[]) this.functions.toArray(new Function[this.functions.size()]);
    }

    /**
     * Appends the specified element to the end of this list (optional operation).
     *
     * @param element element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of the <tt>Collection.add</tt> method).
     * @see java.util.List#add(Object)
     * @uml.property name="functions"
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public boolean addFunctions(Function function) {
        return this.functions.add(function);
    }

    public boolean addFunctions(List<Function> list) {
        return this.functions.addAll(list);
    }

    /**
     * Setter of the property <tt>functions</tt>.
     *
     * @param functions the functions to set.
     * @uml.property name="functions"
     */
    public void setFunctions(List<Function> functions) {
        this.functions = functions;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Messages.getString("TalendType.TypeName", name)).append("\n").append("    "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Function[] f = functionsToArray();

        for (int i = 0; i < f.length; i++) {
            Function function = f[i];
            sb.append(function).append("\n").append("    "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return sb.toString();
    }
}
