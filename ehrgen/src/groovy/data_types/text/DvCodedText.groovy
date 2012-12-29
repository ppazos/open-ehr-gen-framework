package data_types.text

import data_types.text.CodePhrase
import data_types.text.DvText

class DvCodedText extends DvText {

    // A text item whose value must be the rubric from a controlled terminology, the key (i.e. the
    // ‘code’) of which is the defining_code attribute. In other words: a DV_CODED_TEXT is a
    // combination of a CODE_PHRASE (effectively a code) and the rubric of that term, from
    // a terminology service, in the language in which the data was authored.

    CodePhrase definingCode

    // FIXME: validar campos de DvText
    public boolean validate()
    {
       if (!definingCode)
       {
          errors.rejectValue('definingCode', "DvCodedText.error.incomplete")
          return false
       }
       if (!value)
       {
          errors.rejectValue('value', "DvCodedText.error.incomplete")
          return false
       }
       
       return true
    }
    
    /*
    static mapping = {
        definingCode cascade: "save-update"
    }

    static constraints = {
        definingCode (nullable: false)
    }
    */
    
    String toString()
    {
       return "DvCodedText "+ value +" "+ definingCode
    }
}