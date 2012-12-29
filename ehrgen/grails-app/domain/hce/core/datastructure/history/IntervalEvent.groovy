package hce.core.datastructure.history

import data_types.quantity.date_time.DvDuration
import data_types.text.DvCodedText

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream
import data_types.basic.DataValue

class IntervalEvent extends Event { // abstract // representa PointEvent

    DvDuration width         // 1..1
    int sampleCount          // 0..1
    
    // "maximum", "mean", "minimum" (es un agregador de valorres en event.data, que indica que los valores corresponden al maximo, promedio, minimo, etc. de la lectura en este periodo de tiempo)
    // Esta codificado en la terminologia de openEHR
    // TODO: se debe crear un control en la GUI para ingresar este valor, la restriccion de los valores posibles se pone en el arquetipo. Si hay una sola opcion, hacer lo mismo que con las unidades de quantity, mostrar una label.
    DvCodedText mathFunction // 1..1 

    String codedWidth
    String codedMathFunction
    static transients = ['width', 'mathFunction']

    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors")
       codedWidth = xstream.toXML(width)
       codedMathFunction = xstream.toXML(mathFunction)
       codedTime = xstream.toXML(time) // event
       codedName = xstream.toXML(name) // atributo de locatable
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors")
       codedWidth = xstream.toXML(width)
       codedMathFunction = xstream.toXML(mathFunction)
       codedTime = xstream.toXML(time) // event
       codedName = xstream.toXML(name) // atributo de locatable
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedWidth) width = xstream.fromXML(codedWidth)
       if (codedMathFunction) mathFunction = xstream.fromXML(codedMathFunction)
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
        codedMathFunction(maxSize:4096)
    }
}