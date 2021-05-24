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
package org.talend.designer.components.persistent;

/**
 *
 * Interface for row provider.
 *
 * @param <B> row/bean
 */
public interface IRowProvider<B> {

    public B createInstance();

    public B getFreeInstance();

    public boolean hasNext();

    public B next();

    public void resetFreeIndex();

    public void resetInstanceIndex();

}
