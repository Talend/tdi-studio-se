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

package org.talend.sdk.component.studio.documentation.toc;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.help.AbstractTocProvider;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.ITocContribution;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ComponentIndex;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.documentation.Locales;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.util.TaCoKitUtil;
import org.talend.sdk.component.studio.websocket.WebSocketClient;

public class TaCoKitTocProvider extends AbstractTocProvider {

    /**
     * Contributions by language.
     */
    private final Map<String, ITocContribution[]> languagePack = new HashMap<>();

    @Override
    public ITocContribution[] getTocContributions(final String language) {
        try {
            return getTocContributionsInner(language);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return new ITocContribution[0];
    }

    private ITocContribution[] getTocContributionsInner(final String language) throws Exception {
        ITocContribution[] contributions = languagePack.get(language);
        if(contributions != null) {
            return contributions;
        }
        final WebSocketClient client = Lookups.client();
        // we need to get the locale from display language. We might have a "en_US"/"en-US" or something different
        // as an incoming locale String
        final Locale expLocale = Locales.fromLanguagePresentation(language);

        // let's build map of component families.
        final Map<String, TaCoKitContribution> familyContributionsMap = new HashMap<>();
        Stream<Pair<ComponentIndex, ComponentDetail>> details =
                client.v1().component().details(expLocale.getLanguage());
        details.forEach(pair -> {
            final ComponentIndex index = pair.getFirst();
            final String familyName = index.getFamilyDisplayName();
            String displayName = TaCoKitUtil.getDisplayName(index);
            IContext existsContext = HelpSystem.getContext(TaCoKitConst.BASE_HELP_LINK + displayName);
            if (existsContext != null) {
                return;
            }
            TaCoKitContribution familyContribution = familyContributionsMap.computeIfAbsent(familyName, name -> {
                final TaCoKitContribution contribution = new TaCoKitContribution(index.getId().getFamilyId());
                contribution.setLocale(expLocale.getLanguage());
                final TaCoKitIToc familyItoc = new TaCoKitIToc("", Messages.getString("documentation.reference.guide", name));
                contribution.setToc(familyItoc);
                return contribution;
            });
            final TaCoKitTopic topic = new TaCoKitTopic();
            topic.setHref(index.getId().getId() + ".html#_" + displayName.toLowerCase());
            topic.setLabel(displayName);
            familyContribution.getToc().addTopic(topic);
        });


        contributions = familyContributionsMap.values().toArray(new ITocContribution[0]);
        languagePack.put(language, contributions);
        return contributions;
    }

    @Override
    protected void contentChanged() {
        languagePack.clear();
    }
}
