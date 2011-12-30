package util

import java.io.Serializable;
import authorization.LoginAuth
import demographic.party.Person

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 */
class EHRSession implements Serializable {

    // No lo uso, saco al paciente del episodio... serviria para acelerar 
    // el sacado del paciente del episodio y se setearia al seleccionar un
    // paciente o un episodio que tiene un paciente seleccionado.
    
    // Path al dominio actual
    String domainPath
    
    // Id del paciente seleccionado
    Long patientId // parece que no se usa... la idea era: si habia un paciente seleccionado, que en el listado se mostraran solo los registros de este paciente.
    
    // FIXME: no puedo poner domain objects en session: http://grails.1312388.n4.nabble.com/Best-way-to-cache-some-domain-objects-in-a-user-session-td3820978.html
    //Person patient // se setea al seleccionar al paciente en DemographicController
    
    //Long pacienteId // Identificador del paciente (uno de sus Ids), no es el id en la base.
    Long episodioId   // Identificador en la base de la composition que modela el registro del episodio.
    
    
    Long userId       // Identificador en la base del Login del usuario logueado en este momento.
    //Person user       // Persona logueada
    
    // FIXME: no puedo poner domain objects en session: http://grails.1312388.n4.nabble.com/Best-way-to-cache-some-domain-objects-in-a-user-session-td3820978.html
    //LoginAuth login // antes userId == login.id
    
    /**
     * Devuelve la persona logueada
     * @return
     */
    /*
    Person getUser()
    {
       return login.person
    }
    */
}