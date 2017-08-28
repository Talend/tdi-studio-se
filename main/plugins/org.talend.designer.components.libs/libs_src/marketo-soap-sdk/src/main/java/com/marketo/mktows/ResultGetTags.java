
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ResultGetTags complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ResultGetTags">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tagList" type="{http://www.marketo.com/mktows/}ArrayOfTag"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultGetTags", propOrder = {
    "tagList"
})
public class ResultGetTags {

    @XmlElement(required = true)
    protected ArrayOfTag tagList;

    /**
     * Obtient la valeur de la propriété tagList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfTag }
     *     
     */
    public ArrayOfTag getTagList() {
        return tagList;
    }

    /**
     * Définit la valeur de la propriété tagList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfTag }
     *     
     */
    public void setTagList(ArrayOfTag value) {
        this.tagList = value;
    }

}
