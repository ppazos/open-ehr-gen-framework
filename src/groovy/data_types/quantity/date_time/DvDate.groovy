package data_types.quantity.date_time

import java.util.Date
import java.text.*

class DvDate extends DvTemporal {

    // value definido en temporal (en la especificacion esta en cada una de las subclases de temporal)

    /*
    static constraints = {
        // TODO
    }
    */
    
    Date toDate()
    {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = (Date)formatter.parse( this.value );    
    }
}