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
package org.talend.designer.webservice.ws.wsdlutil;

/**
 * DOC gcui class global comment. Detailled comment
 */
public class WsdlTypeUtil {

    public static Boolean isJavaBasicType(String typeName) {
        Boolean isJavaBasicType = false;
        if (typeName == null) {
            return false;
        }
        if ("String".equalsIgnoreCase(typeName)) {
            isJavaBasicType = true;
        } else if ("int".equalsIgnoreCase(typeName)) {
            isJavaBasicType = true;
        } else if ("long".equalsIgnoreCase(typeName)) {
            isJavaBasicType = true;
        } else if ("double".equalsIgnoreCase(typeName)) {
            isJavaBasicType = true;
        } else if ("float".equalsIgnoreCase(typeName)) {
            isJavaBasicType = true;
        } else if ("char".equalsIgnoreCase(typeName)) {
            isJavaBasicType = true;
        }

        return isJavaBasicType;

    }
}
