package org.talend.repository.resource.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.properties.Item;
import org.talend.core.model.relationship.AbstractJobItemRelationshipHandler;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class JobResourceItemRelationshipHandler extends AbstractJobItemRelationshipHandler {
    
    private static final String REPACE_SLASH_TAG = "\\|";

    @Override
    protected Set<Relation> collect(Item baseItem) {
        ProcessType processType = getProcessType(baseItem);
        if (processType == null) {
            return Collections.emptySet();
        }

        List<String> resourceList = new ArrayList<String>();
        List<ContextType> contexts = processType.getContext();
        for (ContextType context : contexts) {
            List<ContextParameterType> contextParameter = context.getContextParameter();
            for (ContextParameterType contextParameterType : contextParameter) {
                if (JavaTypesManager.RESOURCE.getId().equals(contextParameterType.getType())
                        || JavaTypesManager.RESOURCE.getLabel().equals(contextParameterType.getType())) {
                    resourceList.add(contextParameterType.getValue());
                }
            }
        }
        if (resourceList.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Relation> relationSet = new HashSet<Relation>();
        for (String res : resourceList) {
            if (StringUtils.isBlank(res)) {
                continue;
            }
            Relation addedRelation = new Relation(); 
            addedRelation.setId(res.split(REPACE_SLASH_TAG)[0]);
            addedRelation.setType(RelationshipItemBuilder.RESOURCE_RELATION);
            addedRelation.setVersion(RelationshipItemBuilder.LATEST_VERSION); 
            relationSet.add(addedRelation);
        }
        return relationSet;
    }

}
