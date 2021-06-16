package org.talend.designer.spss;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.components.IODataComponentContainer;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.AbstractExternalNode;
import org.talend.core.model.process.IComponentDocumentation;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IExternalData;
import org.talend.core.ui.metadata.editor.MetadataTableEditor;
import org.talend.jspss.spss;
import org.talend.jspss.spssfile;
import org.talend.jspss.spssvariable;
import org.talend.jspss.spssvariables;

public class spssComponent extends AbstractExternalNode {

    private static Logger log = Logger.getLogger(spssComponent.class);

    private String strFileName;

    private String strTranslateLabels;

    @Override
    protected void renameMetadataColumnName(String conectionName, String oldColumnName, String newColumnName) {
    }

    public IComponentDocumentation getComponentDocumentation(String componentName, String tempFolderPath) {
        return null;
    }

    public int open(Composite parent) {
        return open(parent.getDisplay());
    }

    public void initialize() {
    }

    public IExternalData getExternalData() {
        return null;
    }

    public void setExternalData(IExternalData persistentData) {
    }

    public void renameInputConnection(String oldName, String newName) {
    }

    public void renameOutputConnection(String oldName, String newName) {
    }

    @Override
    public IODataComponentContainer getIODataComponents() {
        IODataComponentContainer inAndOut = new IODataComponentContainer();
        List<IODataComponent> outputs = inAndOut.getOuputs();
        if (null != getOutgoingConnections()) {
            for (IConnection currentConnection : getOutgoingConnections()) {
                IODataComponent component = new IODataComponent(currentConnection, getMetadataList().get(0));
                outputs.add(component);
            }
        }
        List<IODataComponent> inputs = inAndOut.getInputs();
        if (null != getIncomingConnections()) {
            for (IConnection currentConnection : getIncomingConnections()) {
                IODataComponent component = new IODataComponent(currentConnection, getMetadataList().get(0));
                inputs.add(component);
            }
        }
        return inAndOut;
    }

    public int open(Display display) {
        try {
            getCompProperties();
            if (MessageDialog.openQuestion(display.getActiveShell(), "SPSS - Sync Schema", "Retrive schema from the File? \n"
                    + strFileName)) {
                getSchemaFromSPSSFile();
                return SWT.OK;
            } else {
                return SWT.CANCEL;
            }
        } catch (Exception e) {
            log.error("", e);
            return SWT.CANCEL;
        }
    }

    private void getCompProperties() {
        try {
            strFileName = (String.valueOf(this.getElementParameter(IspssParameterNames.FILENAME).getValue()))
                    .replaceAll("\"", "");
            strTranslateLabels = String.valueOf(this.getElementParameter(IspssParameterNames.TRANSLATELABELS).getValue());

        } catch (Exception e) {
        }
    }

    private void getSchemaFromSPSSFile() {

        try {
            List<IMetadataTable> metadataList = getMetadataList();
            MetadataTable metaTable = (MetadataTable) metadataList.get(0);
            metaTable.getListColumns().clear();
            MetadataTableEditor tableEditor = new MetadataTableEditor(metaTable, "");

            spss spssIn = new spss();
            spssfile spssFile = spssIn.openFile(strFileName, spssfile.SPSS_READ);

            String strComponentName = this.getComponent().getName();
            spssvariables spssVars = spssFile.getVariables();
            spssvariable spssVar;
            for (int i = 0; i < spssVars.getNumberOfVariables(); i++) {
                spssVar = spssVars.getVariabelAtPos(i);
                IMetadataColumn column = tableEditor.createNewMetadataColumn();
                String lableName = MetadataToolHelper.validateColumnName(spssVar.getName(), i);
                column.setLabel(lableName);
                column.setOriginalDbColumnName(spssVar.getName());

                if (spssVar.hasLabels() && "true".equalsIgnoreCase(strTranslateLabels)) {
                    column.setLength(spssVar.getMaxLengthByLableTranslation());
                    column.setPrecision(0);
                    column.setTalendType("id_String");
                } else {
                    switch (spssVar.getJAVAType()) {
                    case spssvariable.JAVA_TYPE_DOUBLE:
                        column.setTalendType("id_Double");
                        break;
                    case spssvariable.JAVA_TYPE_STRING:
                        column.setTalendType("id_String");
                        break;
                    case spssvariable.JAVA_TYPE_DATE:
                        column.setTalendType("id_Date");
                        break;
                    }
                    column.setLength(spssVar.getDecimals());
                    column.setPrecision(spssVar.getPrecision());

                }

                String strLabel = spssVar.getLabel();
                if (null != strLabel && strLabel.length() > 0)
                    column.setComment(strLabel);
                else
                    column.setComment("");

                if (null == strComponentName || "tspssinput".equalsIgnoreCase(strComponentName))
                    column.setReadOnly(true);
                else
                    column.setReadOnly(false);

                metaTable.getListColumns().add(column);
            }
            spssFile.close();

        } catch (Exception e) {
            log.error("", e);
        }
    }

    public static void main(String[] args) {
        Display display = new Display();
        spssComponent component = new spssComponent();
        component.open(display);
    }

    public IExternalData getTMapExternalData() {
        // TODO Auto-generated method stub
        return null;
    }

}
