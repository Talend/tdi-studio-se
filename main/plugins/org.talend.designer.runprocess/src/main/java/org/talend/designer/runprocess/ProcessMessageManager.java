// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;

import org.apache.commons.collections.buffer.BoundedFifoBuffer;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * 
 */
public class ProcessMessageManager implements IProcessMessageManager {

    /** Change property listeners. */
    private transient PropertyChangeSupport pcsDelegate;

    /** Messages associated to the process. */
    private BoundedFifoBuffer messages;

    public static final String PROP_MESSAGE_ADD = "RunProcessContext.Message.Added"; //$NON-NLS-1$

    public static final String PROP_DEBUG_MESSAGE_ADD = "RunProcessContext.DebugMessage.Added"; //$NON-NLS-1$

    public static final String PROP_MESSAGE_CLEAR = "RunProcessContext.Message.Cleared"; //$NON-NLS-1$

    public static final String UPDATE_CONSOLE = "UPDATE_CONSOLE"; //$NON-NLS-1$

    public static final int LIMIT_MESSAGES = 500;

    public static int lineLimit;

    /**
     * DOC amaumont ProcessMessageManager constructor comment.
     */
    public ProcessMessageManager() {
        super();
        String line = RunProcessPlugin.getDefault().getPluginPreferences()
                .getString(RunprocessConstants.CONSOLE_LINE_LIMIT_COUNT);
        if (!"".equals(line) && line != null) {
            lineLimit = (Integer.parseInt(line));
            if (lineLimit <= 0) {
                lineLimit = 1; // can't have 0 size buffer
            }
            messages = new BoundedFifoBuffer(lineLimit);
        } else {
            messages = new BoundedFifoBuffer(LIMIT_MESSAGES);
        }
        pcsDelegate = new PropertyChangeSupport(this);
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        if (l == null) {
            throw new IllegalArgumentException();
        }

        pcsDelegate.addPropertyChangeListener(l);
    }

    @Override
    public void firePropertyChange(String property, Object oldValue, Object newValue) {
        if (pcsDelegate.hasListeners(property)) {
            pcsDelegate.firePropertyChange(property, oldValue, newValue);
        }
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if (l != null) {
            pcsDelegate.removePropertyChangeListener(l);
        }
    }

    public void updateConsole() {
        firePropertyChange(UPDATE_CONSOLE, null, null);
    }

    @Override
    public void addMessage(IProcessMessage message) {
        synchronized (messages) {
            if (messages.isFull()) {
                messages.remove();
            }
            messages.add(message);
            firePropertyChange(PROP_MESSAGE_ADD, null, message);
        }

    }

    @Override
    public void clearMessages() {
        synchronized (messages) {
            messages.clear();
            firePropertyChange(PROP_MESSAGE_CLEAR, messages, null);
        }

    }

    @Override
    public Collection<IProcessMessage> getMessages() {
        return messages;
    }

    @Override
    public boolean isLastMessageEndWithCR() {
        int i = messages.size() - 1;
        if (i >= 0) {
            IProcessMessage processMessage = (IProcessMessage) messages.toArray()[i];
            return processMessage.getContent().endsWith("\n"); //$NON-NLS-1$
        } else {
            return false;
        }
    }

    @Override
    public void addDebugResultToConsole(IProcessMessage debugResultMessage) {
        synchronized (messages) {
            if (messages.isFull()) {
                messages.remove();
            }
            messages.add(debugResultMessage);
            firePropertyChange(PROP_DEBUG_MESSAGE_ADD, null, debugResultMessage);
        }

    }

}
