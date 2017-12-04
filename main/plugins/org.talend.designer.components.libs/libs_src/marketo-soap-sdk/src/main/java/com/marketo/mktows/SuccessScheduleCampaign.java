
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour SuccessScheduleCampaign complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="SuccessScheduleCampaign">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="result" type="{http://www.marketo.com/mktows/}ResultScheduleCampaign"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SuccessScheduleCampaign", propOrder = {
    "result"
})
@XmlRootElement(name = "successScheduleCampaign")
public class SuccessScheduleCampaign {

    @XmlElement(required = true)
    protected ResultScheduleCampaign result;

    /**
     * Obtient la valeur de la propriété result.
     * 
     * @return
     *     possible object is
     *     {@link ResultScheduleCampaign }
     *     
     */
    public ResultScheduleCampaign getResult() {
        return result;
    }

    /**
     * Définit la valeur de la propriété result.
     * 
     * @param value
     *     allowed object is
     *     {@link ResultScheduleCampaign }
     *     
     */
    public void setResult(ResultScheduleCampaign value) {
        this.result = value;
    }

}
