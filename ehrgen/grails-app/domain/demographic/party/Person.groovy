package demographic.party

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 */
class Person extends Actor {

    Date fechaNacimiento // FIXME: DvDateTime
    String sexo
    String primerNombre
    String segundoNombre
    String primerApellido
    String segundoApellido
    
    
    String toString()
    {
        return primerNombre +' '+
               segundoNombre +' '+
               primerApellido +' '+
               segundoApellido +' '+
               fechaNacimiento +' '+
               '('+ sexo +')'
    }
    
    static String SEXO_MASCULINO = "M"
    static String SEXO_FEMENINO = "F"
    static List getSexCodes()
    {
    	return [SEXO_MASCULINO, SEXO_FEMENINO]
    }
   
    /*
    String toString()
    {
        return "Person: \n"+
               "  nombres: " + this.identities + "\n" +
               "  ids: " + this.ids + "\n" +
               "  fnac: " + this.fechaNacimiento + "\n" + 
               "  sexo: " + this.sexo
    }
    */
    
    
    // Todo es nullable porque puedo ir llenando de a poco.
    static constraints = {
       primerNombre(nullable:true)
       segundoNombre(nullable:true)
       primerApellido(nullable:true)
       segundoApellido(nullable:true)
       fechaNacimiento(nullable:true, max:new Date()) // Debe haber nacido antes de hoy
       sexo(nullable:true, inList:getSexCodes())
    }
}