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

package org.talend.sdk.component.studio.documentation.toc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;

public class TaCoKitIToc implements IToc {

    private final List<ITopic> topicsList = new ArrayList<>();

    private String href;

    private String label;

    public TaCoKitIToc(final String href, final String label) {
        this.href = href;
        this.label = label;
    }

    public void addTopic(final ITopic topic) {
        topicsList.add(topic);
    }

    @Override public ITopic[] getTopics() {
        return topicsList.toArray(new ITopic[0]);
    }


    @Override public ITopic getTopic(final String s) {
        for(ITopic topic : topicsList) {
            if (topic.getLabel().equals(s)) {
                return topic;
            }
        }
        return null;
    }

    @Override public String getHref() {
        return href;
    }

    @Override public String getLabel() {
        return label;
    }

    @Override public boolean isEnabled(final IEvaluationContext iEvaluationContext) {
        return true;
    }

    @Override public IUAElement[] getChildren() {
        return topicsList.toArray(new IUAElement[0]);
    }

}
