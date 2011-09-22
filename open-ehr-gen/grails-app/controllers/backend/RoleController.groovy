package backend

import demographic.role.*
import authorization.Permit

class RoleController {

   def index = {
      redirect(action:list)
   }

   def list = {
      
      def list = Role.list(params)
      def total = Role.count()
      return [roleInstanceList : list, roleInstanceTotal: total]
   }
   
   def show = {
      
      def role = Role.get(params.id)
      def permits = Permit.list()
      
      return [roleInstance: role, permits: permits]
   }
   
   def create = {
      
   }
   
   def save = {
      
      def role = new Role(params)
      if (!role.save())
      {
         render(view:'create', model:[roleInstance:role])
         return
      }
      redirect(action:'show', id:role.id)
   }
   
   def edit = {
      
      def role = Role.get(params.id)
      def permits = Permit.list()
      
      return [roleInstance: role, permits: permits]
   }
   
   /* id: identificador del rol a actualizar
    * permits: listado de controller__action (permit codificado)
    */
   def update = {
      
      // TODO: eliminar los permits actuales del rol.
      // TODO: ver los permits que vienen, pedirlos a la base y setearselos al rol.
      
      def role = Role.get(params.id)
      
      
      def permitsToRemove = []
      permitsToRemove.addAll( role.permits )
      permitsToRemove.each {
         role.removeFromPermits(it)
      }
      
      
      params.permits.each { controllerAction ->
         
         def partes = controllerAction.split("__")
         
         def permit = Permit.findByControllerAndAction(partes[0], partes[1])
         role.addToPermits(permit)
         role.save()
      }
      
      redirect(action:'show', id:params.id)
   }
}