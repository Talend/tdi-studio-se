// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.User;
import org.talend.core.model.properties.impl.JobletProcessItemImpl;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryEditorInput;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.repository.model.ItemReferenceBean;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.seeker.RepositorySeekerManager;
import org.talend.core.repository.ui.actions.DeleteActionCache;
import org.talend.core.repository.ui.dialog.ItemReferenceDialog;
import org.talend.core.repository.utils.ConvertJobsUtil;
import org.talend.core.repository.utils.ConvertJobsUtil.JobType;
import org.talend.core.repository.utils.RepositoryNodeDeleteManager;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.editor.IJobEditorHandler;
import org.talend.core.ui.editor.JobEditorHandlerManager;
import org.talend.core.ui.properties.tab.HorizontalTabFactory;
import org.talend.core.ui.properties.tab.TalendPropertyTabDescriptor;
import org.talend.core.utils.KeywordsValidator;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.ui.properties.StatusHelper;
import org.talend.repository.ui.views.IJobSettingsView;

/**
 * yzhang class global comment. Detailled comment
 */
public class MainComposite extends AbstractTabComposite {

    private boolean enableControl;

    private HorizontalTabFactory tabFactory;

    private Text nameText;

    private Text versionText;

    private CCombo jobTypeCCombo;

    private CCombo jobFrameworkCCombo;

    private Text purposeText;

    private CCombo statusText;

    private Text descriptionText;

    private Button btnDown;

    private Button btnUp;

    private Button btnConfirm;

    private String jobTypeValue = null;

    private String frameworkValue = null;

    private String lastVersionFound;

    private boolean allowEnableControl;

    private ControlDecoration nameTextDecorator;

    private IRepositoryViewObject newRepositoryObject = null;

    protected StatusHelper statusHelper = null;

    protected String statusLabelText = null;

    protected final Map<String, String> statusMap;

    public MainComposite(Composite parent, int style, HorizontalTabFactory tabFactory, IRepositoryViewObject obj) {
        this(parent, style, tabFactory.getWidgetFactory(), obj);
        this.tabFactory = tabFactory;
    }

    /**
     * yzhang MainComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public MainComposite(Composite parent, int style, TabbedPropertySheetWidgetFactory factory, IRepositoryViewObject obj) {
        super(parent, style, factory, obj);

        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        boolean allowVerchange = brandingService.getBrandingConfiguration().isAllowChengeVersion();
        allowEnableControl = false;
        enableControl = false;
        String framework = null;
        if (obj instanceof RepositoryViewObject || obj instanceof IProcess2) {
            if (obj instanceof RepositoryViewObject) {
                framework = ((RepositoryViewObject) obj).getFramework();
            } else {
                framework = (String) ((IProcess2) obj).getAdditionalProperties().get(ConvertJobsUtil.FRAMEWORK);
            }
            if (ERepositoryObjectType.getAllTypesOfProcess().contains(repositoryObject.getRepositoryObjectType())) {
                allowEnableControl = true;
                enableControl = true;
                if (!PluginChecker.isMapReducePluginLoader() && !PluginChecker.isStormPluginLoader()) {
                    allowEnableControl = false;
                }
                Item originalItem = repositoryObject.getProperty().getItem();
                if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                    ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                            .getService(ICamelDesignerCoreService.class);
                    if (camelService != null && camelService.isInstanceofCamel(originalItem)) {
                        allowEnableControl = false;
                        enableControl = false;
                    }
                }
            }
        }

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

        final int labelWidth = getLabelWidth();
        nameText = widgetFactory.createText(composite, ""); //$NON-NLS-1$
        FormData data = new FormData();
        data.left = new FormAttachment(0, labelWidth);
        data.right = new FormAttachment(50, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        nameText.setLayoutData(data);
        String nameContent = repositoryObject.getLabel();
        nameText.setText(nameContent != null ? nameContent : ""); //$NON-NLS-1$
        nameText.setEnabled(allowEnableControl);

        // Adding the decorator for nameText
        nameTextDecorator = new ControlDecoration(nameText, SWT.TOP | SWT.RIGHT);
        FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                FieldDecorationRegistry.DEC_ERROR);
        nameTextDecorator.setImage(fieldDecoration.getImage());
        nameTextDecorator.hide();

        CLabel nameLabel = widgetFactory.createCLabel(composite, Messages.getString("MainComposite.NameSection.Name")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(nameText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(nameText, 0, SWT.CENTER);
        nameLabel.setLayoutData(data);

        Text authorText = widgetFactory.createText(composite, ""); //$NON-NLS-1$
        authorText.setEnabled(false);
        data = new FormData();
        data.left = new FormAttachment(0, labelWidth);
        data.right = new FormAttachment(50, 0);
        data.top = new FormAttachment(nameLabel, ITabbedPropertyConstants.VSPACE);
        authorText.setLayoutData(data);
        User user = repositoryObject.getAuthor();
        if (user != null) {
            String author = user.getLogin();
            authorText.setText(author != null ? author : ""); //$NON-NLS-1$
        } else {
            authorText.setText(nameContent);
        }
        authorText.setEnabled(false);

        CLabel authorLabel = widgetFactory.createCLabel(composite,
                Messages.getString("MainComposite.VersionAuthorSection.authorLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(authorText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(authorText, 0, SWT.CENTER);
        authorLabel.setLayoutData(data);

        if (allowVerchange) {
            btnDown = widgetFactory.createButton(composite, "m", SWT.PUSH); //$NON-NLS-1$
            data = new FormData();
            data.right = new FormAttachment(100, 0);
            data.top = new FormAttachment(authorText, 0, SWT.CENTER);
            btnDown.setLayoutData(data);
            btnDown.setEnabled(allowEnableControl);

            btnUp = widgetFactory.createButton(composite, "M", SWT.PUSH); //$NON-NLS-1$
            data = new FormData();
            data.right = new FormAttachment(btnDown, 0);
            data.top = new FormAttachment(authorText, 0, SWT.CENTER);
            btnUp.setLayoutData(data);
            btnUp.setEnabled(allowEnableControl);

            versionText = widgetFactory.createText(composite, ""); //$NON-NLS-1$
            data = new FormData();
            data.left = new FormAttachment(authorText, labelWidth);
            data.right = new FormAttachment(btnUp, -2);
            data.top = new FormAttachment(authorText, 0, SWT.CENTER);
            versionText.setLayoutData(data);
            String version = repositoryObject.getVersion();
            versionText.setText(version != null ? version : ""); //$NON-NLS-1$
            versionText.setEditable(false);

            CLabel versionLabel = widgetFactory.createCLabel(composite,
                    Messages.getString("MainComposite.VersionAuthorSection.versionLabel")); //$NON-NLS-1$
            data = new FormData();
            data.left = new FormAttachment(authorText, ITabbedPropertyConstants.HSPACE - 10);
            data.right = new FormAttachment(versionText, -ITabbedPropertyConstants.HSPACE);
            data.top = new FormAttachment(versionText, 0, SWT.CENTER);
            versionLabel.setLayoutData(data);
        }

        Text creationDate = widgetFactory.createText(composite, ""); //$NON-NLS-1$
        data = new FormData();

        data.left = new FormAttachment(0, labelWidth);
        data.right = new FormAttachment(50, 0);
        data.top = new FormAttachment(authorLabel, ITabbedPropertyConstants.VSPACE);
        creationDate.setLayoutData(data);
        creationDate.setEnabled(false);
        Date cDate = repositoryObject.getCreationDate();
        creationDate.setText(cDate != null ? FORMATTER.format(cDate) : ""); //$NON-NLS-1$
        creationDate.setEnabled(enableControl);

        CLabel creationLabel = widgetFactory.createCLabel(composite,
                Messages.getString("MainComposite.DateSection.creationLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(creationDate, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(creationDate, 0, SWT.CENTER);
        creationLabel.setLayoutData(data);

        Text modificationDate = widgetFactory.createText(composite, ""); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(creationDate, labelWidth);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(authorLabel, ITabbedPropertyConstants.VSPACE);
        modificationDate.setLayoutData(data);
        modificationDate.setEnabled(false);
        Date mDate = repositoryObject.getModificationDate();
        modificationDate.setText(mDate != null ? FORMATTER.format(mDate) : ""); //$NON-NLS-1$
        modificationDate.setEnabled(enableControl);

        CLabel modificationLabel = widgetFactory.createCLabel(composite,
                Messages.getString("MainComposite.DateSection.ModificationLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(creationDate, ITabbedPropertyConstants.HSPACE - 10);
        data.right = new FormAttachment(modificationDate, ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(modificationDate, 0, SWT.CENTER);
        modificationLabel.setLayoutData(data);

        statusMap = getStatusMap();
        if (allowEnableControl) {
            // Job Type
            jobTypeCCombo = widgetFactory.createCCombo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
            data = new FormData();
            data.left = new FormAttachment(0, labelWidth);
            data.right = new FormAttachment(50, 0);
            data.top = new FormAttachment(creationDate, ITabbedPropertyConstants.VSPACE);
            jobTypeCCombo.setLayoutData(data);
            jobTypeCCombo.setItems(JobType.getJobTypeToDispaly());
            String jobType = ConvertJobsUtil.getJobTypeFromFramework(framework);
            jobTypeCCombo.setText(jobType);
            jobTypeValue = jobTypeCCombo.getText();
            jobTypeCCombo.setEnabled(allowEnableControl);

            String type = Messages.getString("MainComposite.JobTypeSection.jobTypeLabel"); //$NON-NLS-1$
            if (obj.getProperty().getItem() instanceof JobletProcessItem) {
                type = Messages.getString("MainComposite.JobTypeSection.jobletTypeLabel"); //$NON-NLS-1$
            }
            CLabel jobTypeLabel = widgetFactory.createCLabel(composite, type);
            data = new FormData();
            data.left = new FormAttachment(0, 0);
            data.right = new FormAttachment(jobTypeCCombo, -ITabbedPropertyConstants.HSPACE);
            data.top = new FormAttachment(jobTypeCCombo, 0, SWT.CENTER);
            jobTypeLabel.setLayoutData(data);

            // Job Framework
            jobFrameworkCCombo = widgetFactory.createCCombo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
            data = new FormData();
            data.left = new FormAttachment(jobTypeCCombo, labelWidth);
            data.right = new FormAttachment(100, 0);
            data.top = new FormAttachment(creationDate, ITabbedPropertyConstants.VSPACE);
            jobFrameworkCCombo.setLayoutData(data);
            jobFrameworkCCombo.setItems(ConvertJobsUtil.getFrameworkItemsByJobType(jobType,
                    (obj.getProperty().getItem() instanceof JobletProcessItem)));
            framework = ConvertJobsUtil.convertFrameworkByJobType(jobType, framework, true);
            jobFrameworkCCombo.setText(framework != null ? framework : ""); //$NON-NLS-1$
            frameworkValue = jobFrameworkCCombo.getText();
            jobFrameworkCCombo.setEnabled(allowEnableControl);

            CLabel jobFrameworkLabel = widgetFactory.createCLabel(composite,
                    Messages.getString("MainComposite.JobFrameworkSection.jobFrameworkLabel")); //$NON-NLS-1$
            data = new FormData();
            data.left = new FormAttachment(jobTypeCCombo, ITabbedPropertyConstants.HSPACE - 10);
            data.right = new FormAttachment(jobFrameworkCCombo, ITabbedPropertyConstants.HSPACE + 1);
            data.top = new FormAttachment(jobFrameworkCCombo, 0, SWT.CENTER);
            jobFrameworkLabel.setLayoutData(data);

            purposeText = widgetFactory.createText(composite, ""); //$NON-NLS-1$
            data = new FormData();
            data.left = new FormAttachment(0, labelWidth);
            data.right = new FormAttachment(50, 0);
            data.top = new FormAttachment(jobTypeLabel, ITabbedPropertyConstants.VSPACE);
            purposeText.setLayoutData(data);
            String content = repositoryObject.getPurpose();
            purposeText.setText(content != null ? content : ""); //$NON-NLS-1$
            purposeText.setEnabled(allowEnableControl);

            CLabel purposeLabel = widgetFactory.createCLabel(composite,
                    Messages.getString("MainComposite.PurposeStatusSection.purposeLabel")); //$NON-NLS-1$
            data = new FormData();
            data.left = new FormAttachment(0, 0);
            data.right = new FormAttachment(purposeText, -ITabbedPropertyConstants.HSPACE);
            data.top = new FormAttachment(purposeText, 0, SWT.CENTER);
            purposeLabel.setLayoutData(data);

            statusText = widgetFactory.createCCombo(composite, SWT.READ_ONLY);
            data = new FormData();
            data.left = new FormAttachment(purposeText, labelWidth);
            data.right = new FormAttachment(100, 0);
            data.top = new FormAttachment(jobTypeLabel, ITabbedPropertyConstants.VSPACE);
            statusText.setLayoutData(data);
            String status = repositoryObject.getStatusCode();
            statusText.setText(status != null ? status : ""); //$NON-NLS-1$
            statusText.setItems(statusMap.values().toArray(new String[statusMap.values().size()]));
            statusLabelText = statusMap.get(status);
            setStatusComboText(statusLabelText);
            statusText.setEnabled(allowEnableControl);
        } else {
            purposeText = widgetFactory.createText(composite, ""); //$NON-NLS-1$
            data = new FormData();
            data.left = new FormAttachment(0, labelWidth);
            data.right = new FormAttachment(50, 0);
            data.top = new FormAttachment(creationDate, ITabbedPropertyConstants.VSPACE);
            purposeText.setLayoutData(data);
            String content = repositoryObject.getPurpose();
            purposeText.setText(content != null ? content : ""); //$NON-NLS-1$
            purposeText.setEnabled(enableControl);

            CLabel purposeLabel = widgetFactory.createCLabel(composite,
                    Messages.getString("MainComposite.PurposeStatusSection.purposeLabel")); //$NON-NLS-1$
            data = new FormData();
            data.left = new FormAttachment(0, 0);
            data.right = new FormAttachment(purposeText, -ITabbedPropertyConstants.HSPACE);
            data.top = new FormAttachment(purposeText, 0, SWT.CENTER);
            purposeLabel.setLayoutData(data);

            statusText = widgetFactory.createCCombo(composite, SWT.READ_ONLY);
            data = new FormData();
            data.left = new FormAttachment(purposeText, labelWidth);
            data.right = new FormAttachment(100, 0);
            data.top = new FormAttachment(creationDate, ITabbedPropertyConstants.VSPACE);
            statusText.setLayoutData(data);
            String status = repositoryObject.getStatusCode();
            statusText.setText(status != null ? status : ""); //$NON-NLS-1$
            statusLabelText = statusMap.get(status);
            setStatusComboText(statusLabelText);
            statusText.setEnabled(enableControl);
        }

        CLabel statusLabel = widgetFactory.createCLabel(composite,
                Messages.getString("MainComposite.PurposeStatusSection.statusLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(purposeText, ITabbedPropertyConstants.HSPACE - 10);
        data.right = new FormAttachment(statusText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(statusText, 0, SWT.CENTER);
        statusLabel.setLayoutData(data);
        statusText.setItems(statusMap.values().toArray(new String[statusMap.values().size()]));
        String status = repositoryObject.getStatusCode();
        statusLabelText = statusMap.get(status);
        setStatusComboText(statusLabelText);

        descriptionText = widgetFactory.createText(composite, "", SWT.MULTI | SWT.V_SCROLL | SWT.WRAP); //$NON-NLS-1$
        FormData descriptionData = new FormData();
        descriptionData.left = new FormAttachment(0, labelWidth);
        descriptionData.right = new FormAttachment(100, 0);
        descriptionData.top = new FormAttachment(statusText, ITabbedPropertyConstants.VSPACE);
        descriptionData.height = NB_LINES * descriptionText.getLineHeight();
        descriptionText.setLayoutData(descriptionData);
        if (!allowEnableControl) {
            descriptionData.bottom = new FormAttachment(100);
        }

        String description = repositoryObject.getDescription();
        descriptionText.setText(description != null ? description : ""); //$NON-NLS-1$
        descriptionText.setEnabled(true);
        descriptionText.setEditable(allowEnableControl);

        CLabel descriptionLabel = widgetFactory.createCLabel(composite,
                Messages.getString("MainComposite.DescriptionSection.Label")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(descriptionText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(descriptionText, 0, SWT.TOP);
        descriptionLabel.setLayoutData(data);

        if (allowEnableControl) {
            btnConfirm = widgetFactory.createButton(composite, "Confirm", SWT.PUSH); //$NON-NLS-1$
            descriptionData.bottom = new FormAttachment(btnConfirm, 0, SWT.TOP);
            data = new FormData();
            data.right = new FormAttachment(descriptionText, ITabbedPropertyConstants.HSPACE - 1, SWT.RIGHT);
            data.bottom = new FormAttachment(100);
            btnConfirm.setLayoutData(data);
            btnConfirm.setEnabled(false);

            // addListener
            nameText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent e) {
                    evaluateTextField();
                }
            });
            jobTypeCCombo.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent e) {
                    ConvertJobsUtil.updateJobFrameworkPart(jobTypeCCombo.getText(), jobFrameworkCCombo, (obj.getProperty()
                            .getItem() instanceof JobletProcessItem));
                    evaluateTextField();
                }
            });

            jobFrameworkCCombo.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent e) {
                    evaluateTextField();
                }
            });

            purposeText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent e) {
                    evaluateTextField();
                }
            });

            statusText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent e) {
                    evaluateTextField();
                }
            });
            descriptionText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent e) {
                    evaluateTextField();
                }
            });

            if (allowVerchange) {
                btnUp.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        String version = repositoryObject.getVersion();
                        if (lastVersionFound != null && VersionUtils.compareTo(lastVersionFound, version) > 0) {
                            version = lastVersionFound;
                        }
                        version = VersionUtils.upMajor(version);
                        versionText.setText(version);
                        lastVersionFound = version;
                        evaluateTextField();
                    }
                });

                btnDown.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        String version = repositoryObject.getVersion();
                        if (lastVersionFound != null && VersionUtils.compareTo(lastVersionFound, version) > 0) {
                            version = lastVersionFound;
                        }
                        version = VersionUtils.upMinor(version);
                        versionText.setText(version);
                        lastVersionFound = version;
                        evaluateTextField();
                    }
                });

                btnConfirm.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        final IProxyRepositoryFactory proxyRepositoryFactory = CoreRuntimePlugin.getInstance()
                                .getProxyRepositoryFactory();
                        Property property = repositoryObject.getProperty();
                        if (nameText == null || nameText.isDisposed() || versionText == null || versionText.isDisposed()
                                || purposeText == null || purposeText.isDisposed() || jobTypeCCombo == null
                                || jobTypeCCombo.isDisposed() || jobFrameworkCCombo == null || jobFrameworkCCombo.isDisposed()
                                || statusText == null || statusText.isDisposed() || descriptionText == null
                                || descriptionText.isDisposed() || btnConfirm == null || btnConfirm.isDisposed()
                                || property == null) {
                            return;
                        }
                        String oldVersion = repositoryObject.getVersion();
                        String originalName = nameText.getText();
                        String originalJobType = jobTypeCCombo.getText();
                        String originalFramework = ConvertJobsUtil.convertFrameworkByJobType(originalJobType,
                                jobFrameworkCCombo.getText(), false);
                        String originalversion = versionText.getText();
                        String originalPurpose = purposeText.getText();
                        String originalStatus = statusText.getText();
                        String originalDescription = descriptionText.getText();

                        String newJobName = null;
                        String oldName = StringUtils.trimToEmpty(repositoryObject.getLabel());
                        if (!originalName.equals(StringUtils.trimToEmpty(repositoryObject.getLabel()))) {
                            newJobName = originalName;
                        }
                        boolean jobletModified = false;
                        if (newJobName != null) {
                            if (repositoryObject.getProperty().getItem() instanceof JobletProcessItemImpl) {
                                if (isRelatedJobsLocked()) {
                                    MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                            Messages.getString("JobletPropertiesWizard.renameWarning.title"), //$NON-NLS-1$
                                            Messages.getString("JobletPropertiesWizard.renameWarning.msg")); //$NON-NLS-1$
                                    return;
                                }
                                jobletModified = true;
                            }
                        }

                        saveJobEditor(repositoryObject);

                        if (repositoryObject instanceof IProcess2) {
                            IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

                                @Override
                                public void run(final IProgressMonitor monitor) throws CoreException {
                                    DeleteActionCache.getInstance().closeOpenedEditor(repositoryObject);
                                }
                            };
                            // unlockObject();
                            // alreadyEditedByUser = true; // to avoid 2 calls of unlock
                            IWorkspace workspace = ResourcesPlugin.getWorkspace();
                            try {
                                ISchedulingRule schedulingRule = workspace.getRoot();
                                // the update the project files need to be done in the workspace runnable to avoid all
                                // notification
                                // of changes before the end of the modifications.
                                workspace.run(runnable, schedulingRule, IWorkspace.AVOID_UPDATE, null);
                            } catch (CoreException e1) {
                                MessageBoxExceptionHandler.process(e1.getCause());
                            }
                        }

                        if (!originalversion.equals(StringUtils.trimToEmpty(repositoryObject.getVersion()))) {
                            property.setVersion(originalversion);
                        }
                        if (!originalPurpose.equals(StringUtils.trimToEmpty(repositoryObject.getPurpose()))) {
                            property.setPurpose(originalPurpose);
                        }
                        if (!originalStatus.equals(StringUtils.trimToEmpty(repositoryObject.getStatusCode()))) {
                            property.setStatusCode(getStatusCode(statusMap, originalStatus));
                        }
                        if (!originalDescription.equals(StringUtils.trimToEmpty(repositoryObject.getDescription()))) {
                            property.setDescription(originalDescription);
                        }
                        Item originalItem = repositoryObject.getProperty().getItem();
                        if (ConvertJobsUtil.isNeedConvert(originalItem, originalJobType, originalFramework, true)) {
                            boolean hasTestCase = ConvertJobsUtil.hasTestCase(repositoryObject.getProperty());
                            final List<ItemReferenceBean> unDeleteItems = RepositoryNodeDeleteManager.getInstance()
                                    .getUnDeleteItems(repositoryObject, null, true);
                            if (!unDeleteItems.isEmpty()) {
                                ItemReferenceDialog dialog = new ItemReferenceDialog(PlatformUI.getWorkbench()
                                        .getActiveWorkbenchWindow().getShell(), unDeleteItems);
                                dialog.open();
                                return;
                            }
                            // Convert
                            final Item newItem = ConvertJobsUtil.createOperation(originalName, originalJobType,
                                    originalFramework, repositoryObject);
                            if (newItem != null) {
                                ConvertJobsUtil.convertTestcases(newItem, repositoryObject, originalJobType);
                            }
                            RepositoryWorkUnit repositoryWorkUnit = new RepositoryWorkUnit("Convert job") { //$NON-NLS-1$

                                @Override
                                public void run() throws PersistenceException {
                                    IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

                                        @Override
                                        public void run(final IProgressMonitor monitor) throws CoreException {
                                            try {
                                                boolean isOpened = false;
                                                if (repositoryObject instanceof IProcess2) {
                                                    isOpened = true;
                                                    boolean locked = proxyRepositoryFactory.getStatus(repositoryObject) == ERepositoryStatus.LOCK_BY_USER;
                                                    if (locked) {
                                                        proxyRepositoryFactory.unlock(repositoryObject);
                                                    }
                                                }

                                                boolean isNewItemCreated = true;
                                                Property repositoryProperty = repositoryObject.getProperty();
                                                if (repositoryProperty != null) {
                                                    isNewItemCreated = (repositoryProperty.getItem() != newItem);
                                                }
                                                if (isNewItemCreated) {
                                                    // new Item created
                                                    proxyRepositoryFactory.deleteObjectPhysical(repositoryObject);
                                                    proxyRepositoryFactory.saveProject(ProjectManager.getInstance()
                                                            .getCurrentProject());
                                                } else if (repositoryObject.getProperty() != null) {
                                                    proxyRepositoryFactory.save(ProjectManager.getInstance().getCurrentProject(),
                                                            repositoryObject.getProperty().getItem(), false);
                                                }
                                                if (newItem != null && newItem.getProperty() != null) {
                                                    String newId = newItem.getProperty().getId();
                                                    if (isNewItemCreated) {
                                                        newRepositoryObject = proxyRepositoryFactory.getLastVersion(
                                                                ProjectManager.getInstance().getCurrentProject(), newId);
                                                        if (tabFactory != null) {
                                                            IRepositoryNode newRepoViewNode = RepositorySeekerManager
                                                                    .getInstance().searchRepoViewNode(newId);
                                                            if (newRepoViewNode != null) {
                                                                TalendPropertyTabDescriptor selection = tabFactory.getSelection();
                                                                if (selection != null) {
                                                                    selection.setData(newRepoViewNode.getObject());
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        newRepositoryObject = repositoryObject;
                                                    }
                                                    if (!isOpened) {
                                                        repositoryObject = newRepositoryObject;
                                                        if (!jobTypeCCombo.isDisposed() && !jobFrameworkCCombo.isDisposed()) {
                                                            jobTypeValue = jobTypeCCombo.getText();
                                                            frameworkValue = jobFrameworkCCombo.getText();
                                                        }
                                                    }
                                                }

                                            } catch (PersistenceException e1) {
                                                e1.printStackTrace();
                                            } catch (LoginException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    // unlockObject();
                                    // alreadyEditedByUser = true; // to avoid 2 calls of unlock

                                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                                    try {
                                        ISchedulingRule schedulingRule = workspace.getRoot();
                                        // the update the project files need to be done in the workspace runnable to
                                        // avoid all
                                        // notification
                                        // of changes before the end of the modifications.
                                        workspace.run(runnable, schedulingRule, IWorkspace.AVOID_UPDATE, null);
                                    } catch (CoreException e1) {
                                        MessageBoxExceptionHandler.process(e1.getCause());
                                    }
                                }
                            };
                            repositoryWorkUnit.setAvoidSvnUpdate(true);
                            repositoryWorkUnit.setAvoidUnloadResources(true);
                            ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);
                            if (newItem != null && repositoryObject instanceof IProcess2) {
                                if (newRepositoryObject != null && newRepositoryObject.getProperty() != null) {
                                    openEditorOperation(newRepositoryObject.getProperty().getItem());
                                }
                            }
                        } else {
                            // set the new value
                            if (newJobName != null) {
                                property.setLabel(newJobName);
                                property.setDisplayName(newJobName);
                            }
                            if (originalFramework != null) {
                                if (property.getAdditionalProperties().containsKey(ConvertJobsUtil.FRAMEWORK)) {
                                    property.getAdditionalProperties().removeKey(ConvertJobsUtil.FRAMEWORK);
                                }
                                property.getAdditionalProperties().put(ConvertJobsUtil.FRAMEWORK, originalFramework);
                            }
                            final String newName = newJobName;
                            final boolean needjobletRelateUpdate = jobletModified;
                            RepositoryWorkUnit repositoryWorkUnit = new RepositoryWorkUnit("Convert job") { //$NON-NLS-1$
                                @Override
                                public void run() throws PersistenceException {
                                    IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                                        @Override
                                        public void run(final IProgressMonitor monitor) throws CoreException {
                                            try {
                                                if (repositoryObject.getProperty() != null) {
                                                	if (!originalversion.equals(StringUtils.trimToEmpty(oldVersion))) {
                                                        RelationshipItemBuilder.getInstance().addOrUpdateItem(repositoryObject.getProperty().getItem());
                                                    }
                                                    proxyRepositoryFactory.save(ProjectManager.getInstance().getCurrentProject(),
                                                            repositoryObject.getProperty(), oldName, oldVersion);
                                                    if (needjobletRelateUpdate && GlobalServiceRegister.getDefault()
                                                            .isServiceRegistered(IJobletProviderService.class)) {
                                                        IJobletProviderService jobletService = (IJobletProviderService) GlobalServiceRegister
                                                                .getDefault().getService(IJobletProviderService.class);
                                                        jobletService.updateJobleModifiedRelated(repositoryObject.getProperty().getItem(), oldName,
                                                                newName);
                                                        proxyRepositoryFactory
                                                                .saveProject(ProjectManager.getInstance().getCurrentProject());
                                                    }
                                                }
                                            } catch (PersistenceException e1) {
                                                ExceptionHandler.process(e1);
                                            }
                                        }
                                    };
                                    // unlockObject();
                                    // alreadyEditedByUser = true; // to avoid 2 calls of unlock
                                    
                                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                                    try {
                                        ISchedulingRule schedulingRule = workspace.getRoot();
                                        // the update the project files need to be done in the workspace runnable to
                                        // avoid all
                                        // notification
                                        // of changes before the end of the modifications.
                                        workspace.run(runnable, schedulingRule, IWorkspace.AVOID_UPDATE, null);
                                    } catch (CoreException e1) {
                                        MessageBoxExceptionHandler.process(e1.getCause());
                                    }
                                }
                            };
                            repositoryWorkUnit.setAvoidSvnUpdate(true);
                            repositoryWorkUnit.setAvoidUnloadResources(true);
                            ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);

                            if (repositoryObject instanceof IProcess2) {
                                Item item = property.getItem();
                                if (needjobletRelateUpdate) {
                                    try {
                                        repositoryObject = proxyRepositoryFactory.getLastVersion(
                                                ProjectManager.getInstance().getCurrentProject(),
                                                repositoryObject.getProperty().getId());
                                        item = repositoryObject.getProperty().getItem();
                                    } catch (PersistenceException e1) {
                                        ExceptionHandler.process(e1);
                                    }
                                }
                                openEditorOperation(item);
                            }
                        }

                        IViewPart jobSettingView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                .findView(IJobSettingsView.ID);
                        if (jobSettingView != null && jobSettingView instanceof IJobSettingsView
                                && !(repositoryObject instanceof IProcess2)) {
                            Map<String, Object> propertiesMap = new HashMap<String, Object>();
                            propertiesMap.put(IJobSettingsView.JOBTYPE_CHANGED, repositoryObject);
                            ((IJobSettingsView) jobSettingView).onPropertiesChanged(propertiesMap);
                        }

                        if (!btnConfirm.isDisposed()) {
                            btnConfirm.setEnabled(false);
                        }
                    }
                });
            }
        }
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        final ERepositoryStatus repositoryStatus = ProxyRepositoryFactory.getInstance().getStatus(obj);
        if (repositoryFactory.isUserReadOnlyOnCurrentProject() || repositoryStatus == ERepositoryStatus.DELETED
                || repositoryStatus == ERepositoryStatus.LOCK_BY_OTHER
                || !ProjectManager.getInstance().isInCurrentMainProject(obj.getProperty())) {
            parent.setEnabled(false);
        } else {
            parent.setEnabled(true);
        }
    }

    private boolean isRelatedJobsLocked() {
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        List<Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(repositoryObject.getProperty().getId(),
                RelationshipItemBuilder.LATEST_VERSION, RelationshipItemBuilder.JOBLET_RELATION);
        try {
            for (Relation relation : relations) {
                for (IRepositoryViewObject repObj : ProxyRepositoryFactory.getInstance().getAllVersion(relation.getId())) {
                    ERepositoryStatus status = repositoryFactory.getStatus(repObj);
                    if (ERepositoryStatus.LOCK_BY_USER.equals(status) || ERepositoryStatus.LOCK_BY_OTHER.equals(status)) {
                        return true;
                    }
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        return false;
    }

    private int getLabelWidth() {
        Composite parent = getParent();
        Label testFontLabel = null;
        int maxWidth = AbstractPropertySection.STANDARD_LABEL_WIDTH;
        try {
            String[] labels = new String[] { Messages.getString("MainComposite.NameSection.Name"), //$NON-NLS-1$
                    Messages.getString("MainComposite.VersionAuthorSection.authorLabel"), //$NON-NLS-1$
                    Messages.getString("MainComposite.VersionAuthorSection.versionLabel"), //$NON-NLS-1$
                    Messages.getString("MainComposite.DateSection.creationLabel"), //$NON-NLS-1$
                    Messages.getString("MainComposite.DateSection.ModificationLabel"), //$NON-NLS-1$
                    Messages.getString("MainComposite.JobTypeSection.jobTypeLabel"), //$NON-NLS-1$
                    Messages.getString("MainComposite.JobTypeSection.jobletTypeLabel"), //$NON-NLS-1$
                    Messages.getString("MainComposite.JobFrameworkSection.jobFrameworkLabel"), //$NON-NLS-1$
                    Messages.getString("MainComposite.PurposeStatusSection.purposeLabel"), //$NON-NLS-1$
                    Messages.getString("MainComposite.PurposeStatusSection.statusLabel"), //$NON-NLS-1$
                    Messages.getString("MainComposite.DescriptionSection.Label") //$NON-NLS-1$
            };
            testFontLabel = new Label(parent, SWT.NONE);
            for (String label : labels) {
                testFontLabel.setText(label);
                Point testFontSize = testFontLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                if (maxWidth < testFontSize.x) {
                    maxWidth = testFontSize.x;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            if (testFontLabel != null) {
                testFontLabel.dispose();
            }
        }
        Point size = parent.getSize();
        if (size.x / 4 < maxWidth) {
            maxWidth = size.x / 4;
        }
        return maxWidth;
    }

    private void saveJobEditor(IRepositoryViewObject objToSave) {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
            for (IEditorReference editors : page.getEditorReferences()) {
                IEditorPart editor = editors.getEditor(false);
                if (editor != null && editor.isDirty()) {
                    IEditorInput editorInput = editor.getEditorInput();
                    if (editorInput != null && editorInput instanceof IRepositoryEditorInput) {
                        Item item = ((IRepositoryEditorInput) editorInput).getItem();
                        if (item != null) {
                            String id = item.getProperty().getId();
                            if (objToSave.getId() != null && objToSave.getId().equals(id)) {
                                page.saveEditor(editor, false);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    protected Map<String, String> getStatusMap() {
        statusHelper = new StatusHelper(CoreRuntimePlugin.getInstance().getProxyRepositoryFactory());
        Map<String, String> statusMap = new LinkedHashMap();
        try {
            if (statusHelper != null) {
                List<org.talend.core.model.properties.Status> statusList = statusHelper.getStatusList(repositoryObject
                        .getProperty());
                if (statusList != null) {
                    for (org.talend.core.model.properties.Status s : statusList) {
                        statusMap.put(s.getCode(), s.getLabel());
                    }
                }
            }
        } catch (PersistenceException e) {
            CommonExceptionHandler.process(e);
        }
        return statusMap;
    }

    protected String getStatusCode(Map<String, String> map, String statusLabel) {
        String key = null;
        for (Object statusCode : map.keySet()) {
            if (map != null && map.get(statusCode).equals(statusLabel)) {
                key = (String) statusCode;
            }
        }
        return key;
    }

    protected void setStatusComboText(String statusLabel) {
        if (statusLabel == null) {
            statusText.setText("");
            return;
        }
        int i = 0;
        for (String s : statusMap.values()) {
            if (statusLabel != null && !statusLabel.equals(s)) {
                i++;
            } else {
                break;
            }
        }
        statusText.select(i);
    }

    protected void evaluateTextField() {
        IProxyRepositoryFactory proxyRepositoryFactory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        if (nameText == null || nameText.isDisposed() || versionText == null || versionText.isDisposed() || purposeText == null
                || purposeText.isDisposed() || jobTypeCCombo == null || jobTypeCCombo.isDisposed() || jobFrameworkCCombo == null
                || jobFrameworkCCombo.isDisposed() || statusText == null || statusText.isDisposed() || descriptionText == null
                || descriptionText.isDisposed() || btnConfirm == null || btnConfirm.isDisposed()) {
            return;
        }
        nameTextDecorator.hide();
        if (!btnConfirm.isDisposed() && !nameText.getText().equals(StringUtils.trimToEmpty(repositoryObject.getLabel()))) {
            boolean isValid = true;
            String errorMessage = null;
            if (nameText.getText().length() == 0) {
                errorMessage = Messages.getString("MainComposite.NameEmptyError"); //$NON-NLS-1$
                isValid = false;
            } else if (nameText.getText().startsWith(" ")//$NON-NLS-1$
                    || !Pattern.matches(RepositoryConstants.getPattern(repositoryObject.getRepositoryObjectType()),
                            nameText.getText()) || nameText.getText().trim().contains(" ")) { //$NON-NLS-1$
                errorMessage = Messages.getString("MainComposite.NameFormatError"); //$NON-NLS-1$
                isValid = false;
            } else if (JavaConventions.validateClassFileName(nameText.getText() + ".class",//$NON-NLS-1$
                    JavaCore.getOption(JavaCore.COMPILER_SOURCE), JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE)).getSeverity() == IStatus.ERROR
                    || KeywordsValidator.isKeyword(nameText.getText())) {
                errorMessage = Messages.getString("MainComposite.KeywordsError"); //$NON-NLS-1$
                isValid = false;
            } else if (nameText.getText().equalsIgnoreCase(ProjectManager.getInstance().getCurrentProject().getLabel())) {
                errorMessage = Messages.getString("MainComposite.SameAsProjectname");//$NON-NLS-1$
                isValid = false;
            } else {
                try {
                    List<IRepositoryViewObject> listExistingObjects = proxyRepositoryFactory.getAll(
                            ERepositoryObjectType.PROCESS, true, false);
                    if (PluginChecker.isStormPluginLoader()) {
                        listExistingObjects.addAll(proxyRepositoryFactory
                                .getAll(ERepositoryObjectType.PROCESS_STORM, true, false));
                    }
                    if (PluginChecker.isMapReducePluginLoader()) {
                        listExistingObjects.addAll(proxyRepositoryFactory.getAll(ERepositoryObjectType.PROCESS_MR, true, false));
                    }
                    if (repositoryObject.getProperty() != null
                            && !proxyRepositoryFactory.isNameAvailable(repositoryObject.getProperty().getItem(),
                                    nameText.getText(), listExistingObjects)) {
                        errorMessage = Messages.getString("MainComposite.ItemExistsError");//$NON-NLS-1$
                        isValid = false;
                    }
                } catch (PersistenceException e1) {
                    e1.printStackTrace();
                }
            }

            if (isValid) {
                btnConfirm.setEnabled(true);
                nameTextDecorator.hide();
            } else {
                btnConfirm.setEnabled(false);
                nameTextDecorator.setDescriptionText(errorMessage);
                nameTextDecorator.show();
            }
        } else if (!btnConfirm.isDisposed()
                && !versionText.getText().equals(StringUtils.trimToEmpty(repositoryObject.getVersion()))) {
            btnConfirm.setEnabled(true);
        } else if (!btnConfirm.isDisposed() && !jobTypeCCombo.getText().equals(StringUtils.trimToEmpty(jobTypeValue))) {
            btnConfirm.setEnabled(true);
        } else if (!btnConfirm.isDisposed() && !jobFrameworkCCombo.getText().equals(StringUtils.trimToEmpty(frameworkValue))) {
            btnConfirm.setEnabled(true);
        } else if (!btnConfirm.isDisposed()
                && !purposeText.getText().equals(StringUtils.trimToEmpty(repositoryObject.getPurpose()))) {
            btnConfirm.setEnabled(true);
        } else if (!btnConfirm.isDisposed()
                && !statusText.getText().equals(StringUtils.trimToEmpty(repositoryObject.getStatusCode()))) {
            btnConfirm.setEnabled(true);
        } else if (!btnConfirm.isDisposed()
                && !descriptionText.getText().equals(StringUtils.trimToEmpty(repositoryObject.getDescription()))) {
            btnConfirm.setEnabled(true);
        } else if (nameText.getText().equals(repositoryObject.getLabel())) {
            btnConfirm.setEnabled(false);
        }
    }

    public void openEditorOperation(Item item) {
        if (item != null) {
            try {
                ERepositoryObjectType repObjType = ERepositoryObjectType.getItemType(item);
                IJobEditorHandler editorInputFactory = JobEditorHandlerManager.getInstance().extractEditorInputFactory(
                        repObjType.getType());
                editorInputFactory.openJobEditor(editorInputFactory.createJobEditorInput(item, true));
            } catch (PartInitException e) {
                e.printStackTrace();
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
        }
    }
}