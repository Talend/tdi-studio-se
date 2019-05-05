package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class DecrementLimitTFileInputDelimitedTDI32185 extends AbstractJobMigrationTask {

	@Override
	public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2018, 11, 10, 0, 0, 0);
		return gc.getTime();
	}

	@Override
	public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {

        	IComponentFilter filter = new NameComponentFilter("tFileInputDelimited");
        	ModifyComponentsAction.searchAndModify(item, processType, filter,
        			Arrays.<IComponentConversion> asList(new IComponentConversion() {

        				@Override
        				public void transform(NodeType node) {
        					EList metadatasList = node.getMetadata();
        					if(metadatasList == null || metadatasList.size() < 1) {
        						return;
        					}
        					MetadataType metadataType = (MetadataType)metadatasList.get(0);
        					boolean hasDynamic = false;
        					for (Object columnObject : metadataType.getColumn()) {
        						ColumnType columnType = (ColumnType) columnObject;
        						if(columnType.getType().equals("id_Dynamic")) {
        							hasDynamic = true;
        						}
        					}
        					if(!hasDynamic) {
        						return;
        					}
        					ElementParameterType limit = ComponentUtilities.getNodeProperty(node, "LIMIT");
        					try {
        						Integer intLimitValue = Integer.parseInt(limit.getValue());
        						if(intLimitValue > 0) {
        							intLimitValue--;
        							limit.setValue(String.valueOf(intLimitValue));
        						}
        					} catch(NumberFormatException e) {
        						String sLimit = limit.getValue();
                                limit.setValue(sLimit + "-1");
        					}
        				}
        			}));

        	return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
	}

}
