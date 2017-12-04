
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour SyncStatus complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="SyncStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="leadId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="status" type="{http://www.marketo.com/mktows/}LeadSyncStatus"/>
 *         &lt;element name="error" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SyncStatus", propOrder = {
    "leadId",
    "status",
    "error"
})
public class SyncStatus {

    protected int leadId;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected LeadSyncStatus status;
    @XmlElementRef(name = "error", type = JAXBElement.class, required = false)
    protected JAXBElement<String> error;

    /**
     * Obtient la valeur de la propriété leadId.
     * 
     */
    public int getLeadId() {
        return leadId;
    }

    /**
     * Définit la valeur de la propriété leadId.
     * 
     */
    public void setLeadId(int value) {
        this.leadId = value;
    }

    /**
     * Obtient la valeur de la propriété status.
     * 
     * @return
     *     possible object is
     *     {@link LeadSyncStatus }
     *     
     */
    public LeadSyncStatus getStatus() {
        return status;
    }

    /**
     * Définit la valeur de la propriété status.
     * 
     * @param value
     *     allowed object is
     *     {@link LeadSyncStatus }
     *     
     */
    public void setStatus(LeadSyncStatus value) {
        this.status = value;
    }

    /**
     * Obtient la valeur de la propriété error.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getError() {
        return error;
    }

    /**
     * Définit la valeur de la propriété error.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setError(JAXBElement<String> value) {
        this.error = value;
    }

}
