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

import support.identification.*
import data_types.text.*
import data_types.basic.DataValue

//import java.lang.reflect.Method;
import java.util.*;

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream

/**
 * Root structural class of all information models. Instances of
 * this class are immutalbe.
 *
 * @author Leandro Carrasco
 * @version 1.0
 */
// Pablo: prueba para agregar el atributo Pathable.parent a los nodos del RM
class Locatable extends Pathable {

    //UIDBasedID uid // Optional globally unique object identifier for root points of archetyped structures.
    String uid = (java.util.UUID.randomUUID() as String)

    String archetypeNodeId // Id del nodo (ej. at0001) -->  arquetipo.node(path).nodeID
    DvText name // Termino asociado al archetypeNodeId --> termino pedido a la ontologia en base a archetypeNodeId
    String codedName
    
    // Es != null para las raices de estructuras del rm.
    Archetyped archetypeDetails // Tiene ArqchetypeId, TemplateId y VersionID
    
    //FeederAudit feederAudit;
    //Set<Link> links;
    
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedName = xstream.toXML(name)
       //println "beforeInsert codedName " + codedName
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedName = xstream.toXML(name)
       //println "beforeUpdate codedName " + codedName
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedName) name = xstream.fromXML(codedName)
    }
    
    // Guarda los datos de archetypeDetails en la misma tabla que locatable, ahorra un join.
    static embedded = ['archetypeDetails']
    
    static mapping = {
        //name cascade: "save-update"
        archetypeDetails cascade: "save-update"
        uid column: "locatable_uid" //uid reservado en Oracle
        //table 'locatable' // pab
    }

    static constraints = {
        //archetypeNodeId (nullable: false)
        //name (nullable: false)
        //links (nullable: false, minSize: 1)
        archetypeDetails(nullable:true)
        codedName(nullable:true, maxSize:4096) // nullable para que pase validacion
    }

    // Solucion a http://old.nabble.com/Getting-Item_%24%24_javassist_165-from-ins.getClass%28%29.getSimpleName%28%29-td27317238.html
    // Con item.getClass().getSimpleName() obtengo Item_$$_javassist_165 en lugar de Cluster o Element
    static transients = ["className", "name"]
    String getClassName()
    {
        return this.getClass().getSimpleName()
    }
    
    /**
     * True if this node is the root of an archetyped structure
     *
     * @return true if archetype root
     */
    /*boolean isArchetypeRoot() {
        return archetypeDetails != null;
    }*/

    /**
     * String The path to an item relative to the root of this
     * archetyped structure.
     *
     * @param item
     * @return string path
     */
    //String pathOfItem(Pathable item) {}; // Abstracta



    /*
     * Simple fix doesn't take care of "/" inside predicates
     * e.g. data/events[at0006 and name/value='any event']
     */
    /*
    private List<String> dividePathIntoSegments(String path) { // Para "a/b/c/d" Retorna una lista con los strings "a", "b", "c" y "d"
        List<String> segments = new ArrayList<String>();
        StringTokenizer tokens = new StringTokenizer(path, "/");
        while(tokens.hasMoreTokens()) {
            segments.add(tokens.nextToken());
        }
        return segments;
    }
    */

    /*
     * generic path evaluation covers all rmClass
     */
    /*private Object genericPathEvaluator(String path, Object object) { // Dada una ruta y un objeto, retorna la evaluacion de la ruta sobre el objeto
        if(path == null || object == null) {
            return null;
        }
        List<String> segments = dividePathIntoSegments(path);
        return evaluatePathSegment(segments, object);
    }*/

    /*
     * Evaluate recursively the path segments
     */
    /*private Object evaluatePathSegment(List<String> pathSegments, Object object) {
        if(pathSegments.isEmpty()) {
            return object;
        }
        String pathSegment = pathSegments.remove(0);
        Object value =  null;

        int index = pathSegment.indexOf("[");
        String expression = null;
        String attributeName = null;

        // has [....] predicate expression
        if(index > 0) {
            assert(pathSegment.indexOf("]") > index);

            // Obtengo el nombre del atributo (esta desde el inicio hasta el "[")
            attributeName = pathSegment.substring(0, index);
            // Obtengo la expresion que esta luego del atributo (esta desde el "[" y el "]")
            expression = pathSegment.substring(index + 1, pathSegment.indexOf("]"));
        } else {
            attributeName = pathSegment;
        }

        value = retrieveAttributeValue(object, attributeName);
        if(expression != null && value != null ) {
            value = processPredicate(expression, value);
        }
        if(value != null) {
            return evaluatePathSegment(pathSegments, value); // Lamada recursiva
        }
        return null;
    }*/

    /**
     * Processes the predicate expression on given object
     * 1. if the object is a container, select the _first_ matching one
     * 2. only return the object if itself meets the predicate
     *
     * only shortcut expressions for at0000 and name are supported
     * for example: [at0001, 'node name']
     *
     * @param expression
     * @param value
     * @return null if there is no match
     */
    /*Object processPredicate(String expression, Object object) {
        String name = null;
        String archetypeNodeId = null;
        expression = expression.trim();
        int index;

        // [at0001, 'standing']
        if(expression.contains(",")) {
            index = expression.indexOf(",");
            archetypeNodeId = expression.substring(0, index).trim();
            name = expression.substring(expression.indexOf("'") + 1,
                    expression.lastIndexOf("'"));

        // [at0006 and name/value='any event']
        } else if(expression.contains(" AND ")) {
            index = expression.indexOf("AND");
            archetypeNodeId = expression.substring(0, index).trim();
            name = expression.substring(expression.indexOf("'") + 1,
                    expression.lastIndexOf("'"));

        // [at0006]
        } else if (expression.startsWith("at")) {
            archetypeNodeId = expression;

        // ['standing']
        } else if (expression.startsWith("'") && expression.endsWith("'")) {
            name = expression.substring(1, expression.length() - 1);
        }

        Iterable collection = null;
        if(object instanceof Iterable) {
            collection = (Iterable) object;
        } else {
            List list = new ArrayList();
            list.add(object);
            collection = list;
        }

        for(Object item : collection) {
            if(item instanceof Locatable) {
                Locatable locatable = (Locatable) item;
                if(archetypeNodeId != null
                        && !locatable.archetypeNodeId.equals(archetypeNodeId)) {
                    continue;
                }
                if(name != null && !locatable.name.getValue().equals(name)) {
                    continue;
                }
            }
            // TODO other non-locatable predicates!!
            // e.g. time > 10:20:15
            return item; // found a match!
        }
        return null;
    }*/

    /**
     * The item at a path that is relative to this item.
     *
     * @param path
     * @return the item
     * @throws IllegalArgumentException if path invalid
     */
    /*public Object itemAtPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("invalid path: " + path);
        }
        if (Locatable.ROOT.equals(path) || path.equals(whole())) {
            return this;
        }
        return genericPathEvaluator(path, this);
    }*/

    /*
     * Retrieves the value of named attribute of given object
     */
    /*private Object retrieveAttributeValue(Object obj, String attributeName) {
        Class rmClass = obj.getClass();
        Object value = null;
        Method getter;

        String getterName = "get" + attributeName.substring(0, 1).toUpperCase()
                        + attributeName.substring(1);
        try {
            getter = rmClass.getMethod(getterName, null);
            value = getter.invoke(obj, null);
        } catch(Exception ignore) {
            // TODO log as kernel warning
        }
        return value;
    }*/

    /**
     * Clinical concept of the archetype as a whole, derived from the
     * archetypeNodeId  of the root node
     *
     * @return archetype concept
     * @throws UnsupportedOperationException if this node is not root
     */
    /*public DvText concept() {
        if (!isArchetypeRoot()) {
            throw new UnsupportedOperationException("not root node");
        }
        return new DvText(archetypeDetails.archetypeId.conceptName);
    }*/

    /**
     * Return sting presentation of this locatable
     *
     * @return string presentation
     */
    String toString()
    {
       return this.getClass().getSimpleName() +"-> ["+ archetypeNodeId + "] name: " + name?.value
    }

    /**
     * Equals if two actors has same values
     *
     * @param o
     * @return equals if true
     */
    /*
    boolean equals(Object o) {
        if (o == null) { return false; }
        if (o == this) { return true; }
        if (!( o instanceof Archetyped )) return false
        return (this.uid == ((Locatable)o).uid) &&
               (this.archetypeNodeId  == ((Locatable)o).archetypeNodeId) &&
               (this.name  == ((Locatable)o).name) &&
               (this.archetypeDetails  == ((Locatable)o).archetypeDetails) &&
               //(this.feederAudit == ((Locatable)o).feederAudit) &&
               (this.links  == ((Locatable)o).links) &&
               (this.parent  == ((Locatable)o).parent)
    }
    */

    /**
     * Return path of current whole node
     */
    /*
    String whole() {
        return ROOT;// + "[" + getName().getValue() + "]";
    }

    String nodeName() {
        return "[" + name.getValue() + "]";
    }

    String atNode() {
        return ROOT + "[" + archetypeNodeId + "]";
    }
    */

}