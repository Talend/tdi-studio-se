// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.oas.external;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * DOC dsergent class global comment. Detailled comment
 */
public class OASDecoderFactory {

    private static final String OASDECODER_EXTENSION_ID = "org.talend.designer.oas.OASDecoder";

    private static final String OASDECODER_ID = "id";

    private static OASDecoderFactory instance;

    private Map<String, IOASDecoder> decoders;

    /**
     * Getter for instance.
     *
     * @return the instance
     */
    public static OASDecoderFactory getInstance() {
        if (instance == null) {
            instance = new OASDecoderFactory();
        }
        return instance;
    }

    public Map<String, IOASDecoder> getOASDecoders() throws CoreException {

        if (decoders == null) {

            decoders = new HashMap<>();

            IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();

            IConfigurationElement[] extensions = extensionRegistry.getConfigurationElementsFor(OASDECODER_EXTENSION_ID);

            for (IConfigurationElement e : extensions) {

                final String id = e.getAttribute(OASDECODER_ID);

                if (StringUtils.isNotBlank(id)) {
                    final Object o = e.createExecutableExtension("class");
                    if (o instanceof IOASDecoder) {

                        decoders.put(id, (IOASDecoder) o);
                    }
                }
            }

        }
        return decoders;
    }

}
