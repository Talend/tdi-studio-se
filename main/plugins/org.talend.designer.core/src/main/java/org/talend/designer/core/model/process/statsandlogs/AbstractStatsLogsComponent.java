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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IMultipleComponentItem;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.MultipleComponentConnection;
import org.talend.designer.core.model.components.MultipleComponentManager;

/**
 * These components won't be used in the designer part, only for the generation. <br/>
 *
 */
public abstract class AbstractStatsLogsComponent implements IComponent {

    protected boolean useDb = false;

    protected boolean useConsole = false;

    protected String dbComponent;

    protected boolean useFile = false;

    protected String componentId;

    protected String subComponent;

    protected List<IMultipleComponentManager> multipleComponentManagers = new ArrayList<IMultipleComponentManager>();

    // no use for virtual component
    @Override
    public List<? extends INodeConnector> createConnectors(INode node) {
        return null;
    }

    // no use for virtual component
    @Override
    public List<? extends INodeReturn> createReturns(INode node) {
        return null;
    }

    // no use for virtual component
    @Override
    public List<ECodePart> getAvailableCodeParts() {
        return null;
    }

    // no use for virtual component
    @Override
    public String getOriginalFamilyName() {
        return "Virtual"; //$NON-NLS-1$
    }

    @Override
    public String getTranslatedFamilyName() {
        return "Virtual"; //$NON-NLS-1$
    }

    // no use for virtual component
    @Override
    public ImageDescriptor getIcon16() {
        return null;
    }

    // no use for virtual component
    @Override
    public ImageDescriptor getIcon24() {
        return null;
    }

    // no use for virtual component
    @Override
    public ImageDescriptor getIcon32() {
        return null;
    }

    // no use for virtual component
    @Override
    public String getLongName() {
        return null;
    }

    // no use for virtual component ?
    @Override
    public List<ModuleNeeded> getModulesNeeded(INode node) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getModulesNeeded()
     */
    @Override
    public List<ModuleNeeded> getModulesNeeded() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getName()
     */
    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public String getOriginalName() {
        return getName();
    }

    // no use for virtual component
    @Override
    public String getPathSource() {
        return null;
    }

    // no use for virtual component
    @Override
    public List<String> getPluginDependencies() {
        return null;
    }

    // no use for virtual component
    @Override
    public String getPluginExtension() {
        return null;
    }

    // no use for virtual component
    public String getTranslatedName() {
        return null;
    }

    // no use for virtual component
    @Override
    public boolean hasConditionalOutputs() {
        return false;
    }

    // no use for virtual component
    @Override
    public boolean isMultiplyingOutputs() {
        return false;
    }

    // no use for virtual component
    @Override
    public boolean isDataAutoPropagated() {
        return false;
    }

    // no use for virtual component
    @Override
    public boolean isLoaded() {
        return true;
    }

    // no use for virtual component
    @Override
    public boolean isSchemaAutoPropagated() {
        return false;
    }

    // no use for virtual component
    @Override
    public boolean isVisible() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isVisible(java.lang.String)
     */
    @Override
    public boolean isVisible(String family) {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isVisibleInComponentDefinition()
     */
    @Override
    public boolean isVisibleInComponentDefinition() {
        return false;
    }

    // no use for virtual component
    @Override
    public void setImageRegistry(java.util.Map<String, ImageDescriptor> imageRegistry) {
    };

    // no use for virtual component
    @Override
    public boolean useMerge() {
        return false;
    }

    @Override
    public String getRepositoryType() {
        return null;
    }

    @Override
    public List<? extends IElementParameter> createElementParameters(INode node) {
        List<IElementParameter> elemParamList = new ArrayList<IElementParameter>();

        addFileOutputParameters(elemParamList, node);
        addDbParameters(elemParamList, node);
        return elemParamList;
    }

    protected void addDbParameters(List<IElementParameter> elemParamList, INode node) {
        // parameters for db output.
        IElementParameter newParam = new ElementParameter(node);
        newParam.setName("HOST"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        // This parameter is use for Connection Component
        newParam = new ElementParameter(node);
        newParam.setName("USE_EXISTING_CONNECTION"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.CHECK);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("CONNECTION"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        // This parameter is use for Oracle component
        newParam = new ElementParameter(node);
        newParam.setName("CONNECTION_TYPE"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setShow(false);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("SERVER"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("DSN"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("PORT"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("DBNAME"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("DB_VERSION"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("PROPERTIES"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("SCHEMA_DB"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("USER"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("PASS"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("TABLE"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("TABLE_ACTION"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue("CREATE_IF_NOT_EXISTS"); //$NON-NLS-1$
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("DATA_ACTION"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue("INSERT"); //$NON-NLS-1$
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("COMMIT_EVERY"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue("1"); //$NON-NLS-1$
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("USE_SHARED_CONNECTION"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.CHECK);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("SHARED_CONNECTION_NAME"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("USE_TRANSACTION"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.CHECK);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("LOCAL_SERVICE_NAME"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

    }

    protected void addFileOutputParameters(List<IElementParameter> elemParamList, INode node) {
        // parameters for file output.
        IElementParameter newParam = new ElementParameter(node);
        newParam.setName("FILENAME"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("ROWSEPARATOR"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue(TalendTextUtils.addQuotes("\\n", TalendTextUtils.QUOTATION_MARK)); //$NON-NLS-1$
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("FIELDSEPARATOR"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue(TalendTextUtils.addQuotes(";")); //$NON-NLS-1$
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("APPEND"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.CHECK);
        newParam.setValue(Boolean.TRUE);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("INCLUDEHEADER"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.CHECK);
        newParam.setValue(Boolean.FALSE);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("ENCODING"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue(TalendTextUtils.addQuotes("ISO-8859-15")); //$NON-NLS-1$
        elemParamList.add(newParam);

    }

    protected void loadMultipleComponentManager() {
        String lastComponent = null;
        if (useConsole) {
            lastComponent = "CONSOLE"; //$NON-NLS-1$
        } else if (useDb) {
            lastComponent = "DB"; //$NON-NLS-1$
        } else {
            lastComponent = "FILE"; //$NON-NLS-1$
        }
        // create base items
        IMultipleComponentManager multipleComponentManager = new MultipleComponentManager(componentId, lastComponent);
        IMultipleComponentItem currentItem = multipleComponentManager.addItem(componentId, subComponent);
        if (useFile) {
            currentItem.getOutputConnections().add(new MultipleComponentConnection("FLOW", "FILE")); //$NON-NLS-1$ //$NON-NLS-2$
            currentItem = multipleComponentManager.addItem("FILE", "tFileOutputDelimited"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (useDb) {
            currentItem.getOutputConnections().add(new MultipleComponentConnection("FLOW", "DB")); //$NON-NLS-1$ //$NON-NLS-2$
            currentItem = multipleComponentManager.addItem("DB", dbComponent); //$NON-NLS-1$
        }
        if (useConsole) {
            currentItem.getOutputConnections().add(new MultipleComponentConnection("FLOW", "CONSOLE")); //$NON-NLS-1$ //$NON-NLS-2$
            currentItem = multipleComponentManager.addItem("CONSOLE", "tLogRow"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        multipleComponentManager.validateItems();
        multipleComponentManagers.add(multipleComponentManager);
        createMultipleComponentsParameters();
    }

    protected void createMultipleComponentsParameters() {
        // create parameters
        if ((multipleComponentManagers != null) && (multipleComponentManagers.size() > 0)) {
            IMultipleComponentManager multipleComponentManager = multipleComponentManagers.get(0);
            if (useFile) {
                multipleComponentManager.addParam("self.FILENAME", "FILE.FILENAME"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.ROWSEPARATOR", "FILE.ROWSEPARATOR"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.FIELDSEPARATOR", "FILE.FIELDSEPARATOR"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.APPEND", "FILE.APPEND"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.INCLUDEHEADER", "FILE.INCLUDEHEADER"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.ENCODING", "FILE.ENCODING"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if (useDb) {
                multipleComponentManager.addParam("self.USE_EXISTING_CONNECTION", "DB.USE_EXISTING_CONNECTION"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.CONNECTION", "DB.CONNECTION"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.COMMIT_EVERY", "DB.COMMIT_EVERY"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.HOST", "DB.HOST"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.HOST", "DB.SERVER"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.HOST", "DB.DSN"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.PORT", "DB.PORT"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.DBNAME", "DB.DBNAME"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.DB_VERSION", "DB.DB_VERSION"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.PROPERTIES", "DB.PROPERTIES"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.USER", "DB.USER"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.PASS", "DB.PASS"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.TABLE", "DB.TABLE"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.TABLE_ACTION", "DB.TABLE_ACTION"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.DATA_ACTION", "DB.DATA_ACTION"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.COMMIT_EVERY", "DB.COMMIT_EVERY"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.SCHEMA_DB", "DB.SCHEMA_DB"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.DB_TYPE", "DB.DB_TYPE"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.CONNECTION_TYPE", "DB.CONNECTION_TYPE"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.USE_TRANSACTION", "DB.USE_TRANSACTION"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.LOCAL_SERVICE_NAME", "DB.LOCAL_SERVICE_NAME"); //$NON-NLS-1$ //$NON-NLS-2$

                // only for new framework (JDBC)
                multipleComponentManager.addParam("self.CONNECTION", "DB.referencedComponent"); //$NON-NLS-1$ //$NON-NLS-2$ //componentInstanceId
                multipleComponentManager.addParam("self.DATA_ACTION", "DB.dataAction"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getMultipleComponentManager()
     */
    @Override
    public List<IMultipleComponentManager> getMultipleComponentManagers() {
        return multipleComponentManagers;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getComponentType()
     */
    @Override
    public boolean isMultipleOutput() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#useLookup()
     */
    @Override
    public boolean useLookup() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#useImport()
     */
    @Override
    public boolean useImport() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isHashComponent()
     */
    @Override
    public boolean isHashComponent() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isTechnical()
     */
    @Override
    public boolean isTechnical() {
        return false;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#isMainCodeCalled()
     */
    @Override
    public boolean isMainCodeCalled() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#canParallelize()
     */
    @Override
    public boolean canParallelize() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getShortName()
     */
    @Override
    public String getShortName() {
        return "slog";
    }

    @Override
    public String getCombine() {
        return null;
    }

    @Override
    public String getPaletteType() {
        return "HIDDEN";
    }

    @Override
    public void setPaletteType(String paletteType) {
        // TODO Auto-generated method stub

    }

    /**
     * This is just added in <code>HEADER</code>, it is used to present the component belongs to common process, M/R
     * process and etc. The {@link ComponentCategory#CATEGORY_4_DI} is as the default value. Added by Marvin Wang on Jan
     * 11, 2013.
     *
     */
    @Override
    public String getType() {
        return ComponentCategory.CATEGORY_4_DI.getName();
    }

    @Override
    public boolean isReduce() {
        return false;
    }

    @Override
    public boolean isSparkAction() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getInputType()
     */
    @Override
    public String getInputType() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getOutputType()
     */
    @Override
    public String getOutputType() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getPartitioning()
     */
    @Override
    public String getPartitioning() {
        return "AUTO"; //$NON-NLS-1$
    }

    @Override
    public boolean isSupportDbType() {
        return false;
    }

    @Override
    public Map<String, ImageDescriptor> getImageRegistry() {
        return null;
    }

    @Override
    public boolean isLog4JEnabled() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#getCONNECTORList()
     */
    @Override
    public EList getCONNECTORList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAllowedPropagated() {
        return true;
    }

    @Override
    public String getTemplateFolder() {
        return getPathSource() == null ? null : (getPathSource() + "/" + getName());
    }

    @Override
    public String getTemplateNamePrefix() {
        return getName();
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

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponent#setTranslatedFamilyName(java.lang.String)
     */
    @Override
    public void setTranslatedFamilyName(String translatedFamilyName) {
        // TODO Auto-generated method stub

    }

}
