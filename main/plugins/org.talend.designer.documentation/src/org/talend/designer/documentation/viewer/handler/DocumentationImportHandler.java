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
package org.talend.designer.documentation.viewer.handler;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler;
import org.talend.repository.model.RepositoryConstants;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class DocumentationImportHandler extends ImportRepTypeHandler {

    /**
     * DOC ggu DocumentationImportHandler constructor comment.
     */
    public DocumentationImportHandler() {
        super();
    }

     @Override
    protected boolean validRelativePath(IPath relativePath) {
        boolean valid = super.validRelativePath(relativePath);
        if (valid) { // ignore generated documentation
            ERepositoryObjectType type = ERepositoryObjectType.DOCUMENTATION;
            if (type != null) {
                IPath pah = relativePath.makeRelativeTo(new Path(type.getFolder()));
                if (new Path(RepositoryConstants.DOCUMENTATION_GENERATED_PATH).isPrefixOf(pah)) {
                    valid = false; // generated doc
                }
            }
        }
        return valid;
    }

}
