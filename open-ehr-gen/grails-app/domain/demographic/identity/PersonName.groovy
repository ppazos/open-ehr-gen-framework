/**
 * 
 */
package demographic.identity

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 */
class PersonName extends PartyIdentity {

    String primerNombre
    String segundoNombre
    String primerApellido
    String segundoApellido
    
    /*
    def personName()
    {
       // Para esta clase, purpose=='PersonName'
        this.purpose = "Nombre personal"
    }
    */
    
    String toString()
    {
        return "PersonName: " + 
               primerNombre +' '+ 
               segundoNombre +' '+ 
               primerApellido +' '+ 
               segundoApellido
    }
}
