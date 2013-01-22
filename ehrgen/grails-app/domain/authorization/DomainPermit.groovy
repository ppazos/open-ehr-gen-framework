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
package authorization

import domain.Domain
import org.springframework.beans.BeanWrapper
import org.springframework.beans.PropertyAccessorFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder

/**
 * Modela el acceso de alto nivel a dominios y registros (templates) de cada dominio.
 * Se usa * para indicar que se tiene acceso a cualquier registro del dominio.
 * Tener acceso significa poder listar, ver, crear, editar registros, a no ser que
 * Permit diga lo contrario, por ejemplo que solo deje listar y ver, y no crear o editar.
 * 
 * @author Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 */
class DomainPermit {
    
   long domain // id del Domain en la db
   String templateId = "*" // Si no se indica lo contrario, se tiene acceso a todos los templates
    
   static constraints = {
      domain(nullable: false)
      templateId(nullable: false)
   }
    
   static void createDefault()
   {
      //def domains = ApplicationHolder.application.config.domains
      def sections
      def templates
      def p
	   def domains = Domain.list() // Los dominios se deben haber guardado previamente desde el Boostrap
      for (def domain in domains)
      {
         sections = ApplicationHolder.application.config.templates2."${domain.name}"
         for (def section in sections)
         {
            //println section // ADMISION = ['prehospitalario.v1', 'contexto_del_evento.v1']
            //println section.key // ADMISION
            //println section.value // ['prehospitalario.v1', 'contexto_del_evento.v1']
            templates = section.value
            for (def template in templates)
            {
               p = new DomainPermit(
                             domain: domain.id,
                             templateId: section.key+'-'+template) // ADMISION-prehospitalario.v1
               p.save()
            }
            
         }
         
         p = new DomainPermit(domain: domain.id)
         p.save()
      }
   }
}