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
package org.talend.repository.view.di.viewer.handlers;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.model.RepositoryConstants;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class SqlTemplateImportHandler extends ImportRepTypeHandler {

    /**
     * DOC ggu SqlTemplateImportHandler constructor comment.
     */
    public SqlTemplateImportHandler() {
        super();
    }

    @Override
    protected boolean validRelativePath(IPath relativePath) {
        boolean valid = super.validRelativePath(relativePath);
        if (valid) { // ignore system items
            ERepositoryObjectType type = ERepositoryObjectType.SQLPATTERNS;
            if (type != null) {
                IPath pah = relativePath.makeRelativeTo(new Path(type.getFolder()));
                pah = pah.removeFirstSegments(1); // remove for database name
                if (new Path(RepositoryConstants.SYSTEM_DIRECTORY).isPrefixOf(pah)) {
                    valid = false; // system items
                }
            }
        }
        return valid;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler#valid(org.talend.repository.items
     * .importexport.handlers.model.ImportItem)
     */
    @Override
    public boolean valid(ImportItem importItem) {
        boolean valid = super.valid(importItem);
        if (valid) {
            Item item = importItem.getItem();
            if (item instanceof SQLPatternItem) {
                if (isSystemItem(item)) {
                    return false;
                }
            }
        }
        return valid;
    }

    @Override
    public boolean isValidSystemItem(ImportItem importItem) {
        boolean valid = super.valid(importItem);
        if (!valid) {
            return false;
        }
        Item item = importItem.getItem();
        if (item instanceof SQLPatternItem) {
            if (isSystemItem(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSystemItem(Item item) {
        if (item instanceof SQLPatternItem) {
            SQLPatternItem patternItem = (SQLPatternItem) item;
            if (patternItem.isSystem()) {
                return true;
            }
        }
        return false;
    }
}
