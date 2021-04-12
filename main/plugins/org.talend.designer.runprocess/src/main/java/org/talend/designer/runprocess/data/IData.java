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
package org.talend.designer.runprocess.data;

/**
 * DOC mhirt class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public interface IData {

    public String getElementId();

    public long getLineCount();

    public long getProcessingTime();

    public String getAction();

}
