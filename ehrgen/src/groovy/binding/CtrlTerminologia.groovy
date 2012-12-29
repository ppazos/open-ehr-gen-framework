/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package binding

import support.identification.*
import org.openehr.am.archetype.Archetype

import tablasMaestras.* 

/**
 * @author Leandro Carrasco
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 */
class CtrlTerminologia {
   
   private static final INSTANCE = new CtrlTerminologia()
   public static getInstance(){ return INSTANCE }
   private CtrlTerminologia() {}
   
   // FIXME: la logica asociada con cada terminologyId deberia configurarse por 
   //      fuera, por ejemplo en un XML como se hizo lo de los eventos, asi no
   //      es necesario tocar esta clase cada vez que se agrega una nueva tabla
   //      maestra o acceso a terminologia externa mediante WS.
   String getTermino(TerminologyID terminologyId, String codigo, Archetype arquetipo, Locale locale)
   {
      String lang = 'es' // Lenguaje por defecto, FIXME: sacar de config.
      
      // TODO: agregar nuevas terminologias no deberia requerir agregar un case aqui,
      //      se deberia definir una convencion de carga de archivos de configuracion,
      //      que se cacheen o se guarden en la DB, y leer el codigo deberia buscar
      //      en la configuracion, si no se cargo la terminologia, deberia cargarla,
      //      y ponerla en memoria o en DB y la busqueda deberia hacerse en memoria
      //      o DB segun el caso.
      switch ( terminologyId.name )
      {
         case "local":

            // Escala al igual que en ArchetypeTagLib.findTerm
            // Cuidado el locale tiene formato: es_AR
            // Pero el arquetipo tiene formato: es-ar
            
            // FIXME: si el locale es "es" no deberia escalar.
            // FIXME: si el locale es "es_AR" el primer y segundo intento son lo mismo, el tema es que el primero intenta con la variante y si no tiene variante, es lo mismo.
            
            def norm_locale = locale.toString().toLowerCase().replaceAll("_", "-")

            def term = arquetipo.ontology.termDefinition(norm_locale, codigo)
            if (!term) term = arquetipo.ontology.termDefinition(locale.language+'-'+locale.country.toLowerCase(), codigo)
            if (!term) term = arquetipo.ontology.termDefinition(locale.language, codigo)
            if (!term)
            {
               //println " - TERMINO: " + arquetipo.archetypeId.value +
               //      ' codigo ['+codigo+'], y el locale ['+locale.toString()+']'
              
               // Dudas sobre la estrutura?, ver el codigo:
               // http://www.openehr.org/svn/ref_impl_java/TRUNK/openehr-aom/src/main/java/org/openehr/am/archetype/ontology/
               
               // Si pido el locale "es" y en el arquetipo esta "es-ar", quiero que matchee
               // FIXME: hacerlo for para poder parar cuando encuentro
               arquetipo.ontology.termDefinitionMap.each { archlang, map ->
                
                  //println "archlang $archlang"
                
                  // Si el locale por el que pido es menos especifico que el locale del arquetipo
                  // y el del arquetipo empieza con el locale que pido: es-ar empieza con es
                  // entonces es compatible.
                  if (archlang.startsWith(norm_locale))
                  {
                     term = map[codigo] // Map<String, ArchetypeTerm> map
                     //println map  // [ArchetypeTerm]
                  }
               }
            }
         
            // FIXME: throw exception
            if (!term)
            {
               if (!codigo.startsWith('at'))
                  println "getTermino ERROR: "+ terminologyId.name +"::"+ codigo
               
               return 'Termino no encontrado en el arquetipo '+ arquetipo.archetypeId.value +
                      ' codigo ['+codigo+'], y el locale ['+locale.toString()+']'
            }

            return term.getText() // Tambien esta el getDescription!!!

         break
         case "cie10":
            // FIXME: optimizacion: usar criteria y un OR.
            def cie10 = Cie10Trauma.findByCodigo( codigo )
            if (!cie10)
               cie10 = Cie10Trauma.findBySubgrupo( codigo ) // Caso de que se selecciona un subgrupo, no tiene codigo.

            return cie10?.nombre
         break
         case "openehr":
            def oehconcept = OpenEHRConcept.findByConceptId( codigo )
            
            if (!oehconcept)
               return 'Termino no encontrado para openehr::'+codigo
            
            return oehconcept.rubric
         break
         case "motivos_consulta":
            def mc = MotivoConsulta.findByCodigo( codigo )
            return mc?.nombre
         break
         case "departamentos_uy":
            def du = DepartamentoUY.findByIso3166_2UY( codigo )
            return du?.nombre
         break
         case "emergencias_moviles":
            def em = EmergenciaMovil.findByNombre( codigo ) // no tienen codigos, el codigo es el propio nombre
            return em?.nombre
         break
      }
      
      // TODO
      // TerminologyID puede no tener version y en name puede venir la version con la siguiente sintaxis: name(version)
      return "Termino Provisorio | Codigo: " + codigo
   }
   
   public List getNombreTerminos(String terminologyId)
   {
      def list
      switch ( terminologyId )
      {
         case "openehr":
            list = OpenEHRConcept.list()
            return list.rubric // lista
         break
         case "motivos_consulta":
            list = MotivoConsulta.list()
            return list.nombre // lista de nombres
         break
         case "departamentos_uy":
            list = DepartamentoUY.list()
            return list.nombre // lista de nombres
         break
         case "emergencias_moviles":
            list = EmergenciaMovil.list()
            return list.nombre // lista de nombres
         break
      }
      
      return []
   }
   
   public List getCodigoTerminos(String terminologyId)
   {
      def list
      switch ( terminologyId )
      {
         case "openehr":
            list = OpenEHRConcept.list()
            return list.conceptId // lista
         break
         case "motivos_consulta":
            list = MotivoConsulta.list()
            return list.codigo // lista
         break
         case "departamentos_uy":
            list = DepartamentoUY.list()
            return list.iso3166_2UY // lista
         break
         case "emergencias_moviles":
            list = EmergenciaMovil.list()
            return list.nombre // no tienen codigos, el codigo es el propio nombre
         break
      }
      
      return []
   }
}