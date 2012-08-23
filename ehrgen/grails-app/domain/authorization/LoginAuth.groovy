package authorization

class LoginAuth extends PersonAuth  {
    
    String user
    String pass
    
    // TODO: user solo caracteres
    // TODO: pass se debe pasar a md5 para guardar
    
    static mapping = {
       user column: "usr" // en pg user es keyword
    }
}
