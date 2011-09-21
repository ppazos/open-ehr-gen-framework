/**
 * 
 */
package authorization

import demographic.party.Person

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 */
class PersonAuth extends Authorization {

    Person person

    static constraints = {
       person(nullable: false)
    }
    static mapping = {
       person lazy:true // lazily fetch the person
    }
 
}