
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour MktowsContextHeaderInfo complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="MktowsContextHeaderInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="targetWorkspace" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MktowsContextHeaderInfo", propOrder = {
    "targetWorkspace"
})
@XmlRootElement(name = "MktowsContextHeader")
public class MktowsContextHeader {

    @XmlElement(required = true)
    protected String targetWorkspace;

    /**
     * Obtient la valeur de la propriété targetWorkspace.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetWorkspace() {
        return targetWorkspace;
    }

    /**
     * Définit la valeur de la propriété targetWorkspace.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetWorkspace(String value) {
        this.targetWorkspace = value;
    }

}
