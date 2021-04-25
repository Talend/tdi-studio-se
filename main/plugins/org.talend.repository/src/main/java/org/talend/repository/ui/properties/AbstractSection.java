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
package org.talend.repository.ui.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.EmptyRepositoryObject;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public abstract class AbstractSection extends AbstractPropertySection {

    private static final List<AbstractSection> REGISTERED_SECTIONS = new ArrayList<AbstractSection>();

    private IRepositoryViewObject repositoryObject;

    private RepositoryNode repositoryNode;

    private FocusListener listener = new FocusListener() {

        public void focusLost(FocusEvent e) {
            performSave();
        }

        public void focusGained(FocusEvent e) {
            manageLock();
        }
    };

    /**
     * DOC tguiu AbstractSection constructor comment.
     */
    public AbstractSection() {
        super();
        REGISTERED_SECTIONS.add(this);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#createControls(org.eclipse.swt.widgets.Composite,
     * org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
     */
    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);
    }

    protected IRepositoryViewObject getObject() {
        return repositoryObject;
    }

    protected RepositoryNode getNode() {
        return repositoryNode;
    }

    protected ERepositoryObjectType getType() {
        return repositoryObject.getRepositoryObjectType();
    }

    /**
     * DOC tguiu Comment method "addFocusListener".
     *
     * @param nameText2
     */
    protected void addFocusListener(Control control) {
        control.addFocusListener(listener);
    }

    protected void addFocusListenerToChildren(Control control) {
        addFocusListener(control);
        if (control instanceof Composite) {
            for (Control child : ((Composite) control).getChildren()) {
                addFocusListenerToChildren(child);
            }
        }
    }

    protected void performSave() {
        // Because props are now read-only:
        // for (AbstractSection section : REGISTERED_SECTIONS) {
        // section.beforeSave();
        // }
        // save();
        // performRefresh();
        // refreshRepositoryView();
    }

    protected static void performRefresh() {
        for (AbstractSection section : REGISTERED_SECTIONS) {
            if (section.getPart() != null) {
                section.refresh();
            }
        }
    }

    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);

        Assert.isTrue(selection instanceof IStructuredSelection);
        Object input = ((IStructuredSelection) selection).getFirstElement();

        if (!(input instanceof RepositoryNode)) {
            if (input instanceof IAdaptable) {
                // see ProcessPart.getAdapter()
                IAdaptable adaptable = (IAdaptable) input;
                input = adaptable.getAdapter(RepositoryNode.class);
            }
        }

        Assert.isTrue(input instanceof RepositoryNode);
        repositoryNode = (RepositoryNode) input;
        repositoryObject = repositoryNode.getObject();
        if (repositoryObject == null) {
            repositoryObject = new EmptyRepositoryObject();
            enableControls(false);
            showControls(false);
            return;
        }
        manageLock();
        ERepositoryObjectType type = repositoryObject.getRepositoryObjectType();
        showControls(type != ERepositoryObjectType.METADATA_CON_TABLE);
    }

    /**
     * DOC tguiu Comment method "manageLock".
     */
    protected void manageLock() {
        // Because props are now read-only:
        // boolean enableControl = ProxyRepositoryFactory.getInstance().getStatus(repositoryObject).isEditable();
        // enableControls(enableControl);

        // Because props are now read-only:
        enableControls(false);
    }

    protected final IWorkbenchPage getActivePage() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    }

    /**
     * DOC tguiu Comment method "enableControls".
     *
     * @param locked
     */
    private static void showControls(boolean visible) {
        for (AbstractSection section : REGISTERED_SECTIONS) {
            if (section.getPart() != null) {
                section.showControl(visible);
            }
        }
    }

    private static void enableControls(boolean enable) {
        for (AbstractSection section : REGISTERED_SECTIONS) {
            if (section.getPart() != null) {
                section.enableControl(enable);
            }
        }
    }

    /**
     * DOC tguiu Comment method "enableControl".
     *
     * @param b
     */
    protected abstract void showControl(boolean visible);

    protected abstract void enableControl(boolean enable);

    protected abstract void beforeSave();

    @Override
    public void dispose() {
        super.dispose();
        REGISTERED_SECTIONS.remove(this);
    }

    /**
     * DOC tguiu Comment method "getRepositoryFactory".
     *
     * @return
     */
    protected IProxyRepositoryFactory getRepositoryFactory() {
        return ProxyRepositoryFactory.getInstance();
    }
}
