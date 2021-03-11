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
package org.talend.designer.core.model.process;

import java.util.List;

import org.talend.core.model.process.INode;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public interface IGeneratingProcess {

    /**
     * DOC nrousseau Comment method "getNodeList".
     *
     * @return
     */
    List<? extends INode> getNodeList();

    /**
     * DOC nrousseau Comment method "buildFromGraphicalProcess".
     *
     * @param sortedFlow
     */
    void buildFromGraphicalProcess(List<INode> sortedFlow);

    void generateAdditionalCode();

}
