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
package org.talend.designer.codegen.model;

/**
 * Provider for current instance of CodeGeneratorInternalTemplatesFactory.
 *
 * $Id$
 *
 */
public final class CodeGeneratorInternalTemplatesFactoryProvider {

    /**
     * Default Constructor. Must not be used.
     */
    private CodeGeneratorInternalTemplatesFactoryProvider() {
    }

    private static CodeGeneratorInternalTemplatesFactory singleton = null;

    /**
     * Return the instance of CodeGeneratorInternalTemplatesFactory to use.
     *
     * @return
     */
    public static CodeGeneratorInternalTemplatesFactory getInstance() {
        if (singleton == null) {
            singleton = new CodeGeneratorInternalTemplatesFactory();
        }
        return singleton;
    }

}
