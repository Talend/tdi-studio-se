
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ResultGetCampaignsForSource complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ResultGetCampaignsForSource">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="returnCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="campaignRecordList" type="{http://www.marketo.com/mktows/}ArrayOfCampaignRecord" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultGetCampaignsForSource", propOrder = {
    "returnCount",
    "campaignRecordList"
})
public class ResultGetCampaignsForSource {

    protected int returnCount;
    @XmlElementRef(name = "campaignRecordList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfCampaignRecord> campaignRecordList;

    /**
     * Obtient la valeur de la propriété returnCount.
     * 
     */
    public int getReturnCount() {
        return returnCount;
    }

    /**
     * Définit la valeur de la propriété returnCount.
     * 
     */
    public void setReturnCount(int value) {
        this.returnCount = value;
    }

    /**
     * Obtient la valeur de la propriété campaignRecordList.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfCampaignRecord }{@code >}
     *     
     */
    public JAXBElement<ArrayOfCampaignRecord> getCampaignRecordList() {
        return campaignRecordList;
    }

    /**
     * Définit la valeur de la propriété campaignRecordList.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfCampaignRecord }{@code >}
     *     
     */
    public void setCampaignRecordList(JAXBElement<ArrayOfCampaignRecord> value) {
        this.campaignRecordList = value;
    }

}
