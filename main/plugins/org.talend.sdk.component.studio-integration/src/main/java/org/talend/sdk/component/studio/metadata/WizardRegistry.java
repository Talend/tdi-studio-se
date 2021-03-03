/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.metadata;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ComponentIndex;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.service.ComponentService;
import org.talend.sdk.component.studio.websocket.WebSocketClient;

public class WizardRegistry {

    private final WebSocketClient.V1Component client;

    private final Constructor<ERepositoryObjectType> eRepositoryObjectTypeConstructor;

    private final ComponentService service;

    public WizardRegistry() {
        client = Lookups.client().v1().component();
        service = Lookups.service();

        try {
            eRepositoryObjectTypeConstructor = ERepositoryObjectType.class.getDeclaredConstructor(String.class,
                    String.class, String.class, String.class, int.class, String[].class);
            if (!eRepositoryObjectTypeConstructor.isAccessible()) {
                eRepositoryObjectTypeConstructor.setAccessible(true);
            }
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public Collection<RepositoryNode> createNodes(final RepositoryNode curParentNode) {
        final String language = Locale.getDefault().getLanguage();
        return client
                .details(language)
                .filter(detail -> detail.getSecond().getProperties().stream().anyMatch(service::isConfiguration))
                .map(detail -> createNode(curParentNode, detail.getSecond(), detail.getFirst()))
                .filter(Objects::nonNull)
                .collect(toList());
    }

    public ERepositoryObjectType createRepositoryObjectType(final String type, final String label, final String alias,
            final String folder, final int ordinal, final String[] products) throws Exception {
        ERepositoryObjectType repositoryType =
                eRepositoryObjectTypeConstructor.newInstance(type, label, alias, folder, ordinal, products);
        return repositoryType;
    }

    private RepositoryNode createNode(final RepositoryNode curParentNode, final ComponentDetail detail,
            final ComponentIndex index) {
        final String name = detail.getId().getName();
        final String displayName = detail.getDisplayName();

        try {
            final ERepositoryObjectType repositoryType = eRepositoryObjectTypeConstructor.newInstance(name, displayName,
                    name, ERepositoryObjectType.METADATA.getFolder() + "/" + name, 100,
                    new String[] { ERepositoryObjectType.PROD_DI });
            repositoryType.setAParent(ERepositoryObjectType.METADATA);

            final RepositoryNode node = new RepositoryNode(null, RepositoryNode.class.cast(curParentNode.getRoot()),
                    IRepositoryNode.ENodeType.SYSTEM_FOLDER);
            node.setProperties(IRepositoryNode.EProperties.LABEL, name);
            node.setProperties(IRepositoryNode.EProperties.CONTENT_TYPE, repositoryType);
            // todo: icon service.toEclipseIcon(index)
            curParentNode.getChildren().add(node);
            return node;
        } catch (final Exception e) {
            ExceptionHandler.process(e);
            return null;
        }
    }
}
