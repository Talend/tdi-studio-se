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
package org.talend.designer.core.model.components;

import org.talend.core.model.components.IComponent;

/**
 * created by cmeng on Jul 21, 2016
 * Detailled comment
 *
 */
public class ComponentHit implements Comparable<ComponentHit> {

    private int priority = 0;

    private IComponent component;

    public ComponentHit(IComponent comp) {
        this.component = comp;
    }

    public ComponentHit(IComponent comp, int prior) {
        this(comp);
        this.priority = prior;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public IComponent getComponent() {
        return this.component;
    }

    public void setComponent(IComponent component) {
        this.component = component;
    }

    @Override
    public int compareTo(ComponentHit o) {
        if (o == null) {
            return -1;
        }
        int result = (this.priority - o.priority);
        if (result == 0) {
            result = this.getComponent().getName().length() - o.getComponent().getName().length();
        }
        if (result == 0) {
            result = this.getComponent().getName().compareTo(o.getComponent().getName());
        }
        return result;
    }

    @Override
    public String toString() {
        return "Priority " + this.priority + ":" + this.component.toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
