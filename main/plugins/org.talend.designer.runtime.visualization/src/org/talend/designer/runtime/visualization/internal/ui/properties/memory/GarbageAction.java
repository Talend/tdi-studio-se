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
package org.talend.designer.runtime.visualization.internal.ui.properties.memory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.talend.designer.runtime.visualization.Activator;
import org.talend.designer.runtime.visualization.IActiveJvm;
import org.talend.designer.runtime.visualization.ISharedImages;
import org.talend.designer.runtime.visualization.JvmCoreException;
import org.talend.designer.runtime.visualization.internal.ui.properties.AbstractJvmPropertySection;
import org.talend.designer.runtime.visualization.views.RuntimeGraphcsComposite;

/**
 * created by ldong on Apr 7, 2015 Detailled comment
 *
 */
public class GarbageAction extends Action {

    /** The property section. */
    AbstractJvmPropertySection section;

    private RuntimeGraphcsComposite runtimeComposite;

    /**
     * The constructor.
     *
     * @param section The property section
     */
    public GarbageAction(RuntimeGraphcsComposite composite) {
//        setImageDescriptor(Activator.getImageDescriptor(ISharedImages.TRASH_IMG_PATH));
//        setDisabledImageDescriptor(Activator.getImageDescriptor(ISharedImages.DISABLED_TRASH_IMG_PATH));
        setId(getClass().getName());
        setText(Messages.garbageCollectorLabel);
        this.runtimeComposite = composite;
    }

    /*
     * @see Action#run()
     */
    @Override
    public void run() {
        new Job(Messages.runGarbageCollectorJobLabel) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                IActiveJvm jvm = runtimeComposite.getJvm();
                if (jvm == null) {
                    return Status.CANCEL_STATUS;
                }

                try {
                    jvm.getMBeanServer().runGarbageCollector();
                } catch (JvmCoreException e) {
                    Activator.log(Messages.runGarbageCollectorFailedMsg, e);
                    return Status.CANCEL_STATUS;
                }

                return Status.OK_STATUS;
            }
        }.schedule();
    }
}
