// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.action;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.ISubjobContainer;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.MultiPageTalendEditor;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;

/**
 * Toggle whether using the hightlight of subjobs or not, this action is only related to MultiPageTalendEditor.
 */
public class ToggleSubjobsAction extends Action {

    public static final String ID = "org.talend.designer.core.ui.action.ToggleSubjobsAction"; //$NON-NLS-1$

    private static final String JOBLET_ID = "org.talend.designer.joblet.multieditor";//$NON-NLS-1$

    private static final String MAP_REDUCE_MULTIPAGE_EDITOR_ID = "org.talend.designer.mapreduce.editor";//$NON-NLS-1$

    private static final String TEST_CASE_DI_EDITOR = "org.talend.testcontainer.core.ui.editor.TestContainerMultiPageEditor";

    public static final String TEXT = Messages.getString("ToggleSubjobsAction.LABEL2"); //$NON-NLS-1$

    private static ToggleSubjobsAction instance = new ToggleSubjobsAction();

    public static ToggleSubjobsAction getDefault() {
        return instance;
    }

    /**
     * bqian ToggleSubjobsAction constructor comment.
     */
    private ToggleSubjobsAction() {
        super(TEXT, IAction.AS_CHECK_BOX);
        this.setId(ID);
        this.setDescription(TEXT);
        setChecked(getGlobalStore().getBoolean(TalendDesignerPrefConstants.DISPLAY_SUBJOBS));
    }

    @Override
    public void run() {
        boolean display = isChecked();
        getGlobalStore().setValue(TalendDesignerPrefConstants.DISPLAY_SUBJOBS, display);

        IEditorReference[] editorParts = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .getEditorReferences();

        for (IEditorReference reference : editorParts) {
            if (!(reference.getId().equals(MultiPageTalendEditor.ID) || reference.getId().equals(JOBLET_ID)
                    || MAP_REDUCE_MULTIPAGE_EDITOR_ID.equals(reference.getId()) || TEST_CASE_DI_EDITOR.equals(reference.getId()))) {
                continue;
            }

            IEditorPart editorPart = reference.getEditor(false);
            if (editorPart == null) {
                continue;
            }
            if (editorPart instanceof AbstractMultiPageTalendEditor) {
                AbstractMultiPageTalendEditor editor = (AbstractMultiPageTalendEditor) editorPart;
                IProcess2 process = editor.getTalendEditor().getProcess();

                if (process == null) {
                    continue;
                }
                List<? extends ISubjobContainer> subjobs = process.getSubjobContainers();

                for (ISubjobContainer subjobContainer : subjobs) {
                    subjobContainer.updateSubjobDisplay();
                }
            }

        }
    }

    private IPreferenceStore getGlobalStore() {
        return DesignerPlugin.getDefault().getPreferenceStore();
    }
}
