/**
 * 
 */
package org.talend.designer.dbmap.migration;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapData;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry;
import org.talend.designer.dbmap.model.emf.dbmap.InputTable;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * @author hwang
 *
 */
public class DbOperatorMigrationTask extends AbstractJobMigrationTask {

	/*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 04, 24, 12, 0, 0);
        return gc.getTime();
    }

	@Override
	public ExecutionResult execute(Item item) {

        IProxyRepositoryFactory factory = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        ProcessType processType = getProcessType(item);
        boolean modified = false;
        if (processType != null) {
            for (Object obj : processType.getNode()) {
                NodeType nodeType = (NodeType) obj;
                AbstractExternalData externalData = nodeType.getNodeData();
                if (externalData instanceof DBMapData) {
                    DBMapData mapperData = (DBMapData) externalData;
                    for (InputTable pTable : mapperData.getInputTables()) {
                    	for(DBMapperTableEntry entry : pTable.getDBMapperTableEntries()) {
                    		if("EQ".equals(entry.getOperator())) {
                    			entry.setOperator("=");
                    			modified = true;
                    		}else if("NE".equals(entry.getOperator())) {
                    			entry.setOperator("<>");
                    			modified = true;
                    		}else if("LT".equals(entry.getOperator())) {
                    			entry.setOperator("<");
                    			modified = true;
                    		}else if("LE".equals(entry.getOperator())) {
                    			entry.setOperator("<=");
                    			modified = true;
                    		}else if("GT".equals(entry.getOperator())) {
                    			entry.setOperator(">");
                    			modified = true;
                    		}else if("GE".equals(entry.getOperator())) {
                    			entry.setOperator(">=");
                    			modified = true;
                    		}
                    	}
                    }
                }
            }
        }
        try {
            if (modified) {
                factory.save(item, true);
                return ExecutionResult.SUCCESS_WITH_ALERT;
            } else {
                return ExecutionResult.SUCCESS_NO_ALERT;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    
	}
	
	private int firstIndexInString(String expression, String pattern) {
        Pattern regex = Pattern.compile(pattern, Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE);
        Matcher regexMatcher = regex.matcher(expression);
        if (regexMatcher.find()) {
            return regexMatcher.start();
        }
        return -1;
    }

}
