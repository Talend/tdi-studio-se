package org.talend.repository.model.migration;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ICoreService;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.migration.AbstractProjectMigrationTask;

public class ChangeMappingFileMigrationTask extends AbstractProjectMigrationTask {

    @Override
    public ExecutionResult execute(Project project) {
        try {
            URL p = MetadataTalendType.getProjectForderURLOfMappingsFile();
            changeSAPHanaMappingFile(p);
            URL s = MetadataTalendType.getSystemForderURLOfMappingsFile();
            changeSAPHanaMappingFile(s);
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICoreService.class)) {
                ICoreService service = (ICoreService) GlobalServiceRegister.getDefault().getService(ICoreService.class);
                service.synchronizeMapptingXML();
                return ExecutionResult.SUCCESS_NO_ALERT;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return ExecutionResult.FAILURE;
    }
    
    private void changeSAPHanaMappingFile(URL url) {
        File dir = new File(url.getFile());
        File target = null;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.getName().equals("mapping_SAPHana.xml")) { //$NON-NLS-1$
                    target = file;
                    break;
                }
            }
        }
        if (target != null) {
            try {
                SAXReader reader = new SAXReader();                
                Document document = reader.read(target);
                Element root = document.getRootElement();
                Element node = root.element("dbms"); //$NON-NLS-1$
                Attribute attr = node.attribute("label"); //$NON-NLS-1$
                if (attr != null && attr.getValue().equals("Mapping SAP Hana")) { //$NON-NLS-1$
                    attr.setValue("Mapping SAPHana"); //$NON-NLS-1$
                    target.delete();
                    XMLWriter writer = new XMLWriter(new FileWriter(target));
                    writer.write(document);
                    writer.close(); 
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 10, 9, 12, 0, 0);
        return gc.getTime();
    }

}
