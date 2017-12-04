// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;
import org.talend.componentdesigner.ComponentDesigenerPlugin;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.i18n.internal.Messages;
import org.talend.componentdesigner.model.ILibEntry;
import org.talend.componentdesigner.model.libentry.JarLibEntry;
import org.talend.componentdesigner.model.libentry.PmLibEntry;
import org.talend.componentdesigner.ui.composite.ILibListViewer;

/**
 * Adds an internal jar to the runtime class path.
 */
public class AddResourceAction extends UseResourceAction {

    private final ISelectionStatusValidator validator = new ISelectionStatusValidator() {

        public IStatus validate(Object[] selection) {
            if (selection.length == 0) {
                return new Status(IStatus.ERROR, ComponentDesigenerPlugin.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
            }
            for (int i = 0; i < selection.length; i++) {
                if (!(selection[i] instanceof IFile)) {
                    return new Status(IStatus.ERROR, ComponentDesigenerPlugin.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
                }
            }
            return new Status(IStatus.OK, ComponentDesigenerPlugin.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
        }
    };

    public AddResourceAction(ILibListViewer viewer) {
        super(Messages.getString("AddResourceAction.AddLib"), viewer); //$NON-NLS-1$
    }

    /**
     * Prompts for a jar to add.
     * 
     * @see IAction#run()
     */
    @Override
    public void run() {

        // ViewerFilter filter = new ArchiveFilter(getSelectedJars());

        ILabelProvider lp = new WorkbenchLabelProvider();
        ITreeContentProvider cp = new WorkbenchContentProvider();

        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), lp, cp);
        dialog.setValidator(validator);
        dialog.setTitle(Messages.getString("AddResourceAction.JARSecection")); //$NON-NLS-1$
        dialog.setMessage(Messages.getString("AddResourceAction.ChooseResource")); //$NON-NLS-1$
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot().getProject(PluginConstant.COMPONENT_PROJECT));
        dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));

        if (dialog.open() == Window.OK) {
            Object[] elements = dialog.getResult();
            List<ILibEntry> res = new ArrayList<ILibEntry>();
            for (int i = 0; i < elements.length; i++) {
                IResource elem = (IResource) elements[i];
                String name = elem.getName();
                if (name.matches("(?i).*\\.(jar)\\b")) { //$NON-NLS-1$
                    res.add(new JarLibEntry(elem));
                }
                if (name.matches("(?i).*\\.(pm)\\b")) { //$NON-NLS-1$
                    res.add(new PmLibEntry(elem));
                }
            }
            if (res.size() > 0) {
                ILibEntry[] entries = new ILibEntry[res.size()];
                getViewer().addEntries(res.toArray(entries));
            }
        }
    }

    @Override
    protected int getActionType() {
        return ADD;
    }
}
