package data_types.basic

//import hce.core.support.definition.BasicDefinitions
//import hce.core.datastructure.itemstructure.representation.Element
import data_types.Errors

// Para los errores de validacion
import org.springframework.validation.BindException

class DataValue { //extends BasicDefinitions { // Abstract

   //public errors = new Errors()
   
   // Prueba con los errors que usa grails
   // http://static.springsource.org/spring/docs/1.2.x/api/org/springframework/validation/BindException.html
   // BindException(Object target, String objectName)
   public errors = new BindException(this, this.getClass().getSimpleName())
   
   
   // Esta la deben implementar las subclases
   public boolean validate() { return true }
   
   // Serves as a common ancestor of all data value types in openEHR models
   /*
   static mapping = {
      table 'data_value'
   }
   
   static transients = ["className"]
   */
   
   String getClassName()
   {
       return this.getClass().getSimpleName()
   }
}