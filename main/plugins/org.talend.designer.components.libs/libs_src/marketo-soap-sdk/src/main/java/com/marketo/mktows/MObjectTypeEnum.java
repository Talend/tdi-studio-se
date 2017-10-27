
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour MObjectTypeEnum.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="MObjectTypeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Opportunity"/>
 *     &lt;enumeration value="OpportunityLeadRole"/>
 *     &lt;enumeration value="Program"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MObjectTypeEnum")
@XmlEnum
public enum MObjectTypeEnum {

    @XmlEnumValue("Opportunity")
    OPPORTUNITY("Opportunity"),
    @XmlEnumValue("OpportunityLeadRole")
    OPPORTUNITY_LEAD_ROLE("OpportunityLeadRole"),
    @XmlEnumValue("Program")
    PROGRAM("Program");
    private final String value;

    MObjectTypeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MObjectTypeEnum fromValue(String v) {
        for (MObjectTypeEnum c: MObjectTypeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
