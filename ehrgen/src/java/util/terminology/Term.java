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
}