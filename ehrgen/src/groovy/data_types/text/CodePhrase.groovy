package data_types.text

import data_types.basic.DataValue;
import support.identification.TerminologyID

class CodePhrase extends DataValue {

    String codeString // The key used by the terminology service to identify a concept or coordination of concepts. This string is most likely parsable inside the terminology service, but nothing can be assumed about its syntax outside that context. 
    TerminologyID terminologyId // Identifier of the distinct terminology from which the code_string (or its elements) was extracted. 

    public boolean validate()
    {
       boolean valid = true
       if (!codeString)
       {
          //errors.addError('codeString', '', codeString)
          errors.rejectValue('codeString', "CodePhrase.error.incomplete")
          valid = false
       }
       if (!terminologyId)
       {
          //errors.addError('terminologyId', '', terminologyId)
          errors.rejectValue('terminologyId', "CodePhrase.error.incomplete")
          valid = false
       }
       
       return valid
    }
    
    /*
    static mapping = {
        terminologyId cascade: "save-update"
    }

    static constraints = {
        codeString (nullable: false, blank:false)
    	terminologyId (blank:false)
    }
    */
    
    String toString()
    {
       return "CodePhrase "+ codeString + " " + terminologyId.name
    }
}