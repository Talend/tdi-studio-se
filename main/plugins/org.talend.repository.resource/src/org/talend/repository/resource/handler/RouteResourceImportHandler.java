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
package org.talend.repository.resource.handler;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.ReferenceFileItem;
import org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler;
import org.talend.repository.items.importexport.handlers.model.ImportItem;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class RouteResourceImportHandler extends ImportRepTypeHandler {

    /**
     * DOC ggu RouteResourceImportHandler constructor comment.
     */
    public RouteResourceImportHandler() {
        super();
    }

    /*
     * TESB-16305: Set null for reference item name on importing
     */
    @Override
    protected void beforeCreatingItem(ImportItem importItem) {
        Property property = importItem.getProperty();
        if (property != null) {
            for (Object itemRefObj : property.getItem().getReferenceResources()) {
                ReferenceFileItem refItem = (ReferenceFileItem) itemRefObj;
                if (refItem.getName() != null) {
                    refItem.setName(null);
                }
            }
        }
    }

    @Override
    protected IPath getReferenceItemPath(IPath importItemPath, ReferenceFileItem rfItem) {
        // TESB-16314 Log error in case of the import file does not exists.
        IPath filePath = super.getReferenceItemPath(importItemPath, rfItem);
        File f = filePath.toFile();
        if (!f.exists()) {
            if (!f.getParentFile().exists()) {
                // supposedly not in a regular file system
                log.info("File status of " + filePath.lastSegment() +
                        " cannot be verified, assuming import from ZIP archive."); //$NON-NLS-1$
                return filePath;
            }
            String portableString = importItemPath.toPortableString();
            String substring = portableString.substring(0, portableString.lastIndexOf('_'));
            IPath altFilePath = new Path(substring);
            if (altFilePath.toFile().exists()) {
                return altFilePath;
            }
            log.error("File with the name " + filePath.lastSegment() + " does not exits."); //$NON-NLS-2$
        }
        return filePath;
    }

}
