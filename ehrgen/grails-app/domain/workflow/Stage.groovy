package workflow

import templates.Template

class Stage {

   WorkFlow owner

   // Nombre para el menu
   String name

   List recordDefinitions
   static hasMany = [recordDefinitions: Template]

   static belongsTo = [WorkFlow]
   
   static mapping = {
      owner column: 'stage_owner'
   }
}