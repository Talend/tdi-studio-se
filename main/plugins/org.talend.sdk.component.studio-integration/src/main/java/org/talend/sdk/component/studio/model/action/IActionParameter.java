package org.talend.sdk.component.studio.model.action;

import org.talend.sdk.component.studio.lang.Pair;

import java.util.Collection;

/**
 * Action parameter maps IElementParameter to action payload.
 * It has n to 1 mapping to IElementParameter.
 * Some IElementParameter may produce multiple payload parameter (e.g. Table ElementParameter)
 */
public interface IActionParameter {

    /**
     * Returns a list of key-pair values, where each value is payload parameter and its value
     *
     * @return a list of payload parameters
     */
    Collection<Pair<String, String>> parameters();

    /**
     * Returns name of IElementParameter, which is mapped by this IActionParameter
     *
     * @return IElementParameter name
     */
    String getName();

    //TODO remove it
    boolean isHasDirectValue();

    //TODO remove it
    void setValue(String newValue);
}
