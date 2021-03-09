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
package org.talend.designer.runprocess;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class TraceNode {

    Object element;

    TraceNode next;

    public TraceNode(Object element) {
        this(element, null);
    }

    public TraceNode(Object element, TraceNode n) {
        this.element = element;
        next = n;
    }

    public Object getElement() {
        return element;
    }

    public void setElement(Object element) {
        this.element = element;
    }

    public TraceNode getNext() {
        return next;
    }

    public void setNext(TraceNode next) {
        this.next = next;
    }
}
