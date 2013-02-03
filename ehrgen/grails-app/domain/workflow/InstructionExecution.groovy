package workflow

class InstructionExecution {
   
   // Las instrucciones se ejecutan dentro del mismo dominio
   // Igual para algunos roles se podrian mostrar listas de instrucciones
   // para todos los dominios, por ejemplo para el paciente.
   Long domainId
   
   Long instructionId
   Long activityId
   String instructionArchetypeId
   
   Long actionId
   String state = 'planned' // TODO: i18n como DvCodedText
   String actionArchetypeId
   
   
   // Timestamp automatico
   Date dateCreated
   
   // Instruction y Activity se registran en la misma composition
   // Cada Action se registra en una composition nueva.
   Long instructionCompositionId
   Long actionCompositionId
   
   static constraints = {
      actionId(nullable:true)
      actionArchetypeId(nullable:true)
      instructionCompositionId(nullable:true)
      actionCompositionId(nullable:true)
   }
   
   static transients = ['instruction', 'activity']
   
   def getInstruction()
   {
      return hce.core.composition.content.entry.Instruction.get(this.instructionId)
   }
   def getActivity()
   {
      return hce.core.composition.content.entry.Activity.get(this.activityId)
   }
}