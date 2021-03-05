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
package org.talend.designer.codegen.additionaljet;

import java.util.List;

import org.talend.designer.codegen.config.TemplateUtil;
import org.talend.designer.codegen.model.CodeGeneratorInternalTemplatesFactory;

/**
 * DOC wyang class global comment. Detailled comment
 */
public abstract class AbstractJetFileProvider {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TemplateUtil> initializeStubAdditionalJetFile() {
        return CodeGeneratorInternalTemplatesFactory.getTemplatesFrom(getBundleId(), getJetPath());
    }

    protected abstract String getBundleId();

    protected abstract String getJetPath();

}
