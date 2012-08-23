/**
 * 
 */
package demographic

import support.identification.UIDBasedID
import demographic.party.Person
import demographic.role.Role
import demographic.role.RoleValidity
import com.thoughtworks.xstream.XStream

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 */
class LocalDemographicAccess extends DemographicAccess {


    /**
     * Encuentra otros identificadores de la persona, a partir de un identificador conocido.
     * Se corresponde a PIX ids query con un solo identificador como criterio de busqueda.
     */
    public List<UIDBasedID> findIdsById( UIDBasedID id )
    {
        // TODO
        // Buscar las personas con ese id
        // Devolver todos los ids de esas personas
        
        // OBS: si la base no tiene buena calidad puedo obtener varias personas.
        
        return []
    }
    
    /**
     * Encuentra otros identificadores de la persona, a partir de los identificadores conocidos.
     * Se corresponde a PIX ids query con muchos identificadores como criterio de busqueda.
     * El resultado debe ser el mismo que si se invoca findIdsById para cada id de ids y luego se hace merge de los resultados.
     */
    public List<UIDBasedID> findIdsByIds( List<UIDBasedID> ids )
    {
        // TODO
        return []
    }
    
    /**
     * Busca id (root y ext).
     */
    public List<Person> findPersonById( UIDBasedID id )
    {
        /* ids ya no es un atributo en la base, se codifica la lista a xml en el attr codedIds
        def candidatos = Person.withCriteria {
           ids {
              //like('value', '%'+id.extension+'%')
              //like('value', '%::%'+id.extension+'%')
              eq('value', id.value) //id.root+'::'+id.extension) // si no hago busqueda exacta con ids que uno es substring del otro me tira a las 2 personas, el id, si existe deberia tirar una sola.
           }
        }
        */
        
        // Hago la busqueda por codedIds
        //XStream xstream = new XStream()

        def candidatos = Person.withCriteria
        {
            //like('codedIds', '%'+ xstream.toXML(id) +'%') // esto no funciona correctamente si el xml esta identado, porque tiene distintos niveles de identacion la coleccion gaurdada y el id solo que se busca aca.
           
            // Para resolver lo de la identacion, busco solo por el value y listo
            // FIXME: la otra es guardan sin el pretty print, todo como un chorizo.
            like('codedIds', '%>'+ id.value +'<%')
        }
        
        // TODO: ver que es mas rapido si as Set as List o Collection.unique
        //return (candidatos as Set) as List // Saco duplicados
        return candidatos.unique{it.id}
    }
    
    /**
     * Los parametros pueden ser todos nulos.
     */
    public List<Person> findByPersonData( String pn, String sn, String pa, String sa,
                                          Date bithdate, String sex )
    {
        //if (n.primerNombre) println "Tiene primer nombre: " + n.primerNombre
       // if (n.segundoNombre) println "Tiene segundo nombre: " + n.segundoNombre
       // if (n.primerApellido) println "Tiene primer apellido: " + n.primerApellido
        //if (n.segundoApellido) println "Tiene segundo apellido: " + n.segundoApellido
        
        def persons = []
        if ( pn || sn || pa || sa || bithdate || sex )
        {
           // TODO: encontrar las personas con estos nombres
           persons = Person.withCriteria {

              if (pn)
                 like('primerNombre','%'+pn+'%')
              if (sn)
                 like('segundoNombre','%'+sn+'%')
              if (pa)
                 like('primerApellido','%'+pa+'%')
              if (sa)
                 like('segundoApellido','%'+sa+'%')
              if (bithdate)
                 eq('fechaNacimiento', bithdate)
              if (sex)
                 eq('sexo', sex)
           }
        }
        
        
        // TODO: la solucion es el intersec de persons_by_name y persons_by_bd_and_sex
        
        return persons
    }
    
    public List<Person> findByPersonDataAndId( String pn, String sn, String pa, String sa,
                                               Date bithdate, String sex, UIDBasedID id )
    {
        def result = []
        if ( pn || pa || bithdate || sex || id )
        {
           result = Person.withCriteria {
              
              if (id)
              {
                 /* Ahora hay que hacer la busqueda por codedIds
                 ids {
                   eq('value', id.value) //id.root+'::'+id.extension) // si no hago busqueda exacta con ids que uno es substring del otro me tira a las 2 personas, el id, si existe deberia tirar una sola.
                 }
                 */
                 like('codedIds', '%>'+ id.value +'<%')
              }
              
              if (pn)
                 like('primerNombre','%'+pn+'%')
              if (sn)
                 like('segundoNombre','%'+sn+'%')
              if (pa)
                 like('primerApellido','%'+pa+'%')
              if (sa)
                 like('segundoApellido','%'+sa+'%')
              if (bithdate)
                 eq('fechaNacimiento', bithdate)
              if (sex)
                 eq('sexo', sex)
           }
        }
        
        return result
    }
    
    public List<Person> findByPersonDataAndIds( String pn, String sn, String pa, String sa,
                                                Date bithdate, String sex, List<UIDBasedID> ids )
    {
        // TODO
        return []
    }
    
    public List<Person> findByPersonDataAndRole( String pn, String sn, String pa, String sa,
                                                 Date bithdate, String sex, Role role )
    {
        // TODO
        return []
    }
    
    /**
     * El rol debe ser especificado junto a alguno de los demas parametros para obtener algun resultado,
     * sin ese binomio minimo, no se devuelve ningun resultado. http://code.google.com/p/open-ehr-sa/issues/detail?id=61
     * 
     */
    public List<Person> findByPersonDataAndIdAndRole( String pn, String sn, String pa, String sa,
                                                      Date bithdate, String sex, UIDBasedID id, String roleType )
    {
        def result = []
        
        if ( (pn || sn || pa || sa || bithdate || sex || id) && roleType )
        {
           def result_roles = RoleValidity.withCriteria {

              role {
                 eq('type', roleType)
              }
              performer { // FIXME: deberia ser person si no no va a tener todos los atributos
                 
                 if (id)
                 {
                    /* Ahora hay que hacer la busqueda por codedIds
                    ids {
                       eq('value', id.value) //id.root+'::'+id.extension) // si no hago busqueda exacta con ids que uno es substring del otro me tira a las 2 personas, el id, si existe deberia tirar una sola.
                    }
                    */
                    like('codedIds', '%>'+ id.value +'<%')
                 }
                 
                 /*
                 if (names.size()>0)
                 {
                    or {
                       names.each{ name ->
                          identities {
                             eq('id', name.id)
                          }
                       }
                    }
                 }
                 */
                 if (pn) like('primerNombre','%'+pn+'%')
                 if (sn) like('segundoNombre','%'+sn+'%')
                 if (pa) like('primerApellido','%'+pa+'%')
                 if (sa) like('segundoApellido','%'+sa+'%')
                 if (bithdate) eq('fechaNacimiento', bithdate)
                 if (sex) eq('sexo', sex)
              }
           }
           
           result = result_roles.performer
        }
        
        return result
    }
    
    
    public List<Person> findByPersonDataAndIdsAndRole( String pn, String sn, String pa, String sa,
                                                       Date bithdate, String sex, List<UIDBasedID> ids, Role role )
    {
        // TODO
        return []
    }
}