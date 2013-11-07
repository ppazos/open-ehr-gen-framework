package util.terminology;

import java.util.List;

public interface ITerminologyAccess {

   /**
    * Sugiere terminos conocidos a partir de un texto parcial.
    * El resultado puede tener codigos asociados o ser solo textos.
    */
   public List<Term> suggestTerms(String text);
   
   public List<Term> suggestTerms(String text, String language);

   /**
    * Busca terminos codificados, segun un texto, dentro de una terminologia, pudiendo restringir a buscar solo en un subset.
    */
   public List<Term> findCandidates( String text, String terminologyId, String subsetId );
   
   public List<Term> findCandidates( String text, String terminologyId, String subsetId, String language );
}
