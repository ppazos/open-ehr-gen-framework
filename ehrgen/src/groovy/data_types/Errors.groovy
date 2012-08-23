package data_types

// intento de emuacion del errors de grails (spring)
// el tema es que no es 100% compatible entonces no muestra errores si no valida 

class Errors {

   // field -> lista de errores
   private errors = [:]
   
   public void addError(String field, String msg, Object rejectedValue)
   {
      // TODO: Objeto FieldError
      def error = [:]
      error[msg] = msg
      error[rejectedValue] = rejectedValue
      
      if (!errors[field]) errors[field] = []
      errors[field] << error
   }
   
   public Map getAll()
   {
      return errors
   }
   
   // Todos los errores para el campo
   public List getFieldError(String field)
   {
      return errors[field]
   }
   
   public boolean hasFieldErrors(String field)
   {
      return errors[field] != null
   }
   
   public boolean hasErrors()
   {
      return errors.size() > 0
   }
   
   // rejectedValue puede ser null
   public void rejectValue(String field, String msg, Object rejectedValue)
   {
      this.addError(field, msg, rejectedValue)
   }
   
   public void rejectValue(String field, String msg)
   {
      this.addError(field, msg, null)
   }
}