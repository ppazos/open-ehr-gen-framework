
import hce.core.composition.*
import util.RMLoader
import cda.*
import com.thoughtworks.xstream.XStream
//import org.codehaus.groovy.grails.commons.ApplicationHolder

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