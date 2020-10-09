// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.model.migration.spark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.hadoop.distribution.ComponentType;
import org.talend.hadoop.distribution.helper.DistributionsManager;
import org.talend.hadoop.distribution.model.DistributionBean;
import org.talend.hadoop.distribution.model.DistributionVersion;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;

/**
 * Migration task to update the value of spark version when breaking components.
 * If its a spark local job, the distrib version should not affect the job
 * if its not a spark local job, the spark local version should not affect the job
 */
public class UpdateRemovedHadoopVersion extends AbstractJobMigrationTask {
    
    private static final List<String> IMPACTED_COMPONENT_TYPES =
            Arrays.asList("HDFS", "HBASE", "HIVE");
    
    private static final String DEFAULT_DISTRIBUTION = "AMAZON_EMR";;
    
    private final String defaultVersion;
    
    private final DistributionsManager distributionsHelper;
    private final List<DistributionBean> distros;
    private final List<DistributionVersion> versions;
    private final List<String> versionsLabel;    
    
    private ComponentType componentType;
    
    public UpdateRemovedHadoopVersion() {

        componentType = ComponentType.getComponentType("HDFS");
        distributionsHelper = new DistributionsManager(componentType);
        distros = Arrays.asList( distributionsHelper.getDistributions() );
        
        versions = distros.stream().map( d -> Arrays.asList( d.getVersions() ) )
                .flatMap(List::stream)
                .collect(Collectors.toList());
        
        versionsLabel = versions.stream().map( v -> v.version ).collect(Collectors.toList());
        
        
        defaultVersion = versionsLabel.get(0);
        
    }
    
    
    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<>();
        toReturn.add(ERepositoryObjectType.PROCESS);//METADATA_CONNECTIONS
        return toReturn;
    }

    @Override
    public ExecutionResult execute(Item item) {

        boolean fullMigrationSucceded = true;
        ProcessType processType = getProcessType(item);
        
        if (processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        IComponentConversion hdpVersionConverter = new HadoopVersionCoverter();

        
        for (String componentTypeLabel : IMPACTED_COMPONENT_TYPES) {
            
            componentType = ComponentType.getComponentType(componentTypeLabel);
           
            for(String componentName: componentType.getComponentList() ) {
                
                try {
                    ModifyComponentsAction.searchAndModify(item, processType, new NameComponentFilter(componentName),
                            java.util.Collections.singletonList(hdpVersionConverter));
                    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                    factory.save(item, true);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    fullMigrationSucceded = false;
                }
            }
        }

        return fullMigrationSucceded ? ExecutionResult.SUCCESS_NO_ALERT : ExecutionResult.FAILURE;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 10, 05, 13, 0, 0);
        return gc.getTime();
    }
    
    private class HadoopVersionCoverter implements IComponentConversion {

        @Override
        public void transform(NodeType node) {
            
            String versionLabel =  componentType.getVersionParameter();
            String distributionLabel =  componentType.getDistributionParameter();
            
            if (!versionsLabel.contains(ComponentUtilities.getNodePropertyValue(node, versionLabel))) {
                
                try {
                    ComponentUtilities.setNodeValue(node, distributionLabel, DEFAULT_DISTRIBUTION);
                }catch(IllegalArgumentException distException) {
                    distException.printStackTrace();
                }
                
                try {
                    ComponentUtilities.setNodeValue(node, versionLabel, defaultVersion);
                }catch(IllegalArgumentException versionException) {
                    versionException.printStackTrace();
                }
                
            }
        }
    }
}



