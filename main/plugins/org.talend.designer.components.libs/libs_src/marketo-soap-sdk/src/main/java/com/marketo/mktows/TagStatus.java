
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour TagStatus complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="TagStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tagValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="statusList" type="{http://www.marketo.com/mktows/}ArrayOfProgressionStatus"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TagStatus", propOrder = {
    "tagValue",
    "statusList"
})
public class TagStatus {

    @XmlElement(required = true)
    protected String tagValue;
    @XmlElement(required = true)
    protected ArrayOfProgressionStatus statusList;

    /**
     * Obtient la valeur de la propriété tagValue.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTagValue() {
        return tagValue;
    }

    /**
     * Définit la valeur de la propriété tagValue.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTagValue(String value) {
        this.tagValue = value;
    }

    /**
     * Obtient la valeur de la propriété statusList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfProgressionStatus }
     *     
     */
    public ArrayOfProgressionStatus getStatusList() {
        return statusList;
    }

    /**
     * Définit la valeur de la propriété statusList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfProgressionStatus }
     *     
     */
    public void setStatusList(ArrayOfProgressionStatus value) {
        this.statusList = value;
    }

}
