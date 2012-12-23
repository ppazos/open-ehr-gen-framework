package util

import workflow.WorkFlow
import workflow.Stage
import templates.Template

// FIXME: deberia ser workflow utils
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
      //def domain = session.ehrSession.domainId
      def workflow = WorkFlow.get( session.ehrSession.workflowId )
      //def domainTemplates = grailsApplication.config.templates2."$domain"
      
      // TODO: devolver el workflow directamente
      def domainTemplates = [:]
      
      workflow.stages.each { stage ->
      
         domainTemplates[stage.name] = []
         stage.recordDefinitions.each { template ->
         
            domainTemplates[stage.name] << template.templateId
         }
      }
      
      println "domainTemplates: " + domainTemplates

      return domainTemplates
   }
   
   /**
    * Son los nombres de las stages del workflow actual
    * Devuelve todos los prefijos de identificadores de templates del domino actual.
    * @return
    */
   static List getSections(session)
   {
      def workflow = WorkFlow.get( session.ehrSession.workflowId )
    
      def sections = workflow.stages.name
      
      //println "getSections: " + sections
      
      return sections
   }
   
   /**
    * FIXME: esto es un get stage templates, igual se necesita el workflowId
    *        de la session, porque los nombres de stage no son unicos.
    * Obtiene las subsecciones de una seccion dada.
    * 
    * this.getSubsections('EVALUACION_PRIMARIA')
    * 
    * @param templateId
    * @param session
    *
    * @return List
    */
   static List getSubsections( String stageName, session )
   {
      // Lista de ids de templates
      //def subsections = []
/*
      getDomainTemplates(session)."$section".each { subsection ->
         subsections << "EHRGen-EHR-" + subsection
      }
*/
      def wf = WorkFlow.get( session.ehrSession.workflowId )
      
      println "getSubsections $stageName " + wf
      
      def stg = Stage.findByNameAndOwner( stageName, wf )
      
      println "stage: " + stg
      
      return stg.recordDefinitions.templateId
      /*
      wf.stages.each { stage ->
      
         subsections << stage.name
      }
      */
      //return subsections
   }
   
   static List getSubsectionsByTemplateId( String templateId, session )
   {

      def wf = WorkFlow.get( session.ehrSession.workflowId )
      def tpl = Template.findByTemplateId( templateId )
      
      println "getSubsectionsByTemplateId $templateId: " + tpl
      
      def stg = wf.getStage( tpl )
      return stg.recordDefinitions.templateId
   }

}