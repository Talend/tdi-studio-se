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

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.ImageUtils;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.repository.ui.view.RepositoryLabelProvider;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;

/**
 * DOC zywang class global comment. Detailled comment
 */
public class IconSelectionController extends AbstractElementPropertySectionController {

    private static final String ICON_SELECTION = "ICON_SELECTION"; //$NON-NLS-1$

    private static final String ICON_REVERT = "ICON_REVERT"; //$NON-NLS-1$

    private AbstractProcessProvider findProcessProvider = AbstractProcessProvider
            .findProcessProviderFromPID(IComponent.JOBLET_PID);

    private Label filePathText;

    /**
     * yzhang FileController constructor comment.
     *
     * @param parameterBean
     */
    public IconSelectionController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * This method will never be called.
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#createCommand
     * ()
     */
    public Command createCommand() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createCommand()
     */
    public Command createCommand(Button button) {
        Object data = button.getData(NAME);
        if (data != null) {
            if (ICON_SELECTION.equals(data)) {
                FileDialog dial = new FileDialog(composite.getShell(), SWT.NONE);
                dial.setFilterExtensions(new String[] { "*.jpg", "*.png", "*.gif" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                String propertyName = (String) button.getData(PARAMETER_NAME);
                String file = dial.open();
                if (file != null) {
                    if (!file.equals("")) { //$NON-NLS-1$
                        if (!elem.getPropertyValue(propertyName).equals(file)) {
                            ImageData imageData = new ImageData(file);
                            if (ImageUtils
                                    .checkSize(ImageDescriptor.createFromImageData(imageData), ImageUtils.ICON_SIZE.ICON_32)) {
                                if (elem instanceof IProcess2) {
                                    refreshIcon(imageData);
                                    return new IconSelectionCommand((IProcess2) elem,
                                            ImageDescriptor.createFromImageData(imageData), file);
                                }
                            } else {
                                MessageDialog.openError(composite.getShell(),
                                        Messages.getString("IconSelectionController.MessageTitle"), //$NON-NLS-1$
                                        Messages.getString("IconSelectionController.Messages")); //$NON-NLS-1$
                            }
                        }

                    }
                }
            } else if (ICON_REVERT.equals(data)) {
                if (elem instanceof IProcess2) {
                    Image defaultIcon = RepositoryLabelProvider.getDefaultJobletImage(((IProcess2) elem).getProperty().getItem());
                    ImageDescriptor imageData = ImageDescriptor.createFromImage(defaultIcon);
                    refreshIcon(defaultIcon.getImageData());
                    return new IconSelectionCommand((IProcess2) elem, imageData, null);
                }
            }
        }
        return null;
    }

    /**
     * hshen Comment method "refreshIcon".
     */
    private void refreshIcon(ImageData imageData) {
        Image image = new Image(composite.getShell().getDisplay(), imageData);
        addResourceDisposeListener(filePathText, image);
        filePathText.setImage(image);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createControl()
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        this.curParameter = param;
        FormData data;
        Button btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$

        btnEdit.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));

        data = new FormData();
        data.left = new FormAttachment(0, 120);
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT - 2;
        btnEdit.setLayoutData(data);
        btnEdit.setData(NAME, ICON_SELECTION);
        btnEdit.setData(PARAMETER_NAME, param.getName());
        btnEdit.setEnabled(!param.isReadOnly());
        btnEdit.addSelectionListener(listenerSelection);

        DecoratedField dField = new DecoratedField(subComposite, SWT.NONE, new IControlCreator() {

            @Override
            public Control createControl(Composite parent, int style) {

                return new Label(parent, style);
            }

        });
        // revert btn
        Button btnRevert = getWidgetFactory().createButton(subComposite,
                Messages.getString("IconSelectionController.Revert"), SWT.PUSH); //$NON-NLS-1$

        data = new FormData();
        data.left = new FormAttachment(btnEdit, 5);
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT - 2;
        btnRevert.setLayoutData(data);
        btnRevert.setData(NAME, ICON_REVERT);
        btnRevert.setData(PARAMETER_NAME, param.getName());
        btnRevert.setEnabled(!param.isReadOnly());
        btnRevert.addSelectionListener(listenerSelection);

        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription(Messages.getString("FileController.decoration.description")); //$NON-NLS-1$
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        filePathText = (Label) dField.getControl();
        String file = (String) elem.getPropertyValue(PARAMETER_NAME);
        if (file != null) {
            // ImageData imageData = new ImageData(file);
            // image = new Image(composite.getShell().getDisplay(), imageData);
            // filePathText.setImage(image);
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName(), 0);
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
        int currentLabelWidth = 50;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }
        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.top = new FormAttachment(btnEdit, 0, SWT.CENTER);
        data.height = 34;
        data.width = 30;
        cLayout.setLayoutData(data);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
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
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new IControlCreator() {

            @Override
            public Control createControl(Composite parent, int style) {

                return new Label(parent, style);
            }

        });
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

    SelectionListener listenerSelection = new SelectionListener() {

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            Command cmd = createCommand((Button) e.getSource());
            executeCommand(cmd);
        }

    };

    @Override
    public void refresh(IElementParameter param, boolean checkErrorsWhenViewRefreshed) {

        IElement element = param.getElement();
        if (element instanceof IProcess2) {

            if (findProcessProvider != null) {
                ImageDescriptor icon = findProcessProvider.getIcons((IProcess2) element);
                if (icon != null) {
                    Image image = icon.createImage();
                    addResourceDisposeListener(filePathText, image);
                    filePathText.setImage(image);
                }
            }
        }
    }

    class IconSelectionCommand extends Command {

        private ImageDescriptor oldImage, newImage;

        private IProcess2 process;

        private Command changeCmd;

        public IconSelectionCommand(IProcess2 process, ImageDescriptor newImage, String file) {
            super();
            this.process = process;
            this.newImage = newImage;
            if (file != null) {
                changeCmd = new PropertyChangeCommand(process, EParameterName.ICONSELECTION.getName(), file);
            }
        }

        @Override
        public void execute() {
            if (findProcessProvider != null) {
                oldImage = findProcessProvider.getIcons(process);
                findProcessProvider.setIcons((IProcess) elem, newImage);
            }
            if (changeCmd != null) {
                changeCmd.execute();
            }
            // see bug 15062
            final Item item = process.getProperty().getItem();
            if (item instanceof JobletProcessItem) {
                String label = item.getProperty().getLabel();
                CoreImageProvider.removeComponentImage(label);
            }// end
        }

        @Override
        public void undo() {
            if (findProcessProvider != null) {
                findProcessProvider.setIcons((IProcess) elem, oldImage);
            }
            if (changeCmd != null) {
                changeCmd.undo();
            }
        }

    }

}
