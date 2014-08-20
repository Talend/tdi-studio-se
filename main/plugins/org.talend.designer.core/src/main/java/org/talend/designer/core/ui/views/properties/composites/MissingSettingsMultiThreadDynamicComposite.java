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
package org.talend.designer.core.ui.views.properties.composites;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.utils.JavaProcessUtil;
import org.talend.designer.core.utils.ModulesInstallerUtil;
import org.talend.librariesmanager.prefs.LibrariesManagerUtils;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class MissingSettingsMultiThreadDynamicComposite extends TopMessagesMultiThreadDynamicComposite {

    private List<ModuleNeeded> missModulesNeeded = new ArrayList<ModuleNeeded>();

    public MissingSettingsMultiThreadDynamicComposite(Composite parentComposite, int styles, EComponentCategory section,
            Element element, boolean isCompactView) {
        super(parentComposite, styles, section, element, isCompactView);

    }

    @Override
    protected MessagesComposite createMessagesComposite() {
        return new MessagesWithActionComposite(getComposite(), SWT.NONE);

    }

    @Override
    protected void checkVisibleTopMessages() {
        missModulesNeeded.clear();
        final Element ele = this.getElement();
        if (ele instanceof Node) {
            List<ModuleNeeded> modules = new ArrayList<ModuleNeeded>();
            // find the all required modules.
            JavaProcessUtil.addNodeRelatedModules(((Node) ele).getProcess(), modules, ((Node) ele));
            // get not installed modules
            List<ModuleNeeded> updatedModules = LibrariesManagerUtils.getNotInstalledModules(modules);
            for (ModuleNeeded needed : updatedModules) {
                if (needed.isRequired(((Node) ele).getElementParameters())) {
                    needed.setRequired(true);
                }
            }
            missModulesNeeded.addAll(updatedModules);
        }
        setVisibleTopMessage(!missModulesNeeded.isEmpty());
    }

    @Override
    public void afterCreateMessagesComposite(MessagesComposite messComposite) {
        final Element ele = this.getElement();
        if (ele instanceof Node && isVisibleTopMessages()) {
            final IComponent component = ((Node) ele).getComponent();

            messComposite.updateTopMessages(
                    Messages.getString("MissingSettingsMultiThreadDynamicComposite_missingModulesMessages",//$NON-NLS-1$
                            component.getName()), IStatus.WARNING);
            if (messComposite instanceof MessagesWithActionComposite) {
                MessagesWithActionComposite messWithActionComposite = (MessagesWithActionComposite) messComposite;

                messWithActionComposite.updateActionButton(
                        Messages.getString("MissingSettingsMultiThreadDynamicComposite.installName") //$NON-NLS-1$
                                + "...", null);//$NON-NLS-1$
                messWithActionComposite.addActionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (!missModulesNeeded.isEmpty()) {
                            ModulesInstallerUtil.forceInstallModules(getShell(), component, missModulesNeeded);
                        }
                    }

                });
            }
        }

        super.afterCreateMessagesComposite(messComposite);
    }
}
