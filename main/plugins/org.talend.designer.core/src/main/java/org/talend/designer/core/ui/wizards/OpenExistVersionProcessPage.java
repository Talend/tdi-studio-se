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
package org.talend.designer.core.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.views.jobsettings.tabs.ProcessVersionComposite;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC xye class global comment. Detailled comment
 */
public class OpenExistVersionProcessPage extends WizardPage {

    private final static String TITLE = Messages.getString("OpenExistVersionProcess.open.title1"); //$NON-NLS-1$

    private final IRepositoryViewObject processObject;

    private ProcessVersionComposite versionListComposite;

    /** Version text. */
    protected Text versionText;

    /** Version upgrade major button. */
    private Button versionMajorBtn;

    /** Version upgrade minor button. */
    private Button versionMinorBtn;

    private Button createNewVersionButton;

    private Composite versionModifComposite;

    private String originVersion = null;

    private boolean createNewVersionJob = false;

    private boolean alreadyEditedByUser = false;

    private String newVersion;

    /**
     * DOC xye OpenExistVersionProcessPage constructor comment.
     *
     * @param pageName
     */
    protected OpenExistVersionProcessPage(final boolean alreadyEditedByUser, final IRepositoryViewObject processObject) {
        super("OpenExistVersionProcessPage"); //$NON-NLS-1$
        setTitle(TITLE);
        setMessage(Messages.getString("OpenExistVersionProcessPage.messageCreated1")); //$NON-NLS-1$
        this.processObject = processObject;
        originVersion = getProperty().getVersion();
        this.alreadyEditedByUser = alreadyEditedByUser;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        parent.setLayout(new GridLayout());
        versionListComposite = new ProcessVersionComposite(parent, SWT.NULL, null, processObject);
        versionListComposite.setParentWizard(this);
        versionListComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        setControl(versionListComposite);
        versionListComposite.refresh();

        Composite createNewVersionButtonComposite = new Composite(parent, SWT.NONE);
        createNewVersionButtonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        createNewVersionButtonComposite.setLayout(new FormLayout());

        createNewVersionButton = new Button(createNewVersionButtonComposite, SWT.CHECK);
        createNewVersionButton.setText(Messages.getString("OpenExistVersionProcessPage.textContent")); //$NON-NLS-1$
        createNewVersionButton.setEnabled(!alreadyEditedByUser && !isContainedRefProject());
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            createNewVersionButton.setEnabled(false);
        }
        GC gc = new GC(createNewVersionButton);
        Point labelSize = gc.stringExtent(Messages.getString("OpenExistVersionProcessPage.textContent")); //$NON-NLS-1$
        gc.dispose();
        FormData date = new FormData();
        date.left = new FormAttachment(0, 0);
        date.right = new FormAttachment(0, labelSize.x);
        date.top = new FormAttachment(0, 0);
        date.bottom = new FormAttachment(100, 0);
        createNewVersionButton.setLayoutData(date);

        Label redLabel = new Label(createNewVersionButtonComposite, SWT.NONE);
        redLabel.setText(Messages.getString("OpenExistVersionProcessPage.labelContent"));
        gc = new GC(redLabel);
        labelSize = gc.stringExtent(Messages.getString("OpenExistVersionProcessPage.labelContent")); //$NON-NLS-1$
        gc.dispose();
        date = new FormData();
        date.left = new FormAttachment(createNewVersionButton, 10, SWT.RIGHT);
        date.right = new FormAttachment(createNewVersionButton, 10 + labelSize.x, SWT.RIGHT);
        date.top = new FormAttachment(0, 0);
        date.bottom = new FormAttachment(100, 0);
        redLabel.setLayoutData(date);
        redLabel.setForeground(redLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
        redLabel.setVisible(!createNewVersionButton.getEnabled());

        Composite bc = new Composite(parent, SWT.NULL);
        bc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        bc.setLayout(layout);

        // Create Version
        Label versionLab = new Label(bc, SWT.NONE);
        versionLab.setText(Messages.getString("PropertiesWizardPage.Version")); //$NON-NLS-1$

        versionModifComposite = new Composite(bc, SWT.NONE);
        versionModifComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout versionLayout = new GridLayout(3, false);
        versionLayout.marginHeight = 0;
        versionLayout.marginWidth = 0;
        versionLayout.horizontalSpacing = 0;
        versionModifComposite.setLayout(versionLayout);

        versionText = new Text(versionModifComposite, SWT.BORDER);
        versionText.setEnabled(false);
        versionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        versionMajorBtn = new Button(versionModifComposite, SWT.PUSH);
        versionMajorBtn.setText(Messages.getString("PropertiesWizardPage.Version.Major")); //$NON-NLS-1$
        versionMajorBtn.setEnabled(!alreadyEditedByUser && !isContainedRefProject() && createNewVersionButton.getSelection());

        versionMinorBtn = new Button(versionModifComposite, SWT.PUSH);
        versionMinorBtn.setText(Messages.getString("PropertiesWizardPage.Version.Minor")); //$NON-NLS-1$
        versionMinorBtn.setEnabled(!alreadyEditedByUser && !isContainedRefProject() && createNewVersionButton.getSelection());

        versionText.setText(getProperty().getVersion());

        addListener();

        setPageComplete(false);
    }

    private void addListener() {

        createNewVersionButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {
                boolean create = createNewVersionButton.getSelection();
                versionModifComposite.setEnabled(create);
                versionListComposite.setEnabled(!create);
                versionMajorBtn.setEnabled(create);
                versionMinorBtn.setEnabled(create);
                createNewVersionJob = create;
                updatePageStatus();
            }

        });

        versionMajorBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String version = getProperty().getVersion();
                if (newVersion != null) {
                    version = newVersion;
                }
                version = VersionUtils.upMajor(version);
                versionText.setText(version);
                newVersion = version;
                updatePageStatus();
            }
        });

        versionMinorBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String version = getProperty().getVersion();
                if (newVersion != null) {
                    version = newVersion;
                }
                version = VersionUtils.upMinor(version);
                versionText.setText(version);
                newVersion = version;
                updatePageStatus();
            }
        });
    }

    public void updatePageStatus() {
        if (createNewVersionButton.getSelection()) {
            setPageComplete(!versionText.getText().equals(getOriginVersion()));
            if (!isPageComplete()) {
                setErrorMessage(Messages.getString("OpenExistVersionProcessPage.errorMessage")); //$NON-NLS-1$
            } else {
                setErrorMessage(null);
            }
        } else {
            setPageComplete(getSelection() != null);
        }
    }

    public Object getSelection() {
        return versionListComposite.getSelection();
    }

    private Property getProperty() {
        return processObject.getProperty();
    }

    public String getOriginVersion() {
        return this.originVersion;
    }

    public boolean isCreateNewVersionJob() {
        return this.createNewVersionJob;
    }

    public boolean isContainedRefProject() {
        return !ProjectManager.getInstance().getProject(this.getProperty()).getTechnicalLabel()
                .equals(ProjectManager.getInstance().getCurrentProject().getEmfProject().getTechnicalLabel());
    }

    public String getNewVersion() {
        return this.newVersion;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }
}
