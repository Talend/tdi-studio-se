package org.talend.sdk.component.studio.documentation.toc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;

public class TaCoKitTopic implements ITopic {

    private final List<ITopic> subTopics = new ArrayList<>();

    private String label;

    private String href;

    public void addSubTopic(final ITopic topic) {
        subTopics.add(topic);
    }

    @Override public ITopic[] getSubtopics() {
        return subTopics.toArray(new ITopic[0]);
    }

    @Override public boolean isEnabled(final IEvaluationContext iEvaluationContext) {
        return true;
    }

    @Override public IUAElement[] getChildren() {
        return subTopics.toArray(new IUAElement[0]);
    }

    public void setHref(final String href) {
        this.href = href;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    @Override
    public String getHref() {
        return href;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
