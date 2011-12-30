package hce.core.common.generic

import support.identification.PartyRef

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream

class PartyProxy { // Abstracta

    PartyRef externalRef
    String codedExternalRef

    static transients = ['externalRef', 'className']
    
    
    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       codedExternalRef = xstream.toXML(externalRef)
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       codedExternalRef = xstream.toXML(externalRef)
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedExternalRef) externalRef = xstream.fromXML(codedExternalRef)
    }
    
    
    static mapping = {
        //externalRef cascade: "save-update"
    }

    static constraints = {
       codedExternalRef(nullable:true)
    }
    
    // Solucion a http://old.nabble.com/Getting-Item_%24%24_javassist_165-from-ins.getClass%28%29.getSimpleName%28%29-td27317238.html
    // Con item.getClass().getSimpleName() obtengo Item_$$_javassist_165 en lugar de Cluster o Element
    String getClassName()
    {
        return this.getClass().getSimpleName()
    }
}