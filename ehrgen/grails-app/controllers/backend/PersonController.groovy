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
import tablasMaestras.TipoIdentificador
import util.RandomGenerator
import support.identification.UIDBasedID

class PersonController {

   def index = {
      redirect(action:'list')
   }
   
   /**
    * Listado de personas.
    */
   def list = {
      
      def persons
      
      // TODO: paginacion...
      
      if (params.type) // Si viene el filtro por rol...
      {
         def roles
         if (!params.neg)
         {
            roles = RoleValidity.withCriteria {
               role {
                  eq('type', params.type)
               }
            }
         }
         else // viene neg, entonces quiere todos menos ese rol
         {
            roles = RoleValidity.withCriteria {
               role {
                  ne('type', params.type)
               }
            }
         }
         
         persons = roles.performer
      }
      else
      {
         persons = Person.list(params)
      }
      
      // TODO: paginacion
      /* listado de personas con usuario, todos menos los pacientes.
      def roles = RoleValidity.withCriteria {
         role {
            ne('type', Role.PACIENTE)
         }
      }
      */
      
      return [persons: persons]
   }
   
   /**
    * TODO: unificar con demographic.show
    * id: identificador de la persona a mostrar
    */
   def show = {
      
      def person = Person.get(params.id)
      return [person: person]
   }
   
   /**
    * Crea una nueva persona. La crea un administrador o la persona se registra para que el administrador la valide.
    * FIXME: ya existe la accion de agregarPaciente en demographicController, esta deberia hacer lo mismo solo que sin asignar rol. Se podria reutilizar el codigo comun por ejemplo en un servicio.
    */
   def create = {
      
      def person = new Person()
      return [personInstance:person]
   }
   
   /**
    * Guarda una persona creada.
    */
   def save = {
      
      def person = new Person(params)
      
      
      // Identificador
      // Mismo codigo que en DemographicController.agregarPaciente
      def id = null
      if (params.root == TipoIdentificador.AUTOGENERADO)
      {
          // Verificar si este ID existe, para no repetir
          def extension = RandomGenerator.generateDigitString(8)
          id = UIDBasedID.create(params.root, extension)
          
          // Se deberia hacer con doWhile para no repetir el codigo pero groovy no tiene doWhile
          

          def personsWithId = Person.withCriteria {
             like ('codedIds', '%>'+ id.value +'<%')
          }
          
          boolean existId = (personsWithId.size() > 0)
          
          while ( existId )
          {
              extension = RandomGenerator.generateDigitString(8)
              id = UIDBasedID.create(params.root, extension)
              
              personsWithId = Person.withCriteria {
                 like ('codedIds', '%>'+ id.value +'<%')
              }
              
              existId = (personsWithId.size() > 0)
          }
      }
      else
      {
          // Necesito los 2 datos para crear el id
          if (!params.extension || !params.root)
          {
             // Vuelve a la pagina
             flash.message = "identificador obligatorio, si no lo tiene seleccione 'Autogenerado' en el tipo de identificador"
             render(view:'create', model:[personInstance:person])
             return
          }
          
           id = UIDBasedID.create(params.root, params.extension) // TODO: if !hasExtension => error
           
           // FIXME: verificar que no hay otro paciente con el mismo id
           println "===================================================="
           println "Busco por id para ver si existe: " + id.value
           
           //def existId = UIDBasedID.findByValue(id.value)
           def personsWithId = Person.withCriteria {
              like ('codedIds', '%>'+ id.value +'<%')
           }
           
           boolean existId = (personsWithId.size() > 0)
           
           if (existId)
           {
               println "Ya existe!"
               flash.message = "Ya existe la persona con id: " + id.value + ", verifique el id ingresado o vuelva a buscar la persona"
               render(view:'create', model:[personInstance:person])
               return
           }
           else println "No existe!"
      }
      
      // Agrego el id a la persona
      person.addToIds(id)
      
      
      // Si le asigna rol
      if (params.role_type)
      {
         def role = Role.findByType(params.role_type)
         def validity = new RoleValidity(role:role, performer:person)
         person.addToRoles(validity)
      }
      
      if (!person.save())
      {
         // TODO: back y mostrar errores
         println person.errors
         render(view:'create', model:[personInstance:person])
         return
      }
      
      redirect(action:'show', id:person.id)
   }
   
   /**
    * Edita una persona.
    * FIXME: ya existe la accion edit en demographicController, esta deberia hacer lo mismo, se podria reutilizar el codigo haciendolo en un servicio.
    */
   def edit = {
      
      def person = Person.get(params.id)
      return [personInstance:person]
   }
   
   /**
    * Guarda la persona editada.
    */
   def update = {
      
      def person = Person.get(params.id)
      person.setProperties(params)
      if (!person.save())
      {
         render(view:'edit', model:[personInstance:person])
         return
      }
      
      redirect(action:'show', id:person.id)
   }
   
   
   /**
    * Da de baja una persona.
    * TODO: baja logica.
    */
   def delete = {
      
      // TODO
   }
}