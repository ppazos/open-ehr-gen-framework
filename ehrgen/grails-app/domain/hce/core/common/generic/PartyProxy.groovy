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