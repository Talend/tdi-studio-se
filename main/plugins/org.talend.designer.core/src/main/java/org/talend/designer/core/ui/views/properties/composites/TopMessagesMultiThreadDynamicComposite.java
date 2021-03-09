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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.swt.composites.MessagesComposite;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.designer.core.ui.views.properties.MultipleThreadDynamicComposite;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class TopMessagesMultiThreadDynamicComposite extends MultipleThreadDynamicComposite {

    private boolean isVisibleTopMessages;

    private MessagesComposite messagesComp;

    public TopMessagesMultiThreadDynamicComposite(Composite parentComposite, int styles, EComponentCategory section,
            Element element, boolean isCompactView) {
        super(parentComposite, styles, section, element, isCompactView);
    }

    public TopMessagesMultiThreadDynamicComposite(Composite parentComposite, int styles, final EComponentCategory section,
            Element element, boolean isCompactView, Color backgroundColor) {
        super(parentComposite, styles, section, element, isCompactView, backgroundColor);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.views.properties.MultipleThreadDynamicComposite#addComponents(boolean, boolean,
     * int)
     */
    @Override
    public synchronized void addComponents(boolean forceRedraw, boolean reInitialize, int height) {
        checkVisibleTopMessages();
        if (isVisibleTopMessages() && (forceRedraw || isNeedRedraw())) {
            disposeChildren();

            messagesComp = createMessagesComposite();

            afterCreateMessagesComposite(messagesComp);

            FormData data = new FormData();
            data.top = new FormAttachment(0, height);
            data.left = new FormAttachment(0, 0);
            data.right = new FormAttachment(100, 0);
            messagesComp.setLayoutData(data);
            Point initialSize = messagesComp.computeSize(SWT.DEFAULT, SWT.DEFAULT);

            super.addComponents(forceRedraw, false, initialSize.y + ITabbedPropertyConstants.VSPACE);
        } else {
            super.addComponents(forceRedraw, reInitialize, height);
        }

    }

    protected MessagesComposite createMessagesComposite() {
        return new MessagesComposite(getComposite(), SWT.NONE);

    }

    public MessagesComposite getMessagesComp() {
        return this.messagesComp;
    }

    protected void checkVisibleTopMessages() {
        // need do some checks here
    }

    public boolean isVisibleTopMessages() {
        return this.isVisibleTopMessages;
    }

    public void setVisibleTopMessage(boolean visible) {
        this.isVisibleTopMessages = visible;
        if (messagesComp != null && !messagesComp.isDisposed() && !isVisibleTopMessages) {
            messagesComp.dispose();
        }
    }

    public void afterCreateMessagesComposite(MessagesComposite messComposite) {
        // nothing to do by default
    }
}
