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

package org.talend.sdk.component.studio.documentation.content;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

import org.eclipse.help.IHelpContentProducer;
import org.talend.sdk.component.server.front.model.ComponentDetailList;
import org.talend.sdk.component.server.front.model.DocumentationContent;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.websocket.WebSocketClient;

public class TaCoKitHelpContentProducer implements IHelpContentProducer {

    @Override public InputStream getInputStream(String s, String s1, Locale locale) {
        //s is a plugin(contributor) name. s1 is an href which we set.
        // In our case href is a component id + .html[?lang=en_US], which we can use to get the documentation from
        // server.
        String id = s1;
        final WebSocketClient client = Lookups.client();
        int index = id.lastIndexOf(".html");
        if (index != -1) {
            id = id.substring(0, index);
        }
        ComponentDetailList componentList = client.v1().component().getDetail(locale.getLanguage(), new String[] { id });
        if (componentList.getDetails() == null || componentList.getDetails().isEmpty()) {
            return null;
        }
        String componentName = componentList.getDetails().get(0).getDisplayName();
        DocumentationContent content = client.v1().documentation().getDocumentation(locale.getLanguage(), id, "html");
        String source = "<!DOCTYPE html>\r\n" + "<html>\r\n" + "<head>\r\n" + "<meta charset=\"UTF-8\">\r\n"
                + "<title>" + componentName + "</title>\r\n" + "</head>" + content.getSource() + "</body></html>";
        return new ByteArrayInputStream(source.getBytes());
    }
}
