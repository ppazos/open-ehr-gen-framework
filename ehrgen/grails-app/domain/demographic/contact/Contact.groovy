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
package demographic.contact


/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 */
class Contact {

    // Intervalo de validez del medio de contacto: en el modelo de OpenEHR es un Interval<DvDate>
    Date timeValidityFrom
    Date timeValidityTo
    
    //Finalidad para la que este contacto se utiliza, por ejemplo,
    // "mail", "teléfono durante el día", etc. Tomado de valor del
    // atributo de nombre heredado.
    String purpose
    
    static hasMany = [addresses : Address]
    
}
