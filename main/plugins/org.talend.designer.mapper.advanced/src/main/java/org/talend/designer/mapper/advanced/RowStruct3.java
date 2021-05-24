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
package org.talend.designer.mapper.advanced;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
/**
 *
 * DOC amaumont THashStartJavajet class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class RowStruct3 {

    private static final int DEFAULT_HASHCODE = 1;

    private static final int PRIME = 31;

    public static boolean endRangeIsKey;
    public static boolean startRangeIsKey;
    public static boolean stringKeyIsKey;
    public static boolean stringKey2IsKey;
    public static boolean nameIsKey;

    private int hashCode = DEFAULT_HASHCODE;

    public boolean hashCodeDirty = true;



    public int endRange;
    public Integer startRange;
    public String stringKey;
    public String stringKey2;
    public String name;

    /**
     * DOC amaumont BeanStruct constructor comment.
     *
     * @param id1
     * @param name
     * @param id2
     */
    public RowStruct3(Integer startRange, int endRange, String stringKey, String stringKey2, String name) {
        super();
        this.startRange = startRange;
        this.endRange = endRange;
        this.stringKey = stringKey;
        this.stringKey2 = stringKey2;
        this.name = name;
    }

    public RowStruct3() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (this.hashCodeDirty) {
            final int prime = PRIME;
            int result = 0;
            if (endRangeIsKey) {
                result = prime * result + this.endRange;
            }
            if (startRangeIsKey) {
                result = prime * result + ((this.startRange == null) ? 0 : this.startRange.hashCode());
            }
            if (stringKeyIsKey) {
                result = prime * result + ((this.stringKey == null) ? 0 : this.stringKey.hashCode());
            }
            if (stringKey2IsKey) {
                result = prime * result + ((this.stringKey2 == null) ? 0 : this.stringKey2.hashCode());
            }
            if (nameIsKey) {
                result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
            }
            this.hashCode = result;
            this.hashCodeDirty = false;
        }

        return this.hashCode;
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
        final RowStruct3 other = (RowStruct3) obj;
        if (endRangeIsKey && this.endRange != other.endRange)
            return false;
        if (startRangeIsKey) {
            if (this.startRange == null) {
                if (other.startRange != null)
                    return false;
            } else if (!this.startRange.equals(other.startRange))
                return false;
        }
        if (stringKeyIsKey) {
            if (this.stringKey == null) {
                if (other.stringKey != null)
                    return false;
            } else if (!this.stringKey.equals(other.stringKey))
                return false;
        }
        if (stringKey2IsKey) {
            if (this.stringKey2 == null) {
                if (other.stringKey2 != null)
                    return false;
            } else if (!this.stringKey2.equals(other.stringKey2))
                return false;
        }
        if (nameIsKey) {
            if (this.name == null) {
                if (other.name != null)
                    return false;
            } else if (!this.name.equals(other.name))
                return false;
        }
        return true;
    }

    /**
         *
         * @return
         * @author
         */
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("RowStruct3["); //$NON-NLS-1$
            buffer.append("endRange = ").append(endRange); //$NON-NLS-1$
            buffer.append(" hashCodeDirty = ").append(hashCodeDirty); //$NON-NLS-1$
            buffer.append(" name = ").append(name); //$NON-NLS-1$
            buffer.append(" startRange = ").append(startRange); //$NON-NLS-1$
            buffer.append(" stringKey = ").append(stringKey); //$NON-NLS-1$
            buffer.append(" stringKey2 = ").append(stringKey2); //$NON-NLS-1$
            buffer.append("]"); //$NON-NLS-1$
            return buffer.toString();
        }



}
