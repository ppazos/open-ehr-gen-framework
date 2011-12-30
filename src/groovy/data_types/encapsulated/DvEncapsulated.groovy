package data_types.encapsulated

import data_types.basic.DataValue
import data_types.text.CodePhrase
//import org.openehr.rm.support.terminology.OpenEHRCodeSetIdentifiers;
//import org.openehr.rm.support.terminology.TerminologyAccess;
//import org.openehr.rm.support.terminology.TerminologyService;

/**
 * Abstract class defining the common meta-data of all types of
 * encapsulated data.
 */
class DvEncapsulated extends DataValue {

    CodePhrase charset
    CodePhrase language
    int size

    /*
    static mapping = {
        charset cascade: "save-update"
        language cascade: "save-update"
    }

    static constraints = {
        charset(nullable:false) // null charset
        language(nullable:false) // null language
        size(min:0) // negative size
    }
    */
}