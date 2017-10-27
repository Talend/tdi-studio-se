
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour MergeStatus complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="MergeStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="winningLeadId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="losingLeadIdList" type="{http://www.marketo.com/mktows/}ArrayOfInteger" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.marketo.com/mktows/}LeadMergeStatusEnum"/>
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
@XmlType(name = "MergeStatus", propOrder = {
    "winningLeadId",
    "losingLeadIdList",
    "status",
    "error"
})
public class MergeStatus {

    @XmlElementRef(name = "winningLeadId", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> winningLeadId;
    @XmlElementRef(name = "losingLeadIdList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfInteger> losingLeadIdList;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected LeadMergeStatusEnum status;
    @XmlElementRef(name = "error", type = JAXBElement.class, required = false)
    protected JAXBElement<String> error;

    /**
     * Obtient la valeur de la propriété winningLeadId.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getWinningLeadId() {
        return winningLeadId;
    }

    /**
     * Définit la valeur de la propriété winningLeadId.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setWinningLeadId(JAXBElement<Integer> value) {
        this.winningLeadId = value;
    }

    /**
     * Obtient la valeur de la propriété losingLeadIdList.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfInteger }{@code >}
     *     
     */
    public JAXBElement<ArrayOfInteger> getLosingLeadIdList() {
        return losingLeadIdList;
    }

    /**
     * Définit la valeur de la propriété losingLeadIdList.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfInteger }{@code >}
     *     
     */
    public void setLosingLeadIdList(JAXBElement<ArrayOfInteger> value) {
        this.losingLeadIdList = value;
    }

    /**
     * Obtient la valeur de la propriété status.
     * 
     * @return
     *     possible object is
     *     {@link LeadMergeStatusEnum }
     *     
     */
    public LeadMergeStatusEnum getStatus() {
        return status;
    }

    /**
     * Définit la valeur de la propriété status.
     * 
     * @param value
     *     allowed object is
     *     {@link LeadMergeStatusEnum }
     *     
     */
    public void setStatus(LeadMergeStatusEnum value) {
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
