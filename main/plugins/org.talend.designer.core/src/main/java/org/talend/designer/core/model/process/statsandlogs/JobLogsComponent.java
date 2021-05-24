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
package org.talend.designer.core.model.process.statsandlogs;

import java.util.List;

import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.designer.core.model.components.ElementParameter;

/**
 * This class will create a virtual component that will create the logs for the job. It's not used at all in the
 * designer, only during the code generation. <br/>
 *
 */
public class JobLogsComponent extends AbstractStatsLogsComponent {

    public JobLogsComponent(boolean useFile, boolean useConsole, String dbComponent) {
        if (dbComponent != null) {
            this.useDb = true;
            this.dbComponent = dbComponent;
        }
        this.useFile = useFile;
        this.useConsole = useConsole;
        this.componentId = "LOGS"; //$NON-NLS-1$
        this.subComponent = "tLogCatcher"; //$NON-NLS-1$

        loadMultipleComponentManager();
    }

    @Override
    public String getVersion() {
        return "0.1"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.model.process.statsandlogs.AbstractStatsLogsComponent#createElementParameters(org.talend
     * .core.model.process.INode)
     */
    @Override
    public List<? extends IElementParameter> createElementParameters(INode node) {
        List<IElementParameter> paramList = (List<IElementParameter>) super.createElementParameters(node);

        IElementParameter newParam = new ElementParameter(node);
        newParam.setName("CATCH_RUNTIME_ERRORS"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue(Boolean.TRUE);
        paramList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("CATCH_USER_ERRORS"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue(Boolean.TRUE);
        paramList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("CATCH_USER_WARNING"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue(Boolean.FALSE);
        paramList.add(newParam);

        return paramList;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.model.process.statsandlogs.AbstractStatsLogsComponent#createMultipleComponentsParameters
     * ()
     */
    @Override
    protected void createMultipleComponentsParameters() {
        super.createMultipleComponentsParameters();
        if ((multipleComponentManagers != null) && (multipleComponentManagers.size() > 0)) {
            IMultipleComponentManager multipleComponentManager = multipleComponentManagers.get(0);
            multipleComponentManager.addParam("self.CATCH_RUNTIME_ERRORS", "LOGS.CATCH_JAVA_EXCEPTION"); //$NON-NLS-1$ //$NON-NLS-2$
            multipleComponentManager.addParam("self.CATCH_RUNTIME_ERRORS", "LOGS.CATCH_PERL_DIE"); //$NON-NLS-1$ //$NON-NLS-2$
            multipleComponentManager.addParam("self.CATCH_USER_ERRORS", "LOGS.CATCH_TDIE"); //$NON-NLS-1$ //$NON-NLS-2$
            multipleComponentManager.addParam("self.CATCH_USER_WARNING", "LOGS.CATCH_TWARN"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getComponentType()
     */
    @Override
    public EComponentType getComponentType() {
        return EComponentType.JOB_LOGS;
    }

    @Override
    public IProcess getProcess() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#setOriginalFamilyName(java.lang.String)
     */
    @Override
    public void setOriginalFamilyName(String familyName) {
        // TODO Auto-generated method stub

    }

}
