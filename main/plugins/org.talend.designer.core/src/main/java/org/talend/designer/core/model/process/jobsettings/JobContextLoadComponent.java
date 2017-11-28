// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.model.process.jobsettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IMultipleComponentItem;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.components.MultipleComponentConnection;
import org.talend.designer.core.model.components.MultipleComponentManager;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants.ContextLoadInfo;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 * 
 * This class will create a virtual component that will create the logs for the job. <br/>
 * 
 * It's not used at all in the designer, only during the code generation. <br/>
 */
public class JobContextLoadComponent implements IComponent {

    private static final String FILE_INPUT_REGEX = "Regex"; //$NON-NLS-1$

    private static final String FILE_INPUT_COMPONENT = "tFileInputRegex"; //$NON-NLS-1$

    private static final String CONTEXT_LOAD = "Context"; //$NON-NLS-1$

    public static final String CONTEXTLOAD_COMPONENT = "tContextLoad"; //$NON-NLS-1$

    private static final String DB_INPUT = "Database"; //$NON-NLS-1$

    private final String dbComponent;

    private final List<IMultipleComponentManager> multipleComponentManagers = new ArrayList<IMultipleComponentManager>();

    private final boolean isFile;

    public JobContextLoadComponent(boolean isFile, String dbComponent) {
        this.isFile = isFile;
        this.dbComponent = dbComponent;
        loadMultipleComponentManager();
    }

    protected void loadMultipleComponentManager() {
        IMultipleComponentManager multipleComponentManager = null;
        // create base items
        if (isFile) {
            multipleComponentManager = new MultipleComponentManager(FILE_INPUT_REGEX, CONTEXT_LOAD);

            IMultipleComponentItem currentItem = multipleComponentManager.addItem(FILE_INPUT_REGEX, FILE_INPUT_COMPONENT);
            currentItem.getOutputConnections().add(
                    new MultipleComponentConnection(EConnectionType.FLOW_MAIN.getName(), CONTEXT_LOAD));

            currentItem = multipleComponentManager.addItem(CONTEXT_LOAD, CONTEXTLOAD_COMPONENT);

        } else {
            if (dbComponent == null) {
                return;
            }
            multipleComponentManager = new MultipleComponentManager(DB_INPUT, CONTEXT_LOAD);

            IMultipleComponentItem currentItem = multipleComponentManager.addItem(DB_INPUT, dbComponent);
            currentItem.getOutputConnections().add(
                    new MultipleComponentConnection(EConnectionType.FLOW_MAIN.getName(), CONTEXT_LOAD));

            currentItem = multipleComponentManager.addItem(CONTEXT_LOAD, CONTEXTLOAD_COMPONENT);

        }
        multipleComponentManager.validateItems();
        multipleComponentManagers.add(multipleComponentManager);
        createMultipleComponentsParameters();
    }

    // no use for virtual component
    @Override
    public List<? extends INodeConnector> createConnectors(INode node) {
        return null;
    }

    @Override
    public List<? extends INodeReturn> createReturns(INode node) {
        return null;
    }

    @Override
    public List<ECodePart> getAvailableCodeParts() {
        return null;
    }

    @Override
    public ImageDescriptor getIcon16() {
        return null;
    }

    @Override
    public ImageDescriptor getIcon24() {
        return null;
    }

    @Override
    public ImageDescriptor getIcon32() {
        return null;
    }

    @Override
    public String getLongName() {
        return null;
    }

    @Override
    public String getPathSource() {
        return null;
    }

    @Override
    public List<String> getPluginDependencies() {
        return null;
    }

    @Override
    public String getPluginExtension() {
        return null;
    }

    public String getTranslatedName() {
        return null;
    }

    @Override
    public boolean hasConditionalOutputs() {
        return false;
    }

    @Override
    public boolean isMultiplyingOutputs() {
        return false;
    }

    @Override
    public boolean isDataAutoPropagated() {
        return false;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public boolean isSchemaAutoPropagated() {
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

    @Override
    public boolean isVisible() {
        return false;
    }

    public void setIcon16(ImageDescriptor icon16) {
    }

    public void setIcon24(ImageDescriptor icon24) {
    }

    public void setIcon32(ImageDescriptor icon32) {
    }

    @Override
    public boolean useMerge() {
        return false;
    }

    // set name
    @Override
    public String getName() {
        return this.getClass().getName();
    }
    
    @Override
    public String getOriginalName(){
        return getName();
    }

    @Override
    public String getOriginalFamilyName() {
        return "Virtual"; //$NON-NLS-1$
    }

    @Override
    public String getTranslatedFamilyName() {
        return "Virtual"; //$NON-NLS-1$
    }

    @Override
    public String getVersion() {
        return VersionUtils.DEFAULT_VERSION;
    }

    @Override
    public List<IMultipleComponentManager> getMultipleComponentManagers() {
        return multipleComponentManagers;
    }

    protected void createMultipleComponentsParameters() {
        final String self = "self."; //$NON-NLS-1$
        // create parameters
        if ((multipleComponentManagers != null) && (multipleComponentManagers.size() > 0)) {
            IMultipleComponentManager multipleComponentManager = multipleComponentManagers.get(0);
            if (isFile) {
                String source = self + EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getName();
                multipleComponentManager.addParam(source, FILE_INPUT_REGEX + ".FILENAME"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.IMPLICIT_TCONTEXTLOAD_REGEX;
                multipleComponentManager.addParam(source, FILE_INPUT_REGEX + ".REGEX"); //$NON-NLS-1$ 
                
                source = self + "IGNORE_ERROR_MESSAGE"; //$NON-NLS-1$
                multipleComponentManager.addParam(source, FILE_INPUT_REGEX + ".IGNORE_ERROR_MESSAGE"); //$NON-NLS-1$ 
                

            } else {
                String source = self + JobSettingsConstants.getExtraParameterName(EParameterName.URL.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".URL");

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.DRIVER_JAR.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".DRIVER_JAR");

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.DRIVER_CLASS.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".DRIVER_CLASS");

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.HOST.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".HOST"); //$NON-NLS-1$ 
                multipleComponentManager.addParam(source, DB_INPUT + ".SERVER"); //$NON-NLS-1$
                multipleComponentManager.addParam(source, DB_INPUT + ".DSN"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.PORT.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".PORT"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.DBNAME.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".DBNAME"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.DB_VERSION.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".DB_VERSION"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTIES.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".PROPERTIES"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.SCHEMA_DB.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".SCHEMA_DB"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.USER.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".USER"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.PASS.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".PASS"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.DBTABLE.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".DBTABLE"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".DB_TYPE"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.CONNECTION_TYPE.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".CONNECTION_TYPE"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.QUERY;
                multipleComponentManager.addParam(source, DB_INPUT + "." + JobSettingsConstants.QUERY); //$NON-NLS-1$ 

            }
            // context parameter
            final String context = CONTEXT_LOAD + "."; //$NON-NLS-1$  

            String source = self + EParameterName.LOAD_NEW_VARIABLE.getName();
            String target = context + EParameterName.LOAD_NEW_VARIABLE.getName();
            multipleComponentManager.addParam(source, target);

            source = self + EParameterName.NOT_LOAD_OLD_VARIABLE.getName();
            target = context + EParameterName.NOT_LOAD_OLD_VARIABLE.getName();
            multipleComponentManager.addParam(source, target);

            source = self + EParameterName.PRINT_OPERATIONS.getName();
            target = context + EParameterName.PRINT_OPERATIONS.getName();
            multipleComponentManager.addParam(source, target);

            source = self + EParameterName.DISABLE_ERROR.getName();
            target = context + EParameterName.DISABLE_ERROR.getName();
            multipleComponentManager.addParam(source, target);

            source = self + EParameterName.DISABLE_INFO.getName();
            target = context + EParameterName.DISABLE_INFO.getName();
            multipleComponentManager.addParam(source, target);

            source = self + EParameterName.DISABLE_WARNINGS.getName();
            target = context + EParameterName.DISABLE_WARNINGS.getName();
            multipleComponentManager.addParam(source, target);
        }

    }

    @Override
    public List<? extends IElementParameter> createElementParameters(INode node) {

        List<IElementParameter> elemParamList = new ArrayList<IElementParameter>();
        if (isFile) {
            // from file
            addFileInputDelimitedParameters(elemParamList, node);
        } else {
            // from database
            addDatabaseParameter(elemParamList, node);
        }
        // contextload
        addtContextLoadParameter(elemParamList, node);

        return elemParamList;
    }

    private void addFileInputDelimitedParameters(List<IElementParameter> elemParamList, INode node) {

        IElementParameter newParam = new ElementParameter(node);
        newParam.setName(EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getName());
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue(""); //$NON-NLS-1$
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.IMPLICIT_TCONTEXTLOAD_REGEX);
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue(""); //$NON-NLS-1$
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("IGNORE_ERROR_MESSAGE");
        newParam.setFieldType(EParameterFieldType.CHECK);
        newParam.setValue("true"); //$NON-NLS-1$
        elemParamList.add(newParam);

    }

    private void addDatabaseParameter(List<IElementParameter> elemParamList, INode node) {
        //

        IElementParameter newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DRIVER_JAR.getName()));
        newParam.setFieldType(EParameterFieldType.TABLE);
        newParam.setListItemsDisplayName(new String[] { EmfComponent.TEXT_BUILTIN, EmfComponent.TEXT_REPOSITORY });
        newParam.setListItemsDisplayCodeName(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DRIVER_CLASS.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.URL.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.HOST.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.CONNECTION_TYPE.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
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
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PORT.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DBNAME.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DB_VERSION.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTIES.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.SCHEMA_DB.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.USER.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PASS.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DBTABLE.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.QUERY);
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

    }

    private void addtContextLoadParameter(List<IElementParameter> elemParamList, INode node) {

        IElementParameter newParam = new ElementParameter(node);
        newParam.setName(EParameterName.LOAD_NEW_VARIABLE.getName());
        newParam.setFieldType(EParameterFieldType.CLOSED_LIST);
        newParam.setValue(ContextLoadInfo.WARNING);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(EParameterName.NOT_LOAD_OLD_VARIABLE.getName());
        newParam.setFieldType(EParameterFieldType.CLOSED_LIST);
        newParam.setValue(ContextLoadInfo.WARNING);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(EParameterName.PRINT_OPERATIONS.getName());
        newParam.setFieldType(EParameterFieldType.CHECK);
        newParam.setValue(false);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(EParameterName.DISABLE_ERROR.getName());
        newParam.setFieldType(EParameterFieldType.CHECK);
        newParam.setValue(false);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(EParameterName.DISABLE_INFO.getName());
        newParam.setFieldType(EParameterFieldType.CHECK);
        newParam.setValue(true);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(EParameterName.DISABLE_WARNINGS.getName());
        newParam.setFieldType(EParameterFieldType.CHECK);
        newParam.setValue(true);
        elemParamList.add(newParam);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getComponentType()
     */
    @Override
    public boolean isMultipleOutput() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#useLookup()
     */
    @Override
    public boolean useLookup() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#useImport()
     */
    @Override
    public boolean useImport() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getComponentType()
     */
    @Override
    public EComponentType getComponentType() {
        return EComponentType.JOB_CONTEXT_LOAD;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isVisibleInComponentDefinition()
     */
    @Override
    public boolean isVisibleInComponentDefinition() {
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
        return "icl";
    }

    @Override
    public String getCombine() {
        return null;
    }

    @Override
    public IProcess getProcess() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getPaletteType()
     */
    @Override
    public String getPaletteType() {
        return "HIDDEN";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#setPaletteType(java.lang.String)
     */
    @Override
    public void setPaletteType(String paletteType) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setImageRegistry(Map<String, ImageDescriptor> imageRegistry) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getRepositoryType() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getType()
     */
    @Override
    public String getType() {
        return ComponentCategory.CATEGORY_4_DI.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isReduce()
     */
    @Override
    public boolean isReduce() {
        return false;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isSparkAction()
     */
    @Override
    public boolean isSparkAction() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.talend.core.model.components.IComponent#getModulesNeeded()
     */
    @Override
    public List<ModuleNeeded> getModulesNeeded() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ModuleNeeded> getModulesNeeded(INode node) {
        return null;
    }

    public String getTemplateFolder() {
        return getPathSource() == null ? null : (getPathSource() + "/" + getName());
    }

    public String getTemplateNamePrefix() {
        return getName();
    }

}
