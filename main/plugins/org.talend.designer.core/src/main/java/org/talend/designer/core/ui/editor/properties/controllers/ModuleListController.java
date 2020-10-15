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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.runtime.maven.MavenArtifact;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ExternalUtilities;
import org.talend.designer.core.ui.dialog.BrmsDialog;
import org.talend.designer.core.ui.dialog.IBrmsExtension;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.creator.SelectAllTextControlCreator;
import org.talend.librariesmanager.ui.dialogs.ConfigModuleDialog;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: ColumnListController.java 1 2006-12-12 下午02:04:32 +0000 (下午02:04:32) yzhang $
 *
 */
public class ModuleListController extends AbstractElementPropertySectionController {

    private static final String MODULE = "MODULE"; //$NON-NLS-1$

    private static final String BUTTON_EDIT = "buttonEdit"; //$NON-NLS-1$

    /**
     * DOC dev ColumnListController constructor comment.
     *
     * @param parameterBean
     */
    public ModuleListController(IDynamicProperty dp) {
        super(dp);
    }

    public Command createCommand(Button button) {
        Node node = (Node) elem;
        IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen(node);
        if (externalNode != null && externalNode.getUniqueName().contains("tBRMS_")) {
            IConfigurationElement[] elems = Platform.getExtensionRegistry().getConfigurationElementsFor(
                    "org.talend.designer.core.brms_provider");
            String propertyName = (String) button.getData(PARAMETER_NAME);
            for (IConfigurationElement conElem : elems) {
                IBrmsExtension createExecutableExtension;
                try {
                    createExecutableExtension = (IBrmsExtension) conElem.createExecutableExtension("class");
                    createExecutableExtension.initialize(node, propertyName, hashCurControls);
                    BrmsDialog brmsDialog = createExecutableExtension.createBrmsDialog(composite.getShell());
                    String file = brmsDialog.getFile();
                    if (file != null && !file.equals("")) {
                        String lastSegment = TalendTextUtils.addQuotes(Path.fromOSString(file).lastSegment());
                        try {
                            CorePlugin.getDefault().getLibrariesService().deployLibrary(Path.fromOSString(file).toFile().toURL());
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                        }

                        // update the text current value
                        Text text = (Text) hashCurControls.get(propertyName);
                        if (text != null && !text.isDisposed()) {
                            text.setText(Path.fromOSString(file).lastSegment());
                        }
                        return new PropertyChangeCommand(elem, propertyName, lastSegment);
                    }

                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }

            }
        } else {
            String propertyName = (String) button.getData(PARAMETER_NAME);
            Object value = elem.getPropertyValue(propertyName);
            String initValue = value == null ? "" : TalendTextUtils.removeQuotes(value.toString());
            ConfigModuleDialog dial = new ConfigModuleDialog(composite.getShell(), initValue);
            if (Window.OK == dial.open()) {
                String moduleName = dial.getMavenURI();
                if (moduleName != null && !moduleName.equals("")) { //$NON-NLS-1$
                    String lastSegment = TalendTextUtils.addQuotes(moduleName);

                    if (!elem.getPropertyValue(propertyName).equals(lastSegment)) {

                        // update the text current value
                        Text text = (Text) hashCurControls.get(propertyName);
                        if (text != null && !text.isDisposed()) {
                            String modName = getModuleName(moduleName);
                            text.setText(modName);
                        }

                        return new PropertyChangeCommand(elem, propertyName, lastSegment);
                    }
                }
            }
        }

        return null;
    }

    IControlCreator cbCtrl = new IControlCreator() {

        @Override
        public Control createControl(final Composite parent, final int style) {
            Text cb = new Text(parent, style);
            return cb;
        }
    };

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createControl()
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        if (param.getDisplayName().startsWith("!!")) { //$NON-NLS-1$
            if (param.getFieldType() == EParameterFieldType.MODULE_LIST) {
                param.setDisplayName(EParameterName.MODULE_LIST.getDisplayName());
            }
        }

        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new SelectAllTextControlCreator());
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }

        Control cLayout = dField.getLayoutControl();
        Text text = (Text) dField.getControl();

        text.setEditable(false);
        cLayout.setBackground(subComposite.getBackground());
        text.setEnabled(!param.isReadOnly());
        text.addSelectionListener(listenerSelection);
        if (elem instanceof Node) {
            text.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        FormData data = new FormData();
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
        // *********************
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
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment(labelLabel, STANDARD_LABEL_WIDTH * 3, SWT.RIGHT);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);

        Button btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnEdit.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));

        data = new FormData();
        data.left = new FormAttachment(cLayout, 0, SWT.RIGHT);
        data.right = new FormAttachment(cLayout, ITabbedPropertyConstants.HSPACE + STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT - 2;
        btnEdit.setLayoutData(data);
        btnEdit.setData(NAME, MODULE);
        btnEdit.setData(PARAMETER_NAME, param.getName());
        btnEdit.setEnabled(!param.isReadOnly());
        btnEdit.addSelectionListener(listenerSelection);

        // **********************
        hashCurControls.put(param.getName(), text);
        hashCurControls.put(param.getName() + BUTTON_EDIT, btnEdit);
        updateData();
        // this.dynamicTabbedPropertySection.updateColumnList(null);

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
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new TextControlCreator());
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    private void updateData() {
        if (elem instanceof Node) {
            updateModuleList((Node) elem);
        }
    }

    public static void updateModuleList(Node node) {
        ILibraryManagerService repositoryBundleService = (ILibraryManagerService) GlobalServiceRegister.getDefault().getService(
                ILibraryManagerService.class);
        Set<String> existLibraries = repositoryBundleService.list();
        Set<String> moduleNameList = new TreeSet<String>();
        Set<String> moduleValueList = new TreeSet<String>();
        for (String lib : existLibraries) {
            moduleNameList.add(lib);
            moduleValueList.add(TalendTextUtils.addQuotes(lib));
        }
        Comparator<String> comprarator = new IgnoreCaseComparator();
        String[] moduleNameArray = moduleNameList.toArray(new String[0]);
        String[] moduleValueArray = moduleValueList.toArray(new String[0]);
        Arrays.sort(moduleNameArray, comprarator);
        Arrays.sort(moduleValueArray, comprarator);

        for (int i = 0; i < node.getElementParameters().size(); i++) {
            IElementParameter param = node.getElementParameters().get(i);
            if (param.getFieldType() == EParameterFieldType.MODULE_LIST) {
                param.setListItemsDisplayName(moduleNameArray);
                param.setListItemsValue(moduleValueArray);
            } else if (param.getFieldType() == EParameterFieldType.TABLE) {
                Object[] listItemsValue = param.getListItemsValue();
                if (listItemsValue != null) {
                    for (Object o : listItemsValue) {
                        if (o instanceof IElementParameter
                                && ((IElementParameter) o).getFieldType() == EParameterFieldType.MODULE_LIST) {
                            ((IElementParameter) o).setListItemsDisplayName(moduleNameArray);
                            ((IElementParameter) o).setListItemsValue(moduleValueArray);
                        }
                    }
                }
            }
        }
    }

    /**
     * DOC yzhang class global comment. Detailled comment
     */
    private final static class IgnoreCaseComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

    SelectionListener listenerSelection = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent event) {
            if (event.getSource() instanceof Button) {
                Command cmd = createCommand((Button) event.getSource());
                executeCommand(cmd);
            }
        }
    };

    @Override
    public void refresh(IElementParameter param, boolean check) {
        Text text = (Text) hashCurControls.get(param.getName());
        if (text == null || text.isDisposed()) {
            return;
        }
        updateData();

        Object value = param.getValue();
        if (value instanceof String) {

            String txt = TalendTextUtils.removeQuotes((String) value);
            if (param.getFieldType() == EParameterFieldType.MODULE_LIST) {
                txt = getModuleName(txt);
            }

            text.setText(txt);
        }

        if (param.isContextMode()) {
            Button buttonEdit = (Button) hashCurControls.get(param.getName() + BUTTON_EDIT);
            text.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
            text.setEnabled(false);
            buttonEdit.setEnabled(false);
        }
    }

    private static String getModuleName(String jarPath) {
        if (jarPath != null) {
            jarPath = TalendQuoteUtils.removeQuotes(jarPath);
            if (jarPath.startsWith(MavenUrlHelper.MVN_PROTOCOL)) {
                MavenArtifact art = MavenUrlHelper.parseMvnUrl(jarPath);
                return art.getFileName();
            }
        }
        return jarPath;
    }
}
