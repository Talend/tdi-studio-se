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
public class ListStack {

    TraceNode header;

    int elementCount;

    int size;

    public ListStack() {
        header = null;
        elementCount = 0;
        size = 0;
    }

    public ListStack(int size) {
        header = null;
        elementCount = 0;
        this.size = size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setHeader(TraceNode header) {
        this.header = header;
    }

    public int getSize() {
        return size;
    }

    public int getElementCount() {
        return elementCount;
    }

    public boolean isEmpty() {
        if (elementCount == 0)
            return true;
        return false;
    }

    public boolean isFull() {
        if (elementCount == size)
            return true;
        return false;
    }

    public void push(String value) {
        if (this.isFull()) {
            // return;
        }
        header = new TraceNode(value, header);
        elementCount++;
    }

    public Object get(int index) {
        if (this.header != null && index >= 0) {
            int j = 0;
            TraceNode p = this.header;
            while (p != null && j < index) {
                j++;
                p = p.next;
            }
            if (p != null) {
                return p.getElement();
            }
        }
        return null;
    }

    public Object pop() {
        if (this.isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }

        Object obj = header.getElement();

        header = header.getNext();

        elementCount--;
        return obj;
    }

    public Object peek() {
        if (this.isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }

        return header.getElement();
    }

    public static void main(String[] args) {
        ListStack ms = new ListStack(5);
        ms.push("one");
        ms.push("two");
        ms.push("three");
        ms.push("four");
        ms.push("five");
        ms.push("six");
        ms.push("sen");
        for (int i = 0; i < ms.size; i++) {
            System.out.println(ms.get(i));
        }

        System.out.println(ms.size);
        System.out.println(ms.pop());
        System.out.println(ms.peek());
        System.out.println(ms.pop());
        // System.out.println(ms.empty());
    }

}
