package demographic.role

import demographic.party.*

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 */
class RoleValidity {
    
    // Intervalo de validez del rol: en el modelo de OpenEHR es un Interval<DvDate>
    Date timeValidityFrom = new Date()
    Date timeValidityTo
    boolean valid = true // Pasa a false manualmente o cuando se alcanza el timeValidityTo
    
    // Actor que tiene este rol asignado
    Actor performer
    static belongsTo = [performer: Actor]
    
    // Rol para el que se define la validez
    Role role

    static constraints = {
       timeValidityFrom(nullable: false)
       timeValidityTo(nullable: true)
       performer(nullable: false)
       role(nullable: false)
    }
}