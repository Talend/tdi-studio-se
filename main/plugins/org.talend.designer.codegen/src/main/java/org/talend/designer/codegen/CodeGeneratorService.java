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
package org.talend.designer.codegen;

import java.beans.PropertyChangeEvent;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.commons.ui.runtime.CommonUIPlugin;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.model.components.ComponentCompilations;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IProcess;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.views.IComponentSettingsView;
import org.talend.designer.codegen.model.CodeGeneratorEmittersPoolFactory;
import org.talend.designer.core.IDesignerCoreService;

/**
 * DOC bqian class global comment. Provides services for CodeGenerator plugin. <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (星期五, 29 九月 2006) nrousseau $
 *
 */
public class CodeGeneratorService implements ICodeGeneratorService {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.codegen.ICodeGeneratorFactory#getCodeGenerator()
     */
    @Override
    public ICodeGenerator createCodeGenerator() {
        return new CodeGenerator();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.codegen.ICodeGeneratorFactory#getCodeGenerator(org.talend.core.model.process.IProcess,
     * boolean, boolean, boolean, java.lang.String)
     */
    @Override
    public ICodeGenerator createCodeGenerator(IProcess process, boolean statistics, boolean trace, String... options) {
        ICodeGeneratorService codeGenService = null;

        codeGenService = CodeGeneratorService.getService(ISparkCodeGeneratorService.class);
        if (codeGenService != null && ((ISparkCodeGeneratorService) codeGenService).validProcess(process)) {
            return codeGenService.createCodeGenerator(process, statistics, trace, options);
        }

        codeGenService = CodeGeneratorService.getService(ISparkStreamingCodeGeneratorService.class);
        if (codeGenService != null && ((ISparkStreamingCodeGeneratorService) codeGenService).validProcess(process)) {
            return codeGenService.createCodeGenerator(process, statistics, trace, options);
        }

        codeGenService = CodeGeneratorService.getService(IMRCodeGeneratorService.class);
        if (codeGenService != null && ((IMRCodeGeneratorService) codeGenService).validProcess(process)) {
            return codeGenService.createCodeGenerator(process, statistics, trace, options);
        }

        codeGenService = CodeGeneratorService.getService(ICamelCodeGeneratorService.class);
        if (codeGenService != null && ((ICamelCodeGeneratorService) codeGenService).validProcess(process)) {
            return codeGenService.createCodeGenerator(process, statistics, trace, options);
        }

        codeGenService = CodeGeneratorService.getService(IStormCodeGeneratorService.class);
        if (codeGenService != null && ((IStormCodeGeneratorService) codeGenService).validProcess(process)) {
            return codeGenService.createCodeGenerator(process, statistics, trace, options);
        }

        return new CodeGenerator(process, statistics, trace, options);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.codegen.ICodeGeneratorService#getRoutineSynchronizer()
     */
    @Override
    public ITalendSynchronizer createPerlRoutineSynchronizer() {
        return null; // no perl
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.codegen.ICodeGeneratorService#createJavaRoutineSynchronizer()
     */
    @Override
    public ITalendSynchronizer createJavaRoutineSynchronizer() {
        return new JavaRoutineSynchronizer();
    }

    @Override
    public ITalendSynchronizer createRoutineSynchronizer() {
        return createJavaRoutineSynchronizer();
    }

    @Override
    public ISQLPatternSynchronizer getSQLPatternSynchronizer() {
        return new JavaSQLPatternSynchronizer();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.codegen.ICodeGeneratorService#initializeTemplates(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public Job initializeTemplates() {
        return CodeGeneratorEmittersPoolFactory.initialize();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.codegen.ICodeGeneratorService#refreshTemplates()
     */
    @Override
    public Job refreshTemplates() {
        Element oldComponent = null;
        IComponentSettingsView viewer = null;
        if (!CommonUIPlugin.isFullyHeadless()) {
            // TDI-25866:In case select a component and sctrl+shift+f3,need clean its componentSetting view
            IWorkbenchWindow wwindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (wwindow != null && wwindow.getActivePage() != null) {
                viewer = (IComponentSettingsView) wwindow.getActivePage().findView(IComponentSettingsView.ID);

                if (viewer != null) {
                    oldComponent = viewer.getElement();
                    viewer.cleanDisplay();
                }
            }
        }
        ComponentCompilations.deleteMarkers();
        ComponentsFactoryProvider.getInstance().resetCache();
        ILibraryManagerService librairesManagerService = (ILibraryManagerService) GlobalServiceRegister.getDefault().getService(
                ILibraryManagerService.class);
        librairesManagerService.clearCache();
        CorePlugin.getDefault().getLibrariesService().syncLibraries();
        Job job = CodeGeneratorEmittersPoolFactory.initialize();
        // achen modify to record ctrl+shift+f3 is pressed to fix bug 0006107
        IDesignerCoreService designerCoreService = (IDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                IDesignerCoreService.class);
        designerCoreService.getLastGeneratedJobsDateMap().clear();

        if (oldComponent != null && viewer != null) {
            viewer.setElement(oldComponent);
        }
        if (!CommonUIPlugin.isFullyHeadless()) {
            CorePlugin.getDefault().getDesignerCoreService()
                    .synchronizeDesignerUI(new PropertyChangeEvent(this, IComponentConstants.NORMAL, null, null));
        }
        return job;
    }

    @Override
    public boolean isInitializingJet() {
        return !CodeGeneratorEmittersPoolFactory.isInitialized() && CodeGeneratorEmittersPoolFactory.isInitializeStart();
    }

    private static ICodeGeneratorService getService(Class<? extends ICodeGeneratorService> klass) {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(klass)) {
            return (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(klass);
        }
        return null;
    }
}
