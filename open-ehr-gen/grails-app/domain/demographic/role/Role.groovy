/**
 * 
 */
package demographic.role

import demographic.party.*

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 * 
 * Es necesario que Role decienda de Party para poder decir que un rol hizo algo
 * y saber que persona lo hizo a traves de su performer.
 */
class Role extends Party {
    
    // Roles predefinidos
    // FIXME: deberian ir en una tabla maestra
    static String PACIENTE       = 'paciente'
    static String MEDICO         = 'medico'
    static String ENFERMERIA     = 'enfermeria'
    static String ADMINISTRATIVO = 'administrativo'
    
    // Intervalo de validez del rol: en el modelo de OpenEHR es un Interval<DvDate>
    Date timeValidityFrom
    Date timeValidityTo
    
    // Actor que tiene este rol asignado
    Actor performer
    
    static hasMany = [capabilities:Capability]

    static constraints = {
        timeValidityTo(nullable:true)
    }
}
