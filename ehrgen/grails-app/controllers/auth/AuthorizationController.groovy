package auth;

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