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
package org.talend.designer.business.diagram.views.handler;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.commons.runtime.model.emf.TalendXMIResource;
import org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class BusinessDiagramImportHandler extends ImportRepTypeHandler {

    /**
     * DOC ggu BusinessDiagramImportHandler constructor comment.
     */
    public BusinessDiagramImportHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler#createItemResource(org.eclipse
     * .emf.common.util.URI)
     */
    @Override
    protected Resource createItemResource(URI pathUri) {
        return new TalendXMIResource(pathUri);
    }
}
