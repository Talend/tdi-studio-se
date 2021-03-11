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
package org.talend.designer.core.model.process;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.IReplaceNodeInProcess;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryEditorInput;
import org.talend.core.model.update.UpdateResult;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * DOC qzhang class global comment. Detailled comment
 */
public abstract class AbstractProcessProvider implements IReplaceNodeInProcess {

    public static final String EXTENSION_ID = "org.talend.designer.core.process_provider"; //$NON-NLS-1$

    public static final String ATTR_CLASS = "class"; //$NON-NLS-1$

    public static final String ATTR_PID = "pluginId"; //$NON-NLS-1$

    private static boolean canCreateNode = true;

    private static IProcess componentProcess = null;

    private static Map<String, AbstractProcessProvider> providerMap = new HashMap<String, AbstractProcessProvider>();

    public static AbstractProcessProvider findProcessProviderFromPID(String pid) {
        if (providerMap == null || !providerMap.containsKey(pid)) {
            findAllProcessProviders();
        }
        return providerMap.get(pid);
    }

    public static Collection<AbstractProcessProvider> findAllProcessProviders() {
        if (providerMap.isEmpty()) {
            IConfigurationElement[] elems = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_ID);
            for (IConfigurationElement elem : elems) {
                String pid = elem.getAttribute(ATTR_PID);
                try {
                    Object provider = elem.createExecutableExtension(ATTR_CLASS);
                    if (provider instanceof AbstractProcessProvider) {
                        AbstractProcessProvider createExecutableExtension = (AbstractProcessProvider) provider;
                        providerMap.put(pid, createExecutableExtension);
                    }
                } catch (CoreException ex) {
                    ExceptionHandler.process(ex);
                }
            }
        }
        return providerMap.values();
    }

    /**
     * DOC qzhang Comment method "isExtensionProcessForJoblet".
     *
     * @param process
     * @return
     */
    public static boolean isExtensionProcessForJoblet(IProcess process) {
        AbstractProcessProvider findProcessProvider = findProcessProviderFromPID(IComponent.JOBLET_PID);
        if (findProcessProvider != null) {
            return findProcessProvider.isExtensionProcess(process);
        }
        return false;
    }

    public static boolean isExtensionProcess(IProcess process, String pid) {
        AbstractProcessProvider findProcessProvider = findProcessProviderFromPID(pid);
        if (findProcessProvider != null) {
            return findProcessProvider.isExtensionProcess(process);
        }
        return false;
    }

    /**
     * DOC qzhang Comment method "openNewProcessEditor".
     */
    public void openNewProcessEditor(INode node) {
        // do nothing.
    }

    public void loadComponentsFromExtensionPoint() {
        // do nothing.
    }

    public void loadComponentsFromExtensionPoint(ERepositoryObjectType type) {
        // do nothing.
    }

    public abstract Process buildNewGraphicProcess(Item node);

    public abstract Process buildNewGraphicProcess(Item node, boolean needScreenshot);

    /**
     * DOC qzhang Comment method "loadComponentsFromProviders".
     *
     * @return
     */
    public static void loadComponentsFromProviders() {
        for (AbstractProcessProvider processProvider : findAllProcessProviders()) {
            processProvider.loadComponentsFromExtensionPoint();
        }
    }

    /**
     * DOC hwang Comment method "loadComponentsFromProviders".
     *
     * @return
     */
    public static void loadComponentsFromProviders(ERepositoryObjectType type) {
        for (AbstractProcessProvider processProvider : findAllProcessProviders()) {
            if (type == null) {
                processProvider.loadComponentsFromExtensionPoint();
            } else {
                processProvider.loadComponentsFromExtensionPoint(type);
            }
        }
    }
    
    /**
     * DOC qzhang Comment method "canDeleteNode".
     *
     * @param no
     *
     * @return
     */
    public boolean canDeleteNode(INode no) {
        return true;
    }

    public boolean canCopyNode(INode no) {
        return true;
    }

    public boolean canCutNode(INode no) {
        return true;
    }

    // public abstract void updateJobletContext(Node node);

    /**
     * DOC qzhang Comment method "updateJobletContext".
     *
     * @param nodes
     */
    public abstract List<String> updateProcessContexts(IProcess process);

    public abstract List<String> updateProcessContextsWithoutUI(IProcess process);

    /**
     *
     * ggu Comment method "checkJobletNodeSchema".
     *
     *
     */
    public abstract List<UpdateResult> checkJobletNodeSchema(IProcess process);

    public abstract boolean hasJobletComponent(IProcess process);

    public abstract ImageDescriptor getIcons(IProcess2 process);

    public abstract void setIcons(IProcess process, ImageDescriptor image);

    /**
     * DOC qzhang Comment method "canCreate".
     *
     * @param node
     */
    public boolean canCreateNode(INode node) {
        return true;
    }

    public abstract List<PaletteEntry> addJobletEntry();

    public boolean isExtensionComponent(INode node) {
        return false;
    }

    /**
     * DOC qli Comment method "isExtensionProcess".
     *
     * @param process
     *
     * @return
     */
    public boolean isJobletInputOrOutputComponent(INode node) {
        return false;
    }

    /**
     * DOC qli Comment method "isExtensionProcess".
     *
     * @param process
     *
     * @return
     */
    public boolean isExtensionProcess(IProcess process) {
        return false;
    }

    public abstract IProcess getProcessFromJobletProcessItem(JobletProcessItem jobletProcessItem);

    /**
     *
     * cLi Comment method "getJobletItem".
     *
     * bug 6158
     */
    public abstract Item getJobletItem(INode node);

    // for feature 13361
    public abstract Item getJobletItem(INode node, String version);

    public boolean containNodeInMemoryNotProcess() {
        return canCreateNode;
    }

    public void setCanCreateNode(boolean flag) {
        canCreateNode = flag;
    }

    public static IProcess getComponentProcess() {
        return componentProcess;
    }

    public static void setComponentProcess(Process componentProcess) {
        AbstractProcessProvider.componentProcess = componentProcess;
    }

    public boolean canHandleNode(INode node) {
        return false;
    }
    
    public IRepositoryEditorInput createJobletEditorInput(JobletProcessItem processItem, boolean load, Boolean lastVersion, Boolean readonly,
            Boolean openedInJob) throws PersistenceException{
        return null;
    }

}
