package archetype.walkthrough.actions;

public abstract class AbstractAction {

   /**
    * Notifica ante algun evento y le pasa los parametros relacionados con ese evento.
    * @param params
    */
   abstract public void execute(Map params)
}