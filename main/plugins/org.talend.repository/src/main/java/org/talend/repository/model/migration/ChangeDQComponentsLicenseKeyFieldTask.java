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
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.utils.security.StudioEncryption;

/**
 * DOC xqliu class global comment. Detailled comment
 */
public class ChangeDQComponentsLicenseKeyFieldTask extends AbstractJobMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 3, 9, 0, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        try {
            // tAddressRowCloud
            IComponentFilter filter_tAddressRowCloud = new NameComponentFilter("tAddressRowCloud");
            IComponentConversion check_tAddressRowCloud = new Check_tAddressRowCloud();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter_tAddressRowCloud,
                            Arrays.<IComponentConversion> asList(check_tAddressRowCloud));

            // tBatchAddressRowCloud
            IComponentFilter filter_tBatchAddressRowCloud = new NameComponentFilter("tBatchAddressRowCloud");
            IComponentConversion check_tBatchAddressRowCloud = new Check_tBatchAddressRowCloud();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter_tBatchAddressRowCloud,
                            Arrays.<IComponentConversion> asList(check_tBatchAddressRowCloud));

            // tMelissaDataAddress
            IComponentFilter filter_tMelissaDataAddress = new NameComponentFilter("tMelissaDataAddress");
            IComponentConversion check_tMelissaDataAddress = new Check_tMelissaDataAddress();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter_tMelissaDataAddress,
                            Arrays.<IComponentConversion> asList(check_tMelissaDataAddress));

            // tGoogleAddressRow
            IComponentFilter filter_tGoogleAddressRow = new NameComponentFilter("tGoogleAddressRow");
            IComponentConversion check_tGoogleAddressRow = new Check_tGoogleAddressRow();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter_tGoogleAddressRow,
                            Arrays.<IComponentConversion> asList(check_tGoogleAddressRow));

            // tGoogleGeocoder
            IComponentFilter filter_tGoogleGeocoder = new NameComponentFilter("tGoogleGeocoder");
            IComponentConversion check_tGoogleGeocoder = new Check_tGoogleGeocoder();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter_tGoogleGeocoder,
                            Arrays.<IComponentConversion> asList(check_tGoogleGeocoder));

            // tGoogleMapLookup
            IComponentFilter filter_tGoogleMapLookup = new NameComponentFilter("tGoogleMapLookup");
            IComponentConversion check_tGoogleMapLookup = new Check_tGoogleMapLookup();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter_tGoogleMapLookup,
                            Arrays.<IComponentConversion> asList(check_tGoogleMapLookup));

            // tPersonator
            IComponentFilter filter_tPersonator = new NameComponentFilter("tPersonator");
            IComponentConversion check_tPersonator = new Check_tPersonator();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter_tPersonator,
                            Arrays.<IComponentConversion> asList(check_tPersonator));

            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private class Check_tAddressRowCloud implements IComponentConversion {

        public void transform(NodeType node) {
            ElementParameterType nodeProperty = ComponentUtilities.getNodeProperty(node, "LICENSE_KEY");
            if (nodeProperty != null) {
                String cleanPass =
                        nodeProperty.getRawValue() == null ? nodeProperty.getValue() : nodeProperty.getRawValue();
                if (cleanPass != null) {
                    nodeProperty
                            .setValue(StudioEncryption
                                    .getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM)
                                    .encrypt(cleanPass));
                }
                nodeProperty.setField(EParameterFieldType.LICENSEKEY.getName());
            }
        }
    }

    private class Check_tBatchAddressRowCloud implements IComponentConversion {

        public void transform(NodeType node) {
            ElementParameterType nodeProperty = ComponentUtilities.getNodeProperty(node, "LICENSE_KEY");
            if (nodeProperty != null) {
                String cleanPass =
                        nodeProperty.getRawValue() == null ? nodeProperty.getValue() : nodeProperty.getRawValue();
                if (cleanPass != null) {
                    nodeProperty
                            .setValue(StudioEncryption
                                    .getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM)
                                    .encrypt(cleanPass));
                }
                nodeProperty.setField(EParameterFieldType.LICENSEKEY.getName());
            }
        }
    }

    private class Check_tMelissaDataAddress implements IComponentConversion {

        public void transform(NodeType node) {
            ElementParameterType nodeProperty = ComponentUtilities.getNodeProperty(node, "LICENSE");
            if (nodeProperty != null) {
                String cleanPass =
                        nodeProperty.getRawValue() == null ? nodeProperty.getValue() : nodeProperty.getRawValue();
                if (cleanPass != null) {
                    nodeProperty
                            .setValue(StudioEncryption
                                    .getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM)
                                    .encrypt(cleanPass));
                }
                nodeProperty.setField(EParameterFieldType.LICENSEKEY.getName());
            }
        }
    }

    private class Check_tGoogleAddressRow implements IComponentConversion {

        public void transform(NodeType node) {
            ElementParameterType nodeProperty = ComponentUtilities.getNodeProperty(node, "API_KEY");
            if (nodeProperty != null) {
                String cleanPass =
                        nodeProperty.getRawValue() == null ? nodeProperty.getValue() : nodeProperty.getRawValue();
                if (cleanPass != null) {
                    nodeProperty
                            .setValue(StudioEncryption
                                    .getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM)
                                    .encrypt(cleanPass));
                }
                nodeProperty.setField(EParameterFieldType.LICENSEKEY.getName());
            }
        }
    }

    private class Check_tGoogleGeocoder implements IComponentConversion {

        public void transform(NodeType node) {
            ElementParameterType nodeProperty = ComponentUtilities.getNodeProperty(node, "API_KEY");
            if (nodeProperty != null) {
                String cleanPass =
                        nodeProperty.getRawValue() == null ? nodeProperty.getValue() : nodeProperty.getRawValue();
                if (cleanPass != null) {
                    nodeProperty
                            .setValue(StudioEncryption
                                    .getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM)
                                    .encrypt(cleanPass));
                }
                nodeProperty.setField(EParameterFieldType.LICENSEKEY.getName());
            }
        }
    }

    private class Check_tGoogleMapLookup implements IComponentConversion {

        public void transform(NodeType node) {
            ElementParameterType nodeProperty = ComponentUtilities.getNodeProperty(node, "API_KEY");
            if (nodeProperty != null) {
                String cleanPass =
                        nodeProperty.getRawValue() == null ? nodeProperty.getValue() : nodeProperty.getRawValue();
                if (cleanPass != null) {
                    nodeProperty
                            .setValue(StudioEncryption
                                    .getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM)
                                    .encrypt(cleanPass));
                }
                nodeProperty.setField(EParameterFieldType.LICENSEKEY.getName());
            }
        }
    }

    private class Check_tPersonator implements IComponentConversion {

        public void transform(NodeType node) {
            ElementParameterType nodeProperty = ComponentUtilities.getNodeProperty(node, "LICENSEKEY");
            if (nodeProperty != null) {
                String cleanPass =
                        nodeProperty.getRawValue() == null ? nodeProperty.getValue() : nodeProperty.getRawValue();
                if (cleanPass != null) {
                    nodeProperty
                            .setValue(StudioEncryption
                                    .getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM)
                                    .encrypt(cleanPass));
                }
                nodeProperty.setField(EParameterFieldType.LICENSEKEY.getName());
            }
        }
    }
}
