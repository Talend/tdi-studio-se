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

package org.talend.sdk.component.studio.documentation.content;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

import org.eclipse.help.IHelpContentProducer;
import org.talend.sdk.component.server.front.model.ComponentDetailList;
import org.talend.sdk.component.server.front.model.DocumentationContent;
import org.talend.sdk.component.server.front.model.ErrorDictionary;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.websocket.WebSocketClient;
import org.talend.sdk.component.studio.websocket.WebSocketClient.ClientException;

public class TaCoKitHelpContentProducer implements IHelpContentProducer {

    /**
     * @see org.eclipse.help.IHelpContentProducer#getInputStream(java.lang.String, java.lang.String, java.util.Locale)
     *
     * @param pluginId plugin(contributor) name.
     * @param href href is a component id + .html[?lang=en_US], which we can use to get the documentation
     * @param locale to be used
     */
    @Override
    public InputStream getInputStream(String pluginId, String href, Locale locale) {
        String id = href;
        final WebSocketClient client = Lookups.client();
        int index = id.lastIndexOf(".html");
        if (index != -1) {
            id = id.substring(0, index);
        }
        ComponentDetailList componentList = client.v1().component().getDetail(locale.getLanguage(), new String[] { id });
        if (componentList.getDetails() == null || componentList.getDetails().isEmpty()) {
            return null;
        }
        String source = null;
        final String componentName = componentList.getDetails().get(0).getDisplayName();
        try {
            final DocumentationContent content = client.v1().documentation().getDocumentation(locale.getLanguage(), id, "adoc");
            source = "<!DOCTYPE html>\r\n" + "<html>\r\n" + getHeader(componentName) + toHtml(content.getSource()) + "</body></html>";
        } catch (ClientException e) {
            if (e.getErrorPayload().getCode().equals(ErrorDictionary.COMPONENT_MISSING)) {
                source = createErrorPage(componentName);
            }
        }
        if (source == null || source.isEmpty()) {
            return null;
        }
        return new ByteArrayInputStream(source.getBytes());
    }

    private String toHtml(final String adoc) {
        return Lookups.asciidoctor().convert(adoc);
    }

    private String createErrorPage(final String componentName) {
        return "<!DOCTYPE html>\r\n" + "<html>\r\n" + getHeader(componentName) + "<body>\r\n" + "<p>\r\n"
                + Messages.getString("documentation.content.error.missing", componentName) + "\r\n" + "</p>\r\n" + "</body>\r\n"
                + "</html>";
    }

    private String getHeader(final String componentName) {
        return "<head>\r\n" + "<meta charset=\"UTF-8\">\r\n" + "<title>" + componentName.toUpperCase() + "</title>\r\n"
                + "<style>" +
                "table.tableblock { border-collapse: collapse; }\n" +
                "table.tableblock,th.tableblock,td.tableblock {border: 1px solid black;}" +
                "</style>\r\n"
                + "</head>\r\n<body>";
    }

}
