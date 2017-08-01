
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsSyncMultipleLeads complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsSyncMultipleLeads">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="leadRecordList" type="{http://www.marketo.com/mktows/}ArrayOfLeadRecord"/>
 *         &lt;element name="dedupEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsSyncMultipleLeads", propOrder = {
    "leadRecordList",
    "dedupEnabled"
})
@XmlRootElement(name = "paramsSyncMultipleLeads")
public class ParamsSyncMultipleLeads {

    @XmlElement(required = true)
    protected ArrayOfLeadRecord leadRecordList;
    @XmlElementRef(name = "dedupEnabled", type = JAXBElement.class, required = false)
    protected JAXBElement<Boolean> dedupEnabled;

    /**
     * Obtient la valeur de la propriété leadRecordList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfLeadRecord }
     *     
     */
    public ArrayOfLeadRecord getLeadRecordList() {
        return leadRecordList;
    }

    /**
     * Définit la valeur de la propriété leadRecordList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfLeadRecord }
     *     
     */
    public void setLeadRecordList(ArrayOfLeadRecord value) {
        this.leadRecordList = value;
    }

    /**
     * Obtient la valeur de la propriété dedupEnabled.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getDedupEnabled() {
        return dedupEnabled;
    }

    /**
     * Définit la valeur de la propriété dedupEnabled.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setDedupEnabled(JAXBElement<Boolean> value) {
        this.dedupEnabled = value;
    }

}
