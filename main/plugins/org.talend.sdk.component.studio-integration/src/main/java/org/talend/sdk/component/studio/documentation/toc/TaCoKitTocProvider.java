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

package org.talend.sdk.component.studio.documentation.toc;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.help.AbstractTocProvider;
import org.eclipse.help.ITocContribution;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ComponentIndex;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.websocket.WebSocketClient;

public class TaCoKitTocProvider extends AbstractTocProvider {

    //Contributions by language.
    private final Map<String, ITocContribution[]> languagePack = new HashMap<>();

    @Override public ITocContribution[] getTocContributions(String language) {
        ITocContribution[] contributions = languagePack.get(language);
        if(contributions != null) {
            return contributions;
        }
        final WebSocketClient client = Lookups.client();
        // we need to get the locale from display language. We might have a "en_US"/"en-US" or something different
        // as an incoming locale String
        final Locale expLocale = getLocale(language);
        
        // let's build map of component families.
        final Map<String, TaCoKitContribution> familyContributionsMap = new HashMap<>();
        Stream<Pair<ComponentIndex, ComponentDetail>> details =
                client.v1().component().details(expLocale.getLanguage());
        details.forEach(pair -> {
            final ComponentIndex index = pair.getFirst();
            final String familyName = index.getFamilyDisplayName();
            TaCoKitContribution familyContribution = familyContributionsMap.get(familyName);
            if(familyContribution == null) {
                familyContribution = new TaCoKitContribution();
                familyContribution.setLocale(expLocale.getLanguage());
                final TaCoKitIToc familyItoc = new TaCoKitIToc("", Messages.getString("documentation.reference.guide", familyName));
                familyContribution.setToc(familyItoc);
                familyContributionsMap.put(familyName, familyContribution);
            }
            final TaCoKitTopic topic = new TaCoKitTopic();
            topic.setHref(index.getId().getId() + ".html");
            topic.setLabel(index.getDisplayName());
            familyContribution.getToc().addTopic(topic);
        });
        
        
        contributions = familyContributionsMap.values().toArray(new ITocContribution[0]);
        languagePack.put(language, contributions);
        return contributions;
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

    @Override
    protected void contentChanged() {
        languagePack.clear();
    }
}
