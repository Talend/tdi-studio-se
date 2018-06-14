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
package org.talend.sdk.component.studio.model.action;

import java.util.Map;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.websocket.WebSocketClient.V1Action;

public class Action {

    public static final String STATUS = "status";

    public static final String OK = "OK";

    public static final String KO = "KO";

    public static final String MESSAGE = "comment";

    public static final String VALIDATION = "validation";

    public static final String HEALTH_CHECK = "healthcheck";

    private final String actionName;

    private final String family;

    private final String type;

    protected final ActionParameters parameters = new ActionParameters();

    public Action(final String actionName, final String family, final String type) {
        this.actionName = actionName;
        this.family = family;
        this.type = type;
    }

    public void addParameter(final ActionParameter parameter) {
        parameters.add(parameter);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> callback() {
        final V1Action action = Lookups.client().v1().action();
        return action.execute(Map.class, family, type, actionName, parameters.payload());
    }

    public String getActionName() {
        return this.actionName;
    }

    public String getFamily() {
        return this.family;
    }

    public String getType() {
        return this.type;
    }

    public ActionParameters getParameters() {
        return this.parameters;
    }

}
