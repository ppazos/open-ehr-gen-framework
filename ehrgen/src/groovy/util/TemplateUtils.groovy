package util

import workflow.WorkFlow

class TemplateUtils {

   /**
    * Devuelve un Map con los templates configurados para el dominio actual
    * y el workflow para el rol seleccionado.
    * 
    * this.getDomainTemplates()
    * 
    * @return Map
    */
   static Map getDomainTemplates(session)
   {  
      //def domain = session.traumaContext.domainId
      def workflow = WorkFlow.get( session.traumaContext.workflowId )
      //def domainTemplates = grailsApplication.config.templates2."$domain"
      
      // TODO: devolver el workflow directamente
      def domainTemplates = [:]
      
      workflow.stages.each { stage ->
      
         domainTemplates[stage.name] = []
         stage.recordDefinitions.each { template ->
         
            // ej. template.templateId == EVALUACION_PRIMARIA-via_aerea.v1
            // assert EHRGen == template.templateId.split('-')[0]
            // assert EHR == template.templateId.split('-')[1]
            domainTemplates[stage.name] << template.templateId.split('-')[2]
         }
      }
      
      println "domainTemplates: " + domainTemplates

      return domainTemplates
   }
   
   /**
    * Devuelve todos los prefijos de identificadores de templates del domino actual.
    * @return
    */
   static List getSections(session)
   {
      def sections = []
      getDomainTemplates(session).keySet().each {

         sections << it
      }
      
      return sections
   }
   
   /**
    * FIXME: esto es un get stage by name, igual se necesita el workflowId
    *        de la session, porque los nombres de stage no son unicos.
    * Obtiene las subsecciones de una seccion dada.
    * 
    * this.getSubsections('EVALUACION_PRIMARIA')
    * 
    * @param section es el prefijo del id de un template
    * @return List
    */
   static List getSubsections( String section, session )
   {
      // Lista de ids de templates
      def subsections = []

      getDomainTemplates(session)."$section".each { subsection ->
         subsections << "EHRGen-EHR-" + subsection
      }
      
      return subsections
   }

}