
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour LeadKeySelector complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="LeadKeySelector">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.marketo.com/mktows/}LeadSelector">
 *       &lt;sequence>
 *         &lt;element name="keyType" type="{http://www.marketo.com/mktows/}LeadKeyRef"/>
 *         &lt;element name="keyValues" type="{http://www.marketo.com/mktows/}ArrayOfString"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LeadKeySelector", propOrder = {
    "keyType",
    "keyValues"
})
public class LeadKeySelector
    extends LeadSelector
{

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected LeadKeyRef keyType;
    @XmlElement(required = true)
    protected ArrayOfString keyValues;

    /**
     * Obtient la valeur de la propriété keyType.
     * 
     * @return
     *     possible object is
     *     {@link LeadKeyRef }
     *     
     */
    public LeadKeyRef getKeyType() {
        return keyType;
    }

    /**
     * Définit la valeur de la propriété keyType.
     * 
     * @param value
     *     allowed object is
     *     {@link LeadKeyRef }
     *     
     */
    public void setKeyType(LeadKeyRef value) {
        this.keyType = value;
    }

    /**
     * Obtient la valeur de la propriété keyValues.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getKeyValues() {
        return keyValues;
    }

    /**
     * Définit la valeur de la propriété keyValues.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setKeyValues(ArrayOfString value) {
        this.keyValues = value;
    }

}
