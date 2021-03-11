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
package org.talend.designer.core.ui.views.jobsettings.tabs;

import java.io.File;
import java.io.IOException;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.HeaderFooterConnection;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.HeaderFooterConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.process.IGEFProcess;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.librariesmanager.prefs.LibrariesManagerUtils;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

/**
 * yzhang class global comment. Detailled comment
 */
public class HeaderFooterComposite extends AbstractTabComposite {

    private boolean enableControl;

    private Button headerButton;

    private Button footerButton;

    private Text headerText;

    private Text footerText;

    /**
     * yzhang MainComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public HeaderFooterComposite(Composite parent, int style, TabbedPropertySheetWidgetFactory factory, IRepositoryViewObject obj) {
        super(parent, style, factory, obj);

        FormLayout layout = new FormLayout();
        setLayout(layout);

        FormData thisFormData = new FormData();
        thisFormData.left = new FormAttachment(0, 0);
        thisFormData.right = new FormAttachment(100, 0);
        thisFormData.top = new FormAttachment(0, 0);
        thisFormData.bottom = new FormAttachment(100, 0);
        setLayoutData(thisFormData);

        Composite composite = widgetFactory.createFlatFormComposite(this);

        FormData compositeData = new FormData();
        compositeData.left = new FormAttachment(0, 0);
        compositeData.right = new FormAttachment(100, 0);
        compositeData.top = new FormAttachment(0, 0);
        compositeData.bottom = new FormAttachment(100, 0);
        composite.setLayoutData(thisFormData);

        // header
        CLabel headerLabel = widgetFactory.createCLabel(composite, "Header:"); //$NON-NLS-1$
        FormData headerData = new FormData();
        headerData.top = new FormAttachment(20, 0);
        headerData.left = new FormAttachment(0, 50);
        headerData.right = new FormAttachment(25, 0);
        headerLabel.setLayoutData(headerData);

        // header text
        headerText = widgetFactory.createText(composite, "", SWT.BORDER); //$NON-NLS-1$
        FormData data = new FormData();
        data.top = new FormAttachment(20, 0);
        data.left = new FormAttachment(headerLabel, ITabbedPropertyConstants.VSPACE);
        data.right = new FormAttachment(50, 0);
        headerText.setLayoutData(data);
        headerText.setEditable(false);
        if (repositoryObject instanceof Element) {
            Element elem = (Element) repositoryObject;
            String id = (String) elem.getElementParameter(EParameterName.HEADERFOOTER_HEADERID.getName()).getValue();
            IProxyRepositoryFactory proxyFactory = CorePlugin.getDefault().getProxyRepositoryFactory();
            IRepositoryViewObject lastVersion;
            try {
                lastVersion = proxyFactory.getLastVersion(id);
                if (lastVersion != null) {
                    headerText.setText("HEADER:" + lastVersion.getLabel());
                } else {
                    headerText.setText("HEADER:<none>");//$NON-NLS-1$
                }
            } catch (PersistenceException e1) {
                ExceptionHandler.process(e1);
            }
        }

        headerButton = widgetFactory.createButton(composite, "...", SWT.PUSH);//$NON-NLS-1$
        FormData headerButtonData = new FormData();
        headerButtonData.top = new FormAttachment(20, 0);
        headerButtonData.left = new FormAttachment(headerText, 5);
        // headerButtonData.right = new FormAttachment(50, 5);
        headerButton.setLayoutData(headerButtonData);

        headerButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                if (repositoryObject instanceof IProcess2) {
                    IProcess2 process = (IProcess2) repositoryObject;

                    RepositoryReviewDialog dialog = new RepositoryReviewDialog(Display.getCurrent().getActiveShell(),
                            ERepositoryObjectType.METADATA, true, ERepositoryCategoryType.HEADERFOOTER.getName());
                    if (dialog.open() == RepositoryReviewDialog.OK) {
                        String id = dialog.getResult().getObject().getId();
                        HeaderFooterConnectionItem connectionItem = (HeaderFooterConnectionItem) dialog.getResult().getObject()
                                .getProperty().getItem();

                        Connection connection = connectionItem.getConnection();
                        HeaderFooterConnection headerFooterConnection = (HeaderFooterConnection) connection;

                        boolean isHeader = headerFooterConnection.isIsHeader();
                        if (isHeader) {
                            headerText.setText("HEADER:" + connectionItem.getProperty().getLabel());

                            CompoundCommand compoundCommand = new CompoundCommand();
                            IElementParameter headerFooterIdParam = process
                                    .getElementParameter(EParameterName.HEADERFOOTER_HEADERID.getName());
                            if (headerFooterIdParam != null) {
                                headerFooterIdParam.setValue(id);
                                Command command1 = new PropertyChangeCommand(process, EParameterName.HEADERFOOTER_HEADERID
                                        .getName(), id);
                                compoundCommand.add(command1);
                            }
                            // headEnable
                            IElementParameter enableParameter = process.getElementParameter(EParameterName.HEADER_ENABLED
                                    .getName());
                            if (enableParameter != null) {
                                enableParameter.setValue(isHeader);
                                Command command2 = new PropertyChangeCommand(process, EParameterName.HEADER_ENABLED.getName(),
                                        isHeader);
                                compoundCommand.add(command2);

                            }
                            // headLibries
                            IElementParameter libraryParameter = process.getElementParameter(EParameterName.HEADER_LIBRARY
                                    .getName());
                            if (libraryParameter != null) {
                                libraryParameter.setValue(headerFooterConnection.getLibraries());
                                Command command3 = new PropertyChangeCommand(process, EParameterName.HEADER_LIBRARY.getName(),
                                        headerFooterConnection.getLibraries());
                                compoundCommand.add(command3);
                            }
                            // headCode
                            IElementParameter headCodeParameter = process.getElementParameter(EParameterName.HEADER_CODE
                                    .getName());
                            if (headCodeParameter != null) {
                                headCodeParameter.setValue(headerFooterConnection.getMainCode());
                                Command command4 = new PropertyChangeCommand(process, EParameterName.HEADER_CODE.getName(),
                                        headerFooterConnection.getMainCode());
                                compoundCommand.add(command4);
                            }
                            // headImport
                            IElementParameter headImportParameter = process.getElementParameter(EParameterName.HEADER_IMPORT
                                    .getName());
                            if (headImportParameter != null) {
                                headImportParameter.setValue(headerFooterConnection.getImports());
                                Command command5 = new PropertyChangeCommand(process, EParameterName.HEADER_IMPORT.getName(),
                                        headerFooterConnection.getImports());
                                compoundCommand.add(command5);
                            }

                            boolean executed = false;
                            if (process != null && process instanceof IGEFProcess) {
                                IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault()
                                        .getDesignerCoreUIService();
                                if (designerCoreUIService != null) {
                                    executed = designerCoreUIService.executeCommand((IGEFProcess) process, compoundCommand);
                                }
                            }
                            if (!executed) {
                                compoundCommand.execute();
                            }

                            copyLibriesToLibJava(headerFooterConnection);

                        }
                    }
                }
            }
        });

        Button headerRemoveButton = widgetFactory.createButton(composite, "clear", SWT.PUSH);//$NON-NLS-1$
        FormData headerRemoveButtonData = new FormData();
        headerRemoveButtonData.top = new FormAttachment(20, 0);
        headerRemoveButtonData.left = new FormAttachment(headerButton, 2);
        // headerButtonData.right = new FormAttachment(50, 5);
        headerRemoveButton.setLayoutData(headerRemoveButtonData);

        headerRemoveButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (repositoryObject instanceof IProcess2) {
                    IProcess2 process = (IProcess2) repositoryObject;
                    headerText.setText("HEADER:<none>");//$NON-NLS-1$
                    process.getElementParameter(EParameterName.HEADERFOOTER_FOOTERID.getName()).setValue(null);
                    process.getElementParameter(EParameterName.HEADER_ENABLED.getName()).setValue(null);
                    process.getElementParameter(EParameterName.HEADER_LIBRARY.getName()).setValue(null);
                    process.getElementParameter(EParameterName.HEADER_CODE.getName()).setValue(null);
                    process.getElementParameter(EParameterName.HEADER_IMPORT.getName()).setValue(null);
                    CompoundCommand compoundCommand = new CompoundCommand();
                    Command command1 = new PropertyChangeCommand(process, EParameterName.HEADERFOOTER_HEADERID.getName(), null);
                    compoundCommand.add(command1);
                    Command command2 = new PropertyChangeCommand(process, EParameterName.HEADER_ENABLED.getName(), null);
                    compoundCommand.add(command2);
                    Command command3 = new PropertyChangeCommand(process, EParameterName.HEADER_LIBRARY.getName(), null);
                    compoundCommand.add(command3);
                    Command command4 = new PropertyChangeCommand(process, EParameterName.HEADER_CODE.getName(), null);
                    compoundCommand.add(command4);
                    Command command5 = new PropertyChangeCommand(process, EParameterName.HEADER_IMPORT.getName(), null);
                    compoundCommand.add(command5);

                    boolean executed = false;
                    if (process != null && process instanceof IGEFProcess) {
                        IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
                        if (designerCoreUIService != null) {
                            executed = designerCoreUIService.executeCommand((IGEFProcess) process, compoundCommand);
                        }
                    }
                    if (!executed) {
                        compoundCommand.execute();
                    }

                }
            }
        });

        // footer
        CLabel footerLabel = widgetFactory.createCLabel(composite, "Footer:"); //$NON-NLS-1$
        FormData footerData = new FormData();
        footerData.top = new FormAttachment(headerLabel, 15);
        footerData.left = new FormAttachment(0, 50);
        footerData.right = new FormAttachment(25, 0);
        footerLabel.setLayoutData(footerData);

        // footer text
        footerText = widgetFactory.createText(composite, "", SWT.BORDER); //$NON-NLS-1$
        FormData data2 = new FormData();
        data2.top = new FormAttachment(headerText, 15);
        data2.left = new FormAttachment(footerLabel, 5);
        data2.right = new FormAttachment(50, 0);
        footerText.setLayoutData(data2);
        footerText.setEditable(false);
        if (repositoryObject instanceof Element) {
            Element elem = (Element) repositoryObject;
            String id = (String) elem.getElementParameter(EParameterName.HEADERFOOTER_FOOTERID.getName()).getValue();
            IProxyRepositoryFactory proxyFactory = CorePlugin.getDefault().getProxyRepositoryFactory();
            IRepositoryViewObject lastVersion;
            try {
                lastVersion = proxyFactory.getLastVersion(id);
                if (lastVersion != null) {
                    footerText.setText("FOOTER:" + lastVersion.getLabel());
                } else {
                    footerText.setText("FOOTER:<none>");//$NON-NLS-1$
                }
            } catch (PersistenceException e1) {
                ExceptionHandler.process(e1);
            }

        }

        footerButton = widgetFactory.createButton(composite, "...", SWT.PUSH);//$NON-NLS-1$
        FormData footerButtonData = new FormData();
        footerButtonData.top = new FormAttachment(headerText, 15);
        footerButtonData.left = new FormAttachment(footerText, ITabbedPropertyConstants.VSPACE);
        // footerButtonData.right = new FormAttachment(footerText, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        footerButton.setLayoutData(footerButtonData);

        footerButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                if (repositoryObject instanceof IProcess2) {
                    IProcess2 process = (IProcess2) repositoryObject;

                    RepositoryReviewDialog dialog = new RepositoryReviewDialog(Display.getCurrent().getActiveShell(),
                            ERepositoryObjectType.METADATA, false, ERepositoryCategoryType.HEADERFOOTER.getName());
                    if (dialog.open() == RepositoryReviewDialog.OK) {
                        String id = dialog.getResult().getObject().getId();
                        HeaderFooterConnectionItem connectionItem = (HeaderFooterConnectionItem) dialog.getResult().getObject()
                                .getProperty().getItem();

                        Connection connection = connectionItem.getConnection();
                        HeaderFooterConnection headerFooterConnection = (HeaderFooterConnection) connection;

                        boolean isFooter = !(headerFooterConnection.isIsHeader());
                        if (isFooter) {
                            CompoundCommand compoundCommand = new CompoundCommand();
                            footerText.setText("FOOTER:" + connectionItem.getProperty().getLabel());
                            // footerID
                            IElementParameter headerFooterIdParam = process
                                    .getElementParameter(EParameterName.HEADERFOOTER_FOOTERID.getName());
                            if (headerFooterIdParam != null) {
                                headerFooterIdParam.setValue(id);
                                Command command1 = new PropertyChangeCommand(process, EParameterName.HEADERFOOTER_FOOTERID
                                        .getName(), id);
                                compoundCommand.add(command1);
                            }
                            // footerEnable
                            IElementParameter enableParameter = process.getElementParameter(EParameterName.FOOTER_ENABLED
                                    .getName());
                            if (enableParameter != null) {
                                enableParameter.setValue(isFooter);
                                Command command2 = new PropertyChangeCommand(process, EParameterName.FOOTER_ENABLED.getName(),
                                        isFooter);
                                compoundCommand.add(command2);
                            }
                            // footerLibraries
                            IElementParameter libraryParameter = process.getElementParameter(EParameterName.FOOTER_LIBRARY
                                    .getName());
                            if (libraryParameter != null) {
                                libraryParameter.setValue(headerFooterConnection.getLibraries());
                                Command command3 = new PropertyChangeCommand(process, EParameterName.FOOTER_LIBRARY.getName(),
                                        headerFooterConnection.getLibraries());
                                compoundCommand.add(command3);
                            }
                            // footerCode
                            IElementParameter footerCodeParameter = process.getElementParameter(EParameterName.FOOTER_CODE
                                    .getName());
                            if (footerCodeParameter != null) {
                                footerCodeParameter.setValue(headerFooterConnection.getMainCode());
                                Command command4 = new PropertyChangeCommand(process, EParameterName.FOOTER_CODE.getName(),
                                        headerFooterConnection.getMainCode());
                                compoundCommand.add(command4);
                            }
                            // footerImports
                            IElementParameter footerImportParameter = process.getElementParameter(EParameterName.FOOTER_IMPORT
                                    .getName());
                            if (footerImportParameter != null) {
                                footerImportParameter.setValue(headerFooterConnection.getImports());
                                Command command5 = new PropertyChangeCommand(process, EParameterName.FOOTER_IMPORT.getName(),
                                        headerFooterConnection.getImports());
                                compoundCommand.add(command5);
                            }

                            boolean executed = false;
                            if (process != null && process instanceof IGEFProcess) {
                                IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault()
                                        .getDesignerCoreUIService();
                                if (designerCoreUIService != null) {
                                    executed = designerCoreUIService.executeCommand((IGEFProcess) process, compoundCommand);
                                }
                            }
                            if (!executed) {
                                compoundCommand.execute();
                            }

                            copyLibriesToLibJava(headerFooterConnection);

                        }
                    }
                }

            }
        });

        Button footerRemoveButton = widgetFactory.createButton(composite, "clear", SWT.PUSH);//$NON-NLS-1$
        FormData footerRemoveButtonData = new FormData();
        footerRemoveButtonData.top = new FormAttachment(headerText, 15);
        footerRemoveButtonData.left = new FormAttachment(footerButton, 2);
        // footerButtonData.right = new FormAttachment(footerText, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        footerRemoveButton.setLayoutData(footerRemoveButtonData);
        footerRemoveButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (repositoryObject instanceof IProcess2) {
                    IProcess2 process = (IProcess2) repositoryObject;
                    footerText.setText("FOOTER:<none>");//$NON-NLS-1$
                    process.getElementParameter(EParameterName.HEADERFOOTER_FOOTERID.getName()).setValue(null);
                    process.getElementParameter(EParameterName.FOOTER_ENABLED.getName()).setValue(null);
                    process.getElementParameter(EParameterName.FOOTER_LIBRARY.getName()).setValue(null);
                    process.getElementParameter(EParameterName.FOOTER_CODE.getName()).setValue(null);
                    process.getElementParameter(EParameterName.FOOTER_IMPORT.getName()).setValue(null);
                    CompoundCommand compoundCommand = new CompoundCommand();
                    Command command1 = new PropertyChangeCommand(process, EParameterName.HEADERFOOTER_FOOTERID.getName(), null);
                    compoundCommand.add(command1);
                    Command command2 = new PropertyChangeCommand(process, EParameterName.FOOTER_ENABLED.getName(), null);
                    compoundCommand.add(command2);
                    Command command3 = new PropertyChangeCommand(process, EParameterName.FOOTER_LIBRARY.getName(), null);
                    compoundCommand.add(command3);
                    Command command4 = new PropertyChangeCommand(process, EParameterName.FOOTER_CODE.getName(), null);
                    compoundCommand.add(command4);
                    Command command5 = new PropertyChangeCommand(process, EParameterName.FOOTER_IMPORT.getName(), null);
                    compoundCommand.add(command5);

                    boolean executed = false;
                    if (process != null && process instanceof IGEFProcess) {
                        IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
                        if (designerCoreUIService != null) {
                            executed = designerCoreUIService.executeCommand((IGEFProcess) process, compoundCommand);
                        }
                    }
                    if (!executed) {
                        compoundCommand.execute();
                    }

                }
            }
        });

    }

    private void copyLibriesToLibJava(HeaderFooterConnection headerFooterConnection) {
        String libraries = headerFooterConnection.getLibraries();
        String librariesPath = LibrariesManagerUtils.getLibrariesPath(ECodeLanguage.JAVA);

        if (librariesPath != null && libraries != null && !"".equals(libraries)) {//$NON-NLS-1$
            if (libraries.contains(";")) {//$NON-NLS-1$
                String[] split = libraries.split(";");//$NON-NLS-1$
                for (String element : split) {
                    File source = new File(element);
                    File target = new File(librariesPath + File.separatorChar
                            + element.substring(element.lastIndexOf(File.separatorChar)));
                    try {
                        FilesUtils.copyFile(source, target);
                    } catch (IOException e1) {
                        ExceptionHandler.process(e1);
                    }
                }
            } else {
                File source = new File(libraries);
                File target = new File(librariesPath + File.separatorChar
                        + libraries.substring(libraries.lastIndexOf(File.separatorChar)));
                try {
                    FilesUtils.copyFile(source, target);
                } catch (IOException e1) {
                    ExceptionHandler.process(e1);
                }
            }
        }
    }
}
