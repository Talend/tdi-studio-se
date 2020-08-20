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

import org.eclipse.help.ITocContribution;
import org.talend.sdk.component.studio.GAV;

public class TaCoKitContribution implements ITocContribution {

    private final String contributionName;

    private String locale;

    private TaCoKitIToc iToc;

    public TaCoKitContribution(String contributionName) {
        this.contributionName = contributionName;
    }

    @Override
    public String getCategoryId() {
        return null;
    }

    @Override
    public String getContributorId() {
        return GAV.INSTANCE.getArtifactId();
    }

    @Override
    public String[] getExtraDocuments() {
        return new String[0];
    }

    @Override
    public String getId() {
        return GAV.INSTANCE.getArtifactId() + "." + contributionName;
    }

    @Override
    public String getLocale() {
        return locale;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

    @Override
    public String getLinkTo() {
        return null;
    }

    public void setToc(final TaCoKitIToc iToc) {
        this.iToc = iToc;
    }

    @Override
    public TaCoKitIToc getToc() {
        return iToc;
    }

    @Override
    public boolean isPrimary() {
        return true;
    }
}
