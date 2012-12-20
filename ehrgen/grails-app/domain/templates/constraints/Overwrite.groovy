/**
 * 
 */
package templates.constraints

// Esta clase se utilizara para sobreescribir nombres de unidades para mostrar
// las unidades que se usan en una institucion y que no sean compatibles con UCUM.

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 */
class Overwrite extends FieldConstraint {

    // TODO: como las unidades pueden ser una lista, aca deberia poner para 
    //       cada elemento de la lista con que se sobreescribe.
    String with
    
	static transients = ['process']
	
	static mapping = {
		with column: 'overwrite_with' // reserverd mysql, postgres, sqlserver, ...
    }
	
    Object process( Object value )
    {
        return this.with
    }
    
    String toString()
    {
        return "Overwrite: " + this.path + "->" + this.with
    }
}