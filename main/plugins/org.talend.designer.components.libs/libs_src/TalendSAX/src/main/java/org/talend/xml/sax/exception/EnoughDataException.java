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
package org.talend.xml.sax.exception;

public class EnoughDataException extends RuntimeException {

    private static final long serialVersionUID = 3652240965496042451L;

    public EnoughDataException() {
        super();
    }

    public EnoughDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public EnoughDataException(String message) {
        super(message);
    }

    public EnoughDataException(Throwable cause) {
        super(cause);
    }

}
