
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Classe Java pour ParamsScheduleCampaign complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsScheduleCampaign">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="programName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cloneToProgramName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="campaignName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="campaignRunAt" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="programTokenList" type="{http://www.marketo.com/mktows/}ArrayOfAttrib" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsScheduleCampaign", propOrder = {
    "programName",
    "cloneToProgramName",
    "campaignName",
    "campaignRunAt",
    "programTokenList"
})
@XmlRootElement(name = "paramsScheduleCampaign")
public class ParamsScheduleCampaign {

    @XmlElement(required = true)
    protected String programName;
    @XmlElementRef(name = "cloneToProgramName", type = JAXBElement.class, required = false)
    protected JAXBElement<String> cloneToProgramName;
    @XmlElement(required = true)
    protected String campaignName;
    @XmlElementRef(name = "campaignRunAt", type = JAXBElement.class, required = false)
    protected JAXBElement<XMLGregorianCalendar> campaignRunAt;
    @XmlElementRef(name = "programTokenList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfAttrib> programTokenList;

    /**
     * Obtient la valeur de la propriété programName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProgramName() {
        return programName;
    }

    /**
     * Définit la valeur de la propriété programName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProgramName(String value) {
        this.programName = value;
    }

    /**
     * Obtient la valeur de la propriété cloneToProgramName.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCloneToProgramName() {
        return cloneToProgramName;
    }

    /**
     * Définit la valeur de la propriété cloneToProgramName.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCloneToProgramName(JAXBElement<String> value) {
        this.cloneToProgramName = value;
    }

    /**
     * Obtient la valeur de la propriété campaignName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCampaignName() {
        return campaignName;
    }

    /**
     * Définit la valeur de la propriété campaignName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCampaignName(String value) {
        this.campaignName = value;
    }

    /**
     * Obtient la valeur de la propriété campaignRunAt.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getCampaignRunAt() {
        return campaignRunAt;
    }

    /**
     * Définit la valeur de la propriété campaignRunAt.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setCampaignRunAt(JAXBElement<XMLGregorianCalendar> value) {
        this.campaignRunAt = value;
    }

    /**
     * Obtient la valeur de la propriété programTokenList.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAttrib }{@code >}
     *     
     */
    public JAXBElement<ArrayOfAttrib> getProgramTokenList() {
        return programTokenList;
    }

    /**
     * Définit la valeur de la propriété programTokenList.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAttrib }{@code >}
     *     
     */
    public void setProgramTokenList(JAXBElement<ArrayOfAttrib> value) {
        this.programTokenList = value;
    }

}
