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
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.runprocess.IRunProcessService;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class JavaCommandController extends AbstractElementPropertySectionController {

    private static final String JAVA_COMMAND = "JAVA_COMMAND"; //$NON-NLS-1$

    /**
     * DOC nrousseau JavaCommandController constructor comment.
     *
     * @param dp
     */
    public JavaCommandController(IDynamicProperty dp) {
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
    public Control createControl(Composite subComposite, final IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        Button btnEdit;

        btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$

        btnEdit.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        FormData data;
        btnEdit.setData(NAME, JAVA_COMMAND);
        btnEdit.setData(PARAMETER_NAME, param.getName());
        btnEdit.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                // execute Java Command
                ElementParameter fullParam = (ElementParameter) param;
                File jar;
                URL url;
                try {
                    List<URL> listURL = new ArrayList<URL>();
                    ILibraryManagerService libManager = (ILibraryManagerService) GlobalServiceRegister.getDefault().getService(
                            ILibraryManagerService.class);
                    IFolder javaLibFolder = null;
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
                        IRunProcessService processService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                                IRunProcessService.class);
                        javaLibFolder = processService.getJavaProjectLibFolder();
                    }
                    if (javaLibFolder == null) {
                        return;
                    }
                    for (String jarString : fullParam.getJar().split(";")) {
                        IPath libPath = javaLibFolder.getLocation();
                        libManager.retrieve(jarString, libPath.toPortableString(), new NullProgressMonitor());

                        jar = libPath.append(jarString).toFile();
                        url = jar.toURL();
                        listURL.add(url);
                    }
                    URLClassLoader urlClassLoader = new URLClassLoader(listURL.toArray(new URL[0]));
                    Class<?> classLoaded = Class.forName(fullParam.getJavaClass(), true, urlClassLoader);
                    Object object = classLoaded.newInstance();
                    List<String> args = new ArrayList<String>();
                    for (String arg : fullParam.getArgs()) {
                        args.add(ElementParameterParser.parse(elem, arg));
                    }
                    for (Method method : classLoaded.getDeclaredMethods()) {
                        if (method.getName().equals(fullParam.getJavaFunction())) {
                            Object arglist[] = new Object[1];
                            arglist[0] = args.toArray(new String[0]);
                            method.invoke(object, arglist);
                        }
                    }
                } catch (Exception e1) {
                    MessageBoxExceptionHandler.process(e1);
                }
            }

        });
        if (elem instanceof Node) {
            btnEdit.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // **************************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
                data.right = new FormAttachment(lastControl, currentLabelWidth + STANDARD_BUTTON_WIDTH);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
                data.right = new FormAttachment(0, currentLabelWidth + STANDARD_BUTTON_WIDTH);
            }
        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
            data.right = new FormAttachment(labelLabel, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        btnEdit.setLayoutData(data);
        // **************************
        hashCurControls.put(param.getName(), btnEdit);

        Point initialSize = btnEdit.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return btnEdit;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        Button btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnEdit.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        Point initialSize = btnEdit.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        btnEdit.dispose();
        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#refresh(org
     * .talend.core.model.process.IElementParameter, boolean)
     */
    @Override
    public void refresh(IElementParameter param, boolean check) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
    }

}
