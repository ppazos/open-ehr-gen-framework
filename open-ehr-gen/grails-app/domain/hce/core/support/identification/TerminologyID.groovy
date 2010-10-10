package hce.core.support.identification

class TerminologyID extends ObjectID{

    // Identifier for terminologies such accessed via a terminology query service. In this class,
    // the value attribute identifies the Terminology in the terminology service, e.g. “SNOMED-CT”.
    // A terminology is assumed to be in a particular language, which must be explicitly specified.
    // The value if the id attribute is the precise terminology id identifier, including actual
    // release (i.e. actual “version”), local modifications etc; e.g. “ICPC2”

    String name
    String versionId // version

    static constraints = {
        name (nullable: false, blank: false)
        versionId (nullable: false)
    }
    
    /*
    String name(){ // Return the terminology id (which includes the “version” in some cases). Distinct names correspond to distinct (i.e. non-compatible) terminologies. Thus the names “ICD10AM” and “ICD10” refer to distinct terminologies.
        // TO DO
        return ""
    }

    String versionId(){ //Version of this terminology, if versioning supported, else the empty string.
        // TO DO
        return ""
    }
    */
}
