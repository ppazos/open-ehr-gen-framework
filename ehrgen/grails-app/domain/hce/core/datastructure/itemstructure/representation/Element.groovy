/*
Copyright 2013 CaboLabs.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This software was developed by Pablo Pazos at CaboLabs.com

This software uses the openEHR Java Ref Impl developed by Rong Chen
http://www.openehr.org/wiki/display/projects/Java+Project+Download

This software uses MySQL Connector for Java developed by Oracle
http://dev.mysql.com/downloads/connector/j/

This software uses PostgreSQL JDBC Connector developed by Posrgresql.org
http://jdbc.postgresql.org/

This software uses XStream library developed by Jörg Schaible
http://xstream.codehaus.org/
*/
package hce.core.datastructure.itemstructure.representation

import hce.core.common.archetyped.Pathable;
import data_types.basic.*;
import data_types.text.*;

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream

class Element extends Item {

    // FIXME: por ahora no soportamos null_flavor, lo podemos sacar.
    DvCodedText null_flavor // flavour of null value, e.g. indeterminate, not asked etc 
    String codedNullFlavor
    
    // TEST
    // Prueba de guardar cualquier DataValue como un String XML
    DataValue value // Esta queda igual que antes
    String codedValue // Nuevo: donde guardar el string
    static transients = ['value', 'null_flavor'] // Nuevo: no quiero que se guarde value, quiero guardar codedValue
    
    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedValue = xstream.toXML(value)
       codedNullFlavor = xstream.toXML(null_flavor)
       codedName = xstream.toXML(name) // atributo de locatable
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedValue = xstream.toXML(value)
       codedNullFlavor = xstream.toXML(null_flavor)
       codedName = xstream.toXML(name) // atributo de locatable
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedValue) value = xstream.fromXML(codedValue)
       if (codedNullFlavor) null_flavor = xstream.fromXML(codedNullFlavor)
       if (codedName) name = xstream.fromXML(codedName)
    }
 

    static mapping = {
        //null_flavor cascade: "save-update"
        
        // TEST: value no se salva
        //value cascade: "save-update"
        codedValue column: "element_coded_value"
    }

    static constraints = {
       // TEST: la restriccion deberia ser sobre codedValue 
       value (nullable: false)
       // FIXME: debe tener value o nullflavor
       codedValue (maxSize: 10485760, nullable: true) // 1024*1024*10 // FIXME: si subo imagenes esto se puede quedar corto!
       codedNullFlavor (maxSize: 4096, nullable: true)
    }
}