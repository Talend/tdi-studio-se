
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ResultListOperation complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ResultListOperation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="success" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="statusList" type="{http://www.marketo.com/mktows/}ArrayOfLeadStatus" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultListOperation", propOrder = {
    "success",
    "statusList"
})
public class ResultListOperation {

    protected boolean success;
    @XmlElementRef(name = "statusList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfLeadStatus> statusList;

    /**
     * Obtient la valeur de la propriété success.
     * 
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Définit la valeur de la propriété success.
     * 
     */
    public void setSuccess(boolean value) {
        this.success = value;
    }

    /**
     * Obtient la valeur de la propriété statusList.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfLeadStatus }{@code >}
     *     
     */
    public JAXBElement<ArrayOfLeadStatus> getStatusList() {
        return statusList;
    }

    /**
     * Définit la valeur de la propriété statusList.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfLeadStatus }{@code >}
     *     
     */
    public void setStatusList(JAXBElement<ArrayOfLeadStatus> value) {
        this.statusList = value;
    }

}
