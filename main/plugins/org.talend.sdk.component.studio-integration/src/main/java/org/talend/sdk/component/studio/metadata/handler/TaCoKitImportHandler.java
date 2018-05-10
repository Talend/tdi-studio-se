package org.talend.sdk.component.studio.metadata.handler;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.items.importexport.handlers.imports.MetadataConnectionImportHandler;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

/**
 * Connection Import Handler for Tacokit components. It is used during metadata import
 */
public class TaCoKitImportHandler extends MetadataConnectionImportHandler {
    
    /**
     * Constructor fills up Tacokit item types present in the product.
     * Metadata for non-existent Component won't be imported
     */
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
