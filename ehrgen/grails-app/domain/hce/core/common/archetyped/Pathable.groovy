/*
Copyright 2013 CaboLabs.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This software was developed by Pablo Pazos at CaboLabs.com

This software uses the openEHR Java Ref Impl developed by Rong Chen
http://www.openehr.org/wiki/display/projects/Java+Project+Download

This software uses MySQL Connector for Java developed by Oracle
http://dev.mysql.com/downloads/connector/j/

This software uses PostgreSQL JDBC Connector developed by Posrgresql.org
http://jdbc.postgresql.org/

This software uses XStream library developed by Jörg Schaible
http://xstream.codehaus.org/
*/
package hce.core.common.archetyped

import hce.core.composition.Composition

/**
 * Padre abstracto de todas las clases cuyas instancias son accesibles por caminos (path), y
 * que saben cómo localizar objetos secundarios por caminos.
 *
 * @author Leandro Carrasco
 */
class Pathable { // Abstracta

   /**
   * Separator used to delimit segments in the path
   */
   static final String PATH_SEPARATOR = "/";
   static final String ROOT = PATH_SEPARATOR;

   //Pathable parent
   // Si pongo Pathable, genera mal el esquema de la BD, confunde esta relacion
   // con la columna 'parent' para manejar relaciones one to many.
   Long rmParentId // Cumple el rol de Pathable parent
   String path

   
   // =====================================================================================
   // Implementacion particular de EHRGen para poder realizar busquedas semanticas
   // y relacionar bojetos del rm con distintas paths y saber si pertenecen a la misma
   // estructura de registro: COMPOSITION. Los objetos de la misma composition se podran
   // mostrar juntos y bajo el mismo marco de tiempo.
   // Todos los objetos del RM tendran parentComposition menos las compositions.
   
   // Guardar la composicion en los objetos del RM me da un error en backred entre Composition y Section.
   // Voy a guardar solo el id de la composition, eso es suficiente.
   //Composition parentComposition
   Long parentCompositionId
   
   
   
   // El atributo parent y su persistencia se maneja a mano, no se deja que GRAILS guarde o actualiza el parent.
   // rmParentId definido en Pathable
   // Si no se pone tira except property [rmParent] not found on entity
   static transients = ['padre']
   
   // Si pongo Object en lugar de Pathable, funciona, si no tira mil errores de que no
   // encuentra la propiedad 'padre' en las subclases
   Object getPadre()
   {
     if (!this.rmParentId) return null
     return Pathable.get(this.rmParentId)
   }
   
   void setPadre(Object parent) // Object debe ser Pathable!
   {
     if (!parent) throw new Exception("parent no puede ser nulo")
     if (!parent.id) throw new Exception("parent debe tener id (debe guardarse previamente en la base)")
     this.rmParentId = parent.id
   }
   
   static mapping = {
      //parent cascade: "save-update" // yo no deberia salvar a mi parent.
      //parent column:'parent_id'
      path column:'pathable_path'
   }

   static constraints = {
      rmParentId(nullable: true)
      parentCompositionId(nullable: true)
   }

   /**
    * El item como una ruta (relativa a este item) solo valido para path unicos,
    * es decir, paths que resuelven items simples.
    *
    * @param path no nulo y unico
    * @return el item
    * @throws IllegalArgumentException si el path es invalido
    */
   //Object itemAtPath(String path){} // Abstracta

    /**
    * Lista de items que corresponden a un path no unico.
    *
    * @param path no nulo y no unico
    * @return Los items
    */
   //List<Object> itemsAtPath(String path){} // Abstracta

   /**
    * El path de un item relativo a la raiz de la estructura arquetipada.
    *
    * @param item no nulo
    */
   //String pathOfItem(Pathable item){} // Abstracta

   /**
    * True si el path existe en los datos con respecto al item actual
    *
    * @param path no null o vacio
    * @return true si existe
    */
   //boolean pathExists(String path){} // Abstracta

   /**
    * True si el path corresponde a un item simple en los datos.
    * @param path no nulo y existe
    * @return true si es unico
    */
   //boolean pathUnique(String path){} // Abstracta

   /*
   boolean equals(Object o) {
      if (o == null) { return false; }
      if (o == this) { return true; }
      if (!( o instanceof Pathable )) return false;
      return (this.parent == ((Pathable)o).parent)
   }
   */
}