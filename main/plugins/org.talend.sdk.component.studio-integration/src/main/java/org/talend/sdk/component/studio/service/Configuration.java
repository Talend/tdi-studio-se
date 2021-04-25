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
package org.talend.sdk.component.studio.service;

public class Configuration {

    private volatile boolean active;

    private final int debounceTimeout;

    public Configuration(final boolean active, final int debounceTimeout) {
        this.active = active;
        this.debounceTimeout = debounceTimeout;
    }

    public void disable() {
        active = false;
    }

    public boolean isActive() {
        return this.active;
    }

    public int getDebounceTimeout() {
        return this.debounceTimeout;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Configuration))
            return false;
        final Configuration other = (Configuration) o;
        if (!other.canEqual(this))
            return false;
        if (this.isActive() != other.isActive())
            return false;
        if (this.getDebounceTimeout() != other.getDebounceTimeout())
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Configuration;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isActive() ? 79 : 97);
        result = result * PRIME + this.getDebounceTimeout();
        return result;
    }

    @Override
    public String toString() {
        return "Configuration(active=" + this.isActive() + ", debounceTimeout=" + this.getDebounceTimeout() + ")";
    }

}
