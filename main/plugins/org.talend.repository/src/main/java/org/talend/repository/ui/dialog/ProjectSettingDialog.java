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
package org.talend.repository.ui.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.runtime.preference.IProjectSettingPageTester;
import org.talend.core.runtime.services.IMavenUIService;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ProjectSettingNode;

/**
 * DOC aimingchen class global comment. Detailled comment
 */
public class ProjectSettingDialog {

    private static final Point DEFAULT_SIZE = new Point(1000, 600);

    private static final String TITLE = Messages.getString("ProjectSettingDialog.Title"); //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ProjectSettingDialog.class);

    private static final Comparator<IPreferenceNode> COMPARATOR = new Comparator<IPreferenceNode>() {

        @Override
        public int compare(IPreferenceNode o1, IPreferenceNode o2) {
            int compare = 0;
            if (o1 instanceof ProjectSettingNode && o2 instanceof ProjectSettingNode) {
                ProjectSettingNode node1 = (ProjectSettingNode) o1;
                ProjectSettingNode node2 = (ProjectSettingNode) o2;
                if (node1.getOrder() != null && node2.getOrder() != null) {
                    compare = node1.getOrder().compareTo(node2.getOrder());
                }
                if (compare == 0) { // same order. compare the label
                    String labelText1 = node1.getLabelText();
                    String labelText2 = node2.getLabelText();
                    if (labelText1 != null && labelText2 != null) {
                        compare = labelText1.compareTo(labelText2);
                    }
                }
            }
            return compare;
        }
    };

    public ProjectSettingDialog() {

    }

    /**
     * get all projectsettingPage node dynamic. need get the different result each time. because the tester will calc
     * dymamic.
     *
     * @return PreferenceManager
     */
    private PreferenceManager getNodeManager() {
        // PreferenceManager manager = new PreferenceManager(WorkbenchPlugin.PREFERENCE_PAGE_CATEGORY_SEPARATOR);
        PreferenceManager manager = new PreferenceManager('/');
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] configurationElements = registry
                .getConfigurationElementsFor("org.talend.repository.projectsetting_page"); //$NON-NLS-1$

        Map<String, List<IPreferenceNode>> hasCategoriesNodes = new HashMap<String, List<IPreferenceNode>>();

        for (IConfigurationElement element : configurationElements) {
            ProjectSettingNode node = new ProjectSettingNode(element);
            try {
                IPreferencePage page = (IPreferencePage) element.createExecutableExtension("class"); //$NON-NLS-1$
                node.setPage(page);
                String id = element.getAttribute("id");//$NON-NLS-1$
                IConfigurationElement[] testers = element.getChildren("tester");
                if (testers != null && testers.length == 1) { // currently, only one tester is supported.
                    try {
                        IProjectSettingPageTester pageTester = (IProjectSettingPageTester) testers[0]
                                .createExecutableExtension("class");
                        if (pageTester != null) {
                            if (!pageTester.valid(element, node)) {
                                continue; // don't add this page node.
                            }
                        }
                    } catch (CoreException ex) {
                        // can't create the tester
                        log.log(Level.WARN, "can't create the project setting tester for " + id, ex);
                    }
                }

                page.setDescription(element.getAttribute("description")); //$NON-NLS-1$
                page.setTitle(element.getAttribute("title")); //$NON-NLS-1$
            } catch (CoreException e) {
                ExceptionHandler.process(e);
            }
            // add all into root.
            manager.addToRoot(node);

            // has category
            String category = node.getCategory();
            if (category != null && category.length() > 0) {
                List<IPreferenceNode> list = hasCategoriesNodes.get(category);
                if (list == null) {
                    list = new ArrayList<IPreferenceNode>();
                    hasCategoriesNodes.put(category, list);
                }
                list.add(node);
            }
        }

        // add the speciall node for maven custom
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IMavenUIService.class)) {
            IMavenUIService mavenUIService = (IMavenUIService) GlobalServiceRegister.getDefault().getService(
                    IMavenUIService.class);
            IPreferenceNode mavenCostomSetup = manager.find("projectsetting.MavenCustomSetup");
            mavenUIService.addCustomMavenSettingChildren(mavenCostomSetup);
        }

        // find parent nodes for category
        Map<String, IPreferenceNode> parentNodesMap = new HashMap<String, IPreferenceNode>();
        for (String category : hasCategoriesNodes.keySet()) {
            IPreferenceNode parent = manager.find(category);
            if (parent != null) {
                parentNodesMap.put(category, parent);
            }
        }
        // process children nodes
        for (String category : hasCategoriesNodes.keySet()) {
            List<IPreferenceNode> list = hasCategoriesNodes.get(category);
            if (list != null) {

                IPreferenceNode parent = parentNodesMap.get(category);
                Collections.sort(list, COMPARATOR);
                for (IPreferenceNode node : list) {
                    // if the parent is not valid or not existed. the node won't show also.
                    manager.remove(node); // remove from root node.
                    if (parent != null) { // the parent existed.
                        parent.add(node);
                    }
                }
            }
        }

        // sort the root nodes
        List<IPreferenceNode> rootSubNodesList = new ArrayList<IPreferenceNode>(Arrays.asList(manager.getRootSubNodes()));

        Collections.sort(rootSubNodesList, COMPARATOR);
        manager.removeAll(); // clean all to re-add for order

        // add the sorted list to manager
        for (IPreferenceNode rootSubNode : rootSubNodesList) {
            manager.addToRoot(rootSubNode);
        }
        return manager;
    }

    public void open() {
        open(null);
    }

    public void open(final String pageId) {
        PreferenceManager manager = getNodeManager();
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        final PreferenceDialog dialog = new ProjectSettingsPreferenceDialog(shell, manager);
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

            @Override
            public void run() {
                if (StringUtils.isNotEmpty(pageId)) {
                    dialog.setSelectedNode(pageId);
                }
                dialog.create();
                dialog.getShell().setText(TITLE);
                dialog.getShell().setSize(DEFAULT_SIZE);
                dialog.open();
            }
        });
    }
}
