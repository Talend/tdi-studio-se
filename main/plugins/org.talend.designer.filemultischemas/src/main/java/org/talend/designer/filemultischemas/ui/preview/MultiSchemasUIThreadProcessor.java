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
package org.talend.designer.filemultischemas.ui.preview;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.swt.thread.SWTUIThreadProcessor;
import org.talend.core.utils.CsvArray;
import org.talend.designer.filemultischemas.ui.MultiSchemasUI;
import org.talend.metadata.managment.ui.preview.ProcessDescription;
import org.talend.metadata.managment.ui.utils.ShadowProcessHelper;
import org.talend.repository.i18n.Messages;

/**
 * cLi class global comment. Detailled comment
 */
public class MultiSchemasUIThreadProcessor extends SWTUIThreadProcessor {

    private static Logger log = Logger.getLogger(MultiSchemasUIThreadProcessor.class);

    private MultiSchemasUI multiSchemaUI;

    private String previewInformationLabelMsg = null;

    private CsvArray csvArray = null;

    private ProcessDescription processDescription = null;

    boolean firstRowIsCatption = false;

    public MultiSchemasUIThreadProcessor(MultiSchemasUI multiSchemaUI) {
        super();
        this.multiSchemaUI = multiSchemaUI;
    }

    public int getSelectedColumnIndex() {
        return multiSchemaUI.getSelectedColumnIndex();
    }

    public MultiSchemasUI getMultiSchemaUI() {
        return this.multiSchemaUI;
    }

    public boolean preProcessStart() {
        if (getMultiSchemaUI().preCheckProcessStart()) {
            processDescription = getMultiSchemaUI().getProcessDescription();
            return true;
        }
        return false;
    }

    public void nonUIProcessInThread() {
        // get the XmlArray width an adapt ProcessDescription
        try {
            if (processDescription.isCSVOption()) {
                csvArray = ShadowProcessHelper.getCsvArray(processDescription, "FILE_CSV", true); //$NON-NLS-1$
            } else {
                csvArray = ShadowProcessHelper.getCsvArray(processDescription, "FILE_DELIMITED", true); //$NON-NLS-1$

            }
            if (csvArray == null) {
                previewInformationLabelMsg = Messages.getString("FileStep2.previewFailure"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                previewInformationLabelMsg = Messages.getString("FileStep2.previewIsDone"); //$NON-NLS-1$ //$NON-NLS-2$
                // refresh TablePreview on this step
                previewInformationLabelMsg = ""; //$NON-NLS-1$
            }

        } catch (Exception e) {
            setException(e);
            previewInformationLabelMsg = Messages.getString("FileStep2.previewFailure"); //$NON-NLS-1$ //$NON-NLS-2$
            log.error(Messages.getString("FileStep2.previewFailure") + " " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    public void updateUIInThreadIfThreadIsCanceled() {
        if (!getMultiSchemaUI().getPreviewInformationLabel().isDisposed()) {
            getMultiSchemaUI().getPreviewInformationLabel().setText("");
        }
    }

    public void updateUIInThreadIfThreadIsNotCanceled() {
        if (getMultiSchemaUI().getPreviewInformationLabel().isDisposed()) {
            return;
        }

        if (csvArray == null) {
            previewInformationLabelMsg = Messages.getString("FileStep2.previewFailure"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            previewInformationLabelMsg = Messages.getString("FileStep2.previewIsDone"); //$NON-NLS-1$ //$NON-NLS-2$
            // refresh TablePreview on this step
            previewInformationLabelMsg = ""; //$NON-NLS-1$
        }
        getMultiSchemaUI().getPreviewInformationLabel().setText(previewInformationLabelMsg);
        if (getException() != null) {
            getMultiSchemaUI().getPreviewInformationLabel().setText(Messages.getString("FileStep2.previewFailure")); //$NON-NLS-1$ //$NON-NLS-2$
            Display.getDefault().syncExec(new Runnable() {

                public void run() {
                    handleErrorOutput(getMultiSchemaUI().getOutputComposite(), getMultiSchemaUI().getTabFolder(),
                            getMultiSchemaUI().getOutputTabItem());
                }
            });

            return;

        }

        if (csvArray == null || csvArray.getRows().isEmpty()) {
            getMultiSchemaUI().getMultiSchemasFilePreview().removePreviewContent();
            return;
        }
        getMultiSchemaUI().getMultiSchemasFilePreview().refreshTablePreview(csvArray, firstRowIsCatption,
                getSelectedColumnIndex());
    }

    public void updateUIInThreadIfThreadFinally() {
        getMultiSchemaUI().updateUIInThreadIfThreadFinally();
    }

    public void postProcessCancle() {
        getMultiSchemaUI().postProcessCancle();
    }

    public CsvArray getCsvArray() {
        return this.csvArray;
    }

    public void setCsvArray(CsvArray csvArray) {
        this.csvArray = csvArray;
    }

    public void clearCsvArray() {
        if (csvArray != null) {
            this.csvArray.getRows().clear();
        }
    }
}
