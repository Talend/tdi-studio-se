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
package org.talend.sqlbuilder.editors;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class SQLBuilderEditorSite implements IEditorSite {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IEditorSite#getActionBarContributor()
     */
    public IEditorActionBarContributor getActionBarContributor() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IEditorSite#getActionBars()
     */
    public IActionBars getActionBars() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IEditorSite#registerContextMenu(org.eclipse.jface.action.MenuManager,
     * org.eclipse.jface.viewers.ISelectionProvider, boolean)
     */
    public void registerContextMenu(MenuManager menuManager, ISelectionProvider selectionProvider, boolean includeEditorInput) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IEditorSite#registerContextMenu(java.lang.String, org.eclipse.jface.action.MenuManager,
     * org.eclipse.jface.viewers.ISelectionProvider, boolean)
     */
    public void registerContextMenu(String menuId, MenuManager menuManager, ISelectionProvider selectionProvider,
            boolean includeEditorInput) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPartSite#getId()
     */
    public String getId() {
        // TODO Auto-generated method stub
        return "sqlBuilder";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPartSite#getKeyBindingService()
     */
    public IKeyBindingService getKeyBindingService() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPartSite#getPart()
     */
    public IWorkbenchPart getPart() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPartSite#getPluginId()
     */
    public String getPluginId() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPartSite#getRegisteredName()
     */
    public String getRegisteredName() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(org.eclipse.jface.action.MenuManager,
     * org.eclipse.jface.viewers.ISelectionProvider)
     */
    public void registerContextMenu(MenuManager menuManager, ISelectionProvider selectionProvider) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(java.lang.String,
     * org.eclipse.jface.action.MenuManager, org.eclipse.jface.viewers.ISelectionProvider)
     */
    public void registerContextMenu(String menuId, MenuManager menuManager, ISelectionProvider selectionProvider) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchSite#getPage()
     */
    public IWorkbenchPage getPage() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchSite#getSelectionProvider()
     */
    public ISelectionProvider getSelectionProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchSite#getShell()
     */
    public Shell getShell() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchSite#getWorkbenchWindow()
     */
    public IWorkbenchWindow getWorkbenchWindow() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchSite#setSelectionProvider(org.eclipse.jface.viewers.ISelectionProvider)
     */
    public void setSelectionProvider(ISelectionProvider provider) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.services.IServiceLocator#getService(java.lang.Class)
     */
    public Object getService(Class api) {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.services.IServiceLocator#hasService(java.lang.Class)
     */
    public boolean hasService(Class api) {
        // TODO Auto-generated method stub
        return false;
    }

}
