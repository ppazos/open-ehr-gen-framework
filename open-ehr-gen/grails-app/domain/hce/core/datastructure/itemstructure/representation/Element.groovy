package hce.core.datastructure.itemstructure.representation

import hce.core.data_types.basic.*;
import hce.core.data_types.text.*;

class Element extends Item{

    DvCodedText null_flavor // flavour of null value, e.g. indeterminate, not asked etc 
    DataValue value

    static mapping = {
        null_flavor cascade: "save-update"
        value cascade: "save-update"
    }

    static constraints = {
        value (nullable: true)
    }
    
}
