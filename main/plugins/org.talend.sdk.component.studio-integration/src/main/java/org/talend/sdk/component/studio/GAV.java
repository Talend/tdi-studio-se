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
package org.talend.sdk.component.studio;

import java.io.IOException;
import java.util.Properties;

public enum GAV {

    INSTANCE;

    private final Properties props = new Properties() {{
        try {
            load(this.getClass().getClassLoader().getResourceAsStream("org.talend.sdk.studio.runtime-metadata.properties"));
        } catch (IOException e) {
            throw new IllegalStateException("can't find runtime metadata of dependencies for talend component kit");
        }
    }};

    public String getGroupId() {
        return props.getProperty("GROUP_ID", "org.talend.sdk.component");
    }

    public String getArtifactId() {
        return props.getProperty("ARTIFACT_ID", "org.talend.sdk.component.studio-integration");
    }

    public String getComponentRuntimeVersion() {
        return props.getProperty("COMPONENT_RUNTIME_VERSION");
    }

    public String getCliVersion() {
        return props.getProperty("CLI_VERSION");
    }

    public String getSlf4jVersion() {
        return props.getProperty("SLF4J_VERSION");
    }
}
