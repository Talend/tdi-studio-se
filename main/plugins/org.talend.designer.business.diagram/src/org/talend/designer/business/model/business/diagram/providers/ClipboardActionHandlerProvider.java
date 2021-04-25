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
package org.talend.designer.business.model.business.diagram.providers;

import java.util.Hashtable;

import org.eclipse.gmf.runtime.common.ui.services.action.global.AbstractGlobalActionHandlerProvider;
import org.eclipse.gmf.runtime.common.ui.services.action.global.IGlobalActionHandler;
import org.eclipse.gmf.runtime.common.ui.services.action.global.IGlobalActionHandlerContext;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.designer.business.model.business.diagram.handler.ClipboardActionHandler;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ClipboardActionHandlerProvider extends AbstractGlobalActionHandlerProvider {

    /**
     * wchen ClipboardActionHanderProvider constructor comment.
     */
    public ClipboardActionHandlerProvider() {
        // TODO Auto-generated constructor stub
    }

    private Hashtable handlerList = new Hashtable();

    @Override
    public IGlobalActionHandler getGlobalActionHandler(final IGlobalActionHandlerContext context) {

        if (!getHandlerList().containsKey(context.getActivePart())) {
            getHandlerList().put(context.getActivePart(), new ClipboardActionHandler());

            context.getActivePart().getSite().getPage().addPartListener(new IPartListener() {

                private IWorkbenchPart localPart = context.getActivePart();

                /**
                 * @see org.eclipse.ui.IPartListener#partActivated(IWorkbenchPart)
                 */
                public void partActivated(IWorkbenchPart part) {
                    // NULL implementation
                }

                public void partBroughtToTop(IWorkbenchPart part) {
                    // NULL implementation
                }

                public void partClosed(IWorkbenchPart part) {
                    if (part != null && part == localPart && getHandlerList().containsKey(part)) {
                        getHandlerList().remove(part);
                        localPart.getSite().getPage().removePartListener(this);
                        localPart = null;
                    }
                }

                /**
                 * @see org.eclipse.ui.IPartListener#partDeactivated(IWorkbenchPart)
                 */
                public void partDeactivated(IWorkbenchPart part) {
                    // NULL implementation
                }

                /**
                 * @see org.eclipse.ui.IPartListener#partOpened(IWorkbenchPart)
                 */
                public void partOpened(IWorkbenchPart part) {
                    // NULL implementation
                }
            });
        }
        return (ClipboardActionHandler) getHandlerList().get(context.getActivePart());
    }

    private Hashtable getHandlerList() {
        return handlerList;
    }

}
