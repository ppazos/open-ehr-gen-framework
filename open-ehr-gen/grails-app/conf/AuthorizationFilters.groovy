
class AuthorizationFilters {
    
    def openActions = ['authorization-login',
                       'authorization-logout']
    
    def filters = {
        
        loginCheck(controller:'*', action:'*')
        {
            before = {
                
                if( !session?.traumaContext?.userId &&
                    !openActions.contains(controllerName+"-"+actionName) )
                {
                    redirect(controller:'authorization', action:'login')
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
