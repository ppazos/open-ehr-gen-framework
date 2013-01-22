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
package auth

import auth.AuthorizationService
import util.EHRSession


class AuthorizationController {
   
   def authorizationService
   
   def login = {
      
      if (params.doit)
      {
         def login = authorizationService.getLogin(params.user, params.pass)
         if (login)
         {
            // Pone al usuario en session
            session.ehrSession = new EHRSession( userId: login.id )
            
            // FIXME: no puedo poner domain objects en session: http://grails.1312388.n4.nabble.com/Best-way-to-cache-some-domain-objects-in-a-user-session-td3820978.html
            //session.ehrSession = new EHRSession( login: login )
            
            //redirect(controller:'records', action:'list')
            redirect(controller:'domain', action:'list')
            return
         }
         else
         {
            // FIXME: i18n
            flash.message = "Login incorrrecto"
         }
      }
      return []
   }
   
   def logout = {
      
      session.ehrSession = null
      redirect(action:'login')
   }
   
   
   /**
    * Crea un nuevo LoginAuth para una persona, si ya tiene uno, elimina el anterior y crea el nuevo.
    * TODO: deberia enviar por email a la persona que se creo el usuario y cuales son los datos.
    */
   def create = {
      
      // TODO
   }
   
   /**
    * Sirve para que un usuario cambie su clave o que un administrador le asigne una nueva random.
    * El usuario debe estar logueado para poder hacer reset.
    */
   def resetPassword = {
      
      // TODO
   }
   
   /**
    * El usuario especifica su nombre de usuario o email, y el sistema le envia el correo con los datos de login.
    */
   def sendPassword = {
      
      // TODO
   }
}