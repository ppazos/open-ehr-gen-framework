package archetype

import archetype.ArchetypeManager
import support.identification.TerminologyID
import binding.CtrlTerminologia
import org.codehaus.groovy.grails.commons.ApplicationHolder
import demographic.role.Role

class ArchetypeIndex {

   String archetypeId
   String type
   
   Date dateCreated
   Date lastUpdated
   
   List slots = []
   
   // Si es un arquetipo de instruction, que roles deben ver
   // las instrucciones creadas con este arquetipo.
   // https://code.google.com/p/open-ehr-gen-framework/issues/detail?id=106
   List instructionRoles = []
   
   static hasMany = [slots: ArchetypeIndex, instructionRoles: Role]
   
   
   static constraints = {
      // Pueden haber mas arquetipos pero estos son los tipos soportados por ahora.
      // composition todavia no esta soportado
      type(inList:['section','action','evaluation','instruction','observation','admin_entry'])
   }
   
   static transients = ['name']
   
   String getName()
   {
      def man = ArchetypeManager.getInstance()
      def archetype = man.getArchetype(archetypeId)
      def locale_string = ApplicationHolder.application.config.default_locale_string // 'es', 'es_AR'
      
      /*
      def norm_locale = locale_string.toString().toLowerCase().replaceAll("_", "-") // 'es', 'es-ar'
      def term = archetype.ontology.termDefinition(norm_locale, 'at0000') // puede ser nul para el locale
      return term.text // si term es null tira excepcion
      */
      
      def terms = CtrlTerminologia.getInstance()
      def terminologyId = TerminologyID.create('local', null) // Para sacar el nombre del concepto de la ontologia del arquetipo
      def conceptName = terms.getTermino(terminologyId, 'at0000', archetype, new java.util.Locale(locale_string)) // at0000 es el id de la raiz del arquetipo
      return conceptName
   }
}