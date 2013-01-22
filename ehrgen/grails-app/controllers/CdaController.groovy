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
import hce.core.composition.*
import util.RMLoader
import cda.*
import com.thoughtworks.xstream.XStream

class CdaController {

    void imprimirObjetoXML(Object o){
        println "-----------------"
        XStream xstream = new XStream();
        String xml = xstream.toXML(o);
        println xml
        println "-----------------"
    }

    def index = { }

    def create = {
        println "Create CdaControler: " + params

        int idEpisodio = Integer.parseInt(params['id'])
        println "IdEpisodio: " + idEpisodio

        //def composition = Composition.get( params.id )
        //imprimirObjetoXML(composition)
        
        /*
        Composition new_composition = RMLoader.loadComposition(idEpisodio)
        //Composition new_composition = new Composition()
        //recorrerComposition(composition, new_composition) // Al recorrer el composition cargo toda su estructura

        if(new_composition != null)
        {
            println "...====================================..."
            imprimirObjetoXML(new_composition)
            println "...====================================..."          
        }
        else
        {
            flash.message = "trauma.list.messageError2"
            redirect(controller:'trauma', action:'list')
        }
        */

        // Creo el archivo CDA
        def cdaMan = new ManagerCDA()
        cdaMan.createFileCDA(idEpisodio)

        redirect(controller:'records', action:'list')
    }

    //--------------------------------------------------------------------------

    //String getStringFecha(Date fecha){
    //    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss")
    //}

    //--------------------------------------------------------------------------

    //String getNombreArchCDA(int numVersion, Date fecha){
    //    return "CDA-" + idEpisodio + "-" + "V" + numVersion + "-" + getStringFecha(fecha) + ".xml"
    //}

    //--------------------------------------------------------------------------
}