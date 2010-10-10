package hce.core.common.archetyped

import hce.core.*;

/**
 * Padre abstracto de todas las clases cuyas instancias son accesibles por caminos (path), y
 * que saben cómo localizar objetos secundarios por caminos.
 *
 * @author Leandro Carrasco
 */

class Pathable extends RMObject{ // Abstracta

    Pathable parent;

    static mapping = {
        parent cascade: "save-update"
    }

    static constraints = {
    }

    /**
     * El item como una ruta (relativa a este item) solo valido para path unicos,
     * es decir, paths que resuelven items simples.
     *
     * @param path no nulo y unico
     * @return el item
     * @throws IllegalArgumentException si el path es invalido
     */
    Object itemAtPath(String path){} // Abstracta

     /**
     * Lista de items que corresponden a un path no unico.
     *
     * @param path no nulo y no unico
     * @return Los items
     */
    List<Object> itemsAtPath(String path){} // Abstracta

    /**
     * El path de un item relativo a la raiz de la estructura arquetipada.
     *
     * @param item no nulo
     */
    String pathOfItem(Pathable item){} // Abstracta

    /**
     * True si el path existe en los datos con respecto al item actual
     *
     * @param path no null o vacio
     * @return true si existe
     */
    boolean pathExists(String path){} // Abstracta

    /**
     * True si el path corresponde a un item simple en los datos.
     * @param path no nulo y existe
     * @return true si es unico
     */
    boolean pathUnique(String path){} // Abstracta

    boolean equals(Object o) {
        if (o == null) { return false; }
        if (o == this) { return true; }
        if (!( o instanceof Pathable )) return false;
        return (this.parent == ((Pathable)o).parent)
    }
}
