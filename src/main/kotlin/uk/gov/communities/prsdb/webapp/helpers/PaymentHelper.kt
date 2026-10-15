package uk.gov.communities.prsdb.webapp.helpers

import java.math.BigDecimal

class PaymentHelper {
    companion object {
        fun formatPence(pence: Int): String {
            val pounds = BigDecimal(pence).movePointLeft(2)
            return if (pence % 100 == 0) "£${pounds.toBigInteger()}" else "£${pounds.setScale(2)}"
        }
    }
}
