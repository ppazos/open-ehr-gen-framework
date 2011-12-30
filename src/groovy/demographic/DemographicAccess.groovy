/**
 * 
 */
package demographic

import support.identification.UIDBasedID

import demographic.party.Person
import demographic.role.Role

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 * Clase abstracta que modela el acceso a los datos demograficos.
 * Ese acceso se modela como el patron Strategy donde dependiendo
 * de la configuracion la consulta se hace sobre el sistema local
 * o sobre un servicio externo con interfaces PIX/PDQ.
 */
abstract class DemographicAccess {

    /**
     * Encuentra otros identificadores de la persona, a partir de un identificador conocido.
     * Se corresponde a PIX ids query con un solo identificador como criterio de busqueda.
     */
    abstract public List<UIDBasedID> findIdsById( UIDBasedID id );
    
    /**
     * Encuentra otros identificadores de la persona, a partir de los identificadores conocidos.
     * Se corresponde a PIX ids query con muchos identificadores como criterio de busqueda.
     * El resultado debe ser el mismo que si se invoca findIdsById para cada id de ids y luego se hace merge de los resultados.
     */
    abstract public List<UIDBasedID> findIdsByIds( List<UIDBasedID> ids );
    
    abstract public List<Person> findPersonById( UIDBasedID id );
    
    abstract public List<Person> findByPersonData( String pn, String sn, String pa, String sa,
                                                   Date bithdate, String sex );
    
    abstract public List<Person> findByPersonDataAndId( String pn, String sn, String pa, String sa,
                                                        Date bithdate, String sex, UIDBasedID id );
                                                     
    abstract public List<Person> findByPersonDataAndIds( String pn, String sn, String pa, String sa,
                                                         Date bithdate, String sex, List<UIDBasedID> ids );
    
    abstract public List<Person> findByPersonDataAndRole( String pn, String sn, String pa, String sa,
                                                          Date bithdate, String sex, Role role );
    
    abstract public List<Person> findByPersonDataAndIdAndRole( String pn, String sn, String pa, String sa,
                                                               Date bithdate, String sex, UIDBasedID id, String roleType );
                                                            
    abstract public List<Person> findByPersonDataAndIdsAndRole( String pn, String sn, String pa, String sa,
                                                                Date bithdate, String sex, List<UIDBasedID> ids, Role role );
}