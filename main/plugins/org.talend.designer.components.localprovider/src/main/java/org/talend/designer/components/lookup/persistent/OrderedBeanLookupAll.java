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
package org.talend.designer.components.lookup.persistent;

import java.io.IOException;

import org.talend.designer.components.lookup.common.ILookupManagerUnit;

import routines.system.IPersistableLookupRow;

/**
 * Ordered bean lookup for "All rows" matching mode.
 * @param <B> bean
 */
public class OrderedBeanLookupAll<B extends Comparable<B> & IPersistableLookupRow<B>> implements ILookupManagerUnit<B> {

    private String dataFilePath;

    public OrderedBeanLookupAll(String dataFilePath) throws IOException {
        super();
        this.dataFilePath = dataFilePath;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.persistent.TestA#lookup(B)
     */
    public void lookup(B key) throws IOException {

        throw new UnsupportedOperationException("No sense to use this method"); //$NON-NLS-1$

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.persistent.TestA#hasNext()
     */
    public boolean hasNext() throws IOException {

        return false;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.persistent.TestA#next()
     */
    public B next() throws IOException {

        return null;
    }

    public void close() throws IOException {

    }

}
