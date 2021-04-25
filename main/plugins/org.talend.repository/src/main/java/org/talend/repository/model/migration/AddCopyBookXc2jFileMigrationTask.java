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
package org.talend.repository.model.migration;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.EbcdicConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.ReferenceFileItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class AddCopyBookXc2jFileMigrationTask extends AbstractItemMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 3, 19, 14, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        try {
            boolean update = updateXc2jFilePath(item);
            if (update) {
                return ExecutionResult.SUCCESS_NO_ALERT;
            } else {
                return ExecutionResult.NOTHING_TO_DO;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private boolean updateXc2jFilePath(Item item) throws PersistenceException, IOException {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        boolean update = false;
        if (item instanceof ConnectionItem) {
            ConnectionItem connectionItem = (ConnectionItem) item;
            Connection con = connectionItem.getConnection();
            if (con instanceof EbcdicConnection) {
                ReferenceFileItem createReferenceFileItem = null;
                // the old copybook version depands MidFile
                String midFile = ((EbcdicConnection) con).getMidFile();
                if (midFile != null) {
                    File xc2jMidFile = new File(midFile);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if (xc2jMidFile.exists()) {
                        readXc2jFile(xc2jMidFile, baos);
                    }
                    // create a referenceItem first,maybe it has content,maybe is empty,decide by the midFile
                    if (connectionItem.getReferenceResources().isEmpty()) {
                        createReferenceFileItem = PropertiesFactory.eINSTANCE.createReferenceFileItem();
                        ByteArray byteArray = PropertiesFactory.eINSTANCE.createByteArray();
                        createReferenceFileItem.setContent(byteArray);
                        createReferenceFileItem.setExtension("xc2j");
                        connectionItem.getReferenceResources().add(createReferenceFileItem);
                    } else {
                        createReferenceFileItem = (ReferenceFileItem) connectionItem.getReferenceResources().get(0);
                    }
                    createReferenceFileItem.getContent().setInnerContent(baos.toByteArray());
                    // create the phyhical x2cj file and set referenceFileItem content for it
                    String xc2jFilePath = getReferenceXc2jFile(connectionItem).getLocation().makeAbsolute().toFile()
                            .getAbsolutePath();
                    getXc2jFileFromBytes(createReferenceFileItem.getContent().getInnerContent(), xc2jFilePath);
                    update = true;
                }
            }
            if (update) {
                factory.save(connectionItem, true);
            }
        }
        return update;
    }

    public void readXc2jFile(File file, ByteArrayOutputStream bos) throws IOException {
        FileInputStream source = null;
        try {
            source = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = source.read(buf)) != -1) {
                bos.write(buf, 0, i);
            }
        } finally {
            if (null != source) {
                try {
                    source.close();
                } catch (Exception e) {
                    //
                }
            }
        }
    }

    public static IFile getReferenceXc2jFile(ConnectionItem connectionItem) {
        IFile x2cjFile = null;
        try {
            IProject currentProject = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
            String label = connectionItem.getProperty().getLabel();
            String version = connectionItem.getProperty().getVersion();
            String xc2jFileName = label + "_" + version + ".xc2j";
            String folderPath = "";
            String folder = "";
            if (connectionItem.getState() != null) {
                folderPath = connectionItem.getState().getPath();
            }
            if (!folderPath.equals("")) {
                folder = "/" + folderPath;
            }
            x2cjFile = currentProject.getFolder("metadata/fileEBCDIC" + folder).getFile(xc2jFileName);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return x2cjFile;
    }

    public static File getXc2jFileFromBytes(byte[] b, String outputFile) {
        BufferedOutputStream outputStream = null;
        File file = null;

        try {
            file = new File(outputFile);
            FileOutputStream fstream = new FileOutputStream(outputFile);
            outputStream = new BufferedOutputStream(fstream);
            outputStream.write(b);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
