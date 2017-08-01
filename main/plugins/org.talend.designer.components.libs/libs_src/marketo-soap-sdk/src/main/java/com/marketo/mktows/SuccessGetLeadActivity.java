
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour SuccessGetLeadActivity complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="SuccessGetLeadActivity">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="leadActivityList" type="{http://www.marketo.com/mktows/}LeadActivityList"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SuccessGetLeadActivity", propOrder = {
    "leadActivityList"
})
@XmlRootElement(name = "successGetLeadActivity")
public class SuccessGetLeadActivity {

    @XmlElement(required = true)
    protected LeadActivityList leadActivityList;

    /**
     * Obtient la valeur de la propriété leadActivityList.
     * 
     * @return
     *     possible object is
     *     {@link LeadActivityList }
     *     
     */
    public LeadActivityList getLeadActivityList() {
        return leadActivityList;
    }

    /**
     * Définit la valeur de la propriété leadActivityList.
     * 
     * @param value
     *     allowed object is
     *     {@link LeadActivityList }
     *     
     */
    public void setLeadActivityList(LeadActivityList value) {
        this.leadActivityList = value;
    }

}
