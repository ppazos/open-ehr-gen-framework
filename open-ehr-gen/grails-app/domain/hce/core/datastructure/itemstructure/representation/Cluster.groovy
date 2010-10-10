package hce.core.datastructure.itemstructure.representation

import java.util.List

class Cluster extends Item {
	
	List items // Para que guarden en orden
    static hasMany = [items:Item]

    static mapping = {
        items cascade: "save-update"
    }

    static constraints = {
        //items (minSize:1)
    }
}
