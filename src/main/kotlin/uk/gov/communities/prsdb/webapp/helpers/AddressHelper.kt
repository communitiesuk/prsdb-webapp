package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.UNASSIGNED

class AddressHelper {
    companion object {
        fun parseUprnOrNull(uprn: String) = if (uprn.length in 1..12) uprn.toLongOrNull() else null

        fun appendCountry(
            multiLineAddress: String,
            country: String,
        ): String {
            val displayCountry = country.trim().takeUnless { it.isEmpty() || it == UNASSIGNED } ?: return multiLineAddress
            return "$multiLineAddress\n$displayCountry"
        }
    }
}
