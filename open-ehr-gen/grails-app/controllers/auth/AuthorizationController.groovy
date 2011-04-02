package auth;

import auth.AuthorizationService
import util.HCESession


class AuthorizationController {
    
    def authorizationService
    
    def login = {
        
        if (params.doit)
        {
            def login = authorizationService.getLogin(params.user, params.pass)
            if (login)
            {
                // Pone al usuario en session
                session.traumaContext = new HCESession( userId: login.id )
                
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
        
        session.traumaContext = null
        redirect(action:'login')
    }
}
