
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour AuthenticationHeaderInfo complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="AuthenticationHeaderInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mktowsUserId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="requestSignature" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="requestTimestamp" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="partnerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="audit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mode" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuthenticationHeaderInfo", propOrder = {
    "mktowsUserId",
    "requestSignature",
    "requestTimestamp",
    "partnerId",
    "audit",
    "mode"
})
@XmlRootElement(name = "AuthenticationHeader")
public class AuthenticationHeader {

    @XmlElement(required = true)
    protected String mktowsUserId;
    @XmlElement(required = true)
    protected String requestSignature;
    @XmlElement(required = true)
    protected String requestTimestamp;
    protected String partnerId;
    protected String audit;
    protected Integer mode;

    /**
     * Obtient la valeur de la propriété mktowsUserId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMktowsUserId() {
        return mktowsUserId;
    }

    /**
     * Définit la valeur de la propriété mktowsUserId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMktowsUserId(String value) {
        this.mktowsUserId = value;
    }

    /**
     * Obtient la valeur de la propriété requestSignature.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestSignature() {
        return requestSignature;
    }

    /**
     * Définit la valeur de la propriété requestSignature.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestSignature(String value) {
        this.requestSignature = value;
    }

    /**
     * Obtient la valeur de la propriété requestTimestamp.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    /**
     * Définit la valeur de la propriété requestTimestamp.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestTimestamp(String value) {
        this.requestTimestamp = value;
    }

    /**
     * Obtient la valeur de la propriété partnerId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPartnerId() {
        return partnerId;
    }

    /**
     * Définit la valeur de la propriété partnerId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPartnerId(String value) {
        this.partnerId = value;
    }

    /**
     * Obtient la valeur de la propriété audit.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAudit() {
        return audit;
    }

    /**
     * Définit la valeur de la propriété audit.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAudit(String value) {
        this.audit = value;
    }

    /**
     * Obtient la valeur de la propriété mode.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMode() {
        return mode;
    }

    /**
     * Définit la valeur de la propriété mode.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMode(Integer value) {
        this.mode = value;
    }

}
