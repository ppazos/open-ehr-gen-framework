/**
 * 
 */
package templates.controls

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 */
class Control {

    String type // FIXME: deberia ser un enumerado
    String path // Campo particular al que se aplica
    
    static mapping = {
      type column: 'control_type' // reserved DB2
      path column: 'control_path' // reserved DB2, postgres
   }
    
    String toString()
    {
       return "Control: " + type + " ["+path+"]"
    }
}