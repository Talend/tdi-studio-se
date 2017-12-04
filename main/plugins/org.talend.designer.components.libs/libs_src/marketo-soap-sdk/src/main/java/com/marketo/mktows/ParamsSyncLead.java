
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsSyncLead complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsSyncLead">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="leadRecord" type="{http://www.marketo.com/mktows/}LeadRecord"/>
 *         &lt;element name="returnLead" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="marketoCookie" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="moveCookie" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsSyncLead", propOrder = {
    "leadRecord",
    "returnLead",
    "marketoCookie",
    "moveCookie"
})
@XmlRootElement(name = "paramsSyncLead")
public class ParamsSyncLead {

    @XmlElement(required = true)
    protected LeadRecord leadRecord;
    protected boolean returnLead;
    @XmlElementRef(name = "marketoCookie", type = JAXBElement.class, required = false)
    protected JAXBElement<String> marketoCookie;
    @XmlElementRef(name = "moveCookie", type = JAXBElement.class, required = false)
    protected JAXBElement<Boolean> moveCookie;

    /**
     * Obtient la valeur de la propriété leadRecord.
     * 
     * @return
     *     possible object is
     *     {@link LeadRecord }
     *     
     */
    public LeadRecord getLeadRecord() {
        return leadRecord;
    }

    /**
     * Définit la valeur de la propriété leadRecord.
     * 
     * @param value
     *     allowed object is
     *     {@link LeadRecord }
     *     
     */
    public void setLeadRecord(LeadRecord value) {
        this.leadRecord = value;
    }

    /**
     * Obtient la valeur de la propriété returnLead.
     * 
     */
    public boolean isReturnLead() {
        return returnLead;
    }

    /**
     * Définit la valeur de la propriété returnLead.
     * 
     */
    public void setReturnLead(boolean value) {
        this.returnLead = value;
    }

    /**
     * Obtient la valeur de la propriété marketoCookie.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMarketoCookie() {
        return marketoCookie;
    }

    /**
     * Définit la valeur de la propriété marketoCookie.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMarketoCookie(JAXBElement<String> value) {
        this.marketoCookie = value;
    }

    /**
     * Obtient la valeur de la propriété moveCookie.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getMoveCookie() {
        return moveCookie;
    }

    /**
     * Définit la valeur de la propriété moveCookie.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setMoveCookie(JAXBElement<Boolean> value) {
        this.moveCookie = value;
    }

}
