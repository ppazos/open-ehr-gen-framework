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