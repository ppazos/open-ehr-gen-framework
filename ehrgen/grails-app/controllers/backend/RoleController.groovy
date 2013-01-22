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
//import authorization.Permit
import authorization.DomainPermit

class RoleController {

   def index = {
      redirect(action:list)
   }

   def list = {
      
      def list = Role.list(params)
      def total = Role.count()
      return [roleInstanceList : list, roleInstanceTotal: total]
   }
   
   def show = {
      
      def role = Role.get(params.id)
      //def permits = Permit.list()
      def domainPermits = DomainPermit.list()
      
      //return [roleInstance: role, permits: permits, domainPermits: domainPermits]
      return [roleInstance: role, domainPermits: domainPermits]
   }
   
   /*
   def create = {
      
   }
   */
   
   def save = {
      
      def role = new Role(params)
      if (!role.save())
      {
         render(view:'create', model:[roleInstance:role])
         return
      }
      redirect(action:'show', id:role.id)
   }
   
   def edit = {
      
      def role = Role.get(params.id)
      //def permits = Permit.list()
      def domainPermits = DomainPermit.list()
      
      //return [roleInstance: role, permits: permits, domainPermits: domainPermits]
      return [roleInstance: role, domainPermits: domainPermits]
   }
   
   /* id: identificador del rol a actualizar
    * permits: listado de controller__action (permit codificado)
    */
   def update = {
      
      // 1. Elimina los permits actuales del rol.
      // 2. Ve los permits que vienen, pide a la base y setea al rol.
      
      def role = Role.get(params.id)
      
      /*
      def permitsToRemove = []
      permitsToRemove.addAll( role.permits )
      permitsToRemove.each {
         role.removeFromPermits(it)
      }
      */
      
      def permitsToRemove = []
      permitsToRemove.addAll( role.domainPermits )
      permitsToRemove.each {
         role.removeFromDomainPermits(it)
      }
      
      def partes
      def permit
      
      // Permits
      /*
      params.list('permits').each { controllerAction ->
         
         partes = controllerAction.split("__")
         
         permit = Permit.findByControllerAndAction(partes[0], partes[1])
         role.addToPermits(permit)
      }
      */
      
      // DomainPermits
      params.list('dpermits').each { domainTemplateId ->
         
         partes = domainTemplateId.split("__")
         
         permit = DomainPermit.findByDomainAndTemplateId(partes[0], partes[1])
         role.addToDomainPermits(permit)
      }
      
      if (!role.save())
      {
         println role.errors
      }
      
      redirect(action:'show', id:params.id)
   }
}