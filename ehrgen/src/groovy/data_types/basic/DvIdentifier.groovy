package data_types.basic

class DvIdentifier extends DataValue {

    String assigner; // Organisation that assigned the id to the item being identified.
    String code; // NOMBRE ORIGINAL: id // Organisation that assigned the id to the item being identified
    String issuer; // Authority which issues the kind of id used in the id field of this object.
    //String type; // The identifier type, such as “prescription”, or “SSN”. One day a controlled vocabulary might be possible for this.

    public boolean validate()
    {
       boolean valid = true
       if (!issuer)
       {
          //errors.addError('issuer', 'DvIdentifier.issuer es vacio', issuer)
          errors.rejectValue('issuer', "DvIdentifier.error.incomplete")
          valid = false
       }
       if (!assigner)
       {
          //errors.addError('assigner', 'DvIdentifier.assigner es vacio', assigner)
          errors.rejectValue('assigner', "DvIdentifier.error.incomplete")
          valid = false
       }
       if (!code) {
          //errors.addError('code', 'DvIdentifier.code es vacio', code)
          errors.rejectValue('code', "DvIdentifier.error.incomplete")
          valid = false
       }
       
       return valid
    }
    
    /*
    static constraints = {
        issuer (nullable: false, blank: false)
        assigner (nullable: false, blank: false)
        code (nullable: false, blank: false)
        //type (nullable: false, blank: false)
    }
    */
}