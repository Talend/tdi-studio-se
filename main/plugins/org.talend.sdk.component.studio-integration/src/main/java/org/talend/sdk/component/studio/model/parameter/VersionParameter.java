package org.talend.sdk.component.studio.model.parameter;

import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.designer.core.model.components.ElementParameter;

public class VersionParameter extends ElementParameter {

    public static final String VERSION_SUFFIX = ".__version";

    public VersionParameter(final IElement element, final String path, final String version) {
        super(element);
        setTaggedValue("org.talend.sdk.component.source", "tacokit");
        setName(path + VERSION_SUFFIX);
        setValue(version);
        setDisplayName(path + VERSION_SUFFIX);
        setFieldType(EParameterFieldType.TECHNICAL);
        setCategory(EComponentCategory.BASIC);
        setNumRow(1);
        setReadOnly(true);
        setShow(false);
    }
}
