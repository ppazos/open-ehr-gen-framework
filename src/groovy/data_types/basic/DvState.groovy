package data_types.basic

import data_types.text.DvCodedText

class DvState extends DataValue {

    Boolean terminal // Indicates whether this state is a terminal state, such as “aborted”, “completed” etc from which no further transitions are possible.
    DvCodedText value // The state name. State names are determined by a state/event table defined in archetypes, and coded using openEHR Terminology or local archetype terms, as specified by the archetype

    public boolean validate()
    {
       boolean valid = true
       if (!terminal)
       {
          //errors.addError('terminal', 'DvState.terminal es vacio', terminal)
          errors.rejectValue('terminal', "DvState.error.incomplete")
          valid = false
       }
       if (!value)
       {
          errors.addError('value', 'DvState.value es vacio', value)
          valid = false
       }
       
       return valid
    }
    
    /*
    static mapping = {
        value column: "dvstate_value"
        value cascade: "save-update"
    }

    static constraints = {
        value (nullable: false)
        terminal (nullable: false)
    }
    */
}