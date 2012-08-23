package hce.core.datastructure.history

import data_types.quantity.date_time.DvDateTime
import hce.core.datastructure.itemstructure.ItemStructure
import hce.core.common.archetyped.Locatable

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream
import data_types.basic.DataValue

class Event extends Locatable {

    DvDateTime time
    ItemStructure data
    //ItemStructure state

    String codedTime
    static transients = ['time']

    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedTime = xstream.toXML(time)
       codedName = xstream.toXML(name) // atributo de locatable
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedTime = xstream.toXML(time)
       codedName = xstream.toXML(name) // atributo de locatable
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedTime) time = xstream.fromXML(codedTime)
       if (codedName) name = xstream.fromXML(codedName)
    }
    
    static mapping = {
        //time column: "event_time"
        data column: "event_data"
        //time cascade: "save-update"
        data cascade: "save-update"
        //state column: "event_state"
        //state cascade: "save-update"
    }

    static constraints = {
        //time (nullable: false)
        data (nullable: false)
        codedTime (nullable: true, maxSize:4096)
    }
}