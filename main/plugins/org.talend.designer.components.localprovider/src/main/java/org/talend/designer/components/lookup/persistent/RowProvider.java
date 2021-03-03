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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.talend.designer.components.persistent.IRowCreator;
import org.talend.designer.components.persistent.IRowProvider;

/**
 *
 * Row Provider.
 * @param <B> bean
 */
public class RowProvider<B> implements IRowProvider<B> {

    private List<B> cache = new ArrayList<B>();

    private int currentFreeIndex = 0;

    private int currentGetIndex = 0;

    private IRowCreator<B> rowCreator;

    public RowProvider(IRowCreator<B> rowCreator) {
        this.rowCreator = rowCreator;
    }

    public B getFreeInstance() {
        B row = null;
        if (currentFreeIndex >= cache.size()) {
            row = this.rowCreator.createRowInstance();
            cache.add(row);
        } else {
            row = cache.get(currentFreeIndex);
        }
        currentFreeIndex++;
        return row;
    }

    public boolean hasNext() {
        return currentGetIndex < currentFreeIndex - 1;
    }

    public B next() {
        if (currentGetIndex > currentFreeIndex - 1) {
            throw new NoSuchElementException();
        }
        B row = cache.get(currentGetIndex);
        currentGetIndex++;
        return row;
    }

    public B createInstance() {
        return this.rowCreator.createRowInstance();
    }

    public void resetFreeIndex() {
        currentFreeIndex = 0;
    }

    public void resetInstanceIndex() {
        currentGetIndex = 0;
    }

}
