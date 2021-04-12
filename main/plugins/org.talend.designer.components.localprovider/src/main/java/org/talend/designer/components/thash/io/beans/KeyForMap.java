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
package org.talend.designer.components.thash.io.beans;

/**
 *
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 *
 *
 */
public class KeyForMap {

    public int cursorPosition;

    public int hashcode;

    /**
     * DOC amaumont KeyForMap constructor comment.
     */
    public KeyForMap(int cursorPosition, int hashcode) {
        super();
        this.cursorPosition = cursorPosition;
        this.hashcode = hashcode;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.hashcode;
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
        if (this.hashcode != obj.hashCode()) {
            return false;
        }
        if (getClass() != obj.getClass())
            return false;
        final KeyForMap other = (KeyForMap) obj;
        if (this.cursorPosition != other.cursorPosition)
            return false;
        return true;
    }

    /**
     *
     * @return
     * @author
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("KeyForMap["); //$NON-NLS-1$
        buffer.append("cursorPosition = ").append(cursorPosition); //$NON-NLS-1$
        buffer.append(" hashcode = ").append(hashcode); //$NON-NLS-1$
        buffer.append("]"); //$NON-NLS-1$
        return buffer.toString();
    }

}
