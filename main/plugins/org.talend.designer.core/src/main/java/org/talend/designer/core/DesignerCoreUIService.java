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
package org.talend.designer.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.process.IGenericProvider;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.process.IGEFProcess;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.model.process.GenericProcessProvider;
import org.talend.designer.core.ui.editor.TalendEditorPaletteFactory;
import org.talend.designer.core.ui.editor.cmd.MavenDeploymentValueChangeCommand;
import org.talend.designer.core.ui.editor.palette.TalendPaletteDrawer;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class DesignerCoreUIService implements IDesignerCoreUIService {

    @Override
    public PaletteRoot createPalette(IComponentsFactory factory) {
        return TalendEditorPaletteFactory.createPalette(factory);
    }

    @Override
    public PaletteRoot createPalette(IComponentsFactory factory, boolean isFavorite) {
        return TalendEditorPaletteFactory.createPalette(factory, isFavorite);
    }

    @Override
    public PaletteRoot createPalette(IComponentsFactory compFac, PaletteRoot root) {
        return TalendEditorPaletteFactory.createPalette(compFac, root);
    }

    @Override
    public PaletteRoot createPalette(IComponentsFactory compFac, PaletteRoot root, boolean isFavorite) {
        return TalendEditorPaletteFactory.createPalette(compFac, root, isFavorite);
    }

    @Override
    public void deleteJobletConfigurationsFromPalette(String jobletName) {
        TalendEditorPaletteFactory.deleteJobletConfigurationsFromPalette(jobletName);
    }

    @Override
    public PaletteRoot getAllNodeStructure(IComponentsFactory factory) {
        return TalendEditorPaletteFactory.getAllNodeStructure(factory);
    }

    @Override
    public PaletteRoot createEmptyPalette() {
        return TalendEditorPaletteFactory.createEmptyPalette();
    }

    @Override
    public PaletteDrawer createTalendPaletteDrawer(String family) {
        return new TalendPaletteDrawer(family);
    }

    @Override
    public void setPaletteFilter(String filter) {
        TalendEditorPaletteFactory.setFilter(filter);
    }

    @Override
    public IPreferenceStore getPreferenceStore() {
        return DesignerPlugin.getDefault().getPreferenceStore();
    }

    @Override
    public List<PaletteEntry> createPaletteEtnry() {
        List<PaletteEntry> list = new ArrayList<PaletteEntry>();
        for (AbstractProcessProvider provider : AbstractProcessProvider.findAllProcessProviders()) {
            list.addAll(provider.addJobletEntry());
        }
        for (IGenericProvider provider : GenericProcessProvider.getInstance().findAllProcessProviders()) {
            list.addAll((Collection<? extends PaletteEntry>) provider.addPaletteEntry());
        }
        return list;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.service.IDesignerCoreUIService#removePreferenceStorePropertyChangeListener(org.eclipse.jface.
     * util.IPropertyChangeListener)
     */
    @Override
    public void removePreferenceStorePropertyChangeListener(IPropertyChangeListener listener) {
        if (listener != null) {
            DesignerPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(listener);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.service.IDesignerCoreUIService#executeCommand(org.talend.core.model.process.IProcess2,
     * org.eclipse.gef.commands.Command)
     */
    @Override
    public boolean executeCommand(final IGEFProcess process, final Command cmd) {
        if (cmd != null && process instanceof org.talend.designer.core.ui.editor.process.Process) {
            final org.talend.designer.core.ui.editor.process.Process p = ((org.talend.designer.core.ui.editor.process.Process) process);
            if (p.getEditor() != null) {
                p.getEditor().getEditorSite().getShell().getDisplay().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        p.getCommandStack().execute(cmd);
                    }
                });
            } else {
                p.getCommandStack().execute(cmd);
            }
            return true;
        }
        return false;

    }

    @Override
    public Command createMavenDeploymentValueChangeCommand(Object object, String type, String value) {
        return new MavenDeploymentValueChangeCommand(object, type, value);
    }

    @Override
    public void loadComponentsFromProviders(ERepositoryObjectType type) {
        AbstractProcessProvider.loadComponentsFromProviders(type);
    }
}
