package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.ExceptionHandler;
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


public class EnabletPersonatorAddressBasicSettingTask extends AbstractJobMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 10, 13, 0, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        try {
            // tPersonator
            IComponentFilter filter_tAddressRowCloud = new NameComponentFilter("tPersonator");
            IComponentConversion update_tPersonator = new Update_tPersonator();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter_tAddressRowCloud,
                            Arrays.<IComponentConversion> asList(update_tPersonator));

            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private class Update_tPersonator implements IComponentConversion {

        public void transform(NodeType node) {
            // enable Address Options OUTPUT_DEFAULT
            ElementParameterType outputDefaultCheckBox = ComponentUtilities.getNodeProperty(node, "OUTPUT_DEFAULT");
            if (outputDefaultCheckBox != null) {
                outputDefaultCheckBox.setValue("true"); //$NON-NLS-1$
            }
        }
    }
}
