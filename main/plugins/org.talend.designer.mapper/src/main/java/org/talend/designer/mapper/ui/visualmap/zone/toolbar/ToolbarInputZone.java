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
package org.talend.designer.mapper.ui.visualmap.zone.toolbar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.TraceData;
import org.talend.core.model.runprocess.IDebugProcessService;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ExternalNodeChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.ui.visualmap.table.DataMapTableView;
import org.talend.designer.mapper.ui.visualmap.zone.Zone;
import org.talend.designer.runprocess.RunProcessContext;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.ui.ERunprocessImages;
import org.talend.designer.runprocess.ui.ProcessManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ToolbarInputZone extends ToolbarZone {

    public static final String MINIMIZE_TOOLTIP = Messages.getString("ToolbarInputZone.minimizeTooltip"); //$NON-NLS-1$

    public static final String RESTORE_TOOLTIP = Messages.getString("ToolbarInputZone.restoreTooltip"); //$NON-NLS-1$

    private static final String MOVE_UP_TOOLTIP = Messages.getString("ToolbarInputZone.moveupTooltip"); //$NON-NLS-1$

    private static final String MOVE_DOWN_TOOLTIP = Messages.getString("ToolbarInputZone.movedownTooltip"); //$NON-NLS-1$

    private ToolItem previousRow;

    private ToolItem nextRow;

    private ToolItem nextBreakpoint;

    private ToolItem currentRowLabel;

    private ToolItem killBtn;

    private PropertyChangeListener propertyListener;

    /**
     * DOC amaumont MatadataToolbarEditor constructor comment.
     *
     * @param parent
     * @param style
     * @param manager
     * @param metadatEditorView
     */
    public ToolbarInputZone(Composite parent, int style, MapperManager manager) {
        super(parent, style, manager);
        addCommonsComponents();
        addPreviewToolItems();
    }

    public String getMinimizeTooltipText() {
        return MINIMIZE_TOOLTIP;
    }

    public String getRestoreTooltipText() {
        return RESTORE_TOOLTIP;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.zone.ToolbarZone#getZone()
     */
    @Override
    public Zone getZone() {
        return Zone.INPUTS;
    }

    public String getMoveUpTooltipText() {
        return MOVE_UP_TOOLTIP;
    }

    public String getMoveDownTooltipText() {
        return MOVE_DOWN_TOOLTIP;
    }

    private void addPreviewToolItems() {
        if (PluginChecker.isTraceDebugPluginLoaded() && getMapperManager().isTracesActive()) {
            final RunProcessContext activeContext = RunProcessPlugin.getDefault().getRunProcessContextManager()
                    .getActiveContext();

            if (activeContext == null) {
                return;
            }

            new ToolItem(getToolBarActions(), SWT.SEPARATOR);

            previousRow = new ToolItem(getToolBarActions(), SWT.PUSH);
            previousRow.setEnabled(activeContext.isRunning());
            previousRow.setToolTipText("Previous Row");
            previousRow.setImage(ImageProvider.getImage(EImage.LEFT_ICON));

            currentRowLabel = new ToolItem(getToolBarActions(), SWT.PUSH | SWT.BORDER);
            currentRowLabel.setEnabled(false);
            currentRowLabel.setText(getCurrentRowString());
            currentRowLabel.setToolTipText("Current Row");
            currentRowLabel.setWidth(50);

            nextRow = new ToolItem(getToolBarActions(), SWT.PUSH);
            nextRow.setEnabled(!getMapperManager().componentIsReadOnly());
            nextRow.setToolTipText("Next Row");
            nextRow.setImage(ImageProvider.getImage(EImage.RIGHT_ICON));

            nextBreakpoint = new ToolItem(getToolBarActions(), SWT.PUSH);
            nextBreakpoint.setToolTipText("Next Breakpoint");
            nextBreakpoint.setImage(ImageProvider.getImage(EImage.RIGHTX_ICON));
            Boolean bc = activeContext.checkBreakpoint();
            if (!bc) {
                nextBreakpoint.setEnabled(bc);
            } else {
                nextBreakpoint.setEnabled(activeContext.isRunning());
            }

            killBtn = new ToolItem(getToolBarActions(), SWT.PUSH);
            killBtn.setToolTipText("Kill");
            killBtn.setImage(ImageProvider.getImage(ERunprocessImages.KILL_PROCESS_ACTION));
            killBtn.setEnabled(activeContext.isRunning());

            previousRow.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    activeContext.setPreviousRow(true);
                }

            });

            nextRow.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {

                    if (!activeContext.isRunning()) {
                        for (DataMapTableView dataMapTableView : getMapperManager().getUiManager().getOutputsTablesView()) {
                            dataMapTableView.notifyFocusLost();
                        }
                        if (getMapperManager().isDataChanged()) {
                            boolean closeWindow = MessageDialog.openConfirm(getComposite().getShell(),
                                    "tMap configuration modified", //$NON-NLS-1$
                                    "Do you want to apply the modification of the tMap now ?"); //$NON-NLS-1$
                            // save change and regenerate code
                            if (closeWindow) {
                                IExternalNode externalNode = getMapperManager().getAbstractMapComponent().getExternalNode();
                                IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                        .getActiveEditor();
                                if (externalNode != null && (part instanceof AbstractMultiPageTalendEditor)) {
                                    INode node = externalNode.getOriginalNode();
                                    if (node != null && node instanceof Node) {
                                        Command cmd = new ExternalNodeChangeCommand((Node) node, externalNode);
                                        CommandStack cmdStack = (CommandStack) part.getAdapter(CommandStack.class);
                                        cmdStack.execute(cmd);
                                    }
                                }

                            }
                        }

                        activeContext.setLastIsRow(true);
                        IDebugProcessService service = (IDebugProcessService) GlobalServiceRegister.getDefault().getService(
                                IDebugProcessService.class);
                        service.debugProcess();
                    } else {
                        activeContext.setNextRow(true);
                    }
                }

            });
            nextBreakpoint.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    activeContext.setNextBreakPoint(true);
                }

            });

            killBtn.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    IDebugProcessService service = (IDebugProcessService) GlobalServiceRegister.getDefault().getService(
                            IDebugProcessService.class);
                    service.debugKill();
                    killBtn.setEnabled(false);
                    previousRow.setEnabled(false);
                    nextBreakpoint.setEnabled(false);
                }

            });

            propertyListener = new PropertyChangeListener() {

                public void propertyChange(final PropertyChangeEvent evt) {
                    final String propName = evt.getPropertyName();

                    ProcessManager.getInstance().getProcessShell().getDisplay().syncExec(new Runnable() {

                        public void run() {
                            if (RunProcessContext.PREVIOUS_ROW.equals(propName)) {
                                boolean enabled = ((Boolean) evt.getNewValue()).booleanValue();
                                if (!previousRow.isDisposed() && enabled != previousRow.isEnabled()) {
                                    previousRow.setEnabled(enabled);
                                }
                            } else if (RunProcessContext.PROP_RUNNING.equals(propName)) {
                                boolean enabled = ((Boolean) evt.getNewValue()).booleanValue();
                                if (!previousRow.isDisposed() && enabled != previousRow.isEnabled()) {
                                    previousRow.setEnabled(enabled);
                                }
                                if (!nextBreakpoint.isDisposed() && enabled != nextBreakpoint.isEnabled()) {
                                    Boolean bc = activeContext.checkBreakpoint();
                                    if (!bc) {
                                        nextBreakpoint.setEnabled(bc);
                                    } else {
                                        nextBreakpoint.setEnabled(enabled);
                                    }

                                }
                                if (!killBtn.isDisposed() && enabled != killBtn.isEnabled()) {
                                    killBtn.setEnabled(enabled);
                                }
                                if (!nextRow.isDisposed()) {
                                    nextRow.setEnabled(true);
                                }
                            } else if (RunProcessContext.NEXTBREAKPOINT.equals(propName)) {
                                boolean running = ((Boolean) evt.getNewValue()).booleanValue();
                                if (!nextBreakpoint.isDisposed()) {
                                    nextBreakpoint.setEnabled(running);
                                }
                                if (!nextRow.isDisposed()) {
                                    nextRow.setEnabled(true);
                                }

                            } else if (RunProcessContext.BREAKPOINT_BAR.equals(propName)) {
                                boolean enable = ((Boolean) evt.getNewValue()).booleanValue();
                                if (!enable) {
                                    if (!previousRow.isDisposed()) {
                                        previousRow.setEnabled(false);
                                    }
                                    if (!nextRow.isDisposed()) {
                                        nextRow.setEnabled(false);
                                    }
                                    if (!nextBreakpoint.isDisposed()) {
                                        nextBreakpoint.setEnabled(false);
                                    }
                                }

                            }
                        }
                    });
                }

            };

            activeContext.addPropertyChangeListener(propertyListener);

        }
    }

    public void removePropertyChangeListener() {
        final RunProcessContext activeContext = RunProcessPlugin.getDefault().getRunProcessContextManager().getActiveContext();
        if (activeContext != null) {
            activeContext.removePropertyChangeListener(propertyListener);
        }
    }

    private String getCurrentRowString() {
        if (getMapperManager() != null) {
            List<? extends IConnection> incomingConnections = getMapperManager().getAbstractMapComponent()
                    .getIncomingConnections(EConnectionType.FLOW_MAIN);
            if (incomingConnections != null && incomingConnections.size() == 1) {
                IConnection connection = incomingConnections.get(0);
                if (connection != null) {
                    Map<String, TraceData> traceData = connection.getTraceData();
                    if (traceData != null) {
                        TraceData data = traceData.get(connection.getName());
                        if (data != null) {
                            return "row: " + data.getNbLine();
                        }
                    }
                }
            }

        }
        return "Current row";
    }

    public void refreshCurrentRow() {
        if (currentRowLabel != null && !currentRowLabel.isDisposed()) {
            currentRowLabel.setText(getCurrentRowString());
            getToolBarActions().layout();
        }
    }
}
