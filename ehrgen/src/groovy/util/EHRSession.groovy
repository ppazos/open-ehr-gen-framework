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
package util

import java.io.Serializable;
import authorization.LoginAuth
import demographic.party.Person

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 */
class EHRSession implements Serializable {

   // Se usa Long en lugar de long para poder poner null

   // No lo uso, saco al paciente del episodio... serviria para acelerar 
   // el sacado del paciente del episodio y se setearia al seleccionar un
   // paciente o un episodio que tiene un paciente seleccionado.
   
   // Identificador en la DB del dominio actual
   Long domainId
	
   // Identificador en la DB del workflow seleccionado segun el rol del usuario
   Long workflowId
    
   // Id del paciente seleccionado
   Long patientId // parece que no se usa... la idea era: si habia un paciente seleccionado, que en el listado se mostraran solo los registros de este paciente.
    
   // FIXME: no puedo poner domain objects en session: http://grails.1312388.n4.nabble.com/Best-way-to-cache-some-domain-objects-in-a-user-session-td3820978.html
   //Person patient // se setea al seleccionar al paciente en DemographicController
    
   //Long pacienteId // Identificador del paciente (uno de sus Ids), no es el id en la base.
   Long episodioId   // Identificador en la base de la composition que modela el registro del episodio.
   
   // TODO: cambiar por loginId y que userId sea la persona logueada
   Long userId       // Identificador en la base del Login del usuario logueado en este momento.
   
   // FIXME: no puedo poner domain objects en session: http://grails.1312388.n4.nabble.com/Best-way-to-cache-some-domain-objects-in-a-user-session-td3820978.html
   //LoginAuth login // antes userId == login.id
    
   /**
    * Devuelve la persona logueada a partir del id del login.
    * @return
    */
   Person getLoggedPerson()
   {
      def login = LoginAuth.get(userId)
      return login?.person
   }
   
   String toString()
   {
      return "domainId: $domainId\nworkflowId: $workflowId\npatientId: $patientId\nepisodioId: $episodioId\nuserId: $userId\n"
   }
}