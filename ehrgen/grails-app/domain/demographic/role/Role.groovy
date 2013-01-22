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
package demographic.role

import demographic.party.*
//import authorization.Permit
import authorization.DomainPermit

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 * 
 * Es necesario que Role decienda de Party para poder decir que un rol hizo algo
 * y saber que persona lo hizo a traves de su performer.
 * 
 * Una instancia por cada rol.
 */
class Role extends Party {
    
    // FIXME: ESTA CLASE MODELA ROLES DEL DOMINIO, NO ROLES DE SEGURIDAD! SE DEBERIA HACER UNA CLASE AuthRole que contenga los permits, y que pueda hacer referencia a un tipo de party o rol del dominio.
    //        En realidad todo lo que sean permisos y autorizaciones deberian referenciar a esta clase, pero no esta clase tener los permisos ver http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=113
    // Roles predefinidos
    // FIXME: deberian ir en una tabla gestionable
    static String GODLIKE        = 'master_of_the_universe' // rol de prueba con acceso a todo
    static String ADMIN          = 'administrador'  // administrador del sistema con acceso al backend
    static String PACIENTE       = 'paciente'       // este rol no tiene acceso al sistema
    static String MEDICO         = 'medico'         // acceso al sistema con capacidad de registro clinico y demografico
    static String ENFERMERIA     = 'enfermeria'     // acceso al sistema con capacidad de registro clinico y demografico
    static String ADMINISTRATIVO = 'administrativo' // acceso al sistema con capacidad de registro demografico
    
    // Intervalo de validez del rol: en el modelo de OpenEHR es un Interval<DvDate>
    /*
     * se pone en RoleValidity porque rol es una instancia por cada tipo de rol, y la validez es por cada instancia de persona.
     *
     * ESTO CAMBIA, AHORA ES UNA INSTANCIA POR PERSONA, y para verificar que son el mismo
     * se usa el Role.type.
     *
    Date timeValidityFrom
    Date timeValidityTo
    
    // Actor que tiene este rol asignado
    Actor performer
    */
    
    // Esto se cambia por Permit
    //static hasMany = [capabilities:Capability]
    
    // Se combinan permits y domain permits para lograr controlar todo el acceso de cada rol.
    // Para que el rol acceda, se deben cumplir ambos permits
    // El primer punto de control es si tiene acceso al dominio
    // Luego, para las acciones de registro, depende del template y de las acciones de los controladores que tienen que ver con el registro.
    // Por ultimo se usan los permits para el resto de las acciones del sistema, p.e. acciones de gestion.
    //static hasMany = [permits: Permit, domainPermits: DomainPermit]
    static hasMany = [domainPermits: DomainPermit]

    static constraints = {
        //timeValidityTo(nullable:true)
       type(nullable:false, blank:false)
    }
    
    public String toString()
    {
       return this.type
    }
}