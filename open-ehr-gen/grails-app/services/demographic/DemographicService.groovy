/**
 * 
 */
package demographic

import demographic.DemographicAccess
import demographic.LocalDemographicAccess

import hce.core.support.identification.UIDBasedID

import demographic.identity.PersonName
import demographic.party.Person
import demographic.role.Role

// Configuracion de consulta local o remota
import org.codehaus.groovy.grails.commons.ApplicationHolder

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 */
class DemographicService {

    DemographicAccess demographicAccess
    
    //int queryCount = 0
    
    // FIXME: ojo, capaz necesita instanciarse distinto por GRAILS, los servicesson singletons...
    def DemographicService()
    {
        if (ApplicationHolder.application.config.hce.patient_administration.serviceType.local)
        {
            println "DemographicService - PA LOCAL"
            // TODO: instanciar demographicAccess segun configuracion
            demographicAccess = new LocalDemographicAccess()
        }
        else
        {
            println "DemographicService - PA REMOTO"
            demographicAccess = new PixPdqDemographicAccess()
        }
    }
    
    /**
     * True si la persona tiene 1 id, primer nombre, primer apellido, sexo y fecha nacimiento.
     * @param p
     * @return
     */
    public boolean personHasAllData( Person p )
    {
        //println "Person: "+ p
        //println "Identities: " + p.identities
        
        if (!p.identities || p.identities.size()==0) return false
        
        def pn = p.identities.find{ it.purpose == 'PersonName' }
        if (!pn) return false
        
        return ( p.sexo && p.fechaNacimiento && pn.primerNombre && pn.primerApellido && p.ids.size()>0 )
    }
    
    /**
     * Encuentra otros identificadores de la persona, a partir de un identificador conocido.
     * Se corresponde a PIX ids query con un solo identificador como criterio de busqueda.
     */
    public List<UIDBasedID> findIdsById( UIDBasedID id )
    {
        //this.queryCount++
        //println queryCount
        return demographicAccess.findIdsById(id)
    }
    
    /**
     * Encuentra otros identificadores de la persona, a partir de los identificadores conocidos.
     * Se corresponde a PIX ids query con muchos identificadores como criterio de busqueda.
     * El resultado debe ser el mismo que si se invoca findIdsById para cada id de ids y luego se hace merge de los resultados.
     */
    public List<UIDBasedID> findIdsByIds( List<UIDBasedID> ids )
    {
        return demographicAccess.findIdsByIds(ids)
    }
    
    /**
     * Busca por extension, sin considerar root.
     */
    public List<Person> findPersonById( UIDBasedID id )
    {
        //this.queryCount++
        //println queryCount
        return  demographicAccess.findPersonById(id)
    }
    
    public List<Person> findByPersonData( PersonName n, Date bithdate, String sex )
    {
        //this.queryCount++
        //println queryCount
        return demographicAccess.findByPersonData(n, bithdate, sex)
    }
    
    public List<Person> findByPersonDataAndId( PersonName n, Date bithdate, String sex, UIDBasedID id )
    {
        return demographicAccess.findByPersonDataAndId(n, bithdate, sex, id)
    }
    
    public List<Person> findByPersonDataAndIds( PersonName n, Date bithdate, String sex, List<UIDBasedID> ids )
    {
        return demographicAccess.findByPersonDataAndIds(n, bithdate, sex, ids)
    }
    
    public List<Person> findByPersonDataAndRole( PersonName n, Date bithdate, String sex, Role role )
    {
        return demographicAccess.findByPersonDataAndRole(n, bithdate, sex, role)
    }
    
    public List<Person> findByPersonDataAndIdAndRole( PersonName n, Date bithdate, String sex, UIDBasedID id, String roleType )
    {
        return demographicAccess.findByPersonDataAndIdAndRole(n, bithdate, sex, id, roleType)
    }
    
    public List<Person> findByPersonDataAndIdsAndRole( PersonName n, Date bithdate, String sex, List<UIDBasedID> ids, Role role )
    {
        return demographicAccess.findByPersonDataAndIdsAndRole(n, bithdate, sex, ids, role)
    }
}
