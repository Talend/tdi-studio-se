
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsGetTags complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsGetTags">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tagList" type="{http://www.marketo.com/mktows/}ArrayOfTag" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsGetTags", propOrder = {
    "tagList"
})
@XmlRootElement(name = "paramsGetTags")
public class ParamsGetTags {

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
