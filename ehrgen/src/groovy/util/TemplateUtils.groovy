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
package util

import workflow.WorkFlow
import workflow.Stage
import templates.Template

// FIXME: deberia ser workflow utils
// TODO: se podria eliminar poniendo los metodos en WorkFlow o en Domain
class TemplateUtils {

   /**
    * FIXME: esta operacion es innecesaria, solo se necesita el WF actual para recorrer sus stages y templates.
    *
    * Devuelve un Map con los templates configurados para el dominio actual
    * y el workflow para el rol seleccionado.
    * 
    * this.getDomainTemplates()
    * 
    * @return Map
    */
   static Map getDomainTemplates(org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession session)
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
      
      //println "domainTemplates: " + domainTemplates

      return domainTemplates
   }
   
   /**
    * Son los nombres de las stages del workflow actual
    * Devuelve todos los prefijos de identificadores de templates del domino actual.
    * @return
    */
   static List getSections(org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession session)
   {
      //println "session class: " + session.class // org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession
   
      def workflow = WorkFlow.get( session.ehrSession.workflowId )
      return workflow.stages.name
   }
   
   /**
    * Idem a getSections(session) pero en lugar de sacar el workflow de la session
    * lo saca de la composition. Se usa para mostrar registros de dominios distintos
    * al dominio/workflow seleccionado (el que esta en session).
    */
   static List getSections(hce.core.composition.Composition composition)
   {
      def workflow = WorkFlow.get( composition.workflowId )
      return workflow.stages.name
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
   static List getSubsections( String stageName, org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession session )
   {
      // Lista de ids de templates
      //def subsections = []
/*
      getDomainTemplates(session)."$section".each { subsection ->
         subsections << "EHRGen-EHR-" + subsection
      }
*/
      def wf = WorkFlow.get( session.ehrSession.workflowId )
      
      //println "getSubsections $stageName " + wf
      
      def stg = Stage.findByNameAndOwner( stageName, wf )
      
      //println "stage: " + stg
      
      return stg.recordDefinitions.templateId
      /*
      wf.stages.each { stage ->
      
         subsections << stage.name
      }
      */
      //return subsections
   }
   
   static List getSubsectionsByTemplateId( String templateId, org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession session )
   {

      def wf = WorkFlow.get( session.ehrSession.workflowId )
      def tpl = Template.findByTemplateId( templateId )
      
      println "getSubsectionsByTemplateId $templateId: " + tpl
      
      def stg = wf.getStage( tpl )
      return stg.recordDefinitions.templateId
   }
}