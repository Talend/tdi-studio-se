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
package org.talend.designer.core.ui.editor.properties;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.time.TimeMeasure;
import org.talend.core.model.process.Element;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;

/**
 * Used to avoid to have a undo for each character typed and to check too much the process. This class avoids to send
 * too much commands when the text is typed. The commands are sent only when the time is up.
 *
 * $Id$
 *
 */
public class TextPropertyChangeUtil {

    protected static final int TIME_OUT = 500;

    private static Map<TextElement, TextPropertyChangeThread> currentThreads = new HashMap<TextElement, TextPropertyChangeThread>();

    private static TextElement lastElement;

    private static TextPropertyChangeUtil propertyChangeUtil = new TextPropertyChangeUtil();

    public static void changeText(final Display display, final Element elem, final String propertyName, final String text,
            final CommandStack cmdStack) {
        boolean found = false;
        TextPropertyChangeThread textThread;
        if (lastElement != null) {
            textThread = currentThreads.get(lastElement);
            if (textThread.isAlive()) {
                if (lastElement.getElement().equals(elem) && lastElement.getPropertyName().equals(propertyName)) {
                    textThread.setCurrentText(text);
                    found = true;
                }
            }
        }

        if (!found) {
            // check if the Text Element is in the current list
            for (TextElement textElem : currentThreads.keySet()) {
                textThread = currentThreads.get(textElem);
                if (textThread.isAlive()) {
                    if (textElem.getElement().equals(elem) && textElem.getPropertyName().equals(propertyName)) {
                        textThread.setCurrentText(text);
                        found = true;
                    }
                }
            }

            // if not found in the map, then must create it
            if (!found) {
                TextElement tElem = propertyChangeUtil.new TextElement();
                tElem.setElement(elem);
                tElem.setPropertyName(propertyName);

                textThread = propertyChangeUtil.new TextPropertyChangeThread(display, tElem, cmdStack);
                textThread.setCurrentText(text);
                currentThreads.put(tElem, textThread);
                textThread.start();
                // System.out.println("adds thread to list for the property:" + tElem.getPropertyName());
            }

        }
    }

    /**
     * This thread will store each new value of the current text and apply the properties when the timer is up.
     *
     * $Id$
     *
     */
    class TextPropertyChangeThread extends Thread {

        private TextElement textElement;

        private CommandStack cmdStack;

        private String currentText;

        private Display currentDisplay;

        private long lastTime;

        final Runnable applyProperty = new Runnable() {

            public void run() {
                TimeMeasure.begin("PropertyChangeCommand()"); //$NON-NLS-1$

                Command cmd = new PropertyChangeCommand(textElement.getElement(), textElement.getPropertyName(), currentText);
                cmdStack.execute(cmd);
                TimeMeasure.end("PropertyChangeCommand()"); //$NON-NLS-1$
            }
        };

        TextPropertyChangeThread(final Display display, final TextElement textElement, final CommandStack cmdStack) {
            this.textElement = textElement;
            this.cmdStack = cmdStack;
            currentDisplay = display;
        }

        @Override
        public void run() {
            // TimeMeasure.begin("run()");
            try {
                long currentTime;
                do {
                    currentTime = System.currentTimeMillis();
                    Thread.sleep(50);
                } while (currentTime < (lastTime + TextPropertyChangeUtil.TIME_OUT));
                currentThreads.remove(textElement);
                while (!currentThreads.isEmpty()) {
                    Thread.sleep(50);
                }
                currentDisplay.asyncExec(applyProperty);
            } catch (Throwable t) {
                // t.printStackTrace();
                ExceptionHandler.process(t);
            }
            // TimeMeasure.end("run()");
        }

        public void setCurrentText(String currentText) {
            this.currentText = currentText;
            lastTime = System.currentTimeMillis();
        }
    }

    /**
     * This class stores the element and the property used in a thread.
     *
     * $Id$
     *
     */
    class TextElement {

        private Element element;

        private String propertyName;

        public Element getElement() {
            return this.element;
        }

        public void setElement(Element element) {
            this.element = element;
        }

        public String getPropertyName() {
            return this.propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }
    }
}
