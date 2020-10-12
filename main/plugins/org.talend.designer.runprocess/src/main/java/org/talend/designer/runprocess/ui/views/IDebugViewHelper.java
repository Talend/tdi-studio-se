// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess.ui.views;

import org.eclipse.swt.widgets.Composite;
import org.talend.designer.runprocess.EDebugProcessType;
import org.talend.designer.runprocess.ui.TraceDebugProcessComposite;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public interface IDebugViewHelper {

    public TraceDebugProcessComposite getDebugComposite(final Composite container);
    
    public EDebugProcessType getDebugType();
}
