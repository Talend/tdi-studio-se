// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import java.lang.reflect.Method;

import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;

/**
 * created by ycbai on 2017年4月17日 Detailled comment
 *
 */
public class TestUtils {

    public static void invokePrivateMethod(Object owner, String methodName, Object[] args, Class... argTypes) throws Exception {
        try {
            Class ownerClass = owner.getClass();
            Method method = ownerClass.getDeclaredMethod(methodName, argTypes);
            method.setAccessible(true);
            method.invoke(owner, args);
        } catch (Exception e) {
            throw e;
        }
    }

    public static Property createDefaultProperty() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);

        return property;
    }

}
