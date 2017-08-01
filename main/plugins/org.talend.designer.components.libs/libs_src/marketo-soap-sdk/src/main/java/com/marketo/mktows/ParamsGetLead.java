
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsGetLead complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsGetLead">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="leadKey" type="{http://www.marketo.com/mktows/}LeadKey"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsGetLead", propOrder = {
    "leadKey"
})
@XmlRootElement(name = "paramsGetLead")
public class ParamsGetLead {

    @XmlElement(required = true)
    protected LeadKey leadKey;

    /**
     * Obtient la valeur de la propriété leadKey.
     * 
     * @return
     *     possible object is
     *     {@link LeadKey }
     *     
     */
    public LeadKey getLeadKey() {
        return leadKey;
    }

    /**
     * Définit la valeur de la propriété leadKey.
     * 
     * @param value
     *     allowed object is
     *     {@link LeadKey }
     *     
     */
    public void setLeadKey(LeadKey value) {
        this.leadKey = value;
    }

}
