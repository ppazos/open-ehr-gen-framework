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

import hce.core.*

/**
 * Archetypes act as the configuration basis for the particular
 * structures of instances defined by the reference model. To enable
 * archetypes to be used to create valid data, key classes in the
 * reference model act as "root" points for archetyping; accordingly,
 * these classes have the archetype_details attribute set.
 * An instance of the class <code>Archetyped</code> contains the
 * relevant archetype identification information, allowing generating
 * archetypes to be matched up with data instances.
 * <p/>
 * Instancias de esta clase son inmutables.
 *
 * @author Leandro Carrasco
 * @version 1.0
 */
class Archetyped {

    String archetypeId
    String templateId
    //String rmVersion // va a ser siempre 1.0.2

    static constraints = {
        archetypeId(nullable: false)
        //rmVersion(nullable: false, blank: false)
    }

    /*
    public boolean equals(Object o) {
        if (o == null) { return false; }
        if (o == this) { return true; }
        if (!( o instanceof Archetyped )) return false;
        return (this.archetypeId == ((Archetyped)o).archetypeId) && (this.templateId  == ((Archetyped)o).templateId) && (this.rmVersion  == ((Archetyped)o).rmVersion)
    }
    */
}