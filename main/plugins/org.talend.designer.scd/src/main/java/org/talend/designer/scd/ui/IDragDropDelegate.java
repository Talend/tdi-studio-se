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
package org.talend.designer.scd.ui;

import org.eclipse.swt.graphics.Point;

/**
 * DOC hcw class global comment. Detailled comment
 */
public interface IDragDropDelegate {

    public String getDragItemsAsText();

    public void removeDragItems(String data);

    public void onDropItems(String data, Point position);

    public boolean isDropAllowed(String data);
}
