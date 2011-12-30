package data_types.basic

class DvBoolean extends DataValue {

    //boolean value // Boolean value of this item.
    Boolean value // Para poder meterle null y que valide el GORM

    public boolean validate()
    {
       if (value == null)
       {
          //errors.addError('value', 'DvBoolean.value es vacio', value)
          errors.rejectValue('value', "DvBoolean.error.incomplete")
          return false
       }
       
       return true
    }
    
    //static final DvBoolean TRUE = new DvBoolean(value: true)
    //static final DvBoolean FALSE = new DvBoolean(value: false)

    /*
    static mapping = {
        value column: "dvboolean_value"
    }

    static constraints = {
        value (nullable: false)
    }
    */
}