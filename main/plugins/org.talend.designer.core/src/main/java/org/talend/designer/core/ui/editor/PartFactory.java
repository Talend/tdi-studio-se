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
package org.talend.designer.core.ui.editor;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ui.ISparkJobletProviderService;
import org.talend.core.ui.ISparkStreamingJobletProviderService;
import org.talend.designer.core.ITestContainerGEFService;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionLabel;
import org.talend.designer.core.ui.editor.connections.ConnectionPart;
import org.talend.designer.core.ui.editor.connections.ConnectionPerformance;
import org.talend.designer.core.ui.editor.connections.ConnectionPerformanceEditPart;
import org.talend.designer.core.ui.editor.connections.ConnectionResuming;
import org.talend.designer.core.ui.editor.connections.ConnectionResumingEditPart;
import org.talend.designer.core.ui.editor.connections.ConnectionTrace;
import org.talend.designer.core.ui.editor.connections.ConnectionTraceEditPart;
import org.talend.designer.core.ui.editor.connections.MonitorConnectionLabel;
import org.talend.designer.core.ui.editor.connections.MonitorConnectionLabelPart;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainerPart;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeError;
import org.talend.designer.core.ui.editor.nodes.NodeErrorEditPart;
import org.talend.designer.core.ui.editor.nodes.NodeLabel;
import org.talend.designer.core.ui.editor.nodes.NodeLabelEditPart;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.nodes.NodeProgressBar;
import org.talend.designer.core.ui.editor.nodes.NodeProgressBarPart;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

/**
 * The PartFactory will create an EditPart factory for each model object that is created in the diagram. <br/>
 *
 * $Id$
 *
 */
public class PartFactory implements EditPartFactory {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
     */
    @Override
    public EditPart createEditPart(EditPart context, Object model) {
        EditPart part = null;

        if (model instanceof SubjobContainer) {
            part = new SubjobContainerPart();
        } else if (model instanceof Process) {
            part = new ProcessPart();
        } else if (model instanceof Node) {
            part = new NodePart();
        } else if (model instanceof Connection) {
            part = new ConnectionPart();
        } else if (model instanceof ConnectionLabel) {
            part = new ConnLabelEditPart();
        } else if (model instanceof MonitorConnectionLabel) {
            part = new MonitorConnectionLabelPart();
        } else if (model instanceof ConnectionPerformance) {
            part = new ConnectionPerformanceEditPart();
        } else if (model instanceof ConnectionTrace) {
            part = new ConnectionTraceEditPart();
        } else if (model instanceof ConnectionResuming) {
            part = new ConnectionResumingEditPart();
        } else if (model instanceof NodeLabel) {
            part = new NodeLabelEditPart();
        } else if (model instanceof NodeContainer) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerGEFService.class)) {
                ITestContainerGEFService testContainerService = (ITestContainerGEFService) GlobalServiceRegister.getDefault()
                        .getService(ITestContainerGEFService.class);
                if (testContainerService != null) {
                    part = testContainerService.createEditorPart(model);
                    if (part != null) {
                        part.setModel(model);
                        return part;
                    }
                }
            }
            if(((NodeContainer) model).getNode().isSparkJoblet()){
                if (GlobalServiceRegister.getDefault().isServiceRegistered(ISparkJobletProviderService.class)) {
                    ISparkJobletProviderService sparkService = (ISparkJobletProviderService) GlobalServiceRegister.getDefault()
                            .getService(ISparkJobletProviderService.class);
                    if (sparkService != null) {
                        part = (EditPart)sparkService.createEditorPart(model);
                        if (part != null) {
                            part.setModel(model);
                            return part;
                        }
                    }
                }
            }else if(((NodeContainer) model).getNode().isSparkStreamingJoblet()){
                if (GlobalServiceRegister.getDefault().isServiceRegistered(ISparkStreamingJobletProviderService.class)) {
                    ISparkStreamingJobletProviderService sparkService = (ISparkStreamingJobletProviderService) GlobalServiceRegister.getDefault()
                            .getService(ISparkStreamingJobletProviderService.class);
                    if (sparkService != null) {
                        part = (EditPart)sparkService.createEditorPart(model);
                        if (part != null) {
                            part.setModel(model);
                            return part;
                        }
                    }
                }
            }else if (((NodeContainer) model).getNode().isStandardJoblet()) {
                part = new JobletContainerPart();
            } else if (((NodeContainer) model).getNode().isMapReduce()) {
                part = new JobletContainerPart();
            } else {
                part = new NodeContainerPart();
            }
        } else if (model instanceof Note) {
            part = new NoteEditPart();
        } else if (model instanceof NodeError) {
            part = new NodeErrorEditPart();
        } else if (model instanceof NodeProgressBar) {
            part = new NodeProgressBarPart();
        } else {
            return null;
        }
        // tell the newly created part about the model object
        part.setModel(model);

        return part;
    }
}
