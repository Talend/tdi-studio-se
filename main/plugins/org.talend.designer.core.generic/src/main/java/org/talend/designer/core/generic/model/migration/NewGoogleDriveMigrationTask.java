package org.talend.designer.core.generic.model.migration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.talend.core.model.properties.Item;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.generic.utils.ParameterUtilTool;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class NewGoogleDriveMigrationTask extends org.talend.designer.core.generic.model.migration.NewComponentFrameworkMigrationTask {

    public static final String GOOGLE_DRIVE_PREFIX = "tGoogleDrive";

    public static final String JAVAJET_VERSION = "0.102";

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType != null) {
            for (Object obj : processType.getNode()) {
                if (obj != null && obj instanceof NodeType) {
                    String componentName = ((NodeType) obj).getComponentName();
                    String version = ((NodeType) obj).getComponentVersion();
                    if (componentName != null && componentName.startsWith(GOOGLE_DRIVE_PREFIX)) {
                        if (!JAVAJET_VERSION.equals(version)) {
                            // not a javajet component (tcompv0), we skip this migration for not corrupting comps.
                            return ExecutionResult.NOTHING_TO_DO;
                        }
                    }
                }
            }
        }
        return super.execute(item);
    }

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
        // sanitize inexistent props
        ElementParameterType oAuthMethod = ParameterUtilTool.findParameterType(node, "OAUTH_METHOD");
        if (oAuthMethod == null) {
            ParameterUtilTool.addParameterType(node, "TEXT", "OAUTH_METHOD", "AccessToken");
        }
        ElementParameterType dsPath = ParameterUtilTool.findParameterType(node, "DATASTORE_PATH");
        if (dsPath == null) {
            String datastorePath = (System.getProperty("user.home", ".") + "/.credentials/talend-googledrive")
                    .replace("\\", "/");
            ParameterUtilTool.addParameterType(node, "TEXT", "DATASTORE_PATH",
                    TalendQuoteUtils.addQuotesIfNotExist(datastorePath));
        }
        ElementParameterType paramType = ParameterUtilTool.findParameterType(node, paramName);
        if (node != null && paramType != null) {
            String componentName = node.getComponentName();
            if ("tGoogleDriveCopy".equals(componentName)) {
                Object copyModeParam = ParameterUtilTool.findParameterType(node, "COPY_MODE");
                if (copyModeParam == null) {
                    Object fileMode = ParameterUtilTool.getParameterValue(node, "FILE_MODE");
                    Object fileName = ParameterUtilTool.getParameterValue(node, "FILE_NAME");
                    Object folderName = ParameterUtilTool.getParameterValue(node, "FOLDER_NAME");
                    String copyMode = "true".equals(fileMode) ? "File" : "Folder";
                    String source = "File".equals(copyMode) ? String.valueOf(fileName) : String.valueOf(folderName);
                    ParameterUtilTool.addParameterType(node, "TEXT", "COPY_MODE", copyMode);
                    ParameterUtilTool.addParameterType(node, "TEXT", "SOURCE", source);
                }
            }
            if ("tGoogleDrivePut".equals(componentName)) {
                Object uploadModeParam = ParameterUtilTool.findParameterType(node, "UPLOAD_MODE");
                if (uploadModeParam == null) {
                    Object isReadContentFromInput = ParameterUtilTool.getParameterValue(node, "READ_CONTENT_FROM_INPUT");
                    Object uploadLocalFile = ParameterUtilTool.getParameterValue(node, "UPLOAD_LOCAL_FILE");
                    String uploadMode = "READ_CONTENT_FROM_INPUT";
                    if (!"true".equals(isReadContentFromInput)) {
                        uploadMode = "true".equals(uploadLocalFile) ? "UPLOAD_LOCAL_FILE" : "EXPOSE_OUTPUT_STREAM";
                    }
                    ParameterUtilTool.addParameterType(node, "TEXT", "UPLOAD_MODE", uploadMode);
                }
            }
        }
        return paramType;
    }

}
