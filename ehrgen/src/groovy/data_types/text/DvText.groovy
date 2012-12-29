package data_types.text

import data_types.basic.DataValue
import data_types.uri.DvURI
import data_types.text.CodePhrase
import data_types.text.TermMapping
//import support.definition.BasicDefinitions
//import datastructure.itemstructure.representation.Item

class DvText extends DataValue {

    //String formatting // A format string of the form “name:value; name:value...”, e.g. "font-weight : bold; font-family : Arial; font-size : 12pt;". Values taken from W3C CSS2 properties lists “background” and “font”.
    //DvURI hyperlink // Optional link sitting behind a section of plain text or coded term item.
    String value // Displayable rendition of the item, regardless of its underlying structure. For DV_CODED_TEXT, this is the rubric of the complete term as provided by the terminology service. No carriage returns, line feeds, or other non-printing characters permitted.

    //static hasMany = [mappings:TermMapping] // terms from other terminologies most closely matching this term, typically used where the originator (e.g. pathology lab) of information uses a local terminology but also supplies one or more equivalents from wellknown terminologies (e.g. LOINC).
    //List<TermMapping> mappings no se esta usando
    CodePhrase language
    CodePhrase encoding

    def DvText()
    {
       //mappings = []
    }
    
    public boolean validate()
    {
       if (!value)
       {
          //errors.addError('value', '', value)
          errors.rejectValue('value', "DvText.error.incomplete")
          return false
       }
       return true
    }
    
    /*
    static mapping = {
        value column: "dvtext_value"
        hyperlink cascade: "save-update"
        mappings cascade: "save-update"
        language cascade: "save-update"
        encoding cascade: "save-update"
    }

    static constraints = {
        value (nullable: false,
               blank: false,
               maxSize :1024*1024*10)
        formatting (nullable: true) // en gral no vamos a usar formatting
        hyperlink (nullable:true)
    }
    */
    
    String toString()
    {
       return "DvText "+ value
    }
}