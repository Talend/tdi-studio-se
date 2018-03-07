/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

package org.talend.sdk.component.studio.documentation.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.eclipse.help.AbstractContextProvider;
import org.eclipse.help.IContext;
import org.eclipse.help.IHelpResource;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ComponentIndex;
import org.talend.sdk.component.studio.GAV;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.documentation.toc.TaCoKitTopic;
import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.util.TaCoKitUtil;
import org.talend.sdk.component.studio.websocket.WebSocketClient;

public class TaCoKitContextProvider extends AbstractContextProvider {

    @Override
    public IContext getContext(String pluginId, String contextName) {
        final Locale expLocale = getLocale(contextName);
        final WebSocketClient client = Lookups.client();
        //pluginId consists of two parts - plugin name and full component name and locale after the "."
        //we will need to parse it to get the correct value of related topics.
        final String fullComponentName = getFullComponentName(pluginId);
        if (fullComponentName == null) {
            return null;
        }
        //TODO: change to description from documentation
        TaCoKitHelpContext context = new TaCoKitHelpContext(fullComponentName);

        Stream<Pair<ComponentIndex, ComponentDetail>> details =
                client.v1().component().details(expLocale.getLanguage());

        details.filter(pair -> {
            final ComponentIndex index = pair.getFirst();
            return TaCoKitUtil.getFullComponentName(index.getFamilyDisplayName(), index.getDisplayName()).equals(fullComponentName);
        }).forEach(pair -> {
            final ComponentIndex index = pair.getFirst();
            TaCoKitTopic topic = new TaCoKitTopic();
            topic.setHref("/" + GAV.ARTIFACT_ID + "/" + index.getId().getId() + ".html");
            topic.setLabel(index.getDisplayName());
            context.addRelatedTopic(topic);
        });

        return context;
    }

    private Locale getLocale(final String locale) {
        if (locale != null && locale.length() >= 5) {
            return new Locale(locale.substring(0, 2), locale.substring(3, 5));
        } else if (locale != null && locale.length() >= 2) {
            return new Locale(locale.substring(0, 2));
        } else {
            return Locale.getDefault();
        }
    }

    private String getFullComponentName(final String pluginId) {
        for (String plugin : getPlugins()) {
            if (pluginId.startsWith(plugin)) {
                String substr = pluginId.substring(plugin.length() + 1);
                if (!substr.contains(".")) {
                    return substr;
                }
            }
        }
        return null;
    }

    @Override public String[] getPlugins() {
        return new String[] { GAV.ARTIFACT_ID, "org.talend.help" };
    }

    public static class TaCoKitHelpContext implements IContext {

        private final List<IHelpResource> relatedTopics = new ArrayList<>();

        private final String description;

        public TaCoKitHelpContext(final String description) {
            this.description = description;
        }

        public void addRelatedTopic(final IHelpResource resource) {
            relatedTopics.add(resource);
        }

        @Override public IHelpResource[] getRelatedTopics() {
            return relatedTopics.toArray(new IHelpResource[0]);
        }

        @Override public String getText() {
            return description;
        }
    }
}
