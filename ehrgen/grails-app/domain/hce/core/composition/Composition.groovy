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
package hce.core.composition

import hce.core.common.archetyped.Locatable
import data_types.text.*
import hce.core.composition.content.*
import hce.core.common.generic.*
import java.util.List

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream
import data_types.basic.DataValue

class Composition extends Locatable {

   /**
    * The composer is the person who was primarily responsible for the content
    * of the Composition. This is the identifier that should appear on the screen.
    * It could be a junior doctor who did all the work, even if not legally
    * responsible, or it could be a nurse,
    */
   PartyProxy composer
   EventContext context
   DvCodedText category
   CodePhrase territory
   CodePhrase language
   
   // Atributos de EventContext traidos para aqui por simplicidad de implementacion
   // Si la categoria es persistent, estos atributos son nulos
   Date startTime
   Date endTime
   
   // Especifico de EHRGen
   /*
    * wf bajo el que se creo la composition, necesario para visualizar registros
    * de una composition creada para un dominio distinto al seleccionado actualmente
    * en sesion, mostrando las secciones del wf en el dominio donde se creó la composition.
    * wfid puede ser null solo para las compositions que registran acciones para actividades,
    * porque esas compositions no se van a mostrar como un registro en un wf, sino como
    * registros asociados a un registro inicial (la composition donde se ingresó la
    * instrucción originalmente)
    */
   Long workflowId
	
   List content // Para que guarden en orden
   static hasMany = [content:ContentItem]

   String codedCategory
   String codedTerritory
   String codedLanguage
   static transients = ['category', 'territory', 'language']
   
   // Nuevo para calcular codedValue
   def beforeInsert() {
   
      // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
      // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
      XStream xstream = new XStream()
      xstream.omitField(DataValue.class, "errors");
      codedCategory = xstream.toXML(category)
      codedTerritory = xstream.toXML(territory)
      codedLanguage = xstream.toXML(language)
      codedName = xstream.toXML(name) // atributo de locatable
      //println "Compo.codedLanguage: " + codedLanguage
   }
   def beforeUpdate() {
      XStream xstream = new XStream()
      xstream.omitField(DataValue.class, "errors");
      codedCategory = xstream.toXML(category)
      codedTerritory = xstream.toXML(territory)
      codedLanguage = xstream.toXML(language)
      codedName = xstream.toXML(name) // atributo de locatable
      //println "Compo.codedLanguage: " + codedLanguage
   }
   // Al reves
   def afterLoad() {
      XStream xstream = new XStream()
      if (codedCategory) category = xstream.fromXML(codedCategory)
      if (codedTerritory) territory = xstream.fromXML(codedTerritory)
      if (codedLanguage) language = xstream.fromXML(codedLanguage)
      if (codedName) name = xstream.fromXML(codedName) // atributo de locatable
   }
   
   static mapping = {
      composer cascade: "save-update"
      context cascade: "save-update"
      //category cascade: "save-update"
      //territory cascade: "save-update"
      //language cascade: "save-update"
      content cascade: "save-update"
      content column:'composition_content'
      //table 'composition'
   }

   static constraints = {
      //category (nullable: false)
      //territory (nullable: false)
      //language (nullable: false)
      codedCategory (nullable: true, maxSize:4096) // para que pase validacion
      codedTerritory (nullable: true, maxSize:4096)
      codedLanguage (nullable: true, maxSize:4096)

      startTime (nullable:true)
      endTime (nullable:true)
      
      //composer (nullable: false)
      composer (nullable: true) // permito que sea null para poder guardar la composition sin composer, pero al final deberia tener uno!
      context (nullable:true)
      
      workflowId(nullable:true)
   }
}