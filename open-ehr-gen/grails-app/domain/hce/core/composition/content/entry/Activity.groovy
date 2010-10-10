package hce.core.composition.content.entry

import hce.core.datastructure.itemstructure.ItemStructure
import hce.core.data_types.encapsulated.DvParsable
import hce.core.common.archetyped.Locatable

class Activity extends Locatable {

    ItemStructure description
    DvParsable timing
    String action_archetype_id

    static mapping = {
        description cascade: "save-update"
        timing cascade: "save-update"
    }

    static constraints = {
        description (nullable: false)
        timing (nullable: false)
        action_archetype_id (nullable: false)
    }
}
