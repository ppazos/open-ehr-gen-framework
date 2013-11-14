package util.terminology;

/**
 * POJO utilizado para devolver respuestas en consultas a servicios terminologicos usando TerminologyAccessInterface.
 */
public class Term {

   String text;          // Texto asociado al termino
   String description;   // Descripcion opcional para el termino
   
   String language;      // Lenguage en el que se expresan text y description
   
   String code;          // Codigo que identifica el termino
   String terminologyId; // Terminologia a la que pertenece code
   
   String qualifier;     // Califica al termino, por ejemplo si es un termino final que se utiliza directamente para codificar o si es un contenedor o clasificador y puede contener refinamientos expresados como subcodigos.
   
   float match;          // 0.0 no match, 1.0 exact match
   
   // TODO: lista de mappings a terminos de otras terminologias
   
   public String getText() { return this.text; }
   public String getDescription() { return this.description; }
   public String getLanguage() { return this.language; }
   public String getCode() { return this.code; }
   public String getTerminologyId() { return this.terminologyId; }
   public String getQualifier() { return this.qualifier; }
   public float getMatch() { return this.match; }
}