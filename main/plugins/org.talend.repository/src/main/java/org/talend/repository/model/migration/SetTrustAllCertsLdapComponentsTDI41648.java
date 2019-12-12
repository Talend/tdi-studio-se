package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class SetTrustAllCertsLdapComponentsTDI41648 extends AbstractJobMigrationTask {

	@Override
	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2019, 0, 28, 0, 0, 0);
        return gc.getTime();
	}

	@Override
	public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        String[] componentsName = new String[] {
                "tLDAPConnection", "tLDAPAttributesInput", "tLDAPInput", "tLDAPOutput", "tLDAPRenameEntry" };
        IComponentConversion changeAlwaysTrustValue = new IComponentConversion() {

            public void transform(NodeType node) {
            	ElementParameterType advancedCa = ComponentUtilities.getNodeProperty(node, "ADVANCEDCA");
            	if(advancedCa != null) {
            		boolean isAdvancedCa = "true".equalsIgnoreCase(advancedCa.getValue());
            		if(isAdvancedCa) {
           				ComponentUtilities.getNodeProperty(node, "ALWAYS_TRUST").setValue("true");
            		}
            	}
            }
        };

        for (String name : componentsName) {
            IComponentFilter filter = new NameComponentFilter(name); //$NON-NLS-1$

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                        .<IComponentConversion> asList(changeAlwaysTrustValue));
            } catch (PersistenceException e) {
            	ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;
	}

}
