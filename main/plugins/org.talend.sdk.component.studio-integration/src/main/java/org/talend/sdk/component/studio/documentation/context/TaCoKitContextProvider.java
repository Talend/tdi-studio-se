/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.help.AbstractContextProvider;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IHelpResource;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ComponentIndex;
import org.talend.sdk.component.server.front.model.DocumentationContent;
import org.talend.sdk.component.studio.GAV;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.documentation.Locales;
import org.talend.sdk.component.studio.documentation.toc.TaCoKitTopic;
import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.util.TaCoKitUtil;
import org.talend.sdk.component.studio.websocket.WebSocketClient;

public class TaCoKitContextProvider extends AbstractContextProvider {

    @Override
    public IContext getContext(final String pluginId, final String contextName) {
        final Locale expLocale = Locales.fromLanguagePresentation(contextName);
        final WebSocketClient client = Lookups.client();
        //pluginId consists of two parts - plugin name and full component name and locale after the "."
        //we will need to parse it to get the correct value of related topics.
        final String fullComponentName = getFullComponentName(pluginId);
        if (fullComponentName == null) {
            return null;
        }

        Stream<Pair<ComponentIndex, ComponentDetail>> details =
                client.v1().component().details(expLocale.getLanguage());

        List<Pair<ComponentIndex, ComponentDetail>> detailsList = details.filter(pair -> {
            final ComponentIndex index = pair.getFirst();
            return TaCoKitUtil.getFullComponentName(index.getId().getFamily(), index.getId().getName()).equals(fullComponentName);
        }).collect(Collectors.toList());

        if (detailsList == null || detailsList.size() == 0) {
            return null;
        }

        final ComponentIndex firstIndex = detailsList.get(0).getFirst();
        if (firstIndex == null) {
            return null;
        }
        String displayName = TaCoKitUtil.getDisplayName(firstIndex);
        IContext existsContext = HelpSystem.getContext(TaCoKitConst.BASE_HELP_LINK + displayName);
        if (existsContext != null) {
            return existsContext;
        }
        DocumentationContent doc = client.v1().documentation().getDocumentation(expLocale.getLanguage(),
                firstIndex.getId().getId(), "asciidoc");
        TaCoKitHelpContext context = new TaCoKitHelpContext(parseDescription(doc.getSource(), displayName));

        TaCoKitTopic topic = new TaCoKitTopic();
        topic.setHref(
                "/" + GAV.INSTANCE.getArtifactId() + "/" + firstIndex.getId().getId() + ".html#_" + displayName.toLowerCase());
        topic.setLabel(displayName);
        context.addRelatedTopic(topic);

        return context;
    }

    private String parseDescription(final String input, final String componentName) {
        String result = componentName;
        final String title = "== " + componentName;
        int titleIndex = input.indexOf(title);
        if (titleIndex != -1) {
            int descriptionEndIndex = input.indexOf("===", titleIndex);
            if (descriptionEndIndex < 0) {
                descriptionEndIndex = input.length();
            }
            result = input.substring(titleIndex + title.length(), descriptionEndIndex);
            // if we don't have configuration title starting with "===", we might have table starting with "|==="
            // thus we need to trim the "|" symbol.
            if (result.endsWith("|")) {
                result = result.substring(0, result.length() - 1);
            }
            // Now we need to check if we haven't added the next component to description. For that we need to look for
            // "=="
            int nextComponentIndex = result.indexOf("== ");
            if (nextComponentIndex > 0) {
                result = result.substring(0, nextComponentIndex);
            }
            // Now trim if length is too big
            if (result.length() > 200) {
                result = result.substring(0, 197) + "...";
            }
        }
        result = result.trim();
        if (result == null || result.isEmpty()) {
            result = componentName;
        }
        return result;
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
        return new String[] { GAV.INSTANCE.getArtifactId(), "org.talend.help" };
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
