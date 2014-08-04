// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
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
import org.eclipse.ui.PlatformUI;
import org.talend.commons.model.components.IComponentConstants;
import org.talend.commons.ui.runtime.CommonUIPlugin;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.components.ComponentCompilations;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IProcess;
import org.talend.designer.codegen.i18n.Messages;
import org.talend.designer.codegen.model.CodeGeneratorEmittersPoolFactory;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.ui.views.properties.IComponentSettingsView;
import org.talend.repository.model.ComponentsFactoryProvider;

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
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IMRCodeGeneratorService.class)) {
            IMRCodeGeneratorService service = (IMRCodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                    IMRCodeGeneratorService.class);
            if (service.validProcess(process)) {
                return service.createCodeGenerator(process, statistics, trace, options);
            }
        }

        if (GlobalServiceRegister.getDefault().isServiceRegistered(IStormCodeGeneratorService.class)) {
            IStormCodeGeneratorService service = (IStormCodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                    IStormCodeGeneratorService.class);
            if (service.validProcess(process)) {
                return service.createCodeGenerator(process, statistics, trace, options);
            }
        }

        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelCodeGeneratorService.class)) {
			ICamelCodeGeneratorService service = (ICamelCodeGeneratorService) GlobalServiceRegister.getDefault()
					.getService(ICamelCodeGeneratorService.class);
            if (service.validProcess(process)) {
                return service.createCodeGenerator(process, statistics, trace, options);
            }
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
        return new PerlRoutineSynchronizer();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.codegen.ICodeGeneratorService#createJavaRoutineSynchronizer()
     */
    @Override
    public ITalendSynchronizer createJavaRoutineSynchronizer() {
        // TODO Auto-generated method stub
        return new JavaRoutineSynchronizer();
    }

    @Override
    public ITalendSynchronizer createRoutineSynchronizer() {
        ECodeLanguage lan = LanguageManager.getCurrentLanguage();
        if (lan.equals(ECodeLanguage.PERL)) {
            return createPerlRoutineSynchronizer();
        } else if (lan.equals(ECodeLanguage.JAVA)) {
            return createJavaRoutineSynchronizer();
        }
        throw new IllegalArgumentException(Messages.getString("CodeGeneratorService.invalidLanguage1")); //$NON-NLS-1$
    }

    @Override
    public ITalendSynchronizer createCamelBeanSynchronizer() {
        ECodeLanguage lan = LanguageManager.getCurrentLanguage();
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
            ICamelDesignerCoreService service = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                    ICamelDesignerCoreService.class);
            if (lan.equals(ECodeLanguage.JAVA)) {
                return service.createCamelJavaSynchronizer();
            }
        }
        return null;
    }

    @Override
    public ISQLPatternSynchronizer getSQLPatternSynchronizer() {
        ECodeLanguage lan = LanguageManager.getCurrentLanguage();
        if (lan.equals(ECodeLanguage.PERL)) {
            return new PerlSQLPatternSynchronizer();
        } else if (lan.equals(ECodeLanguage.JAVA)) {
            return new JavaSQLPatternSynchronizer();
        }
        throw new IllegalArgumentException(Messages.getString("CodeGeneratorService.invalidLanguage2")); //$NON-NLS-1$
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
            viewer = (IComponentSettingsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .findView(IComponentSettingsView.ID);

            if (viewer != null) {
                oldComponent = viewer.getElement();
                viewer.cleanDisplay();
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

        if (oldComponent != null) {
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
}
