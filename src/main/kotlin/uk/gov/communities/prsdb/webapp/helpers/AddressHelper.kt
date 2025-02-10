package uk.gov.communities.prsdb.webapp.helpers

class AddressHelper {
    companion object {
        fun parseUprnOrNull(uprn: String) = if (uprn.length in 1..12) uprn.toLongOrNull() else null
    }
}
