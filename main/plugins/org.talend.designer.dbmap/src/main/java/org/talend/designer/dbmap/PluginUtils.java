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

package org.talend.designer.dbmap;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.model.IRepositoryService;


/**
 * useful constants and methods <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40Z nrousseau $
 *
 */
public class PluginUtils {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.talend.designer.dbmap"; //$NON-NLS-1$

    /**
     * DOC get a implement of RunProcessService.
     *
     * @return
     */
    public static IRunProcessService getRunProcessService() {
        IService service = GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
        return (IRunProcessService) service;
    }

    /**
     * DOC get a implement of CodeGeneratorService.
     *
     * @return a implement of CodeGeneratorService
     */
    public static ICodeGeneratorService getCodeGeneratorService() {
        IService service = GlobalServiceRegister.getDefault().getService(ICodeGeneratorService.class);
        return (ICodeGeneratorService) service;
    }

    /**
     * DOC get a implement of CodeGeneratorService.
     *
     * @return a implement of CodeGeneratorService
     */
    public static IRepositoryService getRepositoryService() {
        IService service = GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        return (IRepositoryService) service;
    }

}
