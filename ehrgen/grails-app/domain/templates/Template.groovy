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
package templates

import workflow.Stage
import templates.constraints.*

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 */
class Template {

   String templateId // EHRGen-EHR-via_aerea.v1
   String name
    
   ArchetypeReference rootArchetype
   //List includedArchetypes = [] // con esto daba BUG de grails: ValueException: not-null property references a null or
                                  // transient value: templates.ArchetypeReference._Template_includedArchetypesBackref

    
   static hasMany = [includedArchetypes:ArchetypeReference]
	
   static belongsTo = [Stage]
   
   static transients = ['archetypeIDs', 'rootArchetypeID', 'field', 'archetypesByZone', 'transformations']
   
    /**
     * Retorna los identificadores de todos los arquetipos del template.
     * @return
     */
    List<String> getArchetypeIDs()
    {
        def ret = []
        ret << rootArchetype.refId
        if (includedArchetypes.size() > 0)
           ret.addAll( includedArchetypes.refId )
        
        return ret
    }
    
    /**
     * Retorna el identificador del arquetipo raiz del template.
     * @return
     */
    String getRootArchetypeID()
    {
        return rootArchetype.refId
    }
    
    /**
     * Devuelve el campo que coincida con la path, tal que la path
     * del campo sea el prefijo mas largo de path.
     * Puede retornar null si la path no se encuentra o si los archRef not tienen fields.
     */
    ArchetypeField getField( String archetypeId, String path )
    {
        ArchetypeReference archRef
        
        if (rootArchetype.refId == archetypeId) archRef = rootArchetype
        else
        {
            archRef = includedArchetypes.find{ it.refId == archetypeId }
        }
        
        // Si no se encuentra el archRef para archetypeId o
        // si se incluyen todos los campos (no hay fields en el archRef)
        if (!archRef || archRef.includeAll) return null
        
        
        //return archRef?.fields.find{ it.path == path }
        def alternatives = archRef.fields?.findAll{ path.startsWith(it.path) }
        
        return alternatives?.max{ it.path.length() }
    }
    
    List<ArchetypeReference> getArchetypesByZone( String pageZone )
    {
        def ret = []
        if (rootArchetype.pageZone == pageZone) ret << rootArchetype
        def ret2 = includedArchetypes.findAll{ it.pageZone == pageZone }
        ret2.each {
            ret << it
        }
        return ret
    }
    
    List getTransformations()
    {
        def ret = []
        
        //println includedArchetypes.fields
        //println includedArchetypes.fields.constraints.flatten().findAll{ it instanceof Transform }
        //println rootArchetype.fields.constraints.findAll{ it.getClass().getSimpleName() == "Transform" }
        //println includedArchetypes.fields.constraints.findAll{ it.getClass().getSimpleName() == "Transform" }
        
        // FIXME: ahora que es persistente da error de javassist porque los
        //        ArchetypeReference no estan cargados, son proxies...
        
        if (rootArchetype.fields && rootArchetype.fields.constraints)
           ret.addAll( rootArchetype.fields.constraints.findAll{ it instanceof Transform } )
        
        if (includedArchetypes.size() > 0 && includedArchetypes.fields && includedArchetypes.fields.size() > 0 && includedArchetypes.fields.constraints)        
           ret.addAll( includedArchetypes.fields.constraints.flatten().findAll{ it instanceof Transform } )
        
        return ret
    }
}