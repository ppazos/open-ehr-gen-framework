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
package workflow

import domain.Domain
import demographic.role.Role
import workflow.WorkFlow
import workflow.Stage
import templates.Template

class WorkFlow {

   Domain owner

   // Roles que pueden ejecutar este workflow dentro de un dominio.
   List forRoles = []

   List stages = []
   
   static hasMany = [stages: Stage, forRoles: Role]

   static belongsTo = [Domain]
   
   static transients = ['stage']
   
   static mapping = {
      owner column: 'wf_owner'
   }
   
   /**
    * Devuelve la etapa dentro del workflow que contiene al template.
    * Regla: dos etapas del mismo workflow NO pueden contener el mismo template.
    */
   Stage getStage(Template tpl)
   {
      if (!tpl) return null
   
      for (Stage stg : this.stages)
      {
         if (stg.recordDefinitions.size() > 0)
         {
            // Si encuentra el template dentro de la etapa
            if ( stg.recordDefinitions.find{ it.id == tpl.id } )
            {
               return stg
            }
         }
      }
      
      return null
   }
}