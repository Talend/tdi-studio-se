package org.talend.designer.core.ui.viewer.java;

import org.eclipse.jdt.internal.ui.text.java.ContentAssistProcessor;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ContentAssistant;

public class TalendJavaViewerConfiguration4Elt extends TalendJavaViewerConfiguration {

    public TalendJavaViewerConfiguration4Elt(IColorManager colorManager, IPreferenceStore preferenceStore,
            TalendJavaSourceViewer viewer) {
        super(colorManager, preferenceStore, viewer);
    }

    @Override
    protected ContentAssistProcessor createContentAssistProcessor(ContentAssistant assistant, String partition) {
        return new TalendJavaCompletionProcessor4Elt(assistant, partition);
    }

}
