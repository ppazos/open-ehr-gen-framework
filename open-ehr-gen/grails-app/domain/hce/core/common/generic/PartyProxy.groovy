package hce.core.common.generic

import hce.core.support.identification.PartyRef

class PartyProxy { // Abstracta

    PartyRef externalRef;

    static mapping = {
        externalRef cascade: "save-update"
    }

    static constraints = {
    }
}
