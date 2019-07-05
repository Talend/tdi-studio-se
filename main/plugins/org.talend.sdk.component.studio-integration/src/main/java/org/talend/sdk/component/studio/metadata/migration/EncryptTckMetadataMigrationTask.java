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
package org.talend.sdk.component.studio.metadata.migration;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;


/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class EncryptTckMetadataMigrationTask extends AbstractItemMigrationTask {

    private ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2019, 06, 20, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        boolean modified = false;
        if (item instanceof ConnectionItem) {
            try {
                ConnectionItem conItem = (ConnectionItem) item;
                TaCoKitConfigurationModel configModel = new TaCoKitConfigurationModel(conItem.getConnection());
                configModel.setPrintEncryptionException(false);
                Map<String, PropertyDefinitionDecorator> keyMap = configModel.buildPropertyTree();
                for (Map.Entry<String, PropertyDefinitionDecorator> entry : keyMap.entrySet()) {
                    PropertyDefinitionDecorator decorator = entry.getValue();
                    if (decorator.isCredential()) {
                        String key = entry.getKey();
                        configModel.setValue(key, configModel.getValueOfSelf(key));
                        modified = true;
                    }
                }
                if (modified) {
                    factory.save(item, true);
                }
            } catch (IllegalArgumentException e) {
                // ignore
            } catch (Exception e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        if (modified) {
            return ExecutionResult.SUCCESS_NO_ALERT;
        } else {
            return ExecutionResult.NOTHING_TO_DO;
        }
    }

}
