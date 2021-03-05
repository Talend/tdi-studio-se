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

import routines.system.IPersistableComparableLookupRow;

/**
 *
 * Abstract class for persistent lookup ("Store on disk").
 * @param <B> bean
 */
public class AbstractPersistentLookup<B extends IPersistableComparableLookupRow<B>> {

    public AbstractPersistentLookup() {
        super();
    }

}
