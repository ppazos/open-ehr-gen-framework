package domain

import hce.core.composition.Composition
import workflow.WorkFlow

/**
 * Analogo a Folder de openEHR, customizado/simplificado para las funcionalidades de EHRGen.
 */
class Domain {

   // por ahora no es i18n
   String name

   // true si fue creado por un usuario admin
   // false si es creado por defecto en bootstrap
   boolean userDefined = true
   
   List compositions = []
   List workflows = []   // Se puede tener un workflow distinto para cada rol
   static hasMany = [compositions: Composition, workflows: WorkFlow]
}