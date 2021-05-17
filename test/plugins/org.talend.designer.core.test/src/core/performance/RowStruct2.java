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
package core.performance;

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
public class RowStruct2 {

    private static final int DEFAULT_HASHCODE = 1;

    private static final int PRIME = 31;

    public static boolean intKeyIsKey;

    public static boolean integerKeyIsKey;

    public static boolean stringKeyIsKey;

    public static boolean stringKey2IsKey;

    public static boolean nameIsKey;

    private int hashCode = DEFAULT_HASHCODE;

    public boolean hashCodeDirty = true;

    public int intKey;

    public Integer integerKey;

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
    public RowStruct2(int intKey, Integer integerKey, String stringKey, String stringKey2, String name) {
        super();
        this.intKey = intKey;
        this.integerKey = integerKey;
        this.stringKey = stringKey;
        this.stringKey2 = stringKey2;
        this.name = name;
    }

    public RowStruct2() {
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
            if (intKeyIsKey) {
                result = prime * result + this.intKey;
            }
            if (integerKeyIsKey) {
                result = prime * result + ((this.integerKey == null) ? 0 : this.integerKey.hashCode());
            }
            if (stringKeyIsKey) {
                result = prime * result + ((this.stringKey == null) ? 0 : this.stringKey.hashCode());
            }
            if (stringKey2IsKey) {
                result = prime * result + ((this.stringKey2 == null) ? 0 : this.stringKey2.hashCode());
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
        final RowStruct2 other = (RowStruct2) obj;
        if (intKeyIsKey && this.intKey != other.intKey)
            return false;
        if (integerKeyIsKey) {
            if (this.integerKey == null) {
                if (other.integerKey != null)
                    return false;
            } else if (!this.integerKey.equals(other.integerKey))
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
        return true;
    }

}
