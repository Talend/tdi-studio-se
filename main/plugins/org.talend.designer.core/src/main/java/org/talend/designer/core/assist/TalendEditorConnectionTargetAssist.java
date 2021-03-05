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
package org.talend.designer.core.assist;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.widgets.Display;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.model.process.ConnectionManager;

/**
 * DOC Talend class global comment. Detailled comment
 */
public class TalendEditorConnectionTargetAssist extends TalendEditorComponentCreationAssist {

    private String componentName;

    private EConnectionType connectionType;

    public TalendEditorConnectionTargetAssist(String categoryName, GraphicalViewer viewer, CommandStack commandStack,
            IProcess2 process) {
        super(categoryName, viewer, commandStack, process);
        connectionType = ConnectionManager.getNewConnectionType();
    }

    /**
     * open the creation assist according to the trigger character
     *
     * @param triggerChar
     */
    @Override
    public void showComponentCreationAssist(char triggerChar) {
        super.showComponentCreationAssist(triggerChar);
        if (assistText == null || assistText.isDisposed()) {
            return;
        }
        Display display = assistText.getDisplay();
        while (!assistText.isDisposed() && assistText.isVisible()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * create component at current position, according to select proposal label DOC talend2 Comment method
     * "createComponent".
     *
     * @param componentName
     * @param location
     */
    @Override
    protected void acceptProposal() {
        this.componentName = assistText.getText().trim();
        disposeAssistText();
    }

    public String getComponentName() {
        return this.componentName;
    }

    public void releaseText() {
        assistText = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.assist.TalendEditorComponentCreationAssist#disposeAssistText()
     */
    @Override
    protected void disposeAssistText() {
        if (assistText != null && !assistText.isDisposed()) {
            assistText.dispose();
        }
        // restore key event filter on Display
        if (bindingService != null) {
            bindingService.setKeyFilterEnabled(isKeyFilterEnabled);
        }
        if (overedConnection != null) {
            overedConnection.setLineWidth(1);
            overedConnection = null;
        }
    }

    @Override
    protected List<IComponent> filterComponents(List<IComponent> components) {
        components = super.filterComponents(components);
        if (components == null || components.isEmpty()) {
            return components;
        }

        Iterator<IComponent> iter = components.iterator();
        while (iter.hasNext()) {
            IComponent component = iter.next();
            if (component == null) {
                continue;
            }
            if (!TalendEditorComponentCreationUtil.isComponentAllowedWithConnectionType(component, connectionType)) {
                iter.remove();
            }
        }

        return components;
    }

}
