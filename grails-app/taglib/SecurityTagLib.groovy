
import authorization.*
import demographic.party.Person
import demographic.role.RoleValidity

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 */
class SecurityTagLib {
    
    /**
     * Verifica si hay algun DomainPermit para este dominio si no viene el templateId,
     * si vienen ambos, verifica si tiene permiso para ambos elementos.
     * 
     * domain: si no viene, toma el dominio que esta en la sesion.
     * templateId: opcional, si viene debe ser un template definido para el domain.
     */
    def hasDomainPermit = { attrs, body ->
       
       // Si no esta logueado, no tiene permisos
       if (!session.traumaContext.userId) return
       
       // Login del usuario logueado
       LoginAuth login = LoginAuth.get( session.traumaContext.userId )
       Person person = login.person
       
       // Criterio:
       // RoleValidity.person = person // quiero la validez del rol para cada rol de esta persona
       // para todas estas, pregunto si su rol tiene un DomainPermit para el dominio que viene como parametro, y opcionalmente considero el templateId.
       
       String domain = attrs.domain
       if (!domain) domain = session?.traumaContext?.domainPath
       if (!domain) throw new Exception('Debe venir un dominio como parametro o haber un dominio seleccionado en la sesion')
       
       //println "Domain: "+ domain
       //println "TemplateId: "+ attrs.templateId
       
       def criteria = RoleValidity.createCriteria()
       def permitCount = criteria.count {
          
          eq('performer', person) // performer del roleValidity
          role {
             domainPermits {
                
                // Se verifica solo si no viene templateId porque me indica si puedo o no
                // ingresar a ese dominio, luego dentro del dominio se chequean otros permisos.
                eq('domain', domain)
                
                if (attrs.templateId)
                {
                   or {
                      eq('templateId', '*') // Si es para todos los templates
                      eq('templateId', attrs.templateId)
                   }
                }
             }
          }
       }
       
       
       /* esta consulta no considera el usuario logueado ni sus roles asociados...
       def criteria = DomainPermit.createCriteria()
       def permitCount = criteria.count {
          
          // Se verifica solo si no viene templateId porque me indica si puedo o no
          // ingresar a ese dominio, luego dentro del dominio se chequean otros permisos.
          eq('domain', domain)
          
          if (attrs.templateId)
          {
             or {
                eq('templateId', '*') // Si es para todos los templates
                eq('templateId', attrs.templateId)
             }
          }
       }
       */
       
       //println "permitCount: "+ permitCount
       
       if (permitCount > 0)
       {
          out << body()
       }
    }
    
    /*
    def hasContentItemForTemplate = { attrs, body ->
        
        def composition = Composition.get( attrs.episodeId )
        if (!composition)
            throw new Exception("No se encuentra el episodio con id " + attrs.episodeId + " @TraumaTagLib 2")
        
        if (!attrs.templateId)
            throw new Exception("El templateId es obligatorio @TraumaTagLib 2")
        
        def item = hceService.getCompositionContentItemForTemplate(composition, attrs.templateId)

        // Mando un boolean como var para que en la vista pueda discutir si hay o no un item en la composition.
        //out << body(item!=null)
        
        out << body( hasItem:(item!=null), itemId:item?.id)
    }
    
    def reabrirEpisodio = { attrs, body ->
        def composition = Composition.get( attrs.episodeId )
        def version = Version.findByData( composition )
        if (version.lifecycleState == Version.STATE_SIGNED){
            out << body()
        }
    }
    */
}