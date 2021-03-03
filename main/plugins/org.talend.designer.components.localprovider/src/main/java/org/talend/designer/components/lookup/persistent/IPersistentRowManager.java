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

import routines.system.IPersistableRow;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * @param <R> R as row/bean
 */
public interface IPersistentRowManager<R extends IPersistableRow> {

    public void initPut();

    public void put(R row) throws IOException;

    public void endPut() throws IOException;

    public void initGet() throws IOException;

    public boolean hasNext() throws IOException;

    public R next() throws IOException;

    public void endGet() throws IOException;

    public R getNextFreeRow();

}
