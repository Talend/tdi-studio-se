package org.talend.repository.preference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.repository.utils.Log4jUtil;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.constants.Log4jPrefsConstants;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.utils.Log4jPrefsSettingManager;

public class Log4jSettingPage extends ProjectSettingPage {

    private StyledText templateTxt;

    private boolean reset = false;

    Button log4jBtn;

    Button log4jVersion1Btn;

    Button log4jVersion2Btn;

    private Combo combo;

    private String log4jVersion;

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    protected Control createContents(Composite parent) {

        if (Log4jUtil.isEnable()) {
            createLog4jActivateGroup(parent);
        }

        Label headLabel = new Label(parent, SWT.NONE);
        headLabel.setText("Log4j template"); //$NON-NLS-1$

        templateTxt = new StyledText(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        templateTxt.setLayoutData(layoutData);
        templateTxt.setText(Log4jPrefsSettingManager.getInstance().getValueOfPreNode(Log4jPrefsConstants.LOG4J_CONTENT_NODE));

        if (log4jBtn != null && !log4jBtn.isDisposed()) {
            initLog4jStatus();
        }
        initListerner();
        return parent;
    }

    private void initLog4jStatus() {
        if (Log4jPrefsSettingManager.getInstance().isLog4jPrefsExist()) {
            // if already exist,to get its value from prefs
            log4jBtn.setSelection(Boolean
                    .valueOf(Log4jPrefsSettingManager.getInstance().getValueOfPreNode(Log4jPrefsConstants.LOG4J_ENABLE_NODE)));
        }

    }

    protected Composite createLog4jActivateGroup(Composite parent) {

        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setLayout(new GridLayout(3, false));
        Composite composite = new Composite(group, SWT.NONE);
        GridLayout gridLayout = new GridLayout(3, false);
        composite.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 3;
        composite.setLayoutData(gridData);
        group.setText(Messages.getString("Log4jSettingPage.Log4jEnabled")); //$NON-NLS-1$

        log4jBtn = new Button(composite, SWT.CHECK);
        log4jBtn.setText(Messages.getString("Log4jSettingPage.ActivateLog4j"));//$NON-NLS-1$

        Composite compositeVersion = new Composite(composite, SWT.NONE);
        GridLayout gridLayoutVersion = new GridLayout(2, false);
        compositeVersion.setLayout(gridLayoutVersion);
        Label label = new Label(compositeVersion, SWT.NONE);
        label.setText(Messages.getString("Log4jSettingPage.Log4jVersion")); //$NON-NLS-1$
        GridData labelData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1);
        label.setLayoutData(labelData);
        combo = new Combo(compositeVersion, SWT.READ_ONLY);
        GridData comboData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1);
        GC gc = new GC(combo);
        Point labelSize = gc.stringExtent(Log4jPrefsConstants.LOG4J1);
        gc.dispose();
        int hint = labelSize.x + (ITabbedPropertyConstants.HSPACE * 1);
        comboData.widthHint = hint;
        combo.setLayoutData(comboData);
        combo.setItems(Log4jPrefsConstants.LOG4J_VERSIONS.toArray(new String[] {}));
        log4jVersion = Boolean
                .valueOf(Log4jPrefsSettingManager.getInstance().getValueOfPreNode(Log4jPrefsConstants.LOG4J_SELECT_VERSION2))
                        ? Log4jPrefsConstants.LOG4J2
                        : Log4jPrefsConstants.LOG4J1;
        combo.select(Log4jPrefsConstants.LOG4J_VERSIONS.indexOf(log4jVersion));

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 8;
        composite.setLayout(layout);
        String property = System.getProperty("showLog4j2");//$NON-NLS-1$
        Boolean showLog4j2 = Boolean.valueOf(property);
        combo.setVisible(showLog4j2);
        combo.setEnabled(
                Boolean.valueOf(Log4jPrefsSettingManager.getInstance().getValueOfPreNode(Log4jPrefsConstants.LOG4J_ENABLE_NODE)));
        label.setVisible(showLog4j2);
        return group;
    }

    private void initListerner() {
        log4jBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!log4jBtn.getSelection()) {
                    combo.setEnabled(false);
                    combo.select(0);
                    IRunProcessService service = null;
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
                        service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
                    }
                    if (service != null) {
                        String logTemplate = service.getLogTemplate(Log4jPrefsConstants.LOG4JFILEPATH);
                        templateTxt.setText(logTemplate);
                    }
                } else {
                    combo.setEnabled(true);
                }
            }
        });
        combo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = combo.getSelectionIndex();
                String version = Log4jPrefsConstants.LOG4J_VERSIONS.get(index);
                IRunProcessService service = null;
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
                    service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
                }
                if (service != null) {

                    if (version != null && StringUtils.equals(version.trim(), Log4jPrefsConstants.LOG4J2)) {
                        String logTemplate = service.getLogTemplate(Log4jPrefsConstants.LOG4J_VERSION2_FILEPATH);
                        templateTxt.setText(logTemplate);
                    } else {
                        String logTemplate = service.getLogTemplate(Log4jPrefsConstants.LOG4JFILEPATH);
                        templateTxt.setText(logTemplate);
                    }
                }

            }
        });
    }

    @Override
    public boolean performOk() {
        boolean ok = super.performOk();
        if (templateTxt != null && !templateTxt.isDisposed()) {
            IRunProcessService service = null;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
                service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
            }
            if (reset) {
                if (service != null) {
                    Log4jPrefsSettingManager.getInstance().saveLog4jNodeIntoPref(Log4jPrefsConstants.LOG4J_ENABLE_NODE,
                            String.valueOf(false));
                    Log4jPrefsSettingManager.getInstance().saveLog4jNodeIntoPref(Log4jPrefsConstants.LOG4J_CONTENT_NODE,
                            service.getLogTemplate(Log4jPrefsConstants.LOG4JFILEPATH));
                    Log4jPrefsSettingManager.getInstance().saveLog4jNodeIntoPref(Log4jPrefsConstants.LOG4J_SELECT_VERSION2,
                            String.valueOf(false));
                }
            } else {
                if (log4jBtn != null && !log4jBtn.isDisposed()) {
                    Log4jPrefsSettingManager.getInstance().saveLog4jNodeIntoPref(Log4jPrefsConstants.LOG4J_ENABLE_NODE,
                            String.valueOf(log4jBtn.getSelection()));
                }
                Log4jPrefsSettingManager.getInstance().saveLog4jNodeIntoPref(Log4jPrefsConstants.LOG4J_CONTENT_NODE,
                        templateTxt.getText());
                int selectionIndex = combo.getSelectionIndex();
                Log4jPrefsSettingManager.getInstance().saveLog4jNodeIntoPref(Log4jPrefsConstants.LOG4J_SELECT_VERSION2,
                        String.valueOf(selectionIndex == 1));

            }
            reset = false;
        }
        return ok;
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        if (templateTxt != null && !templateTxt.isDisposed()) {
            reset = true;
            if (log4jBtn != null && !log4jBtn.isDisposed()) {
                log4jBtn.setSelection(false);
            }
            templateTxt
                    .setText(Log4jPrefsSettingManager.getInstance().getDefaultTemplateString(Log4jPrefsConstants.LOG4JFILEPATH));
            combo.select(Log4jPrefsConstants.LOG4J_VERSIONS.indexOf(Log4jPrefsConstants.LOG4J1));
        }
    }
}
