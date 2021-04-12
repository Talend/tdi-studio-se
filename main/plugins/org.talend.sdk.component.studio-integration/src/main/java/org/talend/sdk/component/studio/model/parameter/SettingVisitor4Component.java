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
package org.talend.sdk.component.studio.model.parameter;

import org.talend.core.model.process.IElement;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.sdk.component.server.front.model.ComponentDetail;

/**
 * created by hcyi on Dec 24, 2019
 * Detailled comment
 *
 */
public class SettingVisitor4Component extends SettingVisitor {

    public SettingVisitor4Component(IElement iNode, ElementParameter redrawParameter, ComponentDetail detail) {
        super(iNode, redrawParameter, detail);
    }

    @Override
    public void buildHealthCheck(final PropertyNode node) {
        // do nothing
    }
}