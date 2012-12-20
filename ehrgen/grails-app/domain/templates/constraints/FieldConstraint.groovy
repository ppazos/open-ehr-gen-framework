/**
 * 
 */
package templates.constraints

import templates.ArchetypeField

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 */
class FieldConstraint { // No se debe instanciar directamente es "abstract"

    String path
    ArchetypeField owner
	
	static mapping = {
        table 'template_field_cnstrnt'
		owner column: 'cnstrnt_owner'  // reserved postgres
		path column: 'cnstrnt_path'    // reserved postgres, db2, ...
    }
}