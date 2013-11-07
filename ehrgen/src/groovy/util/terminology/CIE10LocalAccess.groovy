package util.terminology

import tablasMaestras.Cie10Trauma

class CIE10LocalAccess implements ITerminologyAccess {

   /**
    * Sugiere terminos conocidos a partir de un texto parcial.
    * El resultado puede tener codigos asociados o ser solo textos.
    */
   public List<Term> suggestTerms(String text)
   {
      return suggestTerms(text, 'es')
   }
   
   public List<Term> suggestTerms(String text, String language)
   {
      def codigos = Cie10Trauma.withCriteria {
         and {
            partes.each { parte ->
               like('nombre', '%'+ parte +'%')
            }
         }
      }
      
      def res = []
      
      codigos.each { cie10 ->
      
         // Sino tiene codigo, es un agrupador, se usa subgrupo como codigo.
         if (cie10.codigo)
            res << new Term(text: cie10.nombre, language:'es', code:cie10.codigo, terminologyId:'CIE10', qualifier:'final')
         else
            res << new Term(text: cie10.nombre, language:'es', code:cie10.subgrupo, terminologyId:'CIE10', qualifier:'container')
      }
      
      return res
   }

   /**
    * Busca terminos codificados, segun un texto, dentro de una terminologia, pudiendo restringir a buscar solo en un subset.
    */
   public List<Term> findCandidates( String text, String terminologyId, String subsetId )
   {
      return suggestTerms(text, 'es')
   }
   
   public List<Term> findCandidates( String text, String terminologyId, String subsetId, String language )
   {
      return suggestTerms(text, 'es')s
   }
}