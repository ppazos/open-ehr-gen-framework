package util.terminology;

/**
 * POJO utilizado para devolver candidatos que correspondan con un criterio dado.
 */
public class TermMatching {

   String text;     // Texto asociado al termino
   
   float match;     // 0.0 no match, 1.0 exact match
   
   String code;     // Codigo asociado al texto del termino
   String language; // Idioma en el que esta expresado el texto
}