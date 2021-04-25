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
package org.talend.designer.core.ui.views.properties.composites;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.swt.composites.MessagesComposite;
import org.talend.commons.ui.swt.composites.MessagesWithActionComposite;
import org.talend.core.CorePlugin;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.utils.ModulesInstallerUtil;
import org.talend.librariesmanager.prefs.LibrariesManagerUtils;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class MissingSettingsMultiThreadDynamicComposite extends TopMessagesMultiThreadDynamicComposite implements
        ILibrariesService.IChangedLibrariesListener {

    private List<ModuleNeeded> missModulesNeeded = new ArrayList<ModuleNeeded>();

    public MissingSettingsMultiThreadDynamicComposite(Composite parentComposite, int styles, EComponentCategory section,
            Element element, boolean isCompactView) {
        super(parentComposite, styles, section, element, isCompactView);
        CorePlugin.getDefault().getLibrariesService().addChangeLibrariesListener(this);
    }

    public MissingSettingsMultiThreadDynamicComposite(Composite parentComposite, int styles, EComponentCategory section,
            Element element, boolean isCompactView, Color backgroundColor) {
        super(parentComposite, styles, section, element, isCompactView, backgroundColor);
        CorePlugin.getDefault().getLibrariesService().addChangeLibrariesListener(this);
    }

    @Override
    protected MessagesComposite createMessagesComposite() {
        MessagesWithActionComposite messagesComposite = new MessagesWithActionComposite(getComposite(), SWT.NONE) {

            @Override
            protected void changeBackgroundColor(int status) {
                switch (status) {
                case IStatus.WARNING:
                    CoreUIPlugin.setCSSId(this, CSS_MESSAGES_WITH_ACTION_COMPOSITE_WARN);
                    break;
                case IStatus.ERROR:
                    CoreUIPlugin.setCSSId(this, CSS_MESSAGES_WITH_ACTION_COMPOSITE_ERROR);
                    break;
                default:
                }
            }
        };
        // CSS
        CoreUIPlugin.setCSSClass(messagesComposite, MessagesWithActionComposite.class.getSimpleName());
        return messagesComposite;

    }

    @Override
    protected void checkVisibleTopMessages() {
        missModulesNeeded.clear();
        final Element ele = this.getElement();
        if (ele instanceof Node) {
            // get not installed modules
            List<ModuleNeeded> updatedModules = LibrariesManagerUtils.getNotInstalledModules(((Node) ele));

            missModulesNeeded.addAll(updatedModules);
            ((Node) ele).checkAndRefreshNode();
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
                                + "...", null); //$NON-NLS-1$
                messWithActionComposite.addActionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (!missModulesNeeded.isEmpty()) {
                            ModulesInstallerUtil.forceInstallModules(getShell(), component, missModulesNeeded);
                            checkVisibleTopMessages();
                        }
                    }

                });
            }
        }

        super.afterCreateMessagesComposite(messComposite);
    }

    @Override
    public void afterChangingLibraries() {
        DisplayUtils.getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                final Element ele = getElement();
                if (ele instanceof Node) {
                    final Node node = (Node) ele;
                    // after install, need update the error marks for node
                    IProcess process = node.getProcess();
                    if (process instanceof IProcess2) {
                        // check whole process to make sure other related nodes can be updated also.
                        ((IProcess2) process).checkProcess();
                    }
                    checkVisibleTopMessages();
                }
            }
        });

    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        CorePlugin.getDefault().getLibrariesService().removeChangeLibrariesListener(this);

    }

    public ConnectionItem getConnectionItem() {
        return null;
    }

}
