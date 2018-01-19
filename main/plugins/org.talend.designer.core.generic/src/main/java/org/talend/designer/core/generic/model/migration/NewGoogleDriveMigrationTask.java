package org.talend.designer.core.generic.model.migration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.talend.designer.core.generic.utils.ParameterUtilTool;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class NewGoogleDriveMigrationTask extends org.talend.designer.core.generic.model.migration.NewComponentFrameworkMigrationTask {

    @Override
    public Date getOrder() {
        return new GregorianCalendar(2017, 10, 23, 10, 15, 0).getTime();
    }

    @Override
    protected Properties getPropertiesFromFile() {
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("NewGoogleDriveMigrationTask.properties");//$NON-NLS-1$
        try {
            props.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return props;
    }

    @Override
    protected ElementParameterType getParameterType(NodeType node, String paramName) {
        ElementParameterType paramType = ParameterUtilTool.findParameterType(node, paramName);
        if (node != null && paramType != null) {
            Object value = ParameterUtilTool.convertParameterValue(paramType);
            String componentName = node.getComponentName();
            //
            if ("tGoogleDriveCopy".equals(componentName)) {
                if ("FILE_MODE".equals(paramName)) {
                    if ("true".equals(String.valueOf(value))) {
                        paramType.setValue("File");
                    } else {
                        paramType.setValue("Folder");
                    }
                }
                if ("FILE_NAME".equals(paramName)) {
                    ElementParameterType fileMode = ParameterUtilTool.findParameterType(node, "FILE_MODE");
                    Object fileModeValue = ParameterUtilTool.convertParameterValue(fileMode);
                    if ("true".equals(String.valueOf(fileModeValue))) {
                        paramType.setValue(String.valueOf(value));
                    } else {
                        ElementParameterType folderName = ParameterUtilTool.findParameterType(node, "FOLDER_NAME");
                        Object folderNameValue = ParameterUtilTool.convertParameterValue(folderName);
                        paramType.setValue(String.valueOf(folderNameValue));
                    }
                }
            }
            if ("tGoogleDrivePut".equals(componentName)) {
                if ("READ_CONTENT_FROM_INPUT".equals(paramName)) {
                    if ("true".equals(String.valueOf(value))) {
                        paramType.setValue("READ_CONTENT_FROM_INPUT");
                    } else {
                        ElementParameterType upl = ParameterUtilTool.findParameterType(node, "UPLOAD_LOCAL_FILE");
                        Object uplv = ParameterUtilTool.convertParameterValue(upl);
                        if ("true".equals(String.valueOf(uplv))) {
                            paramType.setValue("UPLOAD_LOCAL_FILE");
                        } else {
                            paramType.setValue("EXPOSE_OUTPUT_STREAM");
                        }
                    }
                }
            }
        }
        return paramType;
    }

}
