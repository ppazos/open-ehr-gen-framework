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
import templates.TemplateManager
import templates.Template

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 */
class TemplateManagerController {
   
   def index = {
      redirect(action:'list')
   }
   
   def list = {
         
      def manager = TemplateManager.getInstance()
      def templates = manager.getLoadedTemplates()
      def actualizaciones = manager.getLastUse()
      
      //println "Templates: " + templates.values()
      
      return [templateMap: templates, lastUseList: actualizaciones]
   }
   
   def unloadAll = {
      def manager = TemplateManager.getInstance()
      manager.unloadAll()
      redirect(action:'list')
   }
   def unload = {
         
      def manager = TemplateManager.getInstance()
      manager.unload(params.id)
      
      println "UNLOAD: " + params.id
      
      redirect(action:'list')
   }
   def reload = {
      def manager = TemplateManager.getInstance()
      manager.unloadAll()
      manager.loadAll()
      
      
      // No se pueden borrar los templates que son referenciados desde stages en la DB
      // Veo si de los templates cargados, hay alguno que no este en la DB y lo agrego
      def existingTemplates = Template.list()
      
      
      // Guarda todos los templates del repo local en la base de datos
      // https://code.google.com/p/open-ehr-gen-framework/issues/detail?id=122
      Map templates = manager.getLoadedTemplates()
      
      templates.each { templateId, template ->
      
         if ( ! existingTemplates.find{ it.templateId == templateId } )
         {
            println templateId + " es nuevo"
            if (!template.save())
            {
               println entry.errors
            }
         }
      }
      
      flash.message = "Templates recargados"
      redirect(action:'list')
   }
}
