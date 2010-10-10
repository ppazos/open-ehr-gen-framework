package hce.core.composition.content.entry

import hce.core.datastructure.itemstructure.ItemStructure

class AdminEntry extends Entry {

    ItemStructure data

    static mapping = {
        data cascade: "save-update"
    }

    static constraints = {
        data(nullable:false)
    }
}
