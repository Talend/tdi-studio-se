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
package org.talend.sdk.component.studio.lang;

public class Pair<A, B> {

    private final A first;

    private final B second;

    public Pair(final A first, final B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return this.first;
    }

    public B getSecond() {
        return this.second;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Pair))
            return false;
        final Pair<?, ?> other = (Pair<?, ?>) o;
        if (!other.canEqual(this))
            return false;
        final Object this$first = this.getFirst();
        final Object other$first = other.getFirst();
        if (this$first == null ? other$first != null : !this$first.equals(other$first))
            return false;
        final Object this$second = this.getSecond();
        final Object other$second = other.getSecond();
        if (this$second == null ? other$second != null : !this$second.equals(other$second))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Pair;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $first = this.getFirst();
        result = result * PRIME + ($first == null ? 43 : $first.hashCode());
        final Object $second = this.getSecond();
        result = result * PRIME + ($second == null ? 43 : $second.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Pair(first=" + this.getFirst() + ", second=" + this.getSecond() + ")";
    }
}
