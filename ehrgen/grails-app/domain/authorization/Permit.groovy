package authorization

import org.springframework.beans.BeanWrapper
import org.springframework.beans.PropertyAccessorFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder

/**
 * Modela el acceso a ciertos controllers y acciones.
 * Se usa el nombre del controller o de la accion para indicar que se tiene permiso.
 * Y se usa * para indicar que se tiene acceso a cualquiera.
 * 
 * @author pab
 */
class Permit {
    
   String controller
   String action = "*" // Si no se indica lo contrario, se tien acceso a todas las acciones
    
   static constraints = {
      controller(nullable: false)
      action(nullable: false)
   }
    
   static void createDefault()
   {
      def data = []
      for (controller in ApplicationHolder.application.controllerClasses)
      {
         def controllerInfo = [:]
         controllerInfo.controller = controller.logicalPropertyName
         controllerInfo.controllerName = controller.fullName
         List actions = []
         BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(controller.newInstance())
         
         // ========================================================================
         // Crea el permit para el controller y todas las acciones
         def p = new Permit(controller:controllerInfo.controller)
         p.save()
         
         
         for (pd in beanWrapper.propertyDescriptors)
         {
            String closureClassName = controller.getPropertyOrStaticPropertyOrFieldValue(pd.name, Closure)?.class?.name
            if (closureClassName && pd.name != 'class' && pd.name != 'metaClass')
            {
               actions << pd.name
            
               // ========================================================================
               // Crea el permit para cada accion del controller
               p = new Permit(controller:controllerInfo.controller, action:pd.name)
               p.save()
            }
         }
         
         // No es necesario guardar nada
         //controllerInfo.actions = actions.sort()
         //data << controllerInfo
      }
      
      /*
      [
       [controller:archetypeManager, controllerName:ArchetypeManagerController, actions:[index, list, unloadAll]],
       [controller:authorization, controllerName:auth.AuthorizationController, actions:[login, logout]],
       [controller:party, controllerName:backend.PartyController, actions:[index, listPatients, listUsers]],
       [controller:cda, controllerName:CdaController, actions:[create, index]],
       [controller:demographic, controllerName:DemographicController, actions:[admisionPaciente, agregarPaciente, edit, findPatient, index, seleccionarPaciente]],
       [controller:ajaxApi, controllerName:hce.AjaxApiController, actions:[diagnosticos, findCIE10, pathValores, saveDiagnostico]],
       [controller:domain, controllerName:hce.DomainController, actions:[index, list, selectDomain]],
       [controller:guiGen, controllerName:hce.GuiGenController, actions:[fieldPathlist, generar, generarShow, generarTemplate, index, listarArquetipos, loadedArchetypes, save, showRecord]],
       [controller:records, controllerName:RecordsController, actions:[create, fetch_mm, index, list, registroClinico2, reopenRecord, show, signRecord]],
       [controller:templateManager, controllerName:TemplateManagerController, actions:[index, list, unload, unloadAll]]
      ]
      */
      //println data
   }
}