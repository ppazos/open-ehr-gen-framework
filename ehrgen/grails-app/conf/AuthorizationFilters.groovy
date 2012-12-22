
class AuthorizationFilters {
   
   def openActions = ['authorization-login',
                  'authorization-logout']
   
   def filters = {
      
      loginCheck(controller:'*', action:'*')
      {
         before = {
            
            if( !session?.ehrSession?.userId &&
               !openActions.contains(controllerName+"-"+actionName) )
            {
               redirect(controller:'authorization', action:'login')
               return false
            }
         }
      }
      
      
      // Hay acciones que necesitan un dominio seleccionado para poder ejecutarse
      // es mas sencillo poner un chequeo aqui que en cada accion.
      // En ppio son las que usan vistas que usan layout ehr-modal o ehr.
      domainSelected(controller:'(demographic|records|guiGen)', action:'*')
      {
         before = {
            
            if ( !session.ehrSession?.domainId )
            {
              flash.message = 'Seleccione un dominio'
              redirect(controller:'domain', action:'list')
              return false
            }
         }
      }
      
      /*
      noCache(controller:'*', action:'*')
		{
         response.setHeader("Cache-Control",
                        "no-store")
      }
      */
      
   } 
}