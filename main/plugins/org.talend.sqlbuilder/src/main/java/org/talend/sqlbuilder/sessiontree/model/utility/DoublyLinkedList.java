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
package org.talend.sqlbuilder.sessiontree.model.utility;

import java.util.NoSuchElementException;

/**
 * DOC qianbing class global comment. Detailled comment <br/>
 *
 * $Id: DoublyLinkedList.java,v 1.3 2006/11/01 07:49:10 peiqin.hou Exp $
 *
 */
class DoublyLinkedList {

    DLLNode head, last;

    int size = 0;

    /**
     * @param data data.
     */
    public void addFirst(Object data) {
        DLLNode newNode = new DLLNode();
        newNode.data = data;
        if (size == 0) {
            head = newNode;
            last = head;
        } else {
            newNode.nextNode = head;
            head.previousNode = newNode;
            head = newNode;
        }
        size++;
    }

    /**
     * @param data data.
     */
    public void addLast(Object data) {
        DLLNode newNode = new DLLNode();
        newNode.data = data;
        if (size == 0) {
            head = newNode;
        } else {
            last.nextNode = newNode;
            newNode.previousNode = last;
        }
        last = newNode;
        size++;
    }

    /**
     * removeFirst.
     */
    public void removeFirst() {
        if (size <= 1) {
            head = null;
            last = null;
        } else {
            DLLNode oldHead = head;
            head = oldHead.nextNode;
            oldHead.nextNode = null;
            head.previousNode = null;
        }
        size--;
    }

    /**
     * removeLast.
     */
    public void removeLast() {
        if (size <= 1) {
            head = null;
            last = null;
        } else {
            last = last.previousNode;
            last.nextNode.previousNode = null;
            last.nextNode = null;
        }
        size--;
    }

    /**
     * @return size.
     */
    public int size() {
        return size;
    }

    /**
     * clear.
     */
    public void clear() {
        DLLNode currentNode = last;
        DLLNode tempNode;
        while (currentNode != null) {
            tempNode = currentNode.previousNode;
            currentNode.nextNode = null;
            currentNode.previousNode = null;
            currentNode.data = null;
            currentNode = tempNode;
        }
        last = null;
        head = null;
        size = 0;
    }

    /**
     * DOC qianbing DoublyLinkedList class global comment. Detailled comment <br/>
     *
     * $Id: DoublyLinkedList.java,v 1.3 2006/11/01 07:49:10 peiqin.hou Exp $
     *
     */
    protected class DLLNode {

        protected DLLNode nextNode, previousNode;

        protected Object data;
    }

    /**
     * @return DLLIterator
     */
    public DLLIterator iterator() {
        return new DLLIterator();
    }

    /**
     * DOC qianbing DoublyLinkedList class global comment. Detailled comment <br/>
     *
     * $Id: DoublyLinkedList.java,v 1.3 2006/11/01 07:49:10 peiqin.hou Exp $
     *
     */
    public class DLLIterator {

        private DLLNode currentPreviousNode = null;

        private DLLNode currentNextNode = head;

        /**
         * @return hasNext
         */
        public boolean hasNext() {
            if (currentNextNode == null) {
                return false;
            } else {
                return (currentNextNode != null);
            }
        }

        /**
         * @return hasPrevious
         */
        public boolean hasPrevious() {
            if (currentPreviousNode == null) {
                return false;
            } else {
                return (currentPreviousNode != null);
            }
        }

        /**
         * @return next object.
         */
        public Object next() {
            if (currentNextNode == null) {
                throw new NoSuchElementException("Attempt to retrieve next value from " + //$NON-NLS-1$
                        "DoublyLinkedList after all values have already been retrieved. Verify hasNext method returns true "//$NON-NLS-1$
                        + //$NON-NLS-1$
                        "before calling next method."); //$NON-NLS-1$
            }
            Object data = currentNextNode.data;
            DLLNode tempNode = currentNextNode;
            currentNextNode = currentNextNode.nextNode;
            currentPreviousNode = tempNode;
            return data;
        }

        /**
         * @return previous object
         */
        public Object previous() {
            if (currentPreviousNode == null) {
                throw new NoSuchElementException("Attempt to retrieve previous value from " + //$NON-NLS-1$
                        "head node of DoublyLinkedList. Verify hasPrevious method returns true " + //$NON-NLS-1$
                        "before calling previous method."); //$NON-NLS-1$
            }
            Object data = currentPreviousNode.data;
            DLLNode tempNode = currentPreviousNode;
            currentPreviousNode = currentPreviousNode.previousNode;
            currentNextNode = tempNode;
            return data;
        }

        /**
         * resetToBeginning.
         */
        public void resetToBeginning() {
            currentNextNode = head;
            currentPreviousNode = null;
        }

        /**
         * resetToEnd.
         */
        public void resetToEnd() {
            currentNextNode = null;
            currentPreviousNode = last;
        }
    }

    //******************************************************************************************************************
    // ************
    // ***************************************** from here on down is test code
    // *******************************************
    //******************************************************************************************************************
    // ************

    /*
     * public static class Test { public static void main(String[] args) { DoublyLinkedList testListOne = new
     * DoublyLinkedList(); String testObjectOne = "test object one"; testListOne.addFirst(testObjectOne);
     * System.out.println("Size after adding one object by calling addFirst: " + testListOne.size);
     * testListOne.removeFirst(); System.out.println("Then called removeFirst and size is: " + testListOne.size);
     *
     * testListOne.addLast(testObjectOne); System.out.println("Size after adding one object by calling addLast: " +
     * testListOne.size); testListOne.removeLast(); System.out.println("Then called removeLast and size is: " +
     * testListOne.size); testListOne.clear(); testListOne.clear();
     *
     * testListOne.addFirst(testObjectOne); DLLIterator iterator = testListOne.iterator(); System.out.println("hasNext
     * method of iterator after adding one object by calling addFirst: " + iterator.hasNext());
     * System.out.println("hasPrevious method of iterator after adding one object by calling addFirst: " +
     * iterator.hasPrevious()); String resultString = (String)iterator.next(); System.out.println("result string pulled
     * out by calling next: " + resultString); System.out.println("hasNext method of iterator after calling next: " +
     * iterator.hasNext()); System.out.println("hasPrevious method of iterator after calling next: " +
     * iterator.hasPrevious()); resultString = (String)iterator.previous(); System.out.println("result string pulled out
     * by calling previous: " + resultString); testListOne.clear();
     *
     * System.out.println("");
     *
     * String testObjectTwo = "test object two"; String testObjectThree = "test object three";
     *
     * testListOne.addFirst(testObjectTwo); testListOne.addFirst(testObjectOne); iterator.resetToBeginning();
     * while(iterator.hasNext()) System.out.println((String)iterator.next()); testListOne.clear();
     *
     * System.out.println("");
     *
     * testListOne.addLast(testObjectOne); testListOne.addLast(testObjectTwo); iterator.resetToBeginning();
     * while(iterator.hasNext()) System.out.println((String)iterator.next()); testListOne.clear();
     *
     * System.out.println("");
     *
     * testListOne.addFirst(testObjectThree); testListOne.addFirst(testObjectTwo); testListOne.addFirst(testObjectOne);
     * iterator.resetToBeginning(); while(iterator.hasNext()) System.out.println((String)iterator.next());
     * testListOne.clear();
     *
     * System.out.println("");
     *
     * testListOne.addLast(testObjectOne); testListOne.addLast(testObjectTwo); testListOne.addLast(testObjectThree);
     * iterator.resetToBeginning(); while(iterator.hasNext()) System.out.println((String)iterator.next());
     * testListOne.clear();
     *
     * System.out.println("");
     *
     * testListOne.addFirst(testObjectTwo); testListOne.addFirst(testObjectOne); iterator.resetToEnd();
     * while(iterator.hasPrevious()) System.out.println((String)iterator.previous()); testListOne.clear();
     *
     * System.out.println("");
     *
     * testListOne.addFirst(testObjectThree); testListOne.addFirst(testObjectTwo); testListOne.addFirst(testObjectOne);
     * System.out.println("size after adding three objects: " + testListOne.size()); iterator.resetToEnd();
     * while(iterator.hasPrevious()) System.out.println((String)iterator.previous()); testListOne.clear();
     *
     * System.out.println(""); } }
     */
}
