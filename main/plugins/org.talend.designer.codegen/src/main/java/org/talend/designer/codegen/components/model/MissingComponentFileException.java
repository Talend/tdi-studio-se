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
package org.talend.designer.codegen.components.model;

import org.talend.commons.exception.BusinessException;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class MissingComponentFileException extends BusinessException {

    private static final long serialVersionUID = -4188249286691560999L;

    public MissingComponentFileException(String message) {
        super(message);
    }

}
