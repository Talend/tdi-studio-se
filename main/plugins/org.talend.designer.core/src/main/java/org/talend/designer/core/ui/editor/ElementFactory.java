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
package org.talend.designer.core.ui.editor;

import org.eclipse.gef.requests.CreationFactory;

/**
 * Factory used to create elements in the diagram. <br/>
 *
 * $Id$
 *
 */
public class ElementFactory implements CreationFactory {

    private Object template;

    public ElementFactory(Object template) {
        this.template = template;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.requests.CreationFactory#getNewObject()
     */
    public Object getNewObject() {
        try {
            return ((Class) template).newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.requests.CreationFactory#getObjectType()
     */
    public Object getObjectType() {
        return template;
    }
}
