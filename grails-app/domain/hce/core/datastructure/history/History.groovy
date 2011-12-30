package hce.core.datastructure.history

import java.util.List
import data_types.quantity.date_time.DvDateTime
import hce.core.datastructure.DataStructure

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream
import data_types.basic.DataValue

class History extends DataStructure {

    DvDateTime origin
    DvDateTime period
    DvDateTime duration
    String codedOrigin
    String codedPeriod
    String codedDuration
    static transients = ['origin', 'period', 'duration']
    
    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedOrigin = xstream.toXML(origin)
       codedPeriod = xstream.toXML(period)
       codedDuration = xstream.toXML(duration)
       codedName = xstream.toXML(name) // atributo de locatable
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedOrigin = xstream.toXML(origin)
       codedPeriod = xstream.toXML(period)
       codedDuration = xstream.toXML(duration)
       codedName = xstream.toXML(name) // atributo de locatable
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedOrigin) origin = xstream.fromXML(codedOrigin)
       if (codedPeriod) period = xstream.fromXML(codedPeriod)
       if (codedDuration) duration = xstream.fromXML(codedDuration)
       if (codedName) name = xstream.fromXML(codedName)
    }
    
	 List events = [] // Para que guarden en orden
    static hasMany = [events:Event]

    static mapping = {
        //origin cascade: "save-update"
        //period cascade: "save-update"
        //duration cascade: "save-update"
        events cascade: "save-update"
    }

    static constraints = {
        //origin (nullable: false)
    }
}