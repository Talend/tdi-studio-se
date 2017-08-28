
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ResultGetChannels complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ResultGetChannels">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tags" type="{http://www.marketo.com/mktows/}ArrayOfTagStatus"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultGetChannels", propOrder = {
    "tags"
})
public class ResultGetChannels {

    @XmlElement(required = true)
    protected ArrayOfTagStatus tags;

    /**
     * Obtient la valeur de la propriété tags.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfTagStatus }
     *     
     */
    public ArrayOfTagStatus getTags() {
        return tags;
    }

    /**
     * Définit la valeur de la propriété tags.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfTagStatus }
     *     
     */
    public void setTags(ArrayOfTagStatus value) {
        this.tags = value;
    }

}
