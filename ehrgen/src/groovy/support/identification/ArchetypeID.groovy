package support.identification

import java.util.regex.Pattern
//import java.util.ArrayList
//import java.util.Collections
import java.util.List
//import java.util.StringTokenizer

import support.identification.ObjectID

/**
 * Identificador para arquetipos, instancias de esta clase son inmutables.
 *
 * @author Leandro Carrasco
 * @version 1.0
 */

class ArchetypeID extends ObjectID {

    /* static fields */
    static final String AXIS_SEPARATOR = "."
    static final String SECTION_SEPARATOR = "-"
    static Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9()_/%\$#&]*")
    static Pattern VERSION_PATTERN = Pattern.compile("[a-zA-Z0-9]+")

    /* Nombres de entidades OpenEHR */
    static String RM_ENTITY_FOLDER      = "folder"
    static String RM_ENTITY_COMPOSITION = "composition"
    static String RM_ENTITY_SECTION     = "section"
    static String RM_ENTITY_ENTRY       = "entry"
    static String RM_ENTITY_OBSERVATION = "observation"
    static String RM_ENTITY_EVALUATION  = "evaluation"
    static String RM_ENTITY_INDICATION  = "indication"
    static String RM_ENTITY_ACTION      = "action"
    static String RM_ENTITY_ADMIN_ENTRY = "admin_entry"
    
    /* Valores posibles para rmName */
    static String RM_NAME_OPENEHR       = "ehr_rm"
    static String RM_NAME_RIM           = "rim"
    static String RM_NAME_EN13606       = "en13606"
    
    /* Valores posibles para originator */
    static String ORIGINATOR_OPENEHR    = "openehr"
    static String ORIGINATOR_HL7        = "hl7"
    static String ORIGINATOR_CEN        = "cen"
    
    
    /* Entidad del modelo de referencia calificada globalmente,
    * p.e. "openehr-ehr_rm-entry" */
    String qualifiedRmEntity   // calculated
    
    /* Organizacion que origina el modelo de referencia sobre el
    * que esta basado el arquetipo, p.e. "openehr", "cen", "hl7". */
    String rmOriginator
    
    /* Nombre del modelo de referencia, p.e. "rim", "ehr_rm", "en13606" */
    String rmName
    
    /* Nombre de la entidad del modelo de referencia al que
    * apunta el arquetipo, en OpenEHR las opciones pueden ser:
    * folder, composition, section, entry, etc. */
    String rmEntity
    
    /* Nombre del concepto que representa el arquetipo, incluyendo
    * especializacion, p.e. "biochemistry result-cholesterol" */
    String domainConcept       // calculated
    String conceptName
    
    /* Nombre de un concepto de especializacion, en el caso que
    * sea un arquetipo que especialice otro arquetipo. */
    List<String> specialisation
    
    /* Version del arquetipo */
    String versionID

    /*
    static constraints = {
        rmOriginator(validator: {NAME_PATTERN.matcher(it).matches()})
        rmName(validator: {NAME_PATTERN.matcher(it).matches()})
        rmEntity(validator: {NAME_PATTERN.matcher(it).matches()})
        conceptName(validator: {NAME_PATTERN.matcher(it).matches()})
        specialisation(validator: {
            if (it != null) {
        	for(String name : specialisation) {
                    if (!NAME_PATTERN.matcher(name).matches())
                        return false;
        	}
            }
            return true;
        })
        versionID(VERSION_PATTERN.matcher(it).matches())
    }
    */
}