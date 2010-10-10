package hce.core.composition.content.entry

import hce.core.composition.content.ContentItem
import hce.core.data_types.text.*

class Entry extends ContentItem{

    CodePhrase language;
    CodePhrase encoding;
    //PartyProxy subject;
    //PartyProxy provider;
    //ObjectRef workflowId;
    //static hasMany = [otherParticipations:Participation]

    static mapping = {
        language cascade: "save-update"
        encoding cascade: "save-update"
    }

    static constraints = {
        language (nullvalue: false)
        encoding (nullvalue: false)
        //subject (nullvalue: false)
    }
}
