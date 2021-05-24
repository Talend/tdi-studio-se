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
package org.talend.designer.rowgenerator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.components.IODataComponentContainer;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.rowgenerator.external.data.ExternalRowGenTable;
import org.talend.designer.rowgenerator.external.data.ExternalRowGeneratorData;
import org.talend.designer.rowgenerator.external.data.ExternalRowGeneratorUiProperties;
import org.talend.designer.rowgenerator.external.data.IOConnection;
import org.talend.designer.rowgenerator.i18n.Messages;
import org.talend.designer.rowgenerator.managers.RowGeneratorManager;
import org.talend.designer.rowgenerator.ui.RowGeneratorUI;
import org.talend.designer.rowgenerator.ui.dialogs.RowGeneratorDialog;
import org.talend.designer.rowgenerator.ui.editor.MetadataTableEditorViewExt;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: RowGenMain.java,v 1.4 2007/01/31 05:20:52 pub Exp $
 *
 */
public class RowGenMain {

    private RowGeneratorComponent connector;

    private RowGeneratorManager generatorManager;

    private RowGeneratorUI generatorUI;

    private List<ExternalRowGenTable> outputTables;

    /**
     * qzhang RowGeneratorMain constructor comment.
     */
    public RowGenMain(RowGeneratorComponent connector) {
        super();
        this.connector = connector;
        this.generatorManager = new RowGeneratorManager(connector);
    }

    /**
     * qzhang Comment method "loadInitialParamters".
     */
    public void loadInitialParamters() {

    }

    /**
     * qzhang Comment method "loadModelFromInternalData".
     */
    public void loadModelFromInternalData() {

    }

    /**
     * qzhang Comment method "createUI".
     *
     * @param parent
     * @return
     */
    public void createUI(Composite parent, boolean fromDialog) {
        generatorUI = new RowGeneratorUI(parent, generatorManager);
        generatorUI.init(fromDialog);
    }

    /**
     * qzhang Comment method "getMapperDialogResponse".
     *
     * @return
     */
    public int getMapperDialogResponse() {
        return generatorManager.getUiManager().getRowGenResponse();
    }

    /**
     * qzhang Comment method "getMetadataListOut".
     *
     * @return
     */
    public List<IMetadataTable> getMetadataListOut() {
        return null;
    }

    /**
     * yzhang Comment method "createRowGeneratorDialog".
     *
     * @param parentShell
     * @return
     */
    public Dialog createRowGeneratorDialog(Shell parentShell) {
        RowGeneratorDialog dialog = new RowGeneratorDialog(parentShell, this);
        IComponent component = connector.getComponent();

        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        String productName = brandingService.getFullProductName();

        Rectangle boundsRG = ExternalRowGeneratorUiProperties.getBoundsRowGen();
        if (ExternalRowGeneratorUiProperties.isShellMaximized()) {
            dialog.setMaximized(ExternalRowGeneratorUiProperties.isShellMaximized());
        } else {
            boundsRG = ExternalRowGeneratorUiProperties.getBoundsRowGen();
            if (boundsRG.x < 0) {
                boundsRG.x = 0;
            }
            if (boundsRG.y < 0) {
                boundsRG.y = 0;
            }
            dialog.setSize(boundsRG);
        }
        dialog.setIcon(CoreImageProvider.getComponentIcon(component, ICON_SIZE.ICON_32));
        dialog.setTitle(Messages.getString("RowGenMain.ShellTitle", productName, connector.getUniqueName())); //$NON-NLS-1$

        return dialog;
    }

    /**
     * qzhang Comment method "createUI".
     *
     * @param display
     * @return
     */
    public Shell createUI(Display display) {

        Shell shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.BORDER | SWT.RESIZE | SWT.CLOSE | SWT.MIN | SWT.MAX
                | SWT.TITLE);
        IComponent component = connector.getComponent();
        shell.setImage(CoreImageProvider.getComponentIcon(component, ICON_SIZE.ICON_32));

        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        String productName = brandingService.getFullProductName();
        shell.setText(Messages.getString("RowGenMain.ShellTitle", productName, connector.getUniqueName())); //$NON-NLS-1$

        Rectangle boundsRG = ExternalRowGeneratorUiProperties.getBoundsRowGen();
        if (ExternalRowGeneratorUiProperties.isShellMaximized()) {
            shell.setMaximized(ExternalRowGeneratorUiProperties.isShellMaximized());
        } else {
            boundsRG = ExternalRowGeneratorUiProperties.getBoundsRowGen();
            if (boundsRG.x < 0) {
                boundsRG.x = 0;
            }
            if (boundsRG.y < 0) {
                boundsRG.y = 0;
            }
            shell.setBounds(boundsRG);
        }
        createUI(shell, false);
        updateComponentsSize();
        updateTableTitleColumn();
        shell.open();
        shell.addControlListener(new ControlListener() {

            public void controlMoved(ControlEvent e) {
                generatorUI.getDataTableView().attachLabelPosition();
            }

            public void controlResized(ControlEvent e) {
                generatorUI.getDataTableView().attachLabelPosition();
                generatorUI.getDataTableView().fixedLabelSize();
            }

        });

        addAllControlForKeyListener(shell);
        // shell.moveAbove(null);
        generatorUI.getDataTableView().updateHeader(ExternalRowGeneratorUiProperties.getShowColumnsList());
        return shell;
    }

    /**
     * yzhang Comment method "updateComponentSize".
     */
    public void updateTableTitleColumn() {
        if (generatorUI != null) {
            generatorUI.getDataTableView().attachLabelPosition();
            generatorUI.getDataTableView().fixedLabelSize();
        }
    }

    /**
     * yzhang Comment method "updateComponentsSize".
     */
    public void updateComponentsSize() {
        if (generatorUI != null) {
            generatorUI.getDataTableView().getExtendedToolbar().updateComponentsSize();
        }
    }

    /**
     * qzhang Comment method "addAllControlForKeyListener".
     *
     * @param parent
     * @param keyAdapter
     * @return
     */
    private void addAllControlForKeyListener(Control parent) {
        KeyAdapter keyAdapter = new KeyAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
             */
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.F5) {
                    MetadataTableEditorViewExt viewExt = generatorUI.getDataTableView();
                    viewExt.refreshPreviewColumn();
                }
            }
        };
        parent.addKeyListener(keyAdapter);
        if (parent instanceof Composite) {
            Control[] children = ((Composite) parent).getChildren();
            for (Control control : children) {
                if (control != null && !control.isDisposed()) {
                    addAllControlForKeyListener(control);
                }
            }
        }

    }

    /**
     * qzhang Comment method "getRowGenManager".
     *
     * @return
     */
    public RowGeneratorManager getRowGenManager() {
        return generatorManager;
    }

    /**
     * qzhang Comment method "isStandAloneMode".
     *
     * @return
     */
    public static boolean isStandAloneMode() {
        return false;
    }

    /**
     * qzhang Comment method "buildExternalData".
     *
     * @return
     */
    public ExternalRowGeneratorData buildExternalData() {
        ExternalRowGeneratorData externalData = new ExternalRowGeneratorData();
        outputTables = new ArrayList<ExternalRowGenTable>();
        externalData.setOutputTables(outputTables);
        externalData.setUiProperties(generatorManager.getUiManager().getUiProperties());
        return externalData;
    }

    /**
     * amaumont Comment method "loadFromExternalData".
     *
     * @param incomingConnections
     * @param outgoingConnections
     * @param externalData
     * @param checkProblems
     * @param metadataList
     */
    public void createModelFromExternalData(List<? extends IConnection> incomingConnections,
            List<? extends IConnection> outgoingConnections, ExternalRowGeneratorData externalData,
            List<IMetadataTable> outputMetadataTables, boolean checkProblems) {
        ArrayList<IOConnection> inputs = createIOConnections(incomingConnections);
        ArrayList<IOConnection> outputs = createIOConnections(outgoingConnections);
        createModelFromExternalData(inputs, outputs, outputMetadataTables, externalData, checkProblems);
    }

    public ArrayList<IOConnection> createIOConnections(List<? extends IConnection> connections) {
        ArrayList<IOConnection> ioConnections = new ArrayList<IOConnection>(connections.size());
        for (IConnection connection : connections) {
            ioConnections.add(new IOConnection(connection));
        }
        return ioConnections;
    }

    /**
     * qzhang Comment method "createModelFromExternalData".
     *
     * @param dataComponents
     * @param metadataList
     * @param externalData
     * @param b
     */
    public void createModelFromExternalData(IODataComponentContainer dataComponents, List<IMetadataTable> metadataList,
            ExternalRowGeneratorData externalData, boolean b) {
        List<IODataComponent> inputsData = dataComponents.getInputs();
        List<IODataComponent> ouputsData = dataComponents.getOuputs();

        ArrayList<IOConnection> inputs = new ArrayList<IOConnection>(inputsData.size());
        for (IODataComponent iData : inputsData) {
            inputs.add(new IOConnection(iData));
        }
        ArrayList<IOConnection> outputs = new ArrayList<IOConnection>(ouputsData.size());
        for (IODataComponent oData : ouputsData) {
            outputs.add(new IOConnection(oData));
        }
        createModelFromExternalData(inputs, outputs, metadataList, externalData, false);
    }

    public void createModelFromExternalData(List<IOConnection> inputs, List<IOConnection> outputs,
            List<IMetadataTable> outputMetadataTables, ExternalRowGeneratorData externalData, boolean checkProblems) {
        if (externalData == null) {
            externalData = new ExternalRowGeneratorData();
        }
        generatorManager.getUiManager().setUiProperties(externalData.getUiProperties());
    }

    /**
     * Gets the instance of <code>RowGeneratorUI</code>
     *
     * @return
     */
    public RowGeneratorUI getGeneratorUI() {
        return generatorUI;
    }

}
