
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ResultSyncLead complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ResultSyncLead">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="leadId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="syncStatus" type="{http://www.marketo.com/mktows/}SyncStatus"/>
 *         &lt;element name="leadRecord" type="{http://www.marketo.com/mktows/}LeadRecord" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultSyncLead", propOrder = {
    "leadId",
    "syncStatus",
    "leadRecord"
})
public class ResultSyncLead {

    protected int leadId;
    @XmlElement(required = true)
    protected SyncStatus syncStatus;
    @XmlElementRef(name = "leadRecord", type = JAXBElement.class, required = false)
    protected JAXBElement<LeadRecord> leadRecord;

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
     * Obtient la valeur de la propriété syncStatus.
     * 
     * @return
     *     possible object is
     *     {@link SyncStatus }
     *     
     */
    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    /**
     * Définit la valeur de la propriété syncStatus.
     * 
     * @param value
     *     allowed object is
     *     {@link SyncStatus }
     *     
     */
    public void setSyncStatus(SyncStatus value) {
        this.syncStatus = value;
    }

    /**
     * Obtient la valeur de la propriété leadRecord.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link LeadRecord }{@code >}
     *     
     */
    public JAXBElement<LeadRecord> getLeadRecord() {
        return leadRecord;
    }

    /**
     * Définit la valeur de la propriété leadRecord.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link LeadRecord }{@code >}
     *     
     */
    public void setLeadRecord(JAXBElement<LeadRecord> value) {
        this.leadRecord = value;
    }

}
