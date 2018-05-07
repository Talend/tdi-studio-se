package org.talend.sdk.component.studio.metadata.handler;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.items.importexport.handlers.imports.MetadataConnectionImportHandler;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

public class TaCoKitImportHandler extends MetadataConnectionImportHandler {
    
    public TaCoKitImportHandler() {
        final Map<String, ConfigTypeNode> configTypeNodes = Lookups.taCoKitCache().getConfigTypeNodeMap();
        try {
            for (final ConfigTypeNode node : configTypeNodes.values()) {
                // filter parent nodes
                if (StringUtils.isBlank(node.getParentId())) {
                    continue;
                }
                final ERepositoryObjectType type = TaCoKitUtil.getOrCreateERepositoryObjectType(node);
                checkedItemTypes.add(type);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

}
