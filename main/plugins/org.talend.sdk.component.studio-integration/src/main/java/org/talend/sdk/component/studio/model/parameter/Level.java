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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Row in GridLayout
 */
public class Level {

    private int position;

    private int height;

    private final List<Layout> columns = new ArrayList<>();

    public int getPosition() {
        return this.position;
    }

    public int getHeight() {
        return this.height;
    }

    public List<Layout> getColumns() {
        return this.columns;
    }

    void setPosition(final int position) {
        this.position = position;
    }

    void setHeight(final int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "Level(position=" + this.getPosition() + ", height=" + this.getHeight() + ")";
    }
}
