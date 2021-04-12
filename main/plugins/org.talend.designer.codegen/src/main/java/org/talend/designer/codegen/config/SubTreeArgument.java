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
package org.talend.designer.codegen.config;

import org.talend.core.model.process.IConnection;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class SubTreeArgument {

    private IConnection inputSubtreeConnection;

    private boolean sourceComponentHasConditionnalOutputs;

    private boolean isMultiplyingOutputComponents = false;

    /**
     * Getter for inputSubtreeConnection.
     *
     * @return the inputSubtreeConnection
     */
    public IConnection getInputSubtreeConnection() {
        return this.inputSubtreeConnection;
    }

    /**
     * Sets the inputSubtreeConnection.
     *
     * @param inputSubtreeConnection the inputSubtreeConnection to set
     */
    public void setInputSubtreeConnection(IConnection inputSubtreeConnection) {
        this.inputSubtreeConnection = inputSubtreeConnection;
    }

    /**
     * Getter for sourceComponentHasConditionnalOutputs.
     *
     * @return the sourceComponentHasConditionnalOutputs
     */
    public boolean isSourceComponentHasConditionnalOutputs() {
        return this.sourceComponentHasConditionnalOutputs;
    }

    /**
     * Sets the sourceComponentHasConditionnalOutputs.
     *
     * @param sourceComponentHasConditionnalOutputs the sourceComponentHasConditionnalOutputs to set
     */
    public void setSourceComponentHasConditionnalOutputs(boolean sourceComponentHasConditionnalOutputs) {
        this.sourceComponentHasConditionnalOutputs = sourceComponentHasConditionnalOutputs;
    }

    /**
     * Getter for isMultiplyingOutputComponents.
     *
     * @return the isMultiplyingOutputComponents
     */
    public boolean isMultiplyingOutputComponents() {
        return isMultiplyingOutputComponents;
    }

    /**
     * Sets the isMultiplyingOutputComponents.
     *
     * @param isMultiplyingOutputComponents the isMultiplyingOutputComponents to set
     */
    public void setMultiplyingOutputComponents(boolean isMultiplyingOutputComponents) {
        this.isMultiplyingOutputComponents = isMultiplyingOutputComponents;
    }
}
