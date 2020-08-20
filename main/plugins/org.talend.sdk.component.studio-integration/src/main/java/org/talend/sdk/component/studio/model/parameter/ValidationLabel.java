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
package org.talend.sdk.component.studio.model.parameter;

import static java.util.stream.Stream.of;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Synthetic Element Parameter which shows validation message
 */
public class ValidationLabel {

    /**
     * Delimiter between messages in resulting validation message
     */
    private static final String DELIMITER = "; ";

    private final Set<String> constraintMessages = new HashSet<>();

    /**
     * Validation message
     */
    private String validationMessage;

    private Optional<TaCoKitElementParameter> elementParameter = Optional.ofNullable(null);

    public ValidationLabel(final TaCoKitElementParameter elementParameter) {
        this.elementParameter = Optional.ofNullable(elementParameter);
    }

    public void showValidation(final String message) {
        validationMessage = message;
        elementParameter.ifPresent(c -> c.getProblemManager().ifPresent(p -> p.setError(elementParameter.get(), buildValue())));
    }

    public void hideValidation() {
        validationMessage = null;
        elementParameter.ifPresent(c -> c.getProblemManager().ifPresent(p -> p.setError(elementParameter.get(), null)));
    }

    public void showConstraint(final String message) {
        constraintMessages.add(message);
        elementParameter.ifPresent(c -> c.getProblemManager().ifPresent(p -> p.setError(elementParameter.get(), buildValue())));
    }

    public void hideConstraint(final String message) {
        constraintMessages.remove(message);
        elementParameter.ifPresent(c -> c.getProblemManager().ifPresent(p -> p.setError(elementParameter.get(), null)));
    }

    private String buildValue() {
        return Stream
                .concat(constraintMessages.stream(), of(validationMessage))
                .filter(Objects::nonNull)
                .collect(Collectors.joining(DELIMITER));
    }
}
