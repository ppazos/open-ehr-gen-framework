/**
 * 
 */
package demographic.party



/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 */
class Person extends Actor {

    Date fechaNacimiento // FIXME: DvDateTime
    String sexo
    
    static String SEXO_MASCULINO = "M"
    static String SEXO_FEMENINO = "F"

    static List getSexCodes()
    {
    	return [SEXO_MASCULINO, SEXO_FEMENINO]
    }
   
    String toString()
    {
        return "Person: \n"+
               "  nombres: " + this.identities + "\n" +
               "  ids: " + this.ids + "\n" +
               "  fnac: " + this.fechaNacimiento + "\n" + 
               "  sexo: " + this.sexo
    }
    
    static constraints = {
        fechaNacimiento(nullable:true)
        sexo(nullable:true)
    }
}
