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
package org.talend.designer.core.assist;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.talend.core.model.components.IComponent;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class ComponentContentProposal extends ContentProposal {

    protected IComponent component;

    public ComponentContentProposal(IComponent component) {
        super(component.getName(), component.getLongName());
        this.component = component;
    }

    //
    // public ComponentContentProposal(String content, String description, IComponent component) {
    // super(content, description);
    // this.component = component;
    // }

    public IComponent getComponent() {
        return this.component;
    }

    public void setComponent(IComponent component) {
        this.component = component;
    }

}
