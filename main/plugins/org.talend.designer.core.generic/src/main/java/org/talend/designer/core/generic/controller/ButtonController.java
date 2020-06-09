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
package org.talend.designer.core.generic.controller;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.runtime.util.GenericTypeUtils;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.i18n.Messages;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.model.GenericTableUtils;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.core.ui.views.properties.composites.MissingSettingsMultiThreadDynamicComposite;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;

/**
 *
 * created by ycbai on 2015年9月28日 Detailled comment
 *
 */
public class ButtonController extends AbstractElementPropertySectionController {

    private static Logger log = Logger.getLogger(ButtonController.class);

    private static final String TEST_CONNECTION = "Test connection"; //$NON-NLS-1$

    public ButtonController(IDynamicProperty dp) {
        super(dp);
    }

    public Command createCommand(Button button) {
        IElementParameter parameter = (IElementParameter) button.getData();
        if(button.getText() != null && button.getText().equals(TEST_CONNECTION)){
            IElementParameter elementParameter = parameter.getElement().getElementParameter("connection.jdbcUrl");
            if (elementParameter != null) {
                Object jdbcUrl = elementParameter.getValue();
                if (jdbcUrl != null) {
                    if (beginOrEndWithQuote(String.valueOf(jdbcUrl))) {
                        Exception tempE = new Exception(Messages.getString("ButtonController.ErrorDialog.reason"));
                        Status status = new Status(IStatus.ERROR, IGenericConstants.PLUGIN_ID, 1, tempE.getMessage(),
                                tempE.getCause());
                        new ErrorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                Messages.getString("ButtonController.ErrorDialog.title"),
                                Messages.getString("ButtonController.ErrorDialog.message"), status,
                                IStatus.ERROR).open();
                        return null;
                    }
                }
            }
            chooseContext();
            Boolean result = loadJars(parameter);
            if (result != null && !result) {
                log.log(Priority.INFO, "JDBC Test connection install needed module is canceled", new Exception());
                return null;
            }
        }

        if (parameter != null) {
            callBeforeActive(parameter);
            // so as to invoke listeners to perform some actions.
            return new PropertyChangeCommand(elem, parameter.getName(), null);
        }
        return null;
    }

    private boolean beginOrEndWithQuote(String s) {
        if (s == null) {
            return false;
        }
        if (s.startsWith("\"") || s.endsWith("\"")) {
            return true;
        }
        return false;
    }

    private void chooseContext(){
        ConnectionItem connItem = null;
        if(dynamicProperty instanceof MissingSettingsMultiThreadDynamicComposite){
            connItem = ((MissingSettingsMultiThreadDynamicComposite)dynamicProperty).getConnectionItem();
        }
        if(connItem == null){
            return;
        }
        Connection conn = connItem.getConnection();
        if(!conn.isContextMode()){
            return;
        }
        ConnectionContextHelper.context = ConnectionContextHelper.getContextTypeForContextMode(conn,
                null, false);
    }

    private Boolean loadJars(IElementParameter parameter) {
        ILibraryManagerService librairesManagerService = (ILibraryManagerService) GlobalServiceRegister.getDefault().getService(
                ILibraryManagerService.class);
        if(librairesManagerService == null){
            return null;
        }
        if(!(parameter instanceof GenericElementParameter)){
            return null;
        }
        GenericElementParameter gPara = (GenericElementParameter) parameter;
        ComponentProperties rootPro = gPara.getRootProperties();
        if(rootPro == null){
            return null;
        }
        Properties connPro = rootPro.getProperties("connection"); //$NON-NLS-1$
        if(connPro == null){
            return null;
        }
        Properties drivers = connPro.getProperties("driverTable"); //$NON-NLS-1$
        if(drivers == null){
            return null;
        }
        List<String> jars = new ArrayList<String>();
        for(NamedThing thing : drivers.getProperties()){
            if(!(thing instanceof Property)){
                continue;
            }
            if(GenericTypeUtils.isListStringType((Property)thing)){
                List<String> listString = (List<String>) ((Property)thing).getValue();
                if(listString == null){
                	continue;
                }
                for(String path : listString){
                    if (!StringUtils.isBlank(path)) {
                        jars.add(GenericTableUtils.getDriverJarPath(path));
                    }
                }

            }
        }
        return librairesManagerService.retrieve(jars, ExtractMetaDataUtils.getInstance().getJavaLibPath(),
                new NullProgressMonitor());
    }

    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        Button theBtn = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        theBtn.setBackground(subComposite.getBackground());
        if (param.getDisplayName().equals("")) { //$NON-NLS-1$
            theBtn.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        } else {
            theBtn.setText(param.getDisplayName());
        }
        FormData data = new FormData();
        if (isInWizard()) {
            if (lastControl != null) {
                data.right = new FormAttachment(lastControl, 0);
            } else {
                data.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
            }
        } else {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, 0);
            } else {
                data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
            }
        }
        data.top = new FormAttachment(0, top);
        theBtn.setLayoutData(data);
        theBtn.setEnabled(!param.isReadOnly());
        theBtn.setData(param);
        hashCurControls.put(param.getName(), theBtn);
        theBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Command cmd = createCommand((Button) e.getSource());
                executeCommand(cmd);
                ConnectionContextHelper.context = null;
            }
        });
        Point initialSize = theBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return theBtn;
    }

    @Override
    public void refresh(IElementParameter param, boolean check) {
        // TODO Auto-generated method stub
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub
    }

    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        return 0;
    }

}
