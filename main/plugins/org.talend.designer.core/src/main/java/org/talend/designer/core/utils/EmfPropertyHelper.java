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
package org.talend.designer.core.utils;

import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id: EmfPropertyHelper.java 21728 2009-02-09 10:23:23Z plegall $
 *
 */
public class EmfPropertyHelper {

    /**
     * DOC mhelleboid Comment method "getItemPropertyDescriptor".
     *
     * @param itemPropertySource
     * @param eObject TODO
     * @param structuralFeature TODO
     * @return
     */
    public static IItemPropertyDescriptor getItemPropertyDescriptor(IItemPropertySource itemPropertySource,
            EObject eObject, EStructuralFeature structuralFeature) {
        IItemPropertyDescriptor itemPropertyDescriptor = null;

        List propertyDescriptors = itemPropertySource.getPropertyDescriptors(eObject);
        for (Iterator iter = propertyDescriptors.iterator(); iter.hasNext();) {
            IItemPropertyDescriptor tempItemPropertyDescriptor = (IItemPropertyDescriptor) iter.next();
            if (tempItemPropertyDescriptor.getFeature(eObject).equals(structuralFeature)) {
                itemPropertyDescriptor = tempItemPropertyDescriptor;
            }
        }
        return itemPropertyDescriptor;
    }

    /**
     * DOC mhelleboid Comment method "getItemPropertySource".
     *
     * @param adapterFactory TODO
     * @param eObject TODO
     *
     * @return
     */
    public static IItemPropertySource getItemPropertySource(AdapterFactory adapterFactory, EObject eObject) {
        return (IItemPropertySource) adapterFactory.adapt(eObject, IItemPropertySource.class);
    }

    /**
     * DOC mhelleboid Comment method "getValue".
     *
     * @param itemPropertyDescriptor TODO
     * @param eObject TODO
     * @return
     */
    public static String getValue(IItemPropertyDescriptor itemPropertyDescriptor, Object eObject) {
        String value = ""; //$NON-NLS-1$
        Object propertyValue = itemPropertyDescriptor.getPropertyValue(eObject);
        if (propertyValue instanceof IItemPropertySource) {
            IItemPropertySource propertyValueWrapper = (IItemPropertySource) propertyValue;
            value = propertyValueWrapper.getEditableValue(eObject).toString();
        }
        return value;
    }

}
