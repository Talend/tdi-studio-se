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
package org.talend.repository.ui.login.connections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.IColumnImageProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.ILineSelectionListener;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.LineSelectionEvent;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.login.LoginHelper;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class ConnectionsListComposite extends Composite {

    private ConnectionsDialog dialog;

    private FormToolkit toolkit;

    private AbstractDataTableEditorView<ConnectionBean> table;

    private ConnectionFormComposite connectionsFormComposite;

    private List<ConnectionBean> list;

    private ExtendedTableModel<ConnectionBean> model;

    private IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
            IBrandingService.class);

    /**
     * DOC smallet ConnectionsComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public ConnectionsListComposite(Composite parent, int style, ConnectionsDialog dialog) {
        super(parent, style);

        this.dialog = dialog;
        // PreferenceManipulator prefManipulator = new
        // PreferenceManipulator(CorePlugin.getDefault().getPreferenceStore());
        // list = prefManipulator.readConnections();
        list = getClonedList(LoginHelper.getInstance().getStoredConnections());

        if (list.isEmpty()) {
            boolean isOnlyRemoteConnection = brandingService.getBrandingConfiguration().isOnlyRemoteConnection();
            if (!isOnlyRemoteConnection) {
                list.add(ConnectionBean.getDefaultConnectionBean());
            } else {
                list.add(ConnectionBean.getDefaultRemoteConnectionBean());
            }
        }

        model = new ExtendedTableModel<ConnectionBean>(null, list);

        toolkit = new FormToolkit(getDisplay());
        Form form = toolkit.createForm(this);
        Composite formBody = form.getBody();
        formBody.setBackground(ColorConstants.white);

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        form.setLayoutData(new GridData(GridData.FILL_BOTH));

        FormLayout formLayout = new FormLayout();
        formBody.setLayout(formLayout);

        Group group = new Group(formBody, SWT.NONE);
        group.setText(Messages.getString("ConnectionsListComposite.groupText.connection")); //$NON-NLS-1$
        group.setBackground(formBody.getBackground());
        FormData data = new FormData();
        data.left = new FormAttachment(0, ConnectionsDialog.HSPACE);
        data.right = new FormAttachment(100, -ConnectionsDialog.HSPACE);
        data.top = new FormAttachment(0, ConnectionsDialog.VSPACE);
        data.bottom = new FormAttachment(100, -ConnectionsDialog.VSPACE);
        group.setLayoutData(data);

        group.setLayout(new FillLayout());

        table = new AbstractDataTableEditorView<ConnectionBean>(group, SWT.NONE, model, false, true, false) {

            @Override
            protected void setTableViewerCreatorOptions(TableViewerCreator<ConnectionBean> newTableViewerCreator) {
                super.setTableViewerCreatorOptions(newTableViewerCreator);
                newTableViewerCreator.setHeaderVisible(false);
                newTableViewerCreator.setVerticalScroll(true);
                newTableViewerCreator.setReadOnly(true);
            }

            @Override
            protected ExtendedToolbarView initToolBar() {
                if (!PluginChecker.isSVNProviderPluginLoaded()) {
                    return null;
                }
                return new ConnectionsListButtonsToolBar(getMainComposite(), SWT.NONE, getExtendedTableViewer());
            }

            @Override
            protected void createColumns(TableViewerCreator<ConnectionBean> tableViewerCreator, Table table) {
                TableViewerCreatorColumn nameColumn = new TableViewerCreatorColumn(tableViewerCreator);
                nameColumn.setTitle(Messages.getString("ConnectionsListComposite.nameColumnTitle.name")); //$NON-NLS-1$
                nameColumn.setBeanPropertyAccessors(new IBeanPropertyAccessors<ConnectionBean, String>() {

                    @Override
                    public String get(ConnectionBean bean) {
                        return bean.getName();
                    }

                    @Override
                    public void set(ConnectionBean bean, String value) {
                        bean.setName(value);
                    }

                });
                nameColumn.setWeight(20);
                nameColumn.setModifiable(!isReadOnly());
                nameColumn.setMinimumWidth(20);
                nameColumn.setCellEditor(new TextCellEditor(tableViewerCreator.getTable()));

                TableViewerCreatorColumn completeColumn = new TableViewerCreatorColumn(tableViewerCreator);
                completeColumn.setTitle(Messages.getString("ConnectionsListComposite.comleteColumnTitle.complete")); //$NON-NLS-1$
                completeColumn.setBeanPropertyAccessors(new IBeanPropertyAccessors<ConnectionBean, Boolean>() {

                    @Override
                    public Boolean get(ConnectionBean bean) {
                        return bean.isComplete();
                    }

                    @Override
                    public void set(ConnectionBean bean, Boolean value) {
                        bean.setComplete(value);
                    }

                });
                completeColumn.setWeight(5);
                completeColumn.setModifiable(false);
                completeColumn.setMinimumWidth(5);
                completeColumn.setImageProvider(new StatusImageProvider());
                completeColumn.setDisplayedValue(""); //$NON-NLS-1$
                // CheckboxTableEditorContent checkboxTableEditorContent = new CheckboxTableEditorContent();
                // comleteColumn.setTableEditorContent(checkboxTableEditorContent);
                // completeColumn.setCellEditor(new TextCellEditor(tableViewerCreator.getTable()));
            }

        };

        addListeners();
    }

    private void addListeners() {
    }

    protected List<ConnectionBean> getClonedList(List<ConnectionBean> source) {
        if (source == null) {
            return null;
        }
        List<ConnectionBean> target = new ArrayList<ConnectionBean>(source.size());
        Iterator<ConnectionBean> iter = source.iterator();
        while (iter.hasNext()) {
            ConnectionBean sourceBean = iter.next();
            try {
                ConnectionBean targetBean = sourceBean.clone();
                target.add(targetBean);
            } catch (CloneNotSupportedException e) {
                CommonExceptionHandler.process(e);
            }

        }
        return target;
    }

    public ConnectionFormComposite getConnectionsFormComposite() {
        return connectionsFormComposite;
    }

    public void setConnectionsFormComposite(ConnectionFormComposite connectionsFormComposite) {
        this.connectionsFormComposite = connectionsFormComposite;

        table.getTableViewerCreator().getSelectionHelper().addAfterSelectionListener(new ILineSelectionListener() {

            @Override
            public void handle(LineSelectionEvent e) {
                ISelection sel = e.source.getTableViewer().getSelection();
                IStructuredSelection sel2 = (IStructuredSelection) sel;
                ConnectionBean selected = (ConnectionBean) sel2.getFirstElement();
                setSelectedConnection(selected);
            }

        });

        if (!list.isEmpty()) {
            ConnectionBean selectConnection = null;
            ConnectionBean defaultConnectionSelected = dialog.getDefaultConnectionSelected();
            for (int i = 0; i < list.size(); i++) {
                ConnectionBean connectionBean = list.get(i);
                setSelectedConnection(connectionBean);
                if (isTheSameConnection(connectionBean, defaultConnectionSelected)) {
                    selectConnection = connectionBean;
                }
            }
            if (selectConnection != null) {
                table.getTableViewerCreator().getTableViewer()
                        .setSelection(new StructuredSelection(new Object[] { selectConnection }));
            } else {
                table.getTableViewerCreator().getTableViewer()
                        .setSelection(new StructuredSelection(new Object[] { list.get(0) }));
            }
        }
    }

    private boolean isTheSameConnection(ConnectionBean baseConnection, ConnectionBean refConnection) {
        boolean flag = false;
        if (baseConnection == null || refConnection == null) {
            return flag;
        }
        if (baseConnection.getName().equals(refConnection.getName())
                && baseConnection.getUrl().equals(refConnection.getUrl())
                && baseConnection.getRepositoryId().equals(refConnection.getRepositoryId())
                && baseConnection.getUser().equals(refConnection.getUser())) {
            flag = true;
        }
        return flag;
    }

    private void setSelectedConnection(ConnectionBean selected) {
        connectionsFormComposite.setConnection(selected);
    }

    public void refresh(ConnectionBean toRefresh) {
        table.getTableViewerCreator().getTableViewer().refresh(toRefresh);
        table.getTableViewerCreator().refreshTableEditorControls();
    }

    public List<ConnectionBean> getList() {
        return list;
    }

    /**
     * Manage ok/nok image in second column to show is the connection is complete/uncomplete.
     */
    private class StatusImageProvider implements IColumnImageProvider {

        @Override
        public Image getImage(Object bean) {
            ConnectionBean connectionBean = (ConnectionBean) bean;
            if (connectionBean.isComplete()) {
                return ImageProvider.getImage(EImage.OK);
            } else {
                return ImageProvider.getImage(EImage.ERROR_ICON);
            }
        }

    }
}
