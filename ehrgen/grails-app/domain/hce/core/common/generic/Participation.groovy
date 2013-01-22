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

import data_types.text.*
import data_types.quantity.DvInterval

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream
import data_types.basic.DataValue

class Participation {

    PartyProxy performer
    
    /* This attribute
    should be coded, but cannot be limited to the
    HL7v3:ParticipationFunction vocabulary, since it
    is too limited and hospital-oriented.
    */
    DvText function
    DvCodedText mode // coded by "openehr::parcitipation mode"
    DvInterval time

    String codedFunction
    String codedMode
    String codedTime
    static transients = ['function', 'mode', 'time']
    
    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedFunction = xstream.toXML(function)
       codedMode = xstream.toXML(mode)
       codedTime = xstream.toXML(time)
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedFunction = xstream.toXML(function)
       codedMode = xstream.toXML(mode)
       codedTime = xstream.toXML(time)
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedFunction) function = xstream.fromXML(codedFunction)
       if (codedMode) mode = xstream.fromXML(codedMode)
       if (codedTime) time = xstream.fromXML(codedTime)
    }
    
    static mapping = {
        performer cascade: "save-update"
        //function cascade: "save-update"
        //mode cascade: "save-update"
        //time cascade: "save-update"
    }

    static constraints = {
        performer (nullable: false)
        //function (nullable: false)
        //mode (nullable: false)
        //time (nullable: true)
    }
}