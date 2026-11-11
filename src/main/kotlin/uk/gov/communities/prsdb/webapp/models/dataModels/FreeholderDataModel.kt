package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder

@Serializable
data class FreeholderDataModel(
    // Null until the freeholder has been saved. Lets an update journey tell a new freeholder from an
    // existing one, so that existing rows are updated in place and only new freeholders are contacted.
    val databaseId: Long? = null,
    // Set when the user goes through the freeholder's details in this journey. One they never started
    // editing cannot have changed, so this stops them being written back needlessly even when their
    // address has no UPRN to compare on.
    val editedInJourney: Boolean = false,
    val detailsKnown: Boolean,
    val name: String? = null,
    val email: String? = null,
    val address: AddressDataModel? = null,
    val addressSearchPostcode: String? = null,
    val addressSearchHouseNameOrNumber: String? = null,
    val selectedAddress: String? = null,
    val manualAddressLineOne: String? = null,
    val manualAddressLineTwo: String? = null,
    val manualTownOrCity: String? = null,
    val manualCounty: String? = null,
    val manualPostcode: String? = null,
) {
    companion object {
        // TODO PDJB-1993: use this to pre-populate the freeholders list from the database when the
        //  update journey starts, leaving editedInJourney false so freeholders the user never opens
        //  are not written back.
        fun fromFreeholder(freeholder: Freeholder) =
            FreeholderDataModel(
                databaseId = freeholder.id,
                detailsKnown = freeholder.detailsKnown(),
                name = freeholder.name,
                email = freeholder.email,
                address = freeholder.address?.let { AddressDataModel.fromAddress(it) },
            )
    }
}
