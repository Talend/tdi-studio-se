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
package org.talend.designer.runprocess.shadow;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.talend.core.repository.model.preview.IPreview;
import org.talend.core.repository.model.preview.IProcessDescription;
import org.talend.core.utils.CsvArray;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.shadow.ShadowProcess.EShadowProcessType;

/**
 * Previewer for a file delimited input. <br/>
 *
 * $Id$
 *
 */
public class ShadowFilePreview implements IPreview {

    public ShadowProcess<IProcessDescription> shadowProcess;

    /**
     * Constructs a new ShadowPreview.
     */
    public ShadowFilePreview() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.preview.filedelimited.IFileDelimitedPreview#
     * preview(org.talend.repository.preview.filedelimited.ProcessDescription)
     */
    public CsvArray preview(IProcessDescription description, String type) throws CoreException {
        CsvArray res = null;

        EShadowProcessType typeShadow = EShadowProcessType.valueOf(type);

        shadowProcess = new ShadowProcess<IProcessDescription>(description, typeShadow);
        try {
            res = shadowProcess.run();
        } catch (ProcessorException e) {
            Status status = new Status(Status.ERROR, RunProcessPlugin.PLUGIN_ID, Status.OK, e.getMessage(), e);
            RunProcessPlugin.getDefault().getLog().log(status);
            throw new CoreException(status);
        }
        return res;
    }

    /**
     * Stop loading preview.
     */
    public void stopLoading() {
        if (shadowProcess != null) {
            shadowProcess.destroy();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.preview.IPreview#preview(org.talend.repository.preview.IProcessDescription,
     * java.lang.String, boolean)
     */
    public CsvArray preview(IProcessDescription description, String type, boolean outputErrorAsException) throws CoreException {

        CsvArray res = null;

        EShadowProcessType typeShadow = EShadowProcessType.valueOf(type);

        shadowProcess = new ShadowProcess<IProcessDescription>(description, typeShadow);

        try {
            res = shadowProcess.runWithErrorOutputAsException(outputErrorAsException);
        } catch (ProcessorException e) {
            Status status = new Status(Status.ERROR, RunProcessPlugin.PLUGIN_ID, Status.OK, e.getMessage(), e);
            RunProcessPlugin.getDefault().getLog().log(status);
            throw new CoreException(status);
        }
        return res;
    }

    public boolean isTopPreview() {
        return false;
    }

}
