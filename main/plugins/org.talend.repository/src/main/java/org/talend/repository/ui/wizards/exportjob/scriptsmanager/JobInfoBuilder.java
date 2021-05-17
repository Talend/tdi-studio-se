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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.runtime.process.JobInfoProperties;
import org.talend.repository.documentation.ExportFileResource;

public class JobInfoBuilder {

    private String jobPropertyFile = null;

    private boolean applyContextToChildFlag = false;

    private boolean addStatFlag = false;

    private ExportFileResource resource = null;

    private String contextName = null;

    JobInfoBuilder(ExportFileResource efr, String context, boolean contextToChild, boolean addStat, String jobInfoFile) {
        this.resource = efr;
        this.contextName = context;
        this.applyContextToChildFlag = contextToChild;
        this.addStatFlag = addStat;
        this.jobPropertyFile = jobInfoFile;
    }

    /**
     * Builds the jar file.
     *
     * @throws Exception
     */
    public void buildProperty() {

        OutputStream fos = null;
        try {

            File jobInfoFile = new File(jobPropertyFile);
            fos = new FileOutputStream(jobInfoFile);
            JobInfoProperties jobInfoProp = new JobInfoProperties((ProcessItem) resource.getItem(), this.contextName,
                    applyContextToChildFlag, addStatFlag);
            jobInfoProp.store(fos, "just for tac"); //$NON-NLS-1$
        } catch (IOException e) {
            ExceptionHandler.process(e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

}
