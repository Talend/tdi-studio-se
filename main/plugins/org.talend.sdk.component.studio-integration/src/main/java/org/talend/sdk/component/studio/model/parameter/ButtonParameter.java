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
package org.talend.sdk.component.studio.model.parameter;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.sdk.component.studio.model.parameter.command.TacokitCommand;

/**
 * ElementParameter which represents button and stores button command, which is executed on button pushed
 */
public class ButtonParameter extends TaCoKitElementParameter {

    private TacokitCommand command;

    public ButtonParameter(final IElement element) {
        super(element);
        setFieldType(EParameterFieldType.TACOKIT_BUTTON);
    }

    public TacokitCommand getCommand() {
        return this.command;
    }

    public void setCommand(final TacokitCommand command) {
        this.command = command;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ButtonParameter))
            return false;
        final ButtonParameter other = (ButtonParameter) o;
        if (!other.canEqual(this))
            return false;
        final Object this$command = this.getCommand();
        final Object other$command = other.getCommand();
        if (this$command == null ? other$command != null : !this$command.equals(other$command))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ButtonParameter;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $command = this.getCommand();
        result = result * PRIME + ($command == null ? 43 : $command.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ButtonParameter(command=" + this.getCommand() + ")";
    }

    /**
     * {@inheritDoc}
     *
     * @return false
     */
    @Override
    public boolean isPersisted() {
        return false;
    }
}
