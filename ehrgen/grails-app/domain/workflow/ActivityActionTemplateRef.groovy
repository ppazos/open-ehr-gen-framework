package workflow

/**
 * Esta clase no forma parte del modelo de workflow, pero determina la
 * interaccion entre workflows de ordenes (INSTRUCTION) y cumplimientos (ACTION),
 * especificando que templates se deben mostrar para para ACTIVITY a cumplir.
 *
 * El template de cumplimiento tiene una referencia a un arquetipo de ACTION
 * que matchea con el action_archetype_id de la ACTIVITY a la que se esta dando
 * cumplimiento.
 */
class ActivityActionTemplateRef {

   String activityTemplateId // Template que contiene a la INSTRUCTION padre de la ACTIVITY
   String instructionArchetypeId // Referenciado desde el templaet activityTemplateId
   String activityPath       // Identifica una ACTIVITY dentro del arquetipo instructionArchetypeId
   String actionTemplateId   // Template que contiene la ACTION a la que hace referencia ACTIVITY.action_archetype_id
}
