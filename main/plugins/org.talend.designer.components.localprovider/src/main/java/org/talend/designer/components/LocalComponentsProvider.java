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
package org.talend.designer.components;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.AbstractComponentsProvider;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public class LocalComponentsProvider extends AbstractComponentsProvider {

    /**
     * DOC guanglong.du LocalComponentsProvider constructor comment.
     */
    public LocalComponentsProvider() {

    }

    /*
     * (non-Jsdoc)
     *
     * @see org.talend.core.model.components.AbstractComponentsProvider#getExternalComponentsLocation()
     */
    protected File getExternalComponentsLocation() {
        URL url = FileLocator.find(ComponentsLocalProviderPlugin.getDefault().getBundle(), new Path("components"), null); //$NON-NLS-1$
        URL fileUrl;
        try {
            fileUrl = FileLocator.toFileURL(url);
            return new File(fileUrl.getPath());
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

}
