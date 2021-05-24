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
package org.talend.repository.ui.dialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.properties.StatusHelper;

/**
 * DOC gldu class global comment. Detailled comment
 */
public class StatusConfirmSettingDialog extends Dialog {

    private static final String TITLE = Messages.getString("ItemsVersionChangedDialog.Title"); //$NON-NLS-1$

    private List<RepositoryObject> modifiedVersionItems;

    private Map<IImage, Image> cacheItemImages = new HashMap<IImage, Image>();

    private org.talend.repository.ui.properties.StatusHelper statusHelper;

    public StatusConfirmSettingDialog(Shell parentShell, List<RepositoryObject> modifiedVersionItems, StatusHelper statusHelper) {
        super(parentShell);
        this.modifiedVersionItems = modifiedVersionItems;
        this.statusHelper = statusHelper;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(TITLE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        TableViewer viewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = 200;
        data.heightHint = 200;
        data.minimumWidth = 350;
        data.widthHint = 350;
        viewer.getControl().setLayoutData(data);
        final Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.getString("VersionManagementDialog.Items")); //$NON-NLS-1$
        column.setWidth(140);

        column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.getString("StatusManagementDialog.Status")); //$NON-NLS-1$
        column.setWidth(120);

        column = new TableColumn(table, SWT.NONE);
        column.setText(Messages.getString("StatusManagementDialog.NewStatus")); //$NON-NLS-1$
        column.setWidth(90);

        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setLabelProvider(new ITableLabelProvider() {

            public Image getColumnImage(Object element, int columnIndex) {
                RepositoryObject object = (RepositoryObject) element;
                if (columnIndex == 0) {
                    ERepositoryObjectType itemType = object.getRepositoryObjectType();
                    return getItemsImage(CoreImageProvider.getIcon(itemType));
                }
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                RepositoryObject object = (RepositoryObject) element;
                switch (columnIndex) {
                case 0:
                    return object.getLabel();
                case 1:
                    return statusHelper.getStatusLabel(object.getProperty().getOldStatusCode());
                case 2:
                    if (object.getStatusCode().equals(object.getProperty().getOldStatusCode())) {
                        return "-"; //$NON-NLS-1$
                    }
                    return statusHelper.getStatusLabel(object.getStatusCode());
                default:
                }
                return null;
            }

            public void addListener(ILabelProviderListener listener) {

            }

            public void dispose() {

            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void removeListener(ILabelProviderListener listener) {

            }
        });
        viewer.setInput(modifiedVersionItems);
        for (TableItem item : table.getItems()) {
            RepositoryObject object = (RepositoryObject) item.getData();
            // if (!object.getOldVersion().equals(object.getNewVersion())) {
            item.setForeground(2, Display.getDefault().getSystemColor(SWT.COLOR_RED));
            // }
        }
        return composite;
    }

    private Image getItemsImage(IImage iImage) {
        if (iImage == null) {
            iImage = EImage.DEFAULT_IMAGE;
        }
        Image image = cacheItemImages.get(iImage);
        if (image == null) {
            Image oImage = ImageProvider.getImage(iImage);
            ImageData imageData = oImage.getImageData();
            // enlarge image
            final int larger = 4;
            ImageData newData = imageData.scaledTo(imageData.width + larger, imageData.height + larger);
            image = new Image(oImage.getDevice(), newData);
            cacheItemImages.put(iImage, image);
        }
        return image;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID, "Confirm", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }
}
