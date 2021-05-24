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
package org.talend.designer.codegen.exception;

import org.talend.commons.exception.SystemException;

/**
 * CodeGenerator Typed Exception.
 *
 * $Id$
 *
 */
public class CodeGeneratorException extends SystemException {

    private static final long serialVersionUID = -8567088114593303753L;

    public CodeGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodeGeneratorException(Throwable cause) {
        super(cause);
    }

    public CodeGeneratorException(String message) {
        super(message);
    }
}
