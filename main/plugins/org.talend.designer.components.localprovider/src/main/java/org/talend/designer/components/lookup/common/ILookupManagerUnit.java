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
package org.talend.designer.components.lookup.common;

import java.io.IOException;

import routines.system.IPersistableLookupRow;

/**
 *
 * Lookup Manager Unit.
 * @param <B> bean
 */
public interface ILookupManagerUnit<B extends Comparable<B> & IPersistableLookupRow<B>> {

    public abstract void lookup(B key) throws IOException;

    /**
     * DOC slanglois Comment method "hasNext".
     *
     * @return
     * @throws IOException
     */
    public abstract boolean hasNext() throws IOException;

    /**
     * DOC slanglois Comment method "next".
     *
     * @return
     * @throws IOException
     */
    public abstract B next() throws IOException;

    /**
     * DOC slanglois Comment method "close".
     *
     * @throws IOException
     */
    public abstract void close() throws IOException;

}
