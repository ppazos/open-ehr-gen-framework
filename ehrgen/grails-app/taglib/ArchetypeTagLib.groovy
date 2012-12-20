
import java.util.Locale;

import archetype.ArchetypeManager
import org.openehr.am.archetype.Archetype
import org.openehr.am.archetype.constraintmodel.*
import binding.CtrlTerminologia
import support.identification.TerminologyID

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 */
class ArchetypeTagLib {

    // Viene archetypeId o (rmtype y idMarchingKey)
    // in: archetypeId
    // in: rmtype
    // in: idMatchingKey
    def displayArchetype = { attrs, body ->
    
       def manager = ArchetypeManager.getInstance()
       Archetype archetype = null
       
       if (attrs.archetypeId)
           archetype = manager.getArchetype( attrs.archetypeId )
       else if (attrs.rmtype && attrs.idMatchingKey)
           archetype = manager.getArchetype(attrs.rmtype, attrs.idMatchingKey ) // FIXME: podria ser una lista de arquetipos
       else
           throw new Exception("Debe venir archetypeId o (rmtype y idMarchingKey)")
       
       if (archetype)
       {
          out << '<div class="cobjects">'
          out << render(template:"templates/cComplexObject", model:['cComplexObject': archetype.definition, 'archetype': archetype])
          out << '</div>'
       }
    }
    
    
    // FIXME: esta taglib no se usa en ninguna vista...
    //
    // Obtiene el termino asociado al id del nodo en el arquetipo.
    // Si el nodo no tiene id, se busca por los padres hasta llegar
    // a uno con id y con el termino declarado en el arquetipo.
    // Retorna null si no lo encuentra.
    // in: archetypeId
    // in: nodePath
    def displayLabel = { attrs ->
            
       if (!attrs.archetypeId) throw new Exception("Parametro 'archetypeId' es obligatorio")
       if (!attrs.nodePath) throw new Exception("Parametro 'nodePath' es obligatorio")
       
       def manager = ArchetypeManager.getInstance()
       Archetype archetype = manager.getArchetype( attrs.archetypeId )
       def node = null
       if (archetype)
       {
           node = archetype.node( attrs.nodePath )
           out << this.findTerm( archetype, node,  attrs.nodePath )
       }
    }
    
    // Muestra el texto para un arquetipo, nodeid/codigo y locale.
    // Escala en el locale si no encuentra el termino.
    //
    // archetype
    // code
    // locale
    //
    def displayTerm = { attrs ->
       
       def archetype = attrs.archetype
       if (!archetype) throw new Exception("Parametro 'archetype' es obligatorio")
       
       def code = attrs.code
       if (!code) throw new Exception("Parametro 'code' es obligatorio")
       
       def locale = attrs.locale
       if (!locale) throw new Exception("Parametro 'locale' es obligatorio")
       
       
       // Ya escala, si no encuentra devuelve un texto
       out << CtrlTerminologia.getInstance().getTermino(TerminologyID.create('local', null), code, archetype, locale)
    }
    
    
    // Devuelve una lista de textos en el locale especificado, para cada uno
    // de los codigos en la lista de codigos que se definen dentro de un arquetipo.
    // Se utiliza para obtener las descripcioens de los codigos que son restricciones
    // de DvCodedText o DvOrdinal.
    //
    // archetype
    // codeList
    // locale
    //
    def codeListTerms = { attrs, body ->
       
       //println "codeList: " + attrs.codeList
       
       def terms = CtrlTerminologia.getInstance()
       def list = []
       attrs.codeList.each { code ->
          
          //list << this.getTerm(attrs.archetype, code, attrs.locale)
          list << terms.getTermino(TerminologyID.create('local', null), code, attrs.archetype, attrs.locale) // Ya escala, si no encuentra devuelve un texto
       }
       
       //println "list: " + list
       
       out << body( labels: list )
    }
    
    
    // recursivo sobre la path, auxiliar de taglig displayLabel.
    private String findTerm( Archetype archetype, CObject node, String path )
    {
       //println "FindTerm: " + path
       if (node)
       {
          if (node.nodeID)
          {
             //println "nodeId: " + node.nodeID + " " + session.locale.language
             
             // Cuidado el locale tiene formato: es_AR
             // Pero el arquetipo tiene formato: es-ar
             
             // FIXME: esto ya lo hace CtrlTerminologia
             
             // pido para todo el locale
             def archetypeTerm = archetype.ontology.termDefinition(session.locale.toString().toLowerCase().replaceAll("_", "-"), node.nodeID) // podria ser null si el termino no esta definido en el arquetipo
             
             // pido para el idioma y pais
             if (!archetypeTerm) archetypeTerm = archetype.ontology.termDefinition(session.locale.language+'-'+session.locale.country.toLowerCase(), node.nodeID) // podria ser null si el termino no esta definido en el arquetipo
             
             // pido para el idioma
             if (!archetypeTerm) archetypeTerm = archetype.ontology.termDefinition(session.locale.language, node.nodeID) // podria ser null si el termino no esta definido en el arquetipo
             
             if (!archetypeTerm) return null
             
             return archetypeTerm.items.text // + " ("+ path +")"
          }
          else // recursivo en path
          {
             def i = path.lastIndexOf("/")
             if (i>0)
             {
                 def newPath = path[0..(i-1)]
                 def newNode = archetype.node( newPath )
                 return findTerm( archetype, newNode, newPath )
             }
             else // no queda path para llamada recursiva
             {
                return null
             }
          }
       }
       return null
    }
    
    // Devuelve el body si el CObject ELEMENT ancestro del nodo nodePath tiene ocurrences unboundedUpper 
    // in: archetypeId
    // in: nodePath
    def parentElementIsMultiple = { attrs, body ->
    
        if (!attrs.archetypeId) throw new Exception("Parametro 'archetypeId' es obligatorio")
        if (!attrs.nodePath) throw new Exception("Parametro 'nodePath' es obligatorio")
        
        def manager = ArchetypeManager.getInstance()
        Archetype archetype = manager.getArchetype( attrs.archetypeId )
        def node = null
        if (archetype)
        {
            node = archetype.node( attrs.nodePath )
            if ( this.parentElementIsMultipleRecursive(archetype, node,  attrs.nodePath ) )
               out << body()
        }
    }
    
    private boolean parentElementIsMultipleRecursive( Archetype archetype, CObject node, String path )
    {
       //println "parentElementIsMultipleRecursive: " + path
       // El multiple es para element, cluster, de ahi para arriba,
       // no para los objetos basicos, como pasa en el arquetipo.
       if (node && !isBasicType(node.rmTypeName))
       {
          if (node.rmTypeName.toLowerCase() == "element")
          {
             // FIXME: si tiene upperBound > 1 tambien es multiple pero con cota de repeticion.
             //println "  es element y " + ((node.occurrences.isUpperUnbounded())?"MULTIPLE":"NO MULTIPLE")
             return node.occurrences.isUpperUnbounded()
          }
          else if (node.rmTypeName.toLowerCase() == "cluster")
          {
              // FIXME: si tiene upperBound > 1 tambien es multiple pero con cota de repeticion.
              //println "  es element y " + ((node.occurrences.isUpperUnbounded())?"MULTIPLE":"NO MULTIPLE")
              return node.occurrences.isUpperUnbounded()
           }
          else // recursivo en path
          {
             def i = path.lastIndexOf("/")
             if (i>0)
             {
                 def newPath = path[0..(i-1)]
                 def newNode = archetype.node( newPath )
                 return parentElementIsMultipleRecursive( archetype, newNode, newPath )
             }
             else // no queda path para llamada recursiva
             {
                return false
             }
          }
       }
       return false
    }
    
    
    private isBasicType( String type )
    {
        // TODO: faltan tipos
        return ["DV_CODED_TEXT","DV_TEXT","DV_COUNT","DV_QUANTIY","DV_DATE"].contains(type)
    }
}