/**
 * 
 */
package demographic.party

import demographic.role.RoleValidity

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 * Esta clase modela una entidad que puede tener varios roles.
 */
class Actor extends Party {

    //static hasMany = [roles: Role]
    // Ahora lo que tiene son RoleValidity
    List roles = []
    static hasMany = [roles: RoleValidity]
}