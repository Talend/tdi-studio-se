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
package org.talend.designer.core.ui.preferences;

import java.io.File;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.core.CorePlugin;
import org.talend.core.PluginChecker;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.designer.core.i18n.Messages;

/**
 * ftang class global comment. Detailed comment <br/>
 *
 */
public class DocumentationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private boolean isDocumentationPluginLoaded = true;

    private BooleanFieldEditor sourceCodeGenFieldEditor;

    private FileFieldEditor documentCssFile;

    private BooleanFieldEditor useCss;

    private BooleanFieldEditor generateLatestVersionFieldEditor;

    /**
     * ftang DocumentationPreferencePage constructor comment.
     */
    public DocumentationPreferencePage() {
        super(GRID);
        setPreferenceStore(CorePlugin.getDefault().getPreferenceStore());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        sourceCodeGenFieldEditor = new BooleanFieldEditor(
                ITalendCorePrefConstants.DOC_GENERATESOURCECODE,
                org.talend.repository.i18n.Messages.getString("DocumentationPreferencePage.sourceCodeToHTML"), getFieldEditorParent()); //$NON-NLS-1$
        addField(sourceCodeGenFieldEditor);

        generateLatestVersionFieldEditor = new BooleanFieldEditor(ITalendCorePrefConstants.DOC_GENERATE_LASTEST_VERSION,
                Messages.getString("PerformancePreferencePage.generateLatestVersion"), getFieldEditorParent()); //$NON-NLS-1$
        addField(generateLatestVersionFieldEditor);

        // see the bug 7073,qli
        if (isDocumentationPluginLoaded) {
            BooleanFieldEditor booleanFieldEditor = new BooleanFieldEditor(ITalendCorePrefConstants.DOC_GENERATION,
                    Messages.getString("PerformancePreferencePage.autoUpdateDoc"), getFieldEditorParent()); //$NON-NLS-1$
            addField(booleanFieldEditor);

            FileFieldEditor documentationLogo = new FileFieldEditor(ITalendCorePrefConstants.DOC_USER_LOGO,
                    Messages.getString("DocumentationPreferencePage.userDocLogo"), getFieldEditorParent());
            addField(documentationLogo);

            StringFieldEditor companyName = new StringFieldEditor(ITalendCorePrefConstants.DOC_COMPANY_NAME,
                    Messages.getString("DocumentationPreferencePage.companyName"), getFieldEditorParent());
            addField(companyName);
        }

        useCss = new BooleanFieldEditor(ITalendCorePrefConstants.USE_CSS_TEMPLATE,
                Messages.getString("DocumentationPreferencePage.use_css_template"), getFieldEditorParent()); //$NON-NLS-1$
        addField(useCss);

        documentCssFile = new FileFieldEditor(ITalendCorePrefConstants.CSS_FILE_PATH,
                Messages.getString("DocumentationPreferencePage.css_file"), getFieldEditorParent()); //$NON-NLS-1$
        documentCssFile.setFileExtensions(new String[] { "*.css" }); //$NON-NLS-1$
        addField(documentCssFile);
        activateFields(getPreferenceStore().getBoolean(ITalendCorePrefConstants.USE_CSS_TEMPLATE));
        addCssFileListener();
    }

    private void addCssFileListener() {
        final Composite parentComposite = getFieldEditorParent();
        documentCssFile.getTextControl(parentComposite).addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                checkValue(parentComposite);
            }
        });
    }

    private void checkValue(final Composite parentComposite) {
        if (useCss.getBooleanValue()) {
            String path = documentCssFile.getTextControl(parentComposite).getText();
            if (path != null && !path.equals("")) { //$NON-NLS-1$
                StringBuffer str = new StringBuffer();
                File cssFile = new File(path);
                if (!cssFile.exists()) {
                    str.append(Messages.getString("DocumentationPreferencePage.must_existing_value")); //$NON-NLS-1$
                } else if (path.contains(".")) { //$NON-NLS-1$
                    int lastIndexOf = path.lastIndexOf("."); //$NON-NLS-1$
                    String extend = path.substring(lastIndexOf + 1, path.length());
                    if (!extend.equalsIgnoreCase("css")) { //$NON-NLS-1$
                        str.append(Messages.getString("DocumentationPreferencePage.must_css_file")); //$NON-NLS-1$
                    }
                }
                String errMsg = str.toString();
                if (!errMsg.equals("")) { //$NON-NLS-1$
                    documentCssFile.setErrorMessage(errMsg);
                    documentCssFile.showErrorMessage();
                    setValid(false);
                } else {
                    setValid(true);
                }
            } else {
                setValid(true);
            }
        } else {
            setValid(true);
        }
    }

    private void activateFields(boolean useCss) {
        documentCssFile.setEnabled(useCss, getFieldEditorParent());
    }

    class UseCssListener implements IPropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            activateFields(String.valueOf(event.getNewValue()).equals("true")); //$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        isDocumentationPluginLoaded = PluginChecker.isDocumentationPluginLoaded();
    }

    @Override
    protected void initialize() {
        super.initialize();
        useCss.setPropertyChangeListener(new UseCssListener());
    }
}
