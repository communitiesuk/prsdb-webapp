package uk.gov.communities.prsdb.webapp.database.entity

interface AssociatedParty {
    val name: String?
    val email: String?
    val address: Address?

    // We have no way of contacting a fully redacted associated party
    fun isFullyRedacted(): Boolean = email.isNullOrBlank() && address == null
}
