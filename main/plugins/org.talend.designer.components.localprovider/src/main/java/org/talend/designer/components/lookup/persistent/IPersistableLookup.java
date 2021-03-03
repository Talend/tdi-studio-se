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

/**
 * Interface for persistable lookup.
 *
 * @param <B> bean
 */
public interface IPersistableLookup<B> {

    public void initPut() throws IOException;

    public void put(B bean) throws IOException;

    public void endPut() throws IOException;

    public void initGet() throws IOException;

    public void lookup(B key) throws IOException;

    public boolean hasNext() throws IOException;

    public B next() throws IOException;

    public void endGet() throws IOException;

    public B getNextFreeRow();
}
