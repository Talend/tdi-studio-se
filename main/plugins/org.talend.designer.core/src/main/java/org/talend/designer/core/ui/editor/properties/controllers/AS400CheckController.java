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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC qwei class global comment. Detailled comment
 */
public class AS400CheckController extends AbstractElementPropertySectionController {

    public AS400CheckController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#createControl
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter, int, int, int,
     * org.eclipse.swt.widgets.Control)
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new IControlCreator() {

            @Override
            public Control createControl(Composite parent, int style) {
                return getWidgetFactory().createButton(parent, param.getDisplayName(), SWT.CHECK);
            }

        });
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription(Messages.getString("CheckController.decoration.description")); //$NON-NLS-1$
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        cLayout.setBackground(subComposite.getBackground());
        Button checkBtn = (Button) dField.getControl();

        FormData data = new FormData();
        data.top = new FormAttachment(0, top);
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        cLayout.setLayoutData(data);
        hashCurControls.put(param.getName(), checkBtn);
        checkBtn.setEnabled(!param.isReadOnly());
        String propertyType = (String) elem.getPropertyValue(EParameterName.PROPERTY_TYPE.getName());
        if (propertyType.equals(EmfComponent.REPOSITORY)) {
            IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
            List<ConnectionItem> metadataConnectionsItem = null;
            try {
                metadataConnectionsItem = factory.getMetadataConnectionsItem();

            } catch (PersistenceException e) {
                throw new RuntimeException(e);
            }
            if (metadataConnectionsItem != null) {
                for (ConnectionItem connectionItem : metadataConnectionsItem) {
                    String value = connectionItem.getProperty().getId() + ""; //$NON-NLS-1$
                    if (value.equals(elem.getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName()))) {
                        boolean b = getConnection(connectionItem).isStandardSQL();
                        checkBtn.setSelection(b);
                    }
                }
            }
        } else {
            boolean b = CoreUIPlugin.getDefault().getPreferenceStore().getBoolean(ITalendCorePrefConstants.AS400_SQL_SEG);
            checkBtn.setSelection(b);
        }

        if (elem instanceof Node) {
            checkBtn.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        Point initialSize = checkBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return cLayout;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, final IElementParameter param) {
        // TODO Auto-generated method stub
        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new IControlCreator() {

            @Override
            public Control createControl(Composite parent, int style) {
                return getWidgetFactory().createButton(parent, param.getDisplayName(), SWT.CHECK);
            }

        });
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    @Override
    public void refresh(IElementParameter param, boolean check) {
        // TODO Auto-generated method stub
        Button checkBtn = (Button) hashCurControls.get(param.getName());
        if (checkBtn == null || checkBtn.isDisposed()) {
            return;
        }
        String propertyType = (String) elem.getPropertyValue(EParameterName.PROPERTY_TYPE.getName());
        if (propertyType.equals(EmfComponent.REPOSITORY)) {
            IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
            List<ConnectionItem> metadataConnectionsItem = null;
            try {
                metadataConnectionsItem = factory.getMetadataConnectionsItem();

            } catch (PersistenceException e) {
                throw new RuntimeException(e);
            }
            if (metadataConnectionsItem != null) {
                for (ConnectionItem connectionItem : metadataConnectionsItem) {
                    String value = connectionItem.getProperty().getId() + ""; //$NON-NLS-1$
                    if (value.equals(elem.getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName()))) {
                        boolean b = getConnection(connectionItem).isStandardSQL();
                        checkBtn.setSelection(b);
                        if (b) {
                            param.setValue(Boolean.TRUE);
                        } else {
                            param.setValue(Boolean.FALSE);
                        }
                    }
                }
            }
        } else {
            boolean b = CoreUIPlugin.getDefault().getPreferenceStore().getBoolean(ITalendCorePrefConstants.AS400_SQL_SEG);
            checkBtn.setSelection(b);
            if (b) {
                param.setValue(Boolean.TRUE);
            } else {
                param.setValue(Boolean.FALSE);
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub

    }

    protected DatabaseConnection getConnection(ConnectionItem connectionItem) {
        return (DatabaseConnection) connectionItem.getConnection();
    }

}
