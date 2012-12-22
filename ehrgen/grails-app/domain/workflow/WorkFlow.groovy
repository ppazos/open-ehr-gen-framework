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