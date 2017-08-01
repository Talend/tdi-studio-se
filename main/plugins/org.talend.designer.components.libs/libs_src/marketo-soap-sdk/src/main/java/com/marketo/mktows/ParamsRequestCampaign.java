
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsRequestCampaign complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsRequestCampaign">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="source" type="{http://www.marketo.com/mktows/}ReqCampSourceType"/>
 *         &lt;element name="campaignId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="leadList" type="{http://www.marketo.com/mktows/}ArrayOfLeadKey" minOccurs="0"/>
 *         &lt;element name="programName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="campaignName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "ParamsRequestCampaign", propOrder = {
    "source",
    "campaignId",
    "leadList",
    "programName",
    "campaignName",
    "programTokenList"
})
@XmlRootElement(name = "paramsRequestCampaign")
public class ParamsRequestCampaign {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ReqCampSourceType source;
    @XmlElementRef(name = "campaignId", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> campaignId;
    @XmlElementRef(name = "leadList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfLeadKey> leadList;
    @XmlElementRef(name = "programName", type = JAXBElement.class, required = false)
    protected JAXBElement<String> programName;
    @XmlElementRef(name = "campaignName", type = JAXBElement.class, required = false)
    protected JAXBElement<String> campaignName;
    @XmlElementRef(name = "programTokenList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfAttrib> programTokenList;

    /**
     * Obtient la valeur de la propriété source.
     * 
     * @return
     *     possible object is
     *     {@link ReqCampSourceType }
     *     
     */
    public ReqCampSourceType getSource() {
        return source;
    }

    /**
     * Définit la valeur de la propriété source.
     * 
     * @param value
     *     allowed object is
     *     {@link ReqCampSourceType }
     *     
     */
    public void setSource(ReqCampSourceType value) {
        this.source = value;
    }

    /**
     * Obtient la valeur de la propriété campaignId.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getCampaignId() {
        return campaignId;
    }

    /**
     * Définit la valeur de la propriété campaignId.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setCampaignId(JAXBElement<Integer> value) {
        this.campaignId = value;
    }

    /**
     * Obtient la valeur de la propriété leadList.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfLeadKey }{@code >}
     *     
     */
    public JAXBElement<ArrayOfLeadKey> getLeadList() {
        return leadList;
    }

    /**
     * Définit la valeur de la propriété leadList.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfLeadKey }{@code >}
     *     
     */
    public void setLeadList(JAXBElement<ArrayOfLeadKey> value) {
        this.leadList = value;
    }

    /**
     * Obtient la valeur de la propriété programName.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getProgramName() {
        return programName;
    }

    /**
     * Définit la valeur de la propriété programName.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setProgramName(JAXBElement<String> value) {
        this.programName = value;
    }

    /**
     * Obtient la valeur de la propriété campaignName.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCampaignName() {
        return campaignName;
    }

    /**
     * Définit la valeur de la propriété campaignName.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCampaignName(JAXBElement<String> value) {
        this.campaignName = value;
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
