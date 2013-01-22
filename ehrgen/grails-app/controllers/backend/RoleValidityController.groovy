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
package backend

import demographic.role.*
import demographic.party.Person

/**
 * En este controller se gestionan las asignaciones de roles a personas.
 * @author pab
 *
 */
class RoleValidityController {
   
   /**
    * Agrega un conjunto de roles a una persona, quitando antes los roles viejos.
    * id: identificador de la persona
    * id_roles: identificadores de los roles a asignar
    */
   def updateRoles = {
      
      // TODO
   }
   
   /**
    * id: identificador de la persona a la que se le va a crear el validity.
    */
   def create = {
      
      def person = Person.get(params.id)
      def validity = new RoleValidity() // Para ver los valores por defecto
      
      return [person: person, roleValidityInstance: validity]
   }
   
   /**
    * Crea un nuevo RoleValidity y se lo asocia a la persona. Se llama despues del create.
    * id: identiticador de la persona a asociar el RoleValidity
    */
   def save = {
      
      def person = Person.get(params.performer.id)
      def validity = new RoleValidity(params)
      //validity.performer = person // viene en params y se bindea solo
      
      person.addToRoles(validity)
      person.save()
      //validity.save()
      
      redirect(controller:'person', action:'show', id:person.id)
   }
   
   /**
    * Permite modificar un RoleValidity
    * id: identificador de la "validez" a cambiar
    */
   def edit = {
      
      def roleValidity = RoleValidity.get(params.id)
      def person = roleValidity.performer
      
      return [person: person, roleValidityInstance: roleValidity]
   }
   
   /**
    * Actualiza los datos de un RoleValidity de una persona, que se modificaron en el edit
    * id: identificador de la "validez" a modificada
    */
   def update = {
      
      // TODO: actaulizar validity
      def roleValidity = RoleValidity.get(params.id)
      roleValidity.properties = params
      
      roleValidity.save()
      
      redirect(controller:'person', action:'show', id:roleValidity.performer.id)
   }
   
   /**
    * Quita el RoleValidity de la persona.
    * TODO: eliminacion logica para log.
    * 
    * id: identificador de la "validez" a modificada
    */
   def delete = {
      
      def roleValidity = RoleValidity.get(params.id)
      def person = roleValidity.performer
      person.removeFromRoles(roleValidity)
      roleValidity.delete() // fisico
      
      redirect(controller:'person', action:'show', id:person.id)
   }
}