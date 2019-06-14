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
package org.talend.sdk.component.studio.model.update;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.update.IUpdateItemType;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.update.extension.UpdateManagerProviderDetector;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

/**
 * created by hcyi on Jun 14, 2019
 * Detailled comment
 *
 */
public class TaCoKitUpdateManager extends RepositoryUpdateManager {

    public TaCoKitUpdateManager(ConnectionItem connectionItem, List<Relation> relations) {
        super(connectionItem, relations);
    }

    public static boolean updateTaCoKitConnection(ConnectionItem connectionItem) {
        return updateTaCoKitConnection(connectionItem, true, false);
    }

    public static boolean updateTaCoKitConnection(ConnectionItem connectionItem, boolean show, final boolean onlySimpleShow) {
        return updateTaCoKitConnection(connectionItem, RelationshipItemBuilder.LATEST_VERSION, show, onlySimpleShow);
    }

    public static boolean updateTaCoKitConnection(ConnectionItem connectionItem, String version, boolean show,
            final boolean onlySimpleShow) {
        List<Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(connectionItem.getProperty().getId(),
                version, RelationshipItemBuilder.PROPERTY_RELATION);
        RepositoryUpdateManager repositoryUpdateManager = new TaCoKitUpdateManager(connectionItem, relations);
        return repositoryUpdateManager.doWork(show, false);
    }

    @Override
    public Set<? extends IUpdateItemType> getTypes() {
        IUpdateItemType[] allUpdateItemTypes = UpdateManagerProviderDetector.INSTANCE.getAllUpdateItemTypes();
        Set<IUpdateItemType> types = new HashSet<IUpdateItemType>(Arrays.asList(allUpdateItemTypes));
        return types;
    }

    @Override
    public boolean filterForType(UpdateResult result) {
        if (result == null || parameter == null) {
            return false;
        }
        Object object = result.getParameter();
        if (object == null) {
            return false;
        }
        if (object == parameter) {
            return true;
        }
        if (object instanceof ConnectionItem && parameter instanceof ConnectionItem) {
            ConnectionItem parentConnItem = (ConnectionItem) parameter;
            ConnectionItem childConnItem = (ConnectionItem) object;
            TaCoKitConfigurationModel configuration = new TaCoKitConfigurationModel(childConnItem.getConnection());
            if (configuration != null && parentConnItem.getProperty() != null) {
                String parentItemId = parentConnItem.getProperty().getId();
                if (TaCoKitUtil.equals(parentItemId, configuration.getParentItemId())) {
                    return true;
                }
            }
        }
        return super.filterForType(result);
    }
}
