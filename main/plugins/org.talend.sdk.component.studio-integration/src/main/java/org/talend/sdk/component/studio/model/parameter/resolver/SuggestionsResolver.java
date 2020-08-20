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
package org.talend.sdk.component.studio.model.parameter.resolver;

import java.util.Collection;

import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.action.SuggestionsAction;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;

public class SuggestionsResolver extends AbstractParameterResolver {

    public SuggestionsResolver(final SuggestionsAction action, final PropertyNode actionOwner, final Collection<ActionReference> actions) {
        super(action, actionOwner, getActionRef(actions, actionOwner.getProperty().getSuggestions().getName(), Action.Type.SUGGESTIONS));
    }
}
