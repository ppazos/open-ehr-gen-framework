package data_types.quantity.date_time

import data_types.quantity.DvAmount

class DvDuration  extends DvAmount {

    String value // Duration String P[n]Y[n]M[n]DT[n]H[n]M[n]S

    /*
    static mapping = {
        value column: "dvduration_value"
    }

    static constraints = {
    }
    */
}