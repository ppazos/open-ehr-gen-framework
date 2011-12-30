package hce.core.common.archetyped

import data_types.text.*;
import data_types.uri.*;
//import hce.core.RMObject;

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream

class Link { //extends RMObject {

    DvText meaning
    DvText type
    DvEHRURI target
    String codedMeaning
    String codedType
    String codedTarget
    static transients = ['meaning', 'type', 'target']

    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DvText.class, "errors");
       xstream.omitField(DvTEHRURI.class, "errors");
       codedMeaning = xstream.toXML(meaning)
       codedType = xstream.toXML(type)
       codedTarget = xstream.toXML(target)
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DvText.class, "errors");
       xstream.omitField(DvTEHRURI.class, "errors");
       codedMeaning = xstream.toXML(meaning)
       codedType = xstream.toXML(type)
       codedTarget = xstream.toXML(target)
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedMeaning) meaning = xstream.fromXML(codedMeaning)
       if (codedType) type = xstream.fromXML(codedType)
       if (codedTarget) target = xstream.fromXML(codedTarget)
    }
    
    /*
    static mapping = {
        meaning cascade: "save-update"
        type cascade: "save-update"
        target cascade: "save-update"
    }
    */
    
    static constraints = {
        //meaning (nullable: false)
        //type (nullable: false)
        //target (nullable: false)
    }

    public boolean equals(Object o) {
        if (o == null) { return false; }
        if (o == this) { return true; }
        if (!( o instanceof Link )) return false;
        Link l = (Link)o;
        return ((this.meaning == (l.meaning)) && (this.type == (l.type)) && (this.target  == (l.target)))
    }
}