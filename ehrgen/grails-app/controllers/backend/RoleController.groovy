package backend

import demographic.role.*
import authorization.Permit
import authorization.DomainPermit

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
      def domainPermits = DomainPermit.list()
      
      return [roleInstance: role, permits: permits, domainPermits: domainPermits]
   }
   
   /*
   def create = {
      
   }
   */
   
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
      def domainPermits = DomainPermit.list()
      
      return [roleInstance: role, permits: permits, domainPermits: domainPermits]
   }
   
   /* id: identificador del rol a actualizar
    * permits: listado de controller__action (permit codificado)
    */
   def update = {
      
      // 1. Elimina los permits actuales del rol.
      // 2. Ve los permits que vienen, pide a la base y setea al rol.
      
      def role = Role.get(params.id)
      
      
      def permitsToRemove = []
      permitsToRemove.addAll( role.permits )
      permitsToRemove.each {
         role.removeFromPermits(it)
      }
      
      permitsToRemove = []
      permitsToRemove.addAll( role.domainPermits )
      permitsToRemove.each {
         role.removeFromDomainPermits(it)
      }
      
      def partes
      def permit
      
      // Permits
      params.list('permits').each { controllerAction ->
         
         partes = controllerAction.split("__")
         
         permit = Permit.findByControllerAndAction(partes[0], partes[1])
         role.addToPermits(permit)
      }
      
      // DomainPermits
      params.list('dpermits').each { domainTemplateId ->
         
         partes = domainTemplateId.split("__")
         
         permit = DomainPermit.findByDomainAndTemplateId(partes[0], partes[1])
         role.addToDomainPermits(permit)
      }
      
      if (!role.save())
      {
         println role.errors
      }
      
      redirect(action:'show', id:params.id)
   }
}