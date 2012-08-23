package data_types.text

import data_types.basic.DataValue

class DvParagraph extends DataValue {

    List<DvText> items
    //static hasMany = [items:DvText]

    def DvParagraph()
    {
       items = []
    }
    
    public boolean validate()
    {
       if (items.size()==0)
       {
          //errors.addError('items', 'DvParagraph.items no tiene elementos', items)
          errors.rejectValue('items', "DvParagraph.error.incomplete")
          return false
       }
       return true
    }
    
    /*
    static mapping = {
        items cascade: "save-update"
    }

    static constraints = {
        items (nullable: false, minSize: 1)
    }
    */
}