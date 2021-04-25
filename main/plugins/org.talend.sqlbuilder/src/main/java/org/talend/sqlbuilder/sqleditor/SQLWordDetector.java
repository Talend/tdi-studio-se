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
package org.talend.sqlbuilder.sqleditor;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * A Sql aware word detector. <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
 *
 */
public class SQLWordDetector implements IWordDetector {

    /*
     * (non-Javadoc) Method declared on IWordDetector.
     */
    public boolean isWordPart(char character) {
        return Character.isJavaIdentifierPart(character);
    }

    /*
     * (non-Javadoc) Method declared on IWordDetector.
     */
    public boolean isWordStart(char character) {
        return Character.isJavaIdentifierStart(character);
    }
}
